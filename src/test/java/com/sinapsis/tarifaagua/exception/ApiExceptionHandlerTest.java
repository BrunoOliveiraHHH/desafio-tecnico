package com.sinapsis.tarifaagua.exception;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ApiExceptionHandler - todos os caminhos de erro")
class ApiExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        handler = new ApiExceptionHandler(new MensagemResolver(ms));
        lenient().when(request.getRequestURI()).thenReturn("/api/teste");
        lenient().when(request.getMethod()).thenReturn("POST");
    }

    @Test
    @DisplayName("Recurso não encontrado → 404")
    void naoEncontrado() {
        var resp = handler.tratarNaoEncontrado(
                new RecursoNaoEncontradoException(MensagensErro.TABELA_NAO_ENCONTRADA, 99L), request);
        assertStatus(resp, HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().mensagem()).contains("99");
        assertThat(resp.getBody().caminho()).isEqualTo("/api/teste");
    }

    @Test
    @DisplayName("Regra de negócio → 422")
    void regraNegocio() {
        var resp = handler.tratarRegraNegocio(
                new RegraNegocioException(MensagensErro.FAIXA_COBERTURA_INICIAL, Categoria.INDUSTRIAL), request);
        assertStatus(resp, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(resp.getBody().mensagem()).contains("INDUSTRIAL");
    }

    @Test
    @DisplayName("Validação de corpo → 400 com detalhes por campo")
    void validacao() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Object(), "obj");
        br.addError(new FieldError("obj", "consumo", "é obrigatório"));
        when(ex.getBindingResult()).thenReturn(br);

        var resp = handler.tratarValidacao(ex, request);
        assertStatus(resp, HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().detalhes()).containsExactly("consumo: é obrigatório");
    }

    @Test
    @DisplayName("Corpo malformado → 400")
    void corpoInvalido() {
        var resp = handler.tratarCorpoInvalido(mock(HttpMessageNotReadableException.class), request);
        assertStatus(resp, HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Parâmetro de enum inválido → 400 com valores aceitos")
    void tipoInvalidoEnum() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("categoria");
        doReturn(Categoria.class).when(ex).getRequiredType();

        var resp = handler.tratarTipoInvalido(ex, request);
        assertStatus(resp, HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().detalhes()).isNotEmpty();
        assertThat(resp.getBody().detalhes().get(0)).contains("INDUSTRIAL");
    }

    @Test
    @DisplayName("Parâmetro não-enum inválido → 400 sem valores aceitos")
    void tipoInvalidoNaoEnum() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        doReturn(Integer.class).when(ex).getRequiredType();

        var resp = handler.tratarTipoInvalido(ex, request);
        assertStatus(resp, HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().detalhes()).isEmpty();
    }

    @Test
    @DisplayName("Parâmetro inválido sem tipo conhecido → 400 sem valores aceitos")
    void tipoInvalidoSemTipo() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("param");
        doReturn(null).when(ex).getRequiredType();

        var resp = handler.tratarTipoInvalido(ex, request);
        assertStatus(resp, HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().detalhes()).isEmpty();
    }

    @Test
    @DisplayName("Parâmetro obrigatório ausente → 400")
    void parametroAusente() {
        var resp = handler.tratarParametroAusente(
                new MissingServletRequestParameterException("categoria", "String"), request);
        assertStatus(resp, HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Método não suportado → 405")
    void metodoNaoSuportado() {
        var resp = handler.tratarMetodoNaoSuportado(
                new HttpRequestMethodNotSupportedException("PUT"), request);
        assertStatus(resp, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    @DisplayName("Rota inexistente → 404")
    void rotaInexistente() {
        var resp = handler.tratarRotaInexistente(mock(NoResourceFoundException.class), request);
        assertStatus(resp, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Violação de integridade → 409")
    void conflito() {
        var resp = handler.tratarConflito(new DataIntegrityViolationException("constraint"), request);
        assertStatus(resp, HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Erro inesperado → 500")
    void inesperado() {
        var resp = handler.tratarInesperado(new RuntimeException("boom"), request);
        assertStatus(resp, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void assertStatus(ResponseEntity<ErroResponse> resp, HttpStatus esperado) {
        assertThat(resp.getStatusCode()).isEqualTo(esperado);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().status()).isEqualTo(esperado.value());
        assertThat(resp.getBody().mensagem()).isNotBlank();
    }
}
