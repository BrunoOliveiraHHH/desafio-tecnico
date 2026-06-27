package com.sinapsis.tarifaagua.dto;

import com.sinapsis.tarifaagua.domain.Categoria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CategoriaFaixasRequest(
        @NotNull Categoria categoria,
        @NotEmpty @Valid List<FaixaRequest> faixas
) {
}
