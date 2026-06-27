package com.sinapsis.tarifaagua.service;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.domain.FaixaConsumo;
import com.sinapsis.tarifaagua.domain.TabelaTarifaria;
import com.sinapsis.tarifaagua.dto.CategoriaFaixasRequest;
import com.sinapsis.tarifaagua.dto.CriarTabelaRequest;
import com.sinapsis.tarifaagua.dto.FaixaRequest;
import com.sinapsis.tarifaagua.dto.TabelaResponse;
import com.sinapsis.tarifaagua.exception.RecursoNaoEncontradoException;
import com.sinapsis.tarifaagua.exception.RegraNegocioException;
import com.sinapsis.tarifaagua.repository.TabelaTarifariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Regras de gerenciamento das tabelas tarifarias: criacao com validacao de
 * consistencia das faixas, listagem e exclusao logica (soft delete).
 */
@Service
public class TabelaService {

    private final TabelaTarifariaRepository repository;

    public TabelaService(TabelaTarifariaRepository repository) {
        this.repository = repository;
    }

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

    @Transactional(readOnly = true)
    public List<TabelaResponse> listar() {
        return listar(null);
    }

    /**
     * Lista as tabelas ativas. Quando {@code filtro} e informado, retorna apenas
     * as faixas daquela categoria (tabelas sem a categoria sao omitidas).
     */
    @Transactional(readOnly = true)
    public List<TabelaResponse> listar(Categoria filtro) {
        return repository.findByAtivoTrueOrderByDataVigenciaDescIdDesc().stream()
                .map(tabela -> toResponse(tabela, filtro))
                .filter(resposta -> !resposta.categorias().isEmpty())
                .toList();
    }

    /**
     * Exclusao logica: marca a tabela como inativa para que nao seja usada em
     * calculos futuros, preservando o historico.
     */
    @Transactional
    public void excluir(Long id) {
        TabelaTarifaria tabela = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tabela tarifaria nao encontrada: id " + id));
        tabela.setAtivo(false);
        repository.save(tabela);
    }

    // ----- validacao de consistencia das faixas -----

    private void validar(CriarTabelaRequest req) {
        Set<Categoria> vistas = EnumSet.noneOf(Categoria.class);
        for (CategoriaFaixasRequest cat : req.categorias()) {
            if (!vistas.add(cat.categoria())) {
                throw new RegraNegocioException("Categoria duplicada na tabela: " + cat.categoria());
            }
            validarFaixas(cat.categoria(), cat.faixas());
        }
    }

    private void validarFaixas(Categoria categoria, List<FaixaRequest> faixas) {
        List<FaixaRequest> ordenadas = faixas.stream()
                .sorted(Comparator.comparingInt(FaixaRequest::inicio))
                .toList();

        // Cobertura completa: deve iniciar em 0.
        if (ordenadas.get(0).inicio() != 0) {
            throw new RegraNegocioException(
                    "A primeira faixa da categoria " + categoria + " deve iniciar em 0 m3");
        }

        for (int i = 0; i < ordenadas.size(); i++) {
            FaixaRequest atual = ordenadas.get(i);

            // Ordem valida: inicio < fim.
            if (atual.inicio() >= atual.fim()) {
                throw new RegraNegocioException(
                        "Faixa invalida na categoria " + categoria + ": inicio (" + atual.inicio()
                                + ") deve ser menor que fim (" + atual.fim() + ")");
            }

            if (i > 0) {
                FaixaRequest anterior = ordenadas.get(i - 1);
                // Nao sobreposicao.
                if (atual.inicio() <= anterior.fim()) {
                    throw new RegraNegocioException(
                            "Faixas sobrepostas na categoria " + categoria + " entre [" + anterior.inicio()
                                    + "-" + anterior.fim() + "] e [" + atual.inicio() + "-" + atual.fim() + "]");
                }
                // Cobertura sem lacunas: cada faixa inicia logo apos a anterior.
                if (atual.inicio() != anterior.fim() + 1) {
                    throw new RegraNegocioException(
                            "Cobertura incompleta na categoria " + categoria + ": lacuna entre o fim "
                                    + anterior.fim() + " e o inicio " + atual.inicio());
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
                        () -> new java.util.EnumMap<>(Categoria.class),
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
