package com.sinapsis.tarifaagua;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.domain.FaixaConsumo;
import com.sinapsis.tarifaagua.dto.CalculoRequest;
import com.sinapsis.tarifaagua.dto.CalculoResponse;
import com.sinapsis.tarifaagua.dto.CategoriaFaixasRequest;
import com.sinapsis.tarifaagua.dto.CriarTabelaRequest;
import com.sinapsis.tarifaagua.dto.FaixaRequest;
import com.sinapsis.tarifaagua.dto.TabelaResponse;
import com.sinapsis.tarifaagua.repository.FaixaConsumoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Teste de integracao ponta a ponta contra um PostgreSQL 18 real (Testcontainers).
 * Exercita os endpoints HTTP, o Flyway, o mapeamento JPA e, principalmente,
 * comprova a parametrizacao: alterar um valor no banco muda o calculo sem
 * qualquer alteracao de codigo.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Integracao - fluxo completo com PostgreSQL 18 real")
class TarifaAguaIT {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("tarifa_agua")
                    .withUsername("tarifa")
                    .withPassword("tarifa");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private FaixaConsumoRepository faixaRepository;

    private RestClient client() {
        return restClientBuilder.baseUrl("http://localhost:" + port).build();
    }

    private CriarTabelaRequest tabelaIndustrial(LocalDate vigencia) {
        List<FaixaRequest> faixas = List.of(
                new FaixaRequest(0, 10, new BigDecimal("1.00")),
                new FaixaRequest(11, 20, new BigDecimal("2.00")),
                new FaixaRequest(21, 30, new BigDecimal("3.00")),
                new FaixaRequest(31, 99999, new BigDecimal("4.00")));
        return new CriarTabelaRequest("Tabela IT", vigencia,
                List.of(new CategoriaFaixasRequest(Categoria.INDUSTRIAL, faixas)));
    }

    private TabelaResponse criarTabela(LocalDate vigencia) {
        return client().post()
                .uri("/api/tabelas-tarifarias")
                .contentType(MediaType.APPLICATION_JSON)
                .body(tabelaIndustrial(vigencia))
                .retrieve()
                .body(TabelaResponse.class);
    }

    private CalculoResponse calcular(int consumo) {
        return client().post()
                .uri("/api/calculos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CalculoRequest(Categoria.INDUSTRIAL, consumo))
                .retrieve()
                .body(CalculoResponse.class);
    }

    @Test
    @DisplayName("Cria tabela, calcula 18 m3 = R$ 26,00 e comprova a parametrizacao")
    void deveCalcularEDemonstrarParametrizacao() {
        // 1. Cria uma tabela vigente (data futura garante que sera a escolhida).
        TabelaResponse criada = criarTabela(LocalDate.of(2099, 1, 1));
        Long tabelaId = criada.id();

        // 2. Calculo canonico do desafio.
        CalculoResponse calculo = calcular(18);
        assertThat(calculo.valorTotal()).isEqualByComparingTo("26.00");
        assertThat(calculo.detalhamento()).hasSize(2);

        // 3. Parametrizacao: altera no banco o valor da primeira faixa (1,00 -> 2,00).
        FaixaConsumo primeiraFaixa = faixaRepository.findVigentesPorCategoria(Categoria.INDUSTRIAL).stream()
                .filter(f -> f.getTabela().getId().equals(tabelaId))
                .filter(f -> f.getInicio() == 0)
                .findFirst()
                .orElseThrow();
        primeiraFaixa.setValorUnitario(new BigDecimal("2.00"));
        faixaRepository.save(primeiraFaixa);

        // 4. Recalcula: 10*2,00 + 8*2,00 = 36,00 (sem alterar codigo).
        assertThat(calcular(18).valorTotal()).isEqualByComparingTo("36.00");
    }

    @Test
    @DisplayName("DELETE faz exclusao logica: tabela some da listagem")
    void deveExcluirLogicamente() {
        TabelaResponse criada = criarTabela(LocalDate.of(2098, 1, 1));
        Long tabelaId = criada.id();

        client().delete().uri("/api/tabelas-tarifarias/" + tabelaId).retrieve().toBodilessEntity();

        List<TabelaResponse> ativas = client().get()
                .uri("/api/tabelas-tarifarias")
                .retrieve()
                .body(new ParameterizedTypeReference<List<TabelaResponse>>() {
                });

        assertThat(ativas).noneMatch(t -> t.id().equals(tabelaId));
    }

    @Test
    @DisplayName("Consumo fora da cobertura retorna 422")
    void deveRetornar422ParaConsumoForaDaCobertura() {
        criarTabela(LocalDate.of(2097, 1, 1));

        assertThatExceptionOfType(HttpClientErrorException.class)
                .isThrownBy(() -> calcular(999999))
                .satisfies(ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT));
    }
}
