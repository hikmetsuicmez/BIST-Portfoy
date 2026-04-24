package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "hisseler")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hisse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String sembol;

    @Column(name = "sirket_adi", nullable = false, length = 200)
    private String sirketAdi;

    @Column(length = 100)
    private String sektor;

    @Column(length = 20)
    @Builder.Default
    private String piyasa = "BIST";

    @Column
    @Builder.Default
    private Boolean aktif = true;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
