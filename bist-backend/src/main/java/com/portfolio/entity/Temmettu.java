package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "temettular",
    uniqueConstraints = @UniqueConstraint(columnNames = {"hisse_id", "kullanici_id", "yil"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Temmettu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hisse_id", nullable = false)
    private Hisse hisse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private Kullanici kullanici;

    @Column(nullable = false)
    private Integer yil;

    @Column(name = "hisse_basi_brut_temmettu", precision = 10, scale = 4)
    private BigDecimal hisseBasiTBrut;

    @Column(name = "stopaj_orani", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal stopajOrani = new BigDecimal("0.10");

    @Column(name = "hisse_basi_net_temmettu", precision = 10, scale = 4)
    private BigDecimal hisseBasiNet;

    @Column(name = "odeme_tarihi", nullable = false)
    private LocalDate odemeTarihi;

    @Column(precision = 12, scale = 4)
    private BigDecimal lot;

    @Column(name = "toplam_brut", precision = 16, scale = 4)
    private BigDecimal toplamBrut;

    @Column(name = "toplam_net", precision = 16, scale = 4)
    private BigDecimal toplamNet;

    @Column
    private String notlar;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
