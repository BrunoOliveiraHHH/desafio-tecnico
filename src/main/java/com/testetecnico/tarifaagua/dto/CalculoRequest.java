package com.testetecnico.tarifaagua.dto;

import com.testetecnico.tarifaagua.domain.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Dados de entrada do cálculo do valor a pagar.
 *
 * @param categoria categoria do consumidor
 * @param consumo   consumo total em m³
 */
@Schema(name = "CalculoRequest", description = "Entrada do cálculo: categoria e consumo")
public record CalculoRequest(

        @Schema(description = "Categoria do consumidor", example = "INDUSTRIAL")
        @NotNull(message = "{validacao.calculo.categoria}")
        Categoria categoria,

        @Schema(description = "Consumo total em m³", example = "18")
        @NotNull(message = "{validacao.calculo.consumo}")
        @PositiveOrZero(message = "{validacao.calculo.consumo}")
        Integer consumo
) {
}
