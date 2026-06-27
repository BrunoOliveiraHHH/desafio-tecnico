package com.testetecnico.tarifaagua.bdd;

import com.testetecnico.tarifaagua.domain.Categoria;
import com.testetecnico.tarifaagua.domain.FaixaConsumo;
import com.testetecnico.tarifaagua.repository.FaixaConsumoRepository;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Passos relativos ao cálculo do valor a pagar e à demonstração da
 * parametrização (alterar um valor no banco e recalcular).
 */
public class PassosCalculo {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContextoCenario contexto;

    @Autowired
    private FaixaConsumoRepository faixaRepository;

    @Quando("eu calculo o valor para a categoria {string} e consumo {int}")
    public void euCalculoOValor(String categoria, int consumo) throws Exception {
        contexto.setUltimaResposta(mvc.perform(post("/api/calculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"categoria\":\"%s\",\"consumo\":%d}", categoria, consumo))));
    }

    @Quando("eu altero no banco o valor unitário da faixa que inicia em {int} da categoria {string} para {string}")
    public void euAlteroOValorDaFaixa(int inicio, String categoria, String valor) {
        FaixaConsumo faixa = faixaRepository.findVigentesPorCategoria(Categoria.valueOf(categoria)).stream()
                .filter(f -> f.getInicio() == inicio)
                .findFirst()
                .orElseThrow();
        // Valor como texto, convertido com BigDecimal (ponto decimal, independente de locale).
        faixa.setValorUnitario(new BigDecimal(valor));
        faixaRepository.save(faixa);
    }

    @Então("o valor total deve ser {string}")
    public void oValorTotalDeveSer(String esperado) throws Exception {
        contexto.getUltimaResposta()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(Double.parseDouble(esperado)));
    }

    @E("o detalhamento deve conter {int} faixa(s)")
    public void oDetalhamentoDeveConter(int quantidade) throws Exception {
        contexto.getUltimaResposta().andExpect(jsonPath("$.detalhamento.length()").value(quantidade));
    }
}
