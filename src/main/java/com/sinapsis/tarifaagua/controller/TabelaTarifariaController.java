package com.sinapsis.tarifaagua.controller;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.dto.CriarTabelaRequest;
import com.sinapsis.tarifaagua.dto.TabelaResponse;
import com.sinapsis.tarifaagua.service.TabelaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Tabelas Tarifarias", description = "Gerenciamento das tabelas tarifarias e suas faixas")
@RestController
@RequestMapping("/api/tabelas-tarifarias")
public class TabelaTarifariaController {

    private final TabelaService service;

    public TabelaTarifariaController(TabelaService service) {
        this.service = service;
    }

    @Operation(summary = "Cria uma tabela tarifaria completa (categorias + faixas + valores)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tabela criada"),
            @ApiResponse(responseCode = "400", description = "Payload invalido"),
            @ApiResponse(responseCode = "422", description = "Faixas inconsistentes")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TabelaResponse criar(@Valid @RequestBody CriarTabelaRequest request) {
        return service.criar(request);
    }

    @Operation(summary = "Lista as tabelas tarifarias ativas",
            description = "Quando o parametro 'categoria' e informado, retorna apenas as faixas daquela categoria.")
    @GetMapping
    public List<TabelaResponse> listar(
            @Parameter(description = "Filtra as faixas pela categoria informada")
            @RequestParam(required = false) Categoria categoria) {
        return service.listar(categoria);
    }

    @Operation(summary = "Exclui (logicamente) uma tabela tarifaria",
            description = "Exclusao logica: a tabela deixa de ser usada em calculos futuros.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tabela excluida"),
            @ApiResponse(responseCode = "404", description = "Tabela nao encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
