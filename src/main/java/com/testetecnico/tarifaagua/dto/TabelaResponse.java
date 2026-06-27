package com.testetecnico.tarifaagua.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Representação de uma tabela tarifária retornada pela API, com suas categorias
 * e faixas.
 *
 * @param id           identificador da tabela
 * @param nome         nome da tabela
 * @param dataVigencia data de início de vigência
 * @param ativo        indica se a tabela está ativa
 * @param categorias   categorias com suas faixas
 */
@Schema(name = "TabelaResponse", description = "Tabela tarifária com suas categorias e faixas")
public record TabelaResponse(

        @Schema(description = "Identificador da tabela", example = "1")
        Long id,

        @Schema(description = "Nome da tabela", example = "Tabela Tarifária 2026")
        String nome,

        @Schema(description = "Data de início de vigência", example = "2026-01-01")
        LocalDate dataVigencia,

        @Schema(description = "Indica se a tabela está ativa", example = "true")
        boolean ativo,

        @Schema(description = "Categorias com suas faixas")
        List<CategoriaFaixasResponse> categorias
) {

    /**
     * Faixas de uma categoria dentro da tabela.
     *
     * @param categoria nome da categoria
     * @param faixas    faixas da categoria, ordenadas por início
     */
    @Schema(name = "CategoriaFaixasResponse", description = "Categoria e suas faixas")
    public record CategoriaFaixasResponse(

            @Schema(description = "Categoria do consumidor", example = "INDUSTRIAL")
            String categoria,

            @Schema(description = "Faixas da categoria")
            List<FaixaResponse> faixas
    ) {
    }

    /**
     * Faixa de consumo persistida.
     *
     * @param id            identificador da faixa
     * @param inicio        m³ inicial
     * @param fim           m³ final
     * @param valorUnitario valor unitário (R$/m³)
     */
    @Schema(name = "FaixaResponse", description = "Faixa de consumo com seu valor unitário")
    public record FaixaResponse(

            @Schema(description = "Identificador da faixa", example = "1")
            Long id,

            @Schema(description = "m³ inicial", example = "0")
            Integer inicio,

            @Schema(description = "m³ final", example = "10")
            Integer fim,

            @Schema(description = "Valor unitário (R$/m³)", example = "1.00")
            BigDecimal valorUnitario
    ) {
    }
}
