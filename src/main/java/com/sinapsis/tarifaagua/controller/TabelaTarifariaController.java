package com.sinapsis.tarifaagua.controller;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.dto.CriarTabelaRequest;
import com.sinapsis.tarifaagua.dto.ErroResponse;
import com.sinapsis.tarifaagua.dto.TabelaResponse;
import com.sinapsis.tarifaagua.service.TabelaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

/**
 * Endpoints de gerenciamento das tabelas tarifárias: criação, listagem e
 * exclusão lógica.
 */
@Tag(name = "Tabelas Tarifárias", description = "Gerenciamento das tabelas tarifárias e suas faixas")
@RestController
@RequestMapping("/api/tabelas-tarifarias")
@RequiredArgsConstructor
public class TabelaTarifariaController {

    private final TabelaService service;

    /**
     * Cria uma tabela tarifária completa (todas as categorias, faixas e valores).
     *
     * @param request estrutura completa da tabela
     * @return a tabela criada (HTTP 201)
     */
    @Operation(summary = "Cria uma tabela tarifária completa",
            description = "Cria uma nova tabela com todas as categorias e suas faixas de consumo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tabela criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "422", description = "Faixas inconsistentes",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TabelaResponse criar(@Valid @RequestBody CriarTabelaRequest request) {
        return service.criar(request);
    }

    /**
     * Lista as tabelas tarifárias ativas, opcionalmente filtrando pelas faixas de
     * uma categoria.
     *
     * @param categoria categoria a filtrar (opcional)
     * @return as tabelas ativas
     */
    @Operation(summary = "Lista as tabelas tarifárias ativas",
            description = "Retorna todas as tabelas ativas. Se 'categoria' for informada, "
                    + "retorna apenas as faixas daquela categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de tabelas"),
            @ApiResponse(responseCode = "400", description = "Categoria inválida",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping
    public List<TabelaResponse> listar(
            @Parameter(description = "Filtra as faixas pela categoria informada", example = "INDUSTRIAL")
            @RequestParam(required = false) Categoria categoria) {
        return service.listar(categoria);
    }

    /**
     * Exclui (logicamente) uma tabela tarifária.
     *
     * @param id identificador da tabela
     * @return resposta vazia (HTTP 204)
     */
    @Operation(summary = "Exclui (logicamente) uma tabela tarifária",
            description = "Exclusão lógica: a tabela deixa de ser usada em cálculos futuros, "
                    + "preservando o histórico.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tabela excluída"),
            @ApiResponse(responseCode = "404", description = "Tabela não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador da tabela", example = "1")
            @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
