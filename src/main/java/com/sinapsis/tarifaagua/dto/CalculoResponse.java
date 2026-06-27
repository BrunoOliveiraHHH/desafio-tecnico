package com.sinapsis.tarifaagua.dto;

import com.sinapsis.tarifaagua.domain.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado detalhado do cálculo, no formato obrigatório do desafio (item 2.3):
 * categoria, consumo total, valor total e o detalhamento por faixa.
 *
 * @param categoria     categoria utilizada
 * @param consumoTotal  consumo informado (m³)
 * @param valorTotal    valor total a pagar (R$)
 * @param detalhamento  composição do valor, faixa a faixa
 */
@Schema(name = "CalculoResponse", description = "Resultado detalhado do cálculo progressivo")
public record CalculoResponse(

        @Schema(description = "Categoria utilizada", example = "INDUSTRIAL")
        Categoria categoria,

        @Schema(description = "Consumo total informado (m³)", example = "18")
        Integer consumoTotal,

        @Schema(description = "Valor total a pagar (R$)", example = "26.00")
        BigDecimal valorTotal,

        @Schema(description = "Composição do valor, faixa a faixa")
        List<DetalhamentoItem> detalhamento
) {

    /**
     * Linha do detalhamento: quanto foi cobrado em uma faixa específica.
     *
     * @param faixa        intervalo da faixa
     * @param m3Cobrados   quantidade de m³ cobrados nesta faixa
     * @param valorUnitario valor unitário da faixa (R$/m³)
     * @param subtotal     subtotal da faixa (m³ cobrados × valor unitário)
     */
    @Schema(name = "DetalhamentoItem", description = "Valor cobrado em uma faixa")
    public record DetalhamentoItem(

            @Schema(description = "Intervalo da faixa")
            FaixaIntervalo faixa,

            @Schema(description = "Quantidade de m³ cobrados nesta faixa", example = "10")
            Integer m3Cobrados,

            @Schema(description = "Valor unitário da faixa (R$/m³)", example = "1.00")
            BigDecimal valorUnitario,

            @Schema(description = "Subtotal da faixa (R$)", example = "10.00")
            BigDecimal subtotal
    ) {
    }

    /**
     * Intervalo (em m³) de uma faixa.
     *
     * @param inicio m³ inicial
     * @param fim    m³ final
     */
    @Schema(name = "FaixaIntervalo", description = "Intervalo de consumo de uma faixa (m³)")
    public record FaixaIntervalo(

            @Schema(description = "m³ inicial", example = "0")
            Integer inicio,

            @Schema(description = "m³ final", example = "10")
            Integer fim
    ) {
    }
}
