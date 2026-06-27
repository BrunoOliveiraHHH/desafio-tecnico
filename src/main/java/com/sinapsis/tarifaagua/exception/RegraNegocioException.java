package com.sinapsis.tarifaagua.exception;

/**
 * Violacao de regra de negocio (ex.: faixas inconsistentes, consumo sem
 * cobertura). Mapeada para HTTP 422.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
