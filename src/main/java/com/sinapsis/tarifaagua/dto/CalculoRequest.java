package com.sinapsis.tarifaagua.dto;

import com.sinapsis.tarifaagua.domain.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Entrada do calculo: categoria + consumo total (m3).
 */
public record CalculoRequest(

        @Schema(description = "Categoria do consumidor", example = "INDUSTRIAL")
        @NotNull Categoria categoria,

        @Schema(description = "Consumo total em m3", example = "18")
        @NotNull @PositiveOrZero Integer consumo
) {
}
