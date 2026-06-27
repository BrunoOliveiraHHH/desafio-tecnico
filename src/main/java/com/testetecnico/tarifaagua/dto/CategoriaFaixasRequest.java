package com.testetecnico.tarifaagua.dto;

import com.testetecnico.tarifaagua.domain.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Conjunto de faixas de uma categoria dentro de uma tabela tarifária.
 *
 * @param categoria categoria do consumidor
 * @param faixas    faixas progressivas de consumo da categoria
 */
@Schema(name = "CategoriaFaixasRequest", description = "Categoria e suas faixas")
public record CategoriaFaixasRequest(

        @Schema(description = "Categoria do consumidor", example = "INDUSTRIAL")
        @NotNull(message = "{validacao.categoria.obrigatoria}")
        Categoria categoria,

        @Schema(description = "Faixas progressivas de consumo")
        @NotEmpty(message = "{validacao.faixas.obrigatorias}")
        @Valid
        List<FaixaRequest> faixas
) {
}
