package com.sinapsis.tarifaagua.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabela tarifária completa. Agrupa as faixas de consumo de todas as categorias.
 *
 * <p>A exclusão é lógica (<em>soft delete</em>) através do campo {@code ativo},
 * de modo que tabelas excluídas não sejam usadas em cálculos futuros, preservando
 * o histórico.</p>
 */
@Entity
@Table(name = "tabela_tarifaria")
@Getter
@Setter
public class TabelaTarifaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "data_vigencia", nullable = false)
    private LocalDate dataVigencia;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @OneToMany(mappedBy = "tabela", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FaixaConsumo> faixas = new ArrayList<>();

    /**
     * Adiciona uma faixa à tabela, mantendo o relacionamento bidirecional.
     *
     * @param faixa faixa a ser vinculada a esta tabela
     */
    public void adicionarFaixa(FaixaConsumo faixa) {
        faixa.setTabela(this);
        this.faixas.add(faixa);
    }
}
