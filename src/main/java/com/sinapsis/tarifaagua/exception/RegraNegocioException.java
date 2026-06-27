package com.sinapsis.tarifaagua.exception;

/**
 * Exceção de violação de regra de negócio (ex.: faixas inconsistentes, consumo
 * sem cobertura, categoria duplicada). Mapeada para o status HTTP
 * {@code 422 Unprocessable Content} pelo {@link ApiExceptionHandler}.
 *
 * <p>Carrega a <strong>chave</strong> da mensagem (ver {@link MensagensErro}) e
 * seus argumentos, deixando a resolução do texto amigável e parametrizado a
 * cargo do {@link MensagemResolver}. Assim a camada de negócio permanece
 * desacoplada da formatação e do idioma das mensagens.</p>
 */
public class RegraNegocioException extends RuntimeException {

    private final transient Object[] args;

    /**
     * @param chave chave da mensagem em {@code messages.properties}
     * @param args  argumentos que substituem os marcadores {@code {0}}, {@code {1}}, ...
     */
    public RegraNegocioException(String chave, Object... args) {
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
