package com.portfolio.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record GunsonuSonucDto(
        LocalDate tarih,
        int islemSayisi,
        BigDecimal toplamDeger,
        BigDecimal toplamKarZararTl,
        boolean basarili
) {}
