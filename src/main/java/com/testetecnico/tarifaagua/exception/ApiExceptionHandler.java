package com.testetecnico.tarifaagua.exception;

import com.testetecnico.tarifaagua.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Tratamento centralizado de exceções da API.
 *
 * <p>Converte qualquer falha — de regra de negócio, validação, requisição
 * malformada ou erro inesperado — em uma resposta {@link ErroResponse}
 * consistente, sempre com uma <strong>mensagem amigável e parametrizada</strong>
 * resolvida pelo {@link MensagemResolver} (com suporte a i18n).</p>
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final MensagemResolver mensagens;

    /** Recurso inexistente (ex.: tabela não encontrada) → 404. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                             HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, mensagens.resolver(ex.getChave(), ex.getArgs()), request);
    }

    /** Violação de regra de negócio (faixas inconsistentes, consumo fora de cobertura) → 422. */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException ex,
                                                            HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_CONTENT, mensagens.resolver(ex.getChave(), ex.getArgs()), request);
    }

    /** Falha de validação do corpo (Bean Validation) → 400, com detalhes por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex,
                                                         HttpServletRequest request) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatarErroCampo)
                .toList();
        return construir(HttpStatus.BAD_REQUEST,
                mensagens.resolver(MensagensErro.API_REQUISICAO_INVALIDA), detalhes, request);
    }

    /** Corpo JSON ausente, malformado ou com valor incompatível → 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                mensagens.resolver(MensagensErro.API_CORPO_INVALIDO), request);
    }

    /** Parâmetro de path/query com tipo inválido (ex.: ?categoria=XPTO) → 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String mensagem = mensagens.resolver(MensagensErro.API_PARAMETRO_INVALIDO, ex.getName());
        List<String> detalhes = List.of();
        Class<?> tipo = ex.getRequiredType();
        if (tipo != null && tipo.isEnum()) {
            detalhes = List.of(mensagens.resolver(MensagensErro.API_VALORES_ACEITOS,
                    Arrays.toString(tipo.getEnumConstants())));
        }
        return construir(HttpStatus.BAD_REQUEST, mensagem, detalhes, request);
    }

    /** Parâmetro obrigatório ausente → 400. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> tratarParametroAusente(MissingServletRequestParameterException ex,
                                                               HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                mensagens.resolver(MensagensErro.API_PARAMETRO_OBRIGATORIO, ex.getParameterName()), request);
    }

    /** Método HTTP não suportado pelo recurso → 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED,
                mensagens.resolver(MensagensErro.API_METODO_NAO_SUPORTADO, ex.getMethod()), request);
    }

    /** Rota inexistente → 404. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> tratarRotaInexistente(NoResourceFoundException ex,
                                                              HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND,
                mensagens.resolver(MensagensErro.API_ROTA_NAO_ENCONTRADA, request.getRequestURI()), request);
    }

    /** Violação de integridade no banco (ex.: constraint única) → 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarConflito(DataIntegrityViolationException ex,
                                                       HttpServletRequest request) {
        log.debug("Causa da violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT,
                mensagens.resolver(MensagensErro.API_CONFLITO_INTEGRIDADE), request);
    }

    /** Qualquer outra falha não prevista → 500 (com log para diagnóstico). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                mensagens.resolver(MensagensErro.API_ERRO_INTERNO), request);
    }

    // ----- apoio -----

    private String formatarErroCampo(FieldError erro) {
        return erro.getField() + ": " + erro.getDefaultMessage();
    }

    private ResponseEntity<ErroResponse> construir(HttpStatus status, String mensagem, HttpServletRequest request) {
        return construir(status, mensagem, List.of(), request);
    }

    private ResponseEntity<ErroResponse> construir(HttpStatus status, String mensagem,
                                                   List<String> detalhes, HttpServletRequest request) {
        if (status.is4xxClientError()) {
            log.warn("Requisição rejeitada: status={} {} {} - {}",
                    status.value(), request.getMethod(), request.getRequestURI(), mensagem);
        }
        ErroResponse corpo = new ErroResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                detalhes);
        return ResponseEntity.status(status).body(corpo);
    }
}
