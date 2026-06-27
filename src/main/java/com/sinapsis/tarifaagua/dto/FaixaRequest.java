package com.sinapsis.tarifaagua.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record FaixaRequest(

        @Schema(description = "Inicio da faixa em m3 (a primeira faixa deve iniciar em 0)", example = "0")
        @NotNull @PositiveOrZero Integer inicio,

        @Schema(description = "Fim da faixa em m3", example = "10")
        @NotNull @Positive Integer fim,

        @Schema(description = "Valor unitario em R$/m3", example = "1.00")
        @NotNull @Positive BigDecimal valorUnitario
) {
}
