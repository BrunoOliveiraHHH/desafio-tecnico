package com.sinapsis.tarifaagua.dto;

import java.time.LocalDate;
import java.util.List;

public record TabelaResponse(
        Long id,
        String nome,
        LocalDate dataVigencia,
        boolean ativo,
        List<CategoriaFaixasResponse> categorias
) {

    public record CategoriaFaixasResponse(
            String categoria,
            List<FaixaResponse> faixas
    ) {
    }

    public record FaixaResponse(
            Long id,
            Integer inicio,
            Integer fim,
            java.math.BigDecimal valorUnitario
    ) {
    }
}
