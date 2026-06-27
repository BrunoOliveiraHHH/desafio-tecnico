package com.sinapsis.tarifaagua.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Faixa progressiva de consumo, vinculada a uma {@link TabelaTarifaria} e a uma
 * {@link Categoria}. O valor unitario (R$/m3) e armazenado como BigDecimal.
 * Como tudo vive no banco, ajustar valores/faixas reflete automaticamente nos
 * calculos sem alteracao de codigo.
 */
@Entity
@Table(name = "faixa_consumo")
public class FaixaConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tabela_id", nullable = false)
    private TabelaTarifaria tabela;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    @Column(nullable = false)
    private Integer inicio;

    @Column(nullable = false)
    private Integer fim;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TabelaTarifaria getTabela() {
        return tabela;
    }

    public void setTabela(TabelaTarifaria tabela) {
        this.tabela = tabela;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Integer getInicio() {
        return inicio;
    }

    public void setInicio(Integer inicio) {
        this.inicio = inicio;
    }

    public Integer getFim() {
        return fim;
    }

    public void setFim(Integer fim) {
        this.fim = fim;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }
}
