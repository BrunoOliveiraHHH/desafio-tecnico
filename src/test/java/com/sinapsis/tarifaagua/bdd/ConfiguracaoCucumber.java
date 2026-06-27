package com.sinapsis.tarifaagua.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuração do contexto Spring para os cenários Cucumber (BDD).
 *
 * <p>Sobe a aplicação completa contra o banco H2 em memória (perfil {@code test})
 * e disponibiliza o {@code MockMvc} para os passos de teste.</p>
 */
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ConfiguracaoCucumber {
}
