package com.sinapsis.tarifaagua.bdd;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Passos relativos às tabelas tarifárias: criação (a partir de uma data table do
 * Gherkin), listagem (com e sem filtro) e exclusão.
 */
public class PassosTabela {

    private static final String TABELAS = "/api/tabelas-tarifarias";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContextoCenario contexto;

    /** Monta o JSON de criação a partir da data table de faixas do cenário. */
    private String corpoTabela(String categoria, DataTable faixas) {
        String faixasJson = faixas.asMaps().stream()
                .map(linha -> String.format("{\"inicio\":%s,\"fim\":%s,\"valorUnitario\":%s}",
                        linha.get("inicio"), linha.get("fim"), linha.get("valorUnitario")))
                .collect(Collectors.joining(","));
        return String.format(
                "{\"nome\":\"Tabela BDD\",\"dataVigencia\":\"2026-01-01\","
                        + "\"categorias\":[{\"categoria\":\"%s\",\"faixas\":[%s]}]}",
                categoria, faixasJson);
    }

    @Dado("uma tabela tarifária ativa para a categoria {string} com as faixas:")
    public void dadaTabelaAtiva(String categoria, DataTable faixas) throws Exception {
        String corpo = mvc.perform(post(TABELAS).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoTabela(categoria, faixas)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        contexto.setUltimaTabelaId(((Number) JsonPath.read(corpo, "$.id")).longValue());
    }

    @Quando("eu tento criar uma tabela para a categoria {string} com as faixas:")
    public void euTentoCriar(String categoria, DataTable faixas) throws Exception {
        contexto.setUltimaResposta(mvc.perform(post(TABELAS).contentType(MediaType.APPLICATION_JSON)
                .content(corpoTabela(categoria, faixas))));
    }

    @Quando("eu listo as tabelas")
    public void euListoAsTabelas() throws Exception {
        contexto.setUltimaResposta(mvc.perform(get(TABELAS)));
    }

    @Quando("eu listo as tabelas da categoria {string}")
    public void euListoPorCategoria(String categoria) throws Exception {
        contexto.setUltimaResposta(mvc.perform(get(TABELAS).param("categoria", categoria)));
    }

    @Quando("eu excluo a tabela criada")
    public void euExcluoATabelaCriada() throws Exception {
        contexto.setUltimaResposta(mvc.perform(delete(TABELAS + "/" + contexto.getUltimaTabelaId())));
    }

    @Quando("eu excluo a tabela {int}")
    public void euExcluoATabela(int id) throws Exception {
        contexto.setUltimaResposta(mvc.perform(delete(TABELAS + "/" + id)));
    }

    @E("a lista deve conter {int} tabela(s)")
    public void aListaDeveConter(int quantidade) throws Exception {
        contexto.getUltimaResposta().andExpect(jsonPath("$.length()").value(quantidade));
    }

    @E("a primeira tabela deve ter a categoria {string}")
    public void aPrimeiraTabelaDeveTerCategoria(String categoria) throws Exception {
        contexto.getUltimaResposta().andExpect(jsonPath("$[0].categorias[0].categoria").value(categoria));
    }

    @Então("a tabela criada não deve aparecer na listagem")
    public void aTabelaCriadaNaoDeveAparecer() throws Exception {
        contexto.getUltimaResposta()
                .andExpect(jsonPath("$[?(@.id == " + contexto.getUltimaTabelaId() + ")]").isEmpty());
    }
}
