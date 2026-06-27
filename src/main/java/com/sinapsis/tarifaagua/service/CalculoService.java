package com.sinapsis.tarifaagua.service;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.domain.FaixaConsumo;
import com.sinapsis.tarifaagua.dto.CalculoRequest;
import com.sinapsis.tarifaagua.dto.CalculoResponse;
import com.sinapsis.tarifaagua.exception.RegraNegocioException;
import com.sinapsis.tarifaagua.repository.FaixaConsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calcula o valor a pagar de forma progressiva por faixas, usando exclusivamente
 * os valores parametrizados no banco. Mudancas nas faixas/valores refletem
 * automaticamente aqui, sem alteracao de codigo.
 */
@Service
public class CalculoService {

    private final FaixaConsumoRepository faixaRepository;

    public CalculoService(FaixaConsumoRepository faixaRepository) {
        this.faixaRepository = faixaRepository;
    }

    @Transactional(readOnly = true)
    public CalculoResponse calcular(CalculoRequest req) {
        Categoria categoria = req.categoria();
        int consumo = req.consumo();

        List<FaixaConsumo> faixas = obterFaixasVigentes(categoria);

        int coberturaMaxima = faixas.get(faixas.size() - 1).getFim();
        if (consumo > coberturaMaxima) {
            throw new RegraNegocioException("Consumo de " + consumo
                    + " m3 excede a cobertura das faixas (maximo " + coberturaMaxima + " m3)");
        }

        List<CalculoResponse.DetalhamentoItem> detalhamento = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;
        int pisoAnterior = 0;

        for (FaixaConsumo faixa : faixas) {
            int teto = Math.min(consumo, faixa.getFim());
            int m3Cobrados = teto - pisoAnterior;

            if (m3Cobrados > 0) {
                BigDecimal subtotal = faixa.getValorUnitario()
                        .multiply(BigDecimal.valueOf(m3Cobrados));
                detalhamento.add(new CalculoResponse.DetalhamentoItem(
                        new CalculoResponse.FaixaIntervalo(faixa.getInicio(), faixa.getFim()),
                        m3Cobrados,
                        faixa.getValorUnitario(),
                        subtotal));
                valorTotal = valorTotal.add(subtotal);
            }

            pisoAnterior = faixa.getFim();
            if (consumo <= faixa.getFim()) {
                break;
            }
        }

        valorTotal = valorTotal.setScale(2, RoundingMode.HALF_UP);
        return new CalculoResponse(categoria, consumo, valorTotal, detalhamento);
    }

    private List<FaixaConsumo> obterFaixasVigentes(Categoria categoria) {
        List<FaixaConsumo> vigentes = faixaRepository.findVigentesPorCategoria(categoria);
        if (vigentes.isEmpty()) {
            throw new RegraNegocioException(
                    "Nao ha tabela tarifaria ativa com faixas para a categoria " + categoria);
        }
        // Mantem apenas as faixas da tabela vigente mais recente (primeira da lista).
        Long idTabela = vigentes.get(0).getTabela().getId();
        return vigentes.stream()
                .filter(f -> idTabela.equals(f.getTabela().getId()))
                .sorted(Comparator.comparingInt(FaixaConsumo::getInicio))
                .toList();
    }
}
