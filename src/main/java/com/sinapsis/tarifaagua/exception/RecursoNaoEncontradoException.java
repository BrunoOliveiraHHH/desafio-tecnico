package com.sinapsis.tarifaagua.exception;

/**
 * Recurso inexistente (ex.: tabela nao encontrada). Mapeada para HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
