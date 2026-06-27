package com.testetecnico.tarifaagua;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

@DisplayName("TarifaAguaApplication - bootstrap")
class TarifaAguaApplicationTest {

    @Test
    @DisplayName("main delega para SpringApplication.run")
    void mainIniciaAplicacao() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            String[] args = {"--server.port=0"};
            TarifaAguaApplication.main(args);
            springApplication.verify(() -> SpringApplication.run(TarifaAguaApplication.class, args));
        }
    }
}
