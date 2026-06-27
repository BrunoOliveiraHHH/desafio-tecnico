package com.sinapsis.tarifaagua.bdd;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Estado compartilhado entre os passos (steps) de um mesmo cenário.
 *
 * <p>Anotado com {@link ScenarioScope}, é recriado a cada cenário, garantindo
 * isolamento. Permite que diferentes classes de passos (tabela, cálculo, comuns)
 * compartilhem a última resposta HTTP e o id da última tabela criada.</p>
 */
@Component
@ScenarioScope
public class ContextoCenario {

    private ResultActions ultimaResposta;
    private Long ultimaTabelaId;

    public ResultActions getUltimaResposta() {
        return ultimaResposta;
    }

    public void setUltimaResposta(ResultActions ultimaResposta) {
        this.ultimaResposta = ultimaResposta;
    }

    public Long getUltimaTabelaId() {
        return ultimaTabelaId;
    }

    public void setUltimaTabelaId(Long ultimaTabelaId) {
        this.ultimaTabelaId = ultimaTabelaId;
    }
}
