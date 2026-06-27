package com.sinapsis.tarifaagua.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

/**
 * Dados de entrada para criação de uma tabela tarifária completa (categorias e
 * suas faixas). Schema do payload proposto conforme o item 2.2 do desafio.
 *
 * @param nome         nome identificador da tabela
 * @param dataVigencia data de início de vigência (se ausente, assume a data atual)
 * @param categorias   categorias com suas respectivas faixas
 */
@Schema(name = "CriarTabelaRequest", description = "Estrutura completa de uma tabela tarifária")
public record CriarTabelaRequest(

        @Schema(description = "Nome da tabela tarifária", example = "Tabela Tarifária 2026")
        @NotBlank(message = "{validacao.tabela.nome}")
        String nome,

        @Schema(description = "Data de início de vigência (ISO-8601)", example = "2026-01-01")
        LocalDate dataVigencia,

        @Schema(description = "Categorias e suas faixas progressivas")
        @NotEmpty(message = "{validacao.tabela.categorias}")
        @Valid
        List<CategoriaFaixasRequest> categorias
) {
}
