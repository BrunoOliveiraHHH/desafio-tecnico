package com.testetecnico.tarifaagua;

import com.jayway.jsonpath.JsonPath;
import com.testetecnico.tarifaagua.domain.Categoria;
import com.testetecnico.tarifaagua.domain.FaixaConsumo;
import com.testetecnico.tarifaagua.repository.FaixaConsumoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de aceite ponta a ponta — um por critério do desafio.
 *
 * <p>Exercitam toda a pilha (controller → service → repository → JPA) contra um
 * banco <strong>H2 em memória</strong>, sem dependências externas. Cada teste é
 * transacional e sofre <em>rollback</em> ao final, garantindo isolamento.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Critérios de Aceite (integração com H2 em memória)")
class CriteriosDeAceiteIT {

    private static final String TABELAS = "/api/tabelas-tarifarias";
    private static final String CALCULOS = "/api/calculos";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FaixaConsumoRepository faixaRepository;

    /** Payload de uma tabela INDUSTRIAL com as faixas do exemplo do desafio. */
    private static final String TABELA_INDUSTRIAL = """
            {
              "nome": "Tabela Teste",
              "dataVigencia": "2026-01-01",
              "categorias": [
                { "categoria": "INDUSTRIAL", "faixas": [
                  { "inicio": 0,  "fim": 10,    "valorUnitario": 1.00 },
                  { "inicio": 11, "fim": 20,    "valorUnitario": 2.00 },
                  { "inicio": 21, "fim": 30,    "valorUnitario": 3.00 },
                  { "inicio": 31, "fim": 99999, "valorUnitario": 4.00 }
                ]}
              ]
            }
            """;

