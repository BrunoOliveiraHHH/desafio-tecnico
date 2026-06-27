package com.sinapsis.tarifaagua.exception;

import com.sinapsis.tarifaagua.domain.Categoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MensagemResolver - resolução e fallback")
class MensagemResolverTest {

    private MensagemResolver resolver;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        resolver = new MensagemResolver(ms);
    }

    @Test
    @DisplayName("Resolve a chave aplicando os argumentos")
    void resolveChaveConhecida() {
        String texto = resolver.resolver(MensagensErro.FAIXA_COBERTURA_INICIAL, Categoria.INDUSTRIAL);
        assertThat(texto).contains("INDUSTRIAL").contains("iniciar em 0");
    }

    @Test
    @DisplayName("Devolve a própria chave quando a mensagem não existe (fallback)")
    void retornaChaveQuandoDesconhecida() {
        assertThat(resolver.resolver("chave.inexistente")).isEqualTo("chave.inexistente");
    }
}
