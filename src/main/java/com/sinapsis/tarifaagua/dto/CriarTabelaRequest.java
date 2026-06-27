package com.sinapsis.tarifaagua.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

/**
 * Payload de criacao de uma tabela tarifaria completa (categorias + faixas).
 * Schema proposto pelo desenvolvedor, conforme item 2.2 do desafio.
 */
public record CriarTabelaRequest(
        @NotBlank String nome,
        LocalDate dataVigencia,
        @NotEmpty @Valid List<CategoriaFaixasRequest> categorias
) {
}
