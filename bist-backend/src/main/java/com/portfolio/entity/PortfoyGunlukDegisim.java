package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "portfoy_gunluk_degisim")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfoyGunlukDegisim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate tarih;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private Kullanici kullanici;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hisse_id", nullable = false)
    private Hisse hisse;

    @Column(precision = 12, scale = 4)
    private BigDecimal lot;

    @Column(name = "ortalama_maliyet", precision = 12, scale = 4)
    private BigDecimal ortalamaMaliyet;

    @Column(name = "toplam_maliyet", precision = 16, scale = 4)
    private BigDecimal toplamMaliyet;

    @Column(name = "kapanis_fiyat", precision = 12, scale = 4)
    private BigDecimal kapanisFiyat;

    @Column(name = "onceki_kapanis_fiyat", precision = 12, scale = 4)
    private BigDecimal oncekiKapanisFiyat;

    @Column(name = "guncel_deger", precision = 16, scale = 4)
    private BigDecimal guncelDeger;

    @Column(name = "gunluk_degisim_tl", precision = 16, scale = 4)
    private BigDecimal gunlukDegisimTl;

    @Column(name = "gunluk_degisim_yuzde", precision = 8, scale = 4)
    private BigDecimal gunlukDegisimYuzde;

    @Column(name = "toplam_kar_zarar_tl", precision = 16, scale = 4)
    private BigDecimal toplamKarZararTl;

    @Column(name = "toplam_kar_zarar_yuzde", precision = 8, scale = 4)
    private BigDecimal toplamKarZararYuzde;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
