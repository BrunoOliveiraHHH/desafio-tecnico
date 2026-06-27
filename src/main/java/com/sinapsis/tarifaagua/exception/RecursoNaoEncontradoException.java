package com.sinapsis.tarifaagua.exception;

/**
 * Exceção para recurso inexistente (ex.: tabela tarifária não localizada).
 * Mapeada para o status HTTP {@code 404 Not Found} pelo
 * {@link ApiExceptionHandler}.
 *
 * <p>Carrega a <strong>chave</strong> da mensagem (ver {@link MensagensErro}) e
 * seus argumentos; o texto amigável é resolvido pelo {@link MensagemResolver}.</p>
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    private final transient Object[] args;

    /**
     * @param chave chave da mensagem em {@code messages.properties}
     * @param args  argumentos que substituem os marcadores {@code {0}}, {@code {1}}, ...
     */
    public RecursoNaoEncontradoException(String chave, Object... args) {
        super(chave);
        this.args = args != null ? args.clone() : new Object[0];
    }

    /** @return a chave da mensagem (idêntica a {@link #getMessage()}) */
    public String getChave() {
        return getMessage();
    }

    /** @return cópia defensiva dos argumentos da mensagem */
    public Object[] getArgs() {
        return args.clone();
    }
}
