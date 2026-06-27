package com.sinapsis.tarifaagua.service;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.domain.FaixaConsumo;
import com.sinapsis.tarifaagua.domain.TabelaTarifaria;
import com.sinapsis.tarifaagua.dto.CalculoRequest;
import com.sinapsis.tarifaagua.dto.CalculoResponse;
import com.sinapsis.tarifaagua.exception.MensagensErro;
import com.sinapsis.tarifaagua.exception.RegraNegocioException;
import com.sinapsis.tarifaagua.repository.FaixaConsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculoService - calculo progressivo por faixas (cenarios mockados)")
class CalculoServiceTest {

    @Mock
    private FaixaConsumoRepository faixaRepository;

    @InjectMocks
    private CalculoService service;

    private TabelaTarifaria tabela;

    @BeforeEach
    void setUp() {
        tabela = new TabelaTarifaria();
        tabela.setId(1L);
    }

    private FaixaConsumo faixa(int inicio, int fim, String valor) {
        FaixaConsumo f = new FaixaConsumo();
        f.setTabela(tabela);
        f.setCategoria(Categoria.INDUSTRIAL);
        f.setInicio(inicio);
        f.setFim(fim);
        f.setValorUnitario(new BigDecimal(valor));
        return f;
    }

    private List<FaixaConsumo> faixasIndustrial() {
        return List.of(
                faixa(0, 10, "1.00"),
                faixa(11, 20, "2.00"),
                faixa(21, 30, "3.00"),
                faixa(31, 99999, "4.00"));
    }

    @Test
    @DisplayName("Caso canonico do desafio: 18 m3 -> R$ 26,00")
    void deveCalcularCasoCanonico() {
        when(faixaRepository.findVigentesPorCategoria(Categoria.INDUSTRIAL))
                .thenReturn(faixasIndustrial());

        CalculoResponse r = service.calcular(new CalculoRequest(Categoria.INDUSTRIAL, 18));

        assertThat(r.categoria()).isEqualTo(Categoria.INDUSTRIAL);
        assertThat(r.consumoTotal()).isEqualTo(18);
        assertThat(r.valorTotal()).isEqualByComparingTo("26.00");
        assertThat(r.detalhamento()).hasSize(2);

        CalculoResponse.DetalhamentoItem f1 = r.detalhamento().get(0);
        assertThat(f1.faixa().inicio()).isZero();
        assertThat(f1.faixa().fim()).isEqualTo(10);
        assertThat(f1.m3Cobrados()).isEqualTo(10);
        assertThat(f1.subtotal()).isEqualByComparingTo("10.00");

        CalculoResponse.DetalhamentoItem f2 = r.detalhamento().get(1);
        assertThat(f2.m3Cobrados()).isEqualTo(8);
        assertThat(f2.subtotal()).isEqualByComparingTo("16.00");
    }

    @Test
    @DisplayName("Consumo no limite exato da primeira faixa (10 m3) usa apenas a faixa 1")
    void deveCalcularNoLimiteDaFaixa() {
        when(faixaRepository.findVigentesPorCategoria(Categoria.INDUSTRIAL))
                .thenReturn(faixasIndustrial());

        CalculoResponse r = service.calcular(new CalculoRequest(Categoria.INDUSTRIAL, 10));

        assertThat(r.valorTotal()).isEqualByComparingTo("10.00");
        assertThat(r.detalhamento()).hasSize(1);
    }

    @Test
    @DisplayName("Consumo zero resulta em total 0,00 e sem detalhamento")
    void deveCalcularConsumoZero() {
        when(faixaRepository.findVigentesPorCategoria(Categoria.INDUSTRIAL))
                .thenReturn(faixasIndustrial());

        CalculoResponse r = service.calcular(new CalculoRequest(Categoria.INDUSTRIAL, 0));

        assertThat(r.valorTotal()).isEqualByComparingTo("0.00");
        assertThat(r.detalhamento()).isEmpty();
    }

    @Test
    @DisplayName("Consumo alto percorre todas as faixas ate a ultima")
    void deveCalcularConsumoNaUltimaFaixa() {
        when(faixaRepository.findVigentesPorCategoria(Categoria.INDUSTRIAL))
                .thenReturn(faixasIndustrial());

        CalculoResponse r = service.calcular(new CalculoRequest(Categoria.INDUSTRIAL, 100));

        // 10*1 + 10*2 + 10*3 + 70*4 = 10 + 20 + 30 + 280 = 340
        assertThat(r.valorTotal()).isEqualByComparingTo("340.00");
        assertThat(r.detalhamento()).hasSize(4);
        assertThat(r.detalhamento().get(3).m3Cobrados()).isEqualTo(70);
    }

    @Test
    @DisplayName("Categoria sem faixas cadastradas lanca RegraNegocioException")
    void deveFalharQuandoNaoHaFaixas() {
        when(faixaRepository.findVigentesPorCategoria(Categoria.PUBLICO))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.calcular(new CalculoRequest(Categoria.PUBLICO, 10)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage(MensagensErro.CALCULO_SEM_TABELA_ATIVA);
    }

    @Test
    @DisplayName("Consumo acima da cobertura das faixas lanca RegraNegocioException")
    void deveFalharQuandoConsumoExcedeCobertura() {
        when(faixaRepository.findVigentesPorCategoria(Categoria.INDUSTRIAL))
                .thenReturn(List.of(faixa(0, 10, "1.00"), faixa(11, 20, "2.00")));

        assertThatThrownBy(() -> service.calcular(new CalculoRequest(Categoria.INDUSTRIAL, 5000)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage(MensagensErro.CALCULO_CONSUMO_FORA_COBERTURA);
    }
}
