package com.sinapsis.tarifaagua.repository;

import com.sinapsis.tarifaagua.domain.TabelaTarifaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório de {@link TabelaTarifaria}.
 */
public interface TabelaTarifariaRepository extends JpaRepository<TabelaTarifaria, Long> {

    /**
     * Retorna as tabelas ativas, da vigência mais recente para a mais antiga.
     *
     * @return tabelas ativas ordenadas por data de vigência e id, ambos decrescentes
     */
    List<TabelaTarifaria> findByAtivoTrueOrderByDataVigenciaDescIdDesc();
}
