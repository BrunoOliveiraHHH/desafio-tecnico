package com.sinapsis.tarifaagua.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Executor dos testes de aceite BDD (Cucumber) via JUnit Platform.
 *
 * <p>Descobre os arquivos {@code .feature} em {@code src/test/resources/features}
 * e os liga aos passos do pacote {@code com.sinapsis.tarifaagua.bdd}. Por ter o
 * sufixo {@code IT}, é executado pelo Failsafe em {@code mvn verify}.</p>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.sinapsis.tarifaagua.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, summary")
class ExecutarCriteriosAceiteIT {
}
