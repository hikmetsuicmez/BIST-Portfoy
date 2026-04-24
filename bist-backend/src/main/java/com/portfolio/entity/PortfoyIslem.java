package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "portfoy_islemleri")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfoyIslem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hisse_id", nullable = false)
    private Hisse hisse;

    @Enumerated(EnumType.STRING)
    @Column(name = "islem_turu", nullable = false, columnDefinition = "islem_turu")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private IslemTuru islemTuru;

    @Column(nullable = false)
    private LocalDate tarih;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal lot;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal fiyat;

    @Column(precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal komisyon = BigDecimal.ZERO;

    @Column(name = "toplam_tutar", nullable = false, precision = 16, scale = 4)
    private BigDecimal toplamTutar;

    @Column
    private String notlar;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
