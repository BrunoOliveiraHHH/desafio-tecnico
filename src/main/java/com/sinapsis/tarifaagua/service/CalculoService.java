package com.sinapsis.tarifaagua.service;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.domain.FaixaConsumo;
import com.sinapsis.tarifaagua.dto.CalculoRequest;
import com.sinapsis.tarifaagua.dto.CalculoResponse;
import com.sinapsis.tarifaagua.exception.MensagensErro;
import com.sinapsis.tarifaagua.exception.RegraNegocioException;
import com.sinapsis.tarifaagua.repository.FaixaConsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Serviço de cálculo do valor a pagar.
 *
 * <p>O cálculo é <strong>progressivo por faixas</strong>: o consumo é distribuído
 * pelas faixas da categoria e, em cada faixa, a quantidade de m³ consumida é
 * multiplicada pelo valor unitário daquela faixa; os subtotais são somados.</p>
 *
 * <p>Os valores vêm exclusivamente do banco de dados, de modo que ajustes nas
 * faixas/valores refletem automaticamente no cálculo, <em>sem alteração de
 * código</em> (parametrização total).</p>
 *
 * <p>Exemplo (Industrial, 18 m³): faixa 0–10 → 10 m³ × R$ 1,00 = R$ 10,00; faixa
 * 11–20 → 8 m³ × R$ 2,00 = R$ 16,00; total = <strong>R$ 26,00</strong>.</p>
 */
@Service
@RequiredArgsConstructor
public class CalculoService {

    /** Casas decimais usadas nos valores monetários. */
    private static final int ESCALA_MONETARIA = 2;

    private final FaixaConsumoRepository faixaRepository;

    /**
     * Calcula o valor a pagar para uma categoria e um consumo, de forma
     * progressiva por faixas, retornando também o detalhamento por faixa.
     *
     * @param req categoria e consumo total (m³)
     * @return o valor total e o detalhamento do cálculo
     * @throws RegraNegocioException se não houver tabela ativa para a categoria
     *                               ou se o consumo exceder a cobertura das faixas
     */
    @Transactional(readOnly = true)
    public CalculoResponse calcular(CalculoRequest req) {
        Categoria categoria = req.categoria();
        int consumo = req.consumo();

        List<FaixaConsumo> faixas = obterFaixasVigentes(categoria);

        int coberturaMaxima = faixas.get(faixas.size() - 1).getFim();
        if (consumo > coberturaMaxima) {
            throw new RegraNegocioException(
                    MensagensErro.CALCULO_CONSUMO_FORA_COBERTURA, consumo, coberturaMaxima);
        }

        List<CalculoResponse.DetalhamentoItem> detalhamento = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;
        int pisoAnterior = 0;

        for (FaixaConsumo faixa : faixas) {
            int teto = Math.min(consumo, faixa.getFim());
            int m3Cobrados = teto - pisoAnterior;

            if (m3Cobrados > 0) {
                BigDecimal subtotal = faixa.getValorUnitario().multiply(BigDecimal.valueOf(m3Cobrados));
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

        valorTotal = valorTotal.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        return new CalculoResponse(categoria, consumo, valorTotal, detalhamento);
    }

    /**
     * Recupera as faixas da tabela ativa vigente (mais recente) para a categoria,
     * ordenadas por início.
     *
     * @throws RegraNegocioException se não houver tabela ativa para a categoria
     */
    private List<FaixaConsumo> obterFaixasVigentes(Categoria categoria) {
        List<FaixaConsumo> vigentes = faixaRepository.findVigentesPorCategoria(categoria);
        if (vigentes.isEmpty()) {
            throw new RegraNegocioException(MensagensErro.CALCULO_SEM_TABELA_ATIVA, categoria);
        }
        Long idTabela = vigentes.get(0).getTabela().getId();
        return vigentes.stream()
                .filter(f -> idTabela.equals(f.getTabela().getId()))
                .sorted(Comparator.comparingInt(FaixaConsumo::getInicio))
                .toList();
    }
}
