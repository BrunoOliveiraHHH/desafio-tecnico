package com.sinapsis.tarifaagua.controller;

import com.sinapsis.tarifaagua.dto.CalculoRequest;
import com.sinapsis.tarifaagua.dto.CalculoResponse;
import com.sinapsis.tarifaagua.service.CalculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Calculos", description = "Calculo progressivo do valor a pagar")
@RestController
@RequestMapping("/api/calculos")
public class CalculoController {

    private final CalculoService service;

    public CalculoController(CalculoService service) {
        this.service = service;
    }

    @Operation(summary = "Calcula o valor a pagar de forma progressiva por faixas",
            description = "Usa as faixas/valores da tabela tarifaria ativa vigente para a categoria informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calculo realizado"),
            @ApiResponse(responseCode = "400", description = "Payload invalido"),
            @ApiResponse(responseCode = "422", description = "Sem tabela ativa ou consumo fora da cobertura")
    })
    @PostMapping
    public CalculoResponse calcular(@Valid @RequestBody CalculoRequest request) {
        return service.calcular(request);
    }
}
