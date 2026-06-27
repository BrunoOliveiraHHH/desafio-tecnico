package com.sinapsis.tarifaagua.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolve chaves de mensagens em textos amigáveis e parametrizados.
 *
 * <p>Encapsula o {@link MessageSource} do Spring, aplicando o idioma corrente
 * (via {@link LocaleContextHolder}, sensível ao cabeçalho {@code Accept-Language})
 * e substituindo os argumentos posicionais ({@code {0}}, {@code {1}}, ...).</p>
 *
 * <p>É tolerante a falhas: se uma chave não for encontrada, devolve a própria
 * chave em vez de propagar exceção — garantindo que o tratamento de erros nunca
 * falhe por causa de uma mensagem ausente.</p>
 */
@Component
@RequiredArgsConstructor
public class MensagemResolver {

    private final MessageSource messageSource;

    /**
     * Resolve a {@code chave} para o texto correspondente no idioma corrente.
     *
     * @param chave chave da mensagem (ver {@link MensagensErro})
     * @param args  argumentos que substituem os marcadores {@code {0}}, {@code {1}}, ...
     * @return o texto resolvido e formatado, ou a própria chave caso não exista
     */
    public String resolver(String chave, Object... args) {
        try {
            return messageSource.getMessage(chave, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException ex) {
            return chave;
        }
    }
}
