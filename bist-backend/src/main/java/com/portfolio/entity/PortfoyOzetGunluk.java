package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "portfoy_ozet_gunluk")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfoyOzetGunluk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate tarih;

    @Column(name = "toplam_maliyet", precision = 16, scale = 4)
    private BigDecimal toplamMaliyet;

    @Column(name = "toplam_deger", precision = 16, scale = 4)
    private BigDecimal toplamDeger;

    @Column(name = "toplam_kar_zarar_tl", precision = 16, scale = 4)
    private BigDecimal toplamKarZararTl;

    @Column(name = "toplam_kar_zarar_yuzde", precision = 8, scale = 4)
    private BigDecimal toplamKarZararYuzde;

    @Column(name = "gunluk_degisim_tl", precision = 16, scale = 4)
    private BigDecimal gunlukDegisimTl;

    @Column(name = "gunluk_degisim_yuzde", precision = 8, scale = 4)
    private BigDecimal gunlukDegisimYuzde;

    @Column(name = "pozisyon_sayisi")
    private Integer pozisyonSayisi;

    @Column(name = "gunsonu_tamamlandi")
    @Builder.Default
    private Boolean gunsonuTamamlandi = false;

    @Column(name = "tamamlanma_zamani")
    private OffsetDateTime tamamlanmaZamani;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
