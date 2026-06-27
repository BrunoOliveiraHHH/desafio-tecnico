package com.testetecnico.tarifaagua.domain;

/**
 * Categorias de consumidores suportadas pelo sistema.
 *
 * <p>O valor monetário <strong>não</strong> reside aqui: as faixas e tarifas são
 * parametrizadas no banco de dados (entidade {@link FaixaConsumo}), permitindo
 * ajustes sem alteração de código.</p>
 */
public enum Categoria {

    /** Estabelecimentos comerciais. */
    COMERCIAL,

    /** Indústrias e fábricas. */
    INDUSTRIAL,

    /** Residências. */
    PARTICULAR,

    /** Órgãos públicos. */
    PUBLICO
}
