package com.testetecnico.tarifaagua.bdd;

import com.testetecnico.tarifaagua.repository.FaixaConsumoRepository;
import com.testetecnico.tarifaagua.repository.TabelaTarifariaRepository;
import io.cucumber.java.Before;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Passos comuns a vários cenários: limpeza da base e asserções genéricas de
 * status HTTP e de mensagem de erro.
 */
public class PassosComuns {

    @Autowired
    private ContextoCenario contexto;

    @Autowired
    private TabelaTarifariaRepository tabelaRepository;

    @Autowired
    private FaixaConsumoRepository faixaRepository;

    /** Garante isolamento: limpa a base antes de cada cenário. */
    @Before
    public void limparBase() {
        faixaRepository.deleteAll();
        tabelaRepository.deleteAll();
    }

    @Então("a resposta deve ter status {int}")
    public void aRespostaDeveTerStatus(int statusEsperado) throws Exception {
        contexto.getUltimaResposta().andExpect(status().is(statusEsperado));
    }

    @E("a mensagem de erro deve conter {string}")
    public void aMensagemDeErroDeveConter(String trecho) throws Exception {
        contexto.getUltimaResposta().andExpect(jsonPath("$.mensagem", Matchers.containsString(trecho)));
    }
}
