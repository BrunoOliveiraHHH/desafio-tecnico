package com.sinapsis.tarifaagua.controller;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.dto.TabelaResponse;
import com.sinapsis.tarifaagua.exception.RecursoNaoEncontradoException;
import com.sinapsis.tarifaagua.service.TabelaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TabelaTarifariaController.class)
@DisplayName("TabelaTarifariaController - contrato HTTP de /api/tabelas-tarifarias")
class TabelaTarifariaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TabelaService service;

    @Test
    @DisplayName("POST valido retorna 201 com a tabela criada")
    void deveCriarTabela() throws Exception {
        TabelaResponse resposta = new TabelaResponse(1L, "Tabela 2026", LocalDate.of(2026, 1, 1), true,
                List.of(new TabelaResponse.CategoriaFaixasResponse("INDUSTRIAL",
                        List.of(new TabelaResponse.FaixaResponse(1L, 0, 10, new BigDecimal("1.00"))))));
        when(service.criar(any())).thenReturn(resposta);

        String body = """
                {
                  "nome": "Tabela 2026",
                  "dataVigencia": "2026-01-01",
                  "categorias": [
                    { "categoria": "INDUSTRIAL", "faixas": [ { "inicio": 0, "fim": 10, "valorUnitario": 1.00 } ] }
                  ]
                }
                """;

        mvc.perform(post("/api/tabelas-tarifarias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categorias[0].categoria").value("INDUSTRIAL"));
    }

    @Test
    @DisplayName("POST com lista de categorias vazia retorna 400")
    void deveRejeitarPayloadInvalido() throws Exception {
        String body = """
                { "nome": "X", "dataVigencia": "2026-01-01", "categorias": [] }
                """;

        mvc.perform(post("/api/tabelas-tarifarias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST com categoria inexistente retorna 400")
    void deveRejeitarCategoriaInvalida() throws Exception {
        String body = """
                {
                  "nome": "X", "dataVigencia": "2026-01-01",
                  "categorias": [ { "categoria": "RESIDENCIAL", "faixas": [ { "inicio": 0, "fim": 10, "valorUnitario": 1.00 } ] } ]
                }
                """;

        mvc.perform(post("/api/tabelas-tarifarias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET com filtro de categoria delega ao service")
    void deveListarComFiltro() throws Exception {
        when(service.listar(Categoria.INDUSTRIAL)).thenReturn(List.of());

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/tabelas-tarifarias").param("categoria", "INDUSTRIAL"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE de tabela existente retorna 204")
    void deveExcluirTabela() throws Exception {
        mvc.perform(delete("/api/tabelas-tarifarias/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE de tabela inexistente retorna 404")
    void deveRetornar404AoExcluirInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("nao encontrada"))
                .when(service).excluir(99L);

        mvc.perform(delete("/api/tabelas-tarifarias/99"))
                .andExpect(status().isNotFound());
    }
}
