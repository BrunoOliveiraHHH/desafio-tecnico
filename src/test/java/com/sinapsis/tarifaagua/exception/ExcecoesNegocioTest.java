package com.sinapsis.tarifaagua.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exceções de negócio - chave e argumentos")
class ExcecoesNegocioTest {

    @Test
    @DisplayName("RegraNegocioException guarda a chave e os argumentos")
    void regraNegocioGuardaChaveEArgs() {
        RegraNegocioException ex = new RegraNegocioException("negocio.exemplo", "A", 10);
        assertThat(ex.getChave()).isEqualTo("negocio.exemplo");
        assertThat(ex.getArgs()).containsExactly("A", 10);
    }

    @Test
    @DisplayName("RegraNegocioException trata argumentos nulos como vazio")
    void regraNegocioArgsNulos() {
        RegraNegocioException ex = new RegraNegocioException("negocio.exemplo", (Object[]) null);
        assertThat(ex.getArgs()).isEmpty();
    }

    @Test
    @DisplayName("RecursoNaoEncontradoException guarda a chave e os argumentos")
    void recursoGuardaChaveEArgs() {
        RecursoNaoEncontradoException ex = new RecursoNaoEncontradoException("recurso.exemplo", 5L);
        assertThat(ex.getChave()).isEqualTo("recurso.exemplo");
        assertThat(ex.getArgs()).containsExactly(5L);
    }

    @Test
    @DisplayName("RecursoNaoEncontradoException trata argumentos nulos como vazio")
    void recursoArgsNulos() {
        RecursoNaoEncontradoException ex = new RecursoNaoEncontradoException("recurso.exemplo", (Object[]) null);
        assertThat(ex.getArgs()).isEmpty();
    }
}
