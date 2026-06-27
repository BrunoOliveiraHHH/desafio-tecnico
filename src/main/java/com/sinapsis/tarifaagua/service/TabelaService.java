package com.sinapsis.tarifaagua.service;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.domain.FaixaConsumo;
import com.sinapsis.tarifaagua.domain.TabelaTarifaria;
import com.sinapsis.tarifaagua.dto.CategoriaFaixasRequest;
import com.sinapsis.tarifaagua.dto.CriarTabelaRequest;
import com.sinapsis.tarifaagua.dto.FaixaRequest;
import com.sinapsis.tarifaagua.dto.TabelaResponse;
import com.sinapsis.tarifaagua.exception.MensagensErro;
import com.sinapsis.tarifaagua.exception.RecursoNaoEncontradoException;
import com.sinapsis.tarifaagua.exception.RegraNegocioException;
import com.sinapsis.tarifaagua.repository.TabelaTarifariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço de gerenciamento das tabelas tarifárias.
 *
 * <p>Responsável por criar tabelas (validando a consistência das faixas),
 * listá-las (com filtro opcional por categoria) e excluí-las logicamente
 * (<em>soft delete</em>), preservando o histórico e impedindo o uso em cálculos
 * futuros.</p>
 */
@Service
@RequiredArgsConstructor
public class TabelaService {

    private final TabelaTarifariaRepository repository;

    /**
     * Cria uma tabela tarifária completa, com todas as categorias e suas faixas.
     *
     * @param req estrutura completa da tabela (nome, vigência e categorias/faixas)
     * @return a tabela persistida, já mapeada para resposta
     * @throws RegraNegocioException se alguma categoria estiver duplicada ou se as
     *                               faixas violarem as regras de consistência
     */
    @Transactional
    public TabelaResponse criar(CriarTabelaRequest req) {
        validar(req);

        TabelaTarifaria tabela = new TabelaTarifaria();
        tabela.setNome(req.nome());
        tabela.setDataVigencia(req.dataVigencia() != null ? req.dataVigencia() : LocalDate.now());
        tabela.setAtivo(true);

        for (CategoriaFaixasRequest cat : req.categorias()) {
            for (FaixaRequest f : cat.faixas()) {
                FaixaConsumo faixa = new FaixaConsumo();
                faixa.setCategoria(cat.categoria());
                faixa.setInicio(f.inicio());
                faixa.setFim(f.fim());
                faixa.setValorUnitario(f.valorUnitario());
                tabela.adicionarFaixa(faixa);
            }
        }

        return toResponse(repository.save(tabela), null);
    }

    /**
     * Lista todas as tabelas tarifárias ativas.
     *
     * @return as tabelas ativas com suas categorias e faixas
     */
    @Transactional(readOnly = true)
    public List<TabelaResponse> listar() {
        return listar(null);
    }

    /**
     * Lista as tabelas tarifárias ativas, opcionalmente filtradas por categoria.
     *
     * @param filtro categoria a filtrar; se {@code null}, retorna todas as
     *               categorias. Tabelas sem a categoria informada são omitidas.
     * @return as tabelas ativas (com as faixas da categoria, quando filtrado)
     */
    @Transactional(readOnly = true)
    public List<TabelaResponse> listar(Categoria filtro) {
        return repository.findByAtivoTrueOrderByDataVigenciaDescIdDesc().stream()
                .map(tabela -> toResponse(tabela, filtro))
                .filter(resposta -> !resposta.categorias().isEmpty())
                .toList();
    }

    /**
     * Exclui logicamente uma tabela tarifária (<em>soft delete</em>): marca-a como
     * inativa, de modo que não seja mais usada em cálculos, preservando o histórico.
     *
     * @param id identificador da tabela
     * @throws RecursoNaoEncontradoException se não existir tabela com o {@code id}
     */
    @Transactional
    public void excluir(Long id) {
        TabelaTarifaria tabela = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MensagensErro.TABELA_NAO_ENCONTRADA, id));
        tabela.setAtivo(false);
        repository.save(tabela);
    }

    // ----- validação de consistência das faixas -----

    private void validar(CriarTabelaRequest req) {
        Set<Categoria> vistas = EnumSet.noneOf(Categoria.class);
        for (CategoriaFaixasRequest cat : req.categorias()) {
            if (!vistas.add(cat.categoria())) {
                throw new RegraNegocioException(MensagensErro.CATEGORIA_DUPLICADA, cat.categoria());
            }
            validarFaixas(cat.categoria(), cat.faixas());
        }
    }

    /**
     * Valida as quatro regras de consistência das faixas de uma categoria:
     * cobertura inicial em 0, ordem válida (início &lt; fim), ausência de
     * sobreposição e ausência de lacunas.
     */
    private void validarFaixas(Categoria categoria, List<FaixaRequest> faixas) {
        List<FaixaRequest> ordenadas = faixas.stream()
                .sorted(Comparator.comparingInt(FaixaRequest::inicio))
                .toList();

        if (ordenadas.get(0).inicio() != 0) {
            throw new RegraNegocioException(MensagensErro.FAIXA_COBERTURA_INICIAL, categoria);
        }

        for (int i = 0; i < ordenadas.size(); i++) {
            FaixaRequest atual = ordenadas.get(i);

            if (atual.inicio() >= atual.fim()) {
                throw new RegraNegocioException(
                        MensagensErro.FAIXA_ORDEM_INVALIDA, categoria, atual.inicio(), atual.fim());
            }

            if (i > 0) {
                FaixaRequest anterior = ordenadas.get(i - 1);
                if (atual.inicio() <= anterior.fim()) {
                    throw new RegraNegocioException(MensagensErro.FAIXA_SOBREPOSICAO,
                            categoria, anterior.inicio(), anterior.fim(), atual.inicio(), atual.fim());
                }
                if (atual.inicio() != anterior.fim() + 1) {
                    throw new RegraNegocioException(
                            MensagensErro.FAIXA_LACUNA, categoria, anterior.fim(), atual.inicio());
                }
            }
        }
    }

    // ----- mapeamento -----

    private TabelaResponse toResponse(TabelaTarifaria tabela, Categoria filtro) {
        Map<Categoria, List<TabelaResponse.FaixaResponse>> porCategoria = tabela.getFaixas().stream()
                .filter(f -> filtro == null || f.getCategoria() == filtro)
                .sorted(Comparator.comparingInt(FaixaConsumo::getInicio))
                .collect(Collectors.groupingBy(
                        FaixaConsumo::getCategoria,
                        () -> new EnumMap<>(Categoria.class),
                        Collectors.mapping(f -> new TabelaResponse.FaixaResponse(
                                f.getId(), f.getInicio(), f.getFim(), f.getValorUnitario()), Collectors.toList())));

        List<TabelaResponse.CategoriaFaixasResponse> categorias = porCategoria.entrySet().stream()
                .map(e -> new TabelaResponse.CategoriaFaixasResponse(e.getKey().name(), e.getValue()))
                .toList();

        return new TabelaResponse(
                tabela.getId(),
                tabela.getNome(),
                tabela.getDataVigencia(),
                tabela.isAtivo(),
                categorias);
    }
}
