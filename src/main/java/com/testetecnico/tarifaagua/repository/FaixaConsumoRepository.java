package com.testetecnico.tarifaagua.repository;

import com.testetecnico.tarifaagua.domain.Categoria;
import com.testetecnico.tarifaagua.domain.FaixaConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositório de {@link FaixaConsumo}.
 */
public interface FaixaConsumoRepository extends JpaRepository<FaixaConsumo, Long> {

    /**
     * Retorna as faixas da categoria informada pertencentes a tabelas <strong>ativas</strong>,
     * priorizando a tabela vigente mais recente (maior data de vigência / id). O
     * serviço de cálculo utiliza as faixas da primeira tabela retornada.
     *
     * @param categoria categoria do consumidor
     * @return faixas vigentes da categoria, ordenadas por vigência (desc) e início (asc)
     */
    @Query("""
            select f from FaixaConsumo f
            join fetch f.tabela t
            where f.categoria = :categoria and t.ativo = true
            order by t.dataVigencia desc, t.id desc, f.inicio asc
            """)
    List<FaixaConsumo> findVigentesPorCategoria(@Param("categoria") Categoria categoria);
}
