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
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Faixa progressiva de consumo, vinculada a uma {@link TabelaTarifaria} e a uma
 * {@link Categoria}.
 *
 * <p>O valor unitário (R$/m³) é armazenado como {@link BigDecimal}. Como tudo
 * vive no banco, ajustar valores/faixas reflete automaticamente nos cálculos,
 * sem alteração de código.</p>
 */
@Entity
@Table(name = "faixa_consumo")
@Getter
@Setter
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
}
