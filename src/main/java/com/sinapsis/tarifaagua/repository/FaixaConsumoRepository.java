package com.sinapsis.tarifaagua.repository;

import com.sinapsis.tarifaagua.domain.Categoria;
import com.sinapsis.tarifaagua.domain.FaixaConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FaixaConsumoRepository extends JpaRepository<FaixaConsumo, Long> {

    /**
     * Retorna as faixas da categoria informada pertencentes a tabelas ATIVAS,
     * priorizando a tabela vigente mais recente (maior data de vigencia / id).
     * O servico de calculo usa as faixas da primeira tabela retornada.
     */
    @Query("""
            select f from FaixaConsumo f
            join fetch f.tabela t
            where f.categoria = :categoria and t.ativo = true
            order by t.dataVigencia desc, t.id desc, f.inicio asc
            """)
    List<FaixaConsumo> findVigentesPorCategoria(@Param("categoria") Categoria categoria);
}
