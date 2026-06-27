package com.testetecnico.tarifaagua.controller;

import com.testetecnico.tarifaagua.dto.CalculoRequest;
import com.testetecnico.tarifaagua.dto.CalculoResponse;
import com.testetecnico.tarifaagua.dto.ErroResponse;
import com.testetecnico.tarifaagua.service.CalculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de cálculo do valor a pagar, com base na categoria e no consumo.
 */
@Tag(name = "Cálculos", description = "Cálculo progressivo do valor a pagar")
@RestController
@RequestMapping("/api/calculos")
@RequiredArgsConstructor
public class CalculoController {

    private final CalculoService service;

    /**
     * Calcula o valor a pagar de forma progressiva por faixas.
     *
     * @param request categoria e consumo
     * @return o valor total e o detalhamento por faixa
     */
    @Operation(summary = "Calcula o valor a pagar (progressivo por faixas)",
            description = "Usa as faixas e valores da tabela tarifária ativa vigente para a "
                    + "categoria informada. Exemplo: INDUSTRIAL com 18 m³ resulta em R$ 26,00.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cálculo realizado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "422", description = "Sem tabela ativa para a categoria "
                    + "ou consumo fora da cobertura",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    public CalculoResponse calcular(@Valid @RequestBody CalculoRequest request) {
        return service.calcular(request);
    }
}
