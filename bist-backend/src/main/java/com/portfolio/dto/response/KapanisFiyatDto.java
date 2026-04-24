package com.portfolio.dto.response;

import com.portfolio.entity.KapanisFiyat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record KapanisFiyatDto(
        Long id,
        String sembol,
        LocalDate tarih,
        BigDecimal kapanisFiyat,
        BigDecimal acilisFiyat,
        BigDecimal yuksekFiyat,
        BigDecimal dusukFiyat,
        Long hacim
) {
    public static KapanisFiyatDto from(KapanisFiyat kf) {
        return new KapanisFiyatDto(
                kf.getId(),
                kf.getHisse().getSembol(),
                kf.getTarih(),
                kf.getKapanisFiyat(),
                kf.getAcilisFiyat(),
                kf.getYuksekFiyat(),
                kf.getDusukFiyat(),
                kf.getHacim()
        );
    }
}
