package com.isil.edu.pe.spedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * PA4: tabla pedido_revision. Se declara aquí solo para que Hibernate la cree (igual que pedido);
 * las lecturas y escrituras se hacen con Spring JDBC en PedidoRevisionJdbcRepository.
 * FK hacia pedido (integridad referencial) y UNIQUE en pedido_id (una sola revisión por pedido).
 */
@Entity
@Table(name = "pedido_revision",
    uniqueConstraints = @UniqueConstraint(name = "uk_pedido_revision_pedido", columnNames = "pedido_id"))
public class PedidoRevision {

  public static final int OBSERVACION_MAXIMA = 200;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "pedido_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pedido_revision_pedido"))
  private Pedido pedido;

  @Column(nullable = false, length = OBSERVACION_MAXIMA)
  private String observacion;

  @Column(name = "fecha_revision", nullable = false)
  private LocalDateTime fechaRevision;

  protected PedidoRevision() {
  }

  public Long getId() {
    return id;
  }

  public String getObservacion() {
    return observacion;
  }

  public LocalDateTime getFechaRevision() {
    return fechaRevision;
  }
}
