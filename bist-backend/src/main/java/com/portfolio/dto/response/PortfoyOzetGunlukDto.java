package com.portfolio.dto.response;

import com.portfolio.entity.PortfoyOzetGunluk;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PortfoyOzetGunlukDto(
        LocalDate tarih,
        BigDecimal toplamMaliyet,
        BigDecimal toplamDeger,
        BigDecimal toplamKarZararTl,
        BigDecimal toplamKarZararYuzde,
        BigDecimal gunlukDegisimTl,
        BigDecimal gunlukDegisimYuzde,
        Integer pozisyonSayisi,
        Boolean gunsonuTamamlandi,
        OffsetDateTime tamamlanmaZamani
) {
    public static PortfoyOzetGunlukDto from(PortfoyOzetGunluk ozet) {
        return new PortfoyOzetGunlukDto(
                ozet.getTarih(),
                ozet.getToplamMaliyet(),
                ozet.getToplamDeger(),
                ozet.getToplamKarZararTl(),
                ozet.getToplamKarZararYuzde(),
                ozet.getGunlukDegisimTl(),
                ozet.getGunlukDegisimYuzde(),
                ozet.getPozisyonSayisi(),
                ozet.getGunsonuTamamlandi(),
                ozet.getTamamlanmaZamani()
        );
    }
}