    private long criarTabelaIndustrial() throws Exception {
        String corpo = mvc.perform(post(TABELAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TABELA_INDUSTRIAL))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    // -------------------- Critério 1: criar tabela --------------------

    @Test
    @DisplayName("Critério 1: cria tabela completa com as quatro categorias e suas faixas")
    void criterio1_criarTabelaCompleta() throws Exception {
        String corpo = """
                {
                  "nome": "Tabela 2026",
                  "dataVigencia": "2026-01-01",
                  "categorias": [
                    { "categoria": "COMERCIAL",  "faixas": [ {"inicio":0,"fim":10,"valorUnitario":1.50}, {"inicio":11,"fim":99999,"valorUnitario":2.50} ] },
                    { "categoria": "INDUSTRIAL", "faixas": [ {"inicio":0,"fim":10,"valorUnitario":1.00}, {"inicio":11,"fim":99999,"valorUnitario":2.00} ] },
                    { "categoria": "PARTICULAR", "faixas": [ {"inicio":0,"fim":10,"valorUnitario":0.80}, {"inicio":11,"fim":99999,"valorUnitario":1.60} ] },
                    { "categoria": "PUBLICO",    "faixas": [ {"inicio":0,"fim":10,"valorUnitario":1.20}, {"inicio":11,"fim":99999,"valorUnitario":2.20} ] }
                  ]
                }
                """;

        mvc.perform(post(TABELAS).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.categorias.length()").value(4));
    }

    @Test
    @DisplayName("Critério 1: rejeita faixas inconsistentes (sobreposição) com 422 e mensagem amigável")
    void criterio1_rejeitarFaixasInconsistentes() throws Exception {
        String corpo = """
                {
                  "nome": "Tabela Inválida",
                  "categorias": [
                    { "categoria": "INDUSTRIAL", "faixas": [
                      {"inicio":0,"fim":10,"valorUnitario":1.00},
                      {"inicio":8,"fim":20,"valorUnitario":2.00}
                    ]}
                  ]
                }
                """;

        mvc.perform(post(TABELAS).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensagem", org.hamcrest.Matchers.containsString("sobrep")))
                .andExpect(jsonPath("$.caminho").value(TABELAS));
    }

    // -------------------- Critério 2: listar por categoria --------------------

    @Test
    @DisplayName("Critério 2: lista as tarifas por faixa, filtrando pela categoria informada")
    void criterio2_listarPorCategoria() throws Exception {
        criarTabelaIndustrial();

        mvc.perform(get(TABELAS).param("categoria", "INDUSTRIAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categorias.length()").value(1))
                .andExpect(jsonPath("$[0].categorias[0].categoria").value("INDUSTRIAL"))
                .andExpect(jsonPath("$[0].categorias[0].faixas.length()").value(4));
    }

    // -------------------- Critério 3: cálculo progressivo --------------------

    @Test
    @DisplayName("Critério 3: calcula 18 m³ Industrial = R$ 26,00 com detalhamento por faixa")
    void criterio3_calculoCanonico() throws Exception {
        criarTabelaIndustrial();

        mvc.perform(post(CALCULOS).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"INDUSTRIAL\",\"consumo\":18}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("INDUSTRIAL"))
                .andExpect(jsonPath("$.consumoTotal").value(18))
                .andExpect(jsonPath("$.valorTotal").value(26.00))
                .andExpect(jsonPath("$.detalhamento.length()").value(2))
                .andExpect(jsonPath("$.detalhamento[0].m3Cobrados").value(10))
                .andExpect(jsonPath("$.detalhamento[0].subtotal").value(10.00))
                .andExpect(jsonPath("$.detalhamento[1].m3Cobrados").value(8))
                .andExpect(jsonPath("$.detalhamento[1].subtotal").value(16.00));
    }

    @Test
    @DisplayName("Critério 3: rejeita consumo fora da cobertura com 422")
    void criterio3_consumoForaDaCobertura() throws Exception {
        criarTabelaIndustrial();

        mvc.perform(post(CALCULOS).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"INDUSTRIAL\",\"consumo\":999999}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", org.hamcrest.Matchers.containsString("excede")));
    }

    // -------------------- Critério 4: parametrização --------------------

    @Test
    @DisplayName("Critério 4: alterar o valor de uma faixa no banco muda o cálculo, sem alterar código")
    void criterio4_parametrizacao() throws Exception {
        long tabelaId = criarTabelaIndustrial();

        // Antes: 18 m³ = R$ 26,00
        mvc.perform(post(CALCULOS).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"INDUSTRIAL\",\"consumo\":18}"))
                .andExpect(jsonPath("$.valorTotal").value(26.00));

        // Altera no banco o valor da primeira faixa (1,00 -> 2,00)
        FaixaConsumo primeira = faixaRepository.findVigentesPorCategoria(Categoria.INDUSTRIAL).stream()
                .filter(f -> f.getTabela().getId().equals(tabelaId))
                .filter(f -> f.getInicio() == 0)
                .findFirst()
                .orElseThrow();
        primeira.setValorUnitario(new BigDecimal("2.00"));
        faixaRepository.save(primeira);

        // Depois: 18 m³ = R$ 36,00 (10×2,00 + 8×2,00)
        mvc.perform(post(CALCULOS).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"INDUSTRIAL\",\"consumo\":18}"))
                .andExpect(jsonPath("$.valorTotal").value(36.00));
    }

    // -------------------- Exclusão lógica (soft delete) --------------------

    @Test
    @DisplayName("Exclusão lógica: tabela excluída deixa de aparecer na listagem")
    void exclusaoLogica() throws Exception {
        long tabelaId = criarTabelaIndustrial();

        mvc.perform(delete(TABELAS + "/" + tabelaId)).andExpect(status().isNoContent());

        mvc.perform(get(TABELAS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + tabelaId + ")]").isEmpty());

        assertThat(faixaRepository.findVigentesPorCategoria(Categoria.INDUSTRIAL)).isEmpty();
    }

    @Test
    @DisplayName("Exclusão lógica: excluir tabela inexistente retorna 404")
    void exclusaoInexistente() throws Exception {
        mvc.perform(delete(TABELAS + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
