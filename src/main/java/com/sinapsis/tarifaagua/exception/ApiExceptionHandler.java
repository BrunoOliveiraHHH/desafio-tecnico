package com.sinapsis.tarifaagua.exception;

import com.sinapsis.tarifaagua.dto.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException ex) {
        return construir(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatarErro)
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Requisicao invalida", detalhes);
    }

    /**
     * Corpo JSON ausente, malformado ou com valor incompativel (ex.: categoria
     * inexistente no enum).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido", List.of());
    }

    /**
     * Parametro de path/query com tipo invalido (ex.: ?categoria=XPTO). Para
     * enums, lista os valores aceitos.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        String mensagem = "Parametro invalido: " + ex.getName();
        List<String> detalhes = List.of();
        Class<?> tipo = ex.getRequiredType();
        if (tipo != null && tipo.isEnum()) {
            detalhes = List.of("Valores aceitos: " + Arrays.toString(tipo.getEnumConstants()));
        }
        return construir(HttpStatus.BAD_REQUEST, mensagem, detalhes);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarInesperado(Exception ex) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", List.of());
    }

    private String formatarErro(FieldError erro) {
        return erro.getField() + ": " + erro.getDefaultMessage();
    }

    private ResponseEntity<ErroResponse> construir(HttpStatus status, String mensagem, List<String> detalhes) {
        ErroResponse corpo = new ErroResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                detalhes);
        return ResponseEntity.status(status).body(corpo);
    }
}
