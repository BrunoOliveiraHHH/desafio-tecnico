package com.sinapsis.tarifaagua.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabela tarifaria completa. Agrupa as faixas de consumo de todas as
 * categorias. A exclusao e logica (soft delete) atraves do campo {@code ativo},
 * de modo que tabelas excluidas nao sejam usadas em calculos futuros.
 */
@Entity
@Table(name = "tabela_tarifaria")
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

    public void adicionarFaixa(FaixaConsumo faixa) {
        faixa.setTabela(this);
        this.faixas.add(faixa);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataVigencia() {
        return dataVigencia;
    }

    public void setDataVigencia(LocalDate dataVigencia) {
        this.dataVigencia = dataVigencia;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<FaixaConsumo> getFaixas() {
        return faixas;
    }

    public void setFaixas(List<FaixaConsumo> faixas) {
        this.faixas = faixas;
    }
}
