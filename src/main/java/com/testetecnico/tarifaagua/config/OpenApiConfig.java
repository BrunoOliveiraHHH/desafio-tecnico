package com.testetecnico.tarifaagua.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tarifaAguaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Tabela Tarifaria de Agua")
                        .description("""
                                Gerenciamento e calculo de tarifas de agua por categoria de consumidor
                                e faixas progressivas de consumo. As faixas e valores sao totalmente
                                parametrizados no banco de dados: ajustes refletem nos calculos sem
                                alteracao de codigo.""")
                        .version("1.0.0")
                        .contact(new Contact().name("Teste Técnico"))
                        .license(new License().name("Proprietario")));
    }
}
