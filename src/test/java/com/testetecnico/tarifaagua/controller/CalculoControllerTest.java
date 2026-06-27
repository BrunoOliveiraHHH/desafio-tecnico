package com.testetecnico.tarifaagua.controller;

import com.testetecnico.tarifaagua.domain.Categoria;
import com.testetecnico.tarifaagua.dto.CalculoResponse;
import com.testetecnico.tarifaagua.exception.MensagemResolver;
import com.testetecnico.tarifaagua.service.CalculoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalculoController.class)
@DisplayName("CalculoController - contrato HTTP do endpoint /api/calculos")
class CalculoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CalculoService service;

    @MockitoBean
    private MensagemResolver mensagemResolver;

    @Test
    @DisplayName("POST valido retorna 200 e o JSON no schema obrigatorio")
    void deveCalcularComSucesso() throws Exception {
        CalculoResponse resposta = new CalculoResponse(
                Categoria.INDUSTRIAL, 18, new BigDecimal("26.00"),
                List.of(
                        new CalculoResponse.DetalhamentoItem(
                                new CalculoResponse.FaixaIntervalo(0, 10), 10,
                                new BigDecimal("1.00"), new BigDecimal("10.00")),
                        new CalculoResponse.DetalhamentoItem(
                                new CalculoResponse.FaixaIntervalo(11, 20), 8,
                                new BigDecimal("2.00"), new BigDecimal("16.00"))));
        when(service.calcular(any())).thenReturn(resposta);

        mvc.perform(post("/api/calculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"INDUSTRIAL\",\"consumo\":18}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("INDUSTRIAL"))
                .andExpect(jsonPath("$.consumoTotal").value(18))
                .andExpect(jsonPath("$.valorTotal").value(26.00))
                .andExpect(jsonPath("$.detalhamento.length()").value(2))
                .andExpect(jsonPath("$.detalhamento[0].faixa.inicio").value(0))
                .andExpect(jsonPath("$.detalhamento[0].faixa.fim").value(10))
                .andExpect(jsonPath("$.detalhamento[0].m3Cobrados").value(10))
                .andExpect(jsonPath("$.detalhamento[0].subtotal").value(10.00))
                .andExpect(jsonPath("$.detalhamento[1].subtotal").value(16.00));
    }

    @Test
    @DisplayName("POST sem o campo consumo retorna 400 (validacao)")
    void deveRejeitarRequisicaoInvalida() throws Exception {
        mvc.perform(post("/api/calculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"INDUSTRIAL\"}"))
                .andExpect(status().isBadRequest());
    }
}
