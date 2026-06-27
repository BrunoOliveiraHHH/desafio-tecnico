package com.sinapsis.tarifaagua.service;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.domain.FaixaConsumo;
import com.sinapsis.tarifaagua.domain.TabelaTarifaria;
import com.sinapsis.tarifaagua.dto.CategoriaFaixasRequest;
import com.sinapsis.tarifaagua.dto.CriarTabelaRequest;
import com.sinapsis.tarifaagua.dto.FaixaRequest;
import com.sinapsis.tarifaagua.dto.TabelaResponse;
import com.sinapsis.tarifaagua.exception.MensagensErro;
import com.sinapsis.tarifaagua.exception.RecursoNaoEncontradoException;
import com.sinapsis.tarifaagua.exception.RegraNegocioException;
import com.sinapsis.tarifaagua.repository.TabelaTarifariaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TabelaService - validacao de faixas e soft delete (cenarios mockados)")
class TabelaServiceTest {

    @Mock
    private TabelaTarifariaRepository repository;

    @InjectMocks
    private TabelaService service;

    private FaixaRequest faixa(int inicio, int fim, String valor) {
        return new FaixaRequest(inicio, fim, new BigDecimal(valor));
    }

    private CriarTabelaRequest tabelaCom(List<FaixaRequest> faixas) {
        return new CriarTabelaRequest(
                "Tabela Teste",
                LocalDate.of(2026, 1, 1),
                List.of(new CategoriaFaixasRequest(Categoria.INDUSTRIAL, faixas)));
    }

    @Test
    @DisplayName("Cria tabela valida e persiste")
    void deveCriarTabelaValida() {
        when(repository.save(any(TabelaTarifaria.class))).thenAnswer(inv -> {
            TabelaTarifaria t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        TabelaResponse r = service.criar(tabelaCom(List.of(
                faixa(0, 10, "1.00"),
                faixa(11, 20, "2.00"))));

        assertThat(r.id()).isEqualTo(10L);
        assertThat(r.ativo()).isTrue();
        assertThat(r.categorias()).hasSize(1);
        verify(repository).save(any(TabelaTarifaria.class));
    }

    @Test
    @DisplayName("Cria tabela sem data de vigencia (assume a data atual)")
    void deveCriarSemDataVigencia() {
        when(repository.save(any(TabelaTarifaria.class))).thenAnswer(inv -> inv.getArgument(0));

        CriarTabelaRequest req = new CriarTabelaRequest("Sem data", null,
                List.of(new CategoriaFaixasRequest(Categoria.INDUSTRIAL,
                        List.of(faixa(0, 10, "1.00"), faixa(11, 20, "2.00")))));

        service.criar(req);

        verify(repository).save(any(TabelaTarifaria.class));
    }

    @Test
    @DisplayName("Rejeita faixas que nao iniciam em 0")
    void deveRejeitarSemInicioEmZero() {
        CriarTabelaRequest req = tabelaCom(List.of(faixa(1, 10, "1.00")));

        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage(MensagensErro.FAIXA_COBERTURA_INICIAL);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita inicio maior ou igual ao fim")
    void deveRejeitarOrdemInvalida() {
        CriarTabelaRequest req = tabelaCom(List.of(faixa(0, 0, "1.00")));

        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage(MensagensErro.FAIXA_ORDEM_INVALIDA);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita faixas sobrepostas")
    void deveRejeitarSobreposicao() {
        CriarTabelaRequest req = tabelaCom(List.of(
                faixa(0, 10, "1.00"),
                faixa(8, 20, "2.00")));

        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage(MensagensErro.FAIXA_SOBREPOSICAO);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita lacuna entre faixas (cobertura incompleta)")
    void deveRejeitarLacuna() {
        CriarTabelaRequest req = tabelaCom(List.of(
                faixa(0, 10, "1.00"),
                faixa(15, 20, "2.00")));

        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage(MensagensErro.FAIXA_LACUNA);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Excluir realiza soft delete (ativo = false), sem remocao fisica")
    void deveFazerSoftDelete() {
        TabelaTarifaria tabela = new TabelaTarifaria();
        tabela.setId(5L);
        tabela.setAtivo(true);
        when(repository.findById(5L)).thenReturn(Optional.of(tabela));

        service.excluir(5L);

        assertThat(tabela.isAtivo()).isFalse();
        verify(repository).save(tabela);
        verify(repository, never()).delete(any());
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Excluir id inexistente lanca RecursoNaoEncontradoException")
    void deveFalharAoExcluirInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Listar com filtro retorna apenas as faixas da categoria informada")
    void deveListarFiltrandoPorCategoria() {
        TabelaTarifaria tabela = new TabelaTarifaria();
        tabela.setId(1L);
        tabela.setNome("Tabela");
        tabela.setDataVigencia(LocalDate.of(2026, 1, 1));
        tabela.setAtivo(true);
        tabela.adicionarFaixa(faixaEntidade(Categoria.INDUSTRIAL, 0, 10, "1.00"));
        tabela.adicionarFaixa(faixaEntidade(Categoria.COMERCIAL, 0, 10, "1.50"));
        when(repository.findByAtivoTrueOrderByDataVigenciaDescIdDesc()).thenReturn(List.of(tabela));

        List<TabelaResponse> resultado = service.listar(Categoria.INDUSTRIAL);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).categorias()).hasSize(1);
        assertThat(resultado.get(0).categorias().get(0).categoria()).isEqualTo("INDUSTRIAL");
    }

    private FaixaConsumo faixaEntidade(Categoria categoria, int inicio, int fim, String valor) {
        FaixaConsumo f = new FaixaConsumo();
        f.setCategoria(categoria);
        f.setInicio(inicio);
        f.setFim(fim);
        f.setValorUnitario(new BigDecimal(valor));
        return f;
    }
}
