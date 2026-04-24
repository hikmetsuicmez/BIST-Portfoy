package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "kapanis_fiyatlari")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KapanisFiyat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hisse_id", nullable = false)
    private Hisse hisse;

    @Column(nullable = false)
    private LocalDate tarih;

    @Column(name = "kapanis_fiyat", nullable = false, precision = 12, scale = 4)
    private BigDecimal kapanisFiyat;

    @Column(name = "acilis_fiyat", precision = 12, scale = 4)
    private BigDecimal acilisFiyat;

    @Column(name = "yuksek_fiyat", precision = 12, scale = 4)
    private BigDecimal yuksekFiyat;

    @Column(name = "dusuk_fiyat", precision = 12, scale = 4)
    private BigDecimal dusukFiyat;

    @Column
    private Long hacim;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
