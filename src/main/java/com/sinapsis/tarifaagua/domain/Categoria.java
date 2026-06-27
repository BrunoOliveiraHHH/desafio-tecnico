package com.sinapsis.tarifaagua.domain;

/**
 * Categorias de consumidores suportadas pelo sistema.
 * O valor monetario NAO vive aqui: as faixas e tarifas sao parametrizadas
 * no banco de dados (entidade {@link FaixaConsumo}).
 */
public enum Categoria {
    COMERCIAL,
    INDUSTRIAL,
    PARTICULAR,
    PUBLICO
}
