package com.sinapsis.tarifaagua.dto;

import com.sinapsis.tarifaagua.domain.Categoria;

import java.math.BigDecimal;
import java.util.List;

/**
 * Retorno detalhado do calculo, no formato obrigatorio do desafio (item 2.3):
 * categoria, consumoTotal, valorTotal e detalhamento por faixa.
 */
public record CalculoResponse(
        Categoria categoria,
        Integer consumoTotal,
        BigDecimal valorTotal,
        List<DetalhamentoItem> detalhamento
) {

    public record DetalhamentoItem(
            FaixaIntervalo faixa,
            Integer m3Cobrados,
            BigDecimal valorUnitario,
            BigDecimal subtotal
    ) {
    }

    public record FaixaIntervalo(
            Integer inicio,
            Integer fim
    ) {
    }
}
