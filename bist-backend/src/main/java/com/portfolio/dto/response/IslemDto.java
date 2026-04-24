package com.portfolio.dto.response;

import com.portfolio.entity.IslemTuru;
import com.portfolio.entity.PortfoyIslem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record IslemDto(
        Long id,
        String sembol,
        String sirketAdi,
        IslemTuru islemTuru,
        LocalDate tarih,
        BigDecimal lot,
        BigDecimal fiyat,
        BigDecimal komisyon,
        BigDecimal toplamTutar,
        String notlar,
        OffsetDateTime createdAt
) {
    public static IslemDto from(PortfoyIslem islem) {
        return new IslemDto(
                islem.getId(),
                islem.getHisse().getSembol(),
                islem.getHisse().getSirketAdi(),
                islem.getIslemTuru(),
                islem.getTarih(),
                islem.getLot(),
                islem.getFiyat(),
                islem.getKomisyon(),
                islem.getToplamTutar(),
                islem.getNotlar(),
                islem.getCreatedAt()
        );
    }
}
