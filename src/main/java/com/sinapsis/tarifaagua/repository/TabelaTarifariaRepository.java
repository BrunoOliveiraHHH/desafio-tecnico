package com.sinapsis.tarifaagua.repository;

import com.sinapsis.tarifaagua.domain.TabelaTarifaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TabelaTarifariaRepository extends JpaRepository<TabelaTarifaria, Long> {

    List<TabelaTarifaria> findByAtivoTrueOrderByDataVigenciaDescIdDesc();
}
