package com.portfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PortfoyOzetDto(
        LocalDate tarih,
        BigDecimal toplamMaliyet,
        BigDecimal toplamDeger,
        BigDecimal toplamKarZararTl,
        BigDecimal toplamKarZararYuzde,
        BigDecimal gunlukDegisimTl,
        BigDecimal gunlukDegisimYuzde,
        int pozisyonSayisi,
        List<PozisyonDto> pozisyonlar,
        BigDecimal toplamTemmettuGeliri,
        BigDecimal temmettuDahilGetiriYuzde
) {}
