package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "portfoy_pozisyonlari",
    uniqueConstraints = @UniqueConstraint(columnNames = {"hisse_id", "kullanici_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfoyPozisyon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private Kullanici kullanici;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hisse_id", nullable = false)
    private Hisse hisse;

    @Column(name = "toplam_lot", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal toplamLot = BigDecimal.ZERO;

    @Column(name = "ortalama_maliyet", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal ortalamaMaliyet = BigDecimal.ZERO;

    @Column(name = "toplam_maliyet", precision = 16, scale = 4)
    @Builder.Default
    private BigDecimal toplamMaliyet = BigDecimal.ZERO;

    @Column(name = "ilk_alim_tarihi")
    private LocalDate ilkAlimTarihi;

    @Column(name = "son_islem_tarihi")
    private LocalDate sonIslemTarihi;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
