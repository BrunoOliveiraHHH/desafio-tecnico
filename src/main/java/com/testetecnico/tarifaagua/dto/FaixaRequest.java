package com.testetecnico.tarifaagua.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Faixa progressiva de consumo informada na criação de uma tabela tarifária.
 *
 * @param inicio        m³ inicial da faixa (a primeira faixa deve iniciar em 0)
 * @param fim           m³ final da faixa (deve ser maior que o início)
 * @param valorUnitario valor cobrado por m³ nesta faixa (R$/m³)
 */
@Schema(name = "FaixaRequest", description = "Faixa de consumo com seu valor unitário")
public record FaixaRequest(

        @Schema(description = "Início da faixa em m³ (a primeira faixa deve iniciar em 0)", example = "0")
        @NotNull(message = "{validacao.faixa.inicio}")
        @PositiveOrZero(message = "{validacao.faixa.inicio}")
        Integer inicio,

        @Schema(description = "Fim da faixa em m³", example = "10")
        @NotNull(message = "{validacao.faixa.fim}")
        @Positive(message = "{validacao.faixa.fim}")
        Integer fim,

        @Schema(description = "Valor unitário em R$/m³", example = "1.00")
        @NotNull(message = "{validacao.faixa.valorUnitario}")
        @Positive(message = "{validacao.faixa.valorUnitario}")
        BigDecimal valorUnitario
) {
}
