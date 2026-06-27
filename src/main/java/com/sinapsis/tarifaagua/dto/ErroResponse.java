package com.sinapsis.tarifaagua.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Corpo padronizado de resposta de erro, retornado por todos os endpoints em
 * caso de falha. As mensagens são amigáveis e parametrizadas (ver
 * {@code messages.properties}).
 *
 * @param timestamp instante em que o erro ocorreu (UTC)
 * @param status    código HTTP
 * @param erro      descrição curta do status (reason phrase)
 * @param mensagem  mensagem amigável explicando a falha
 * @param caminho   caminho (URI) da requisição que originou o erro
 * @param detalhes  lista opcional de detalhes (ex.: erros de validação por campo)
 */
@Schema(name = "ErroResponse", description = "Resposta padrão de erro da API")
public record ErroResponse(

        @Schema(description = "Instante do erro (UTC)", example = "2026-01-01T12:00:00Z")
        Instant timestamp,

        @Schema(description = "Código HTTP", example = "422")
        int status,

        @Schema(description = "Descrição curta do status HTTP", example = "Unprocessable Content")
        String erro,

        @Schema(description = "Mensagem amigável explicando a falha",
                example = "A primeira faixa da categoria INDUSTRIAL deve iniciar em 0 m³.")
        String mensagem,

        @Schema(description = "Caminho da requisição", example = "/api/calculos")
        String caminho,

        @Schema(description = "Detalhes adicionais (ex.: erros de validação por campo)")
        List<String> detalhes
) {
}
