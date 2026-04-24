package com.portfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PozisyonDto(
        String sembol,
        String sirketAdi,
        String sektor,
        BigDecimal toplamLot,
        BigDecimal ortalamaMaliyet,
        BigDecimal toplamMaliyet,
        BigDecimal sonKapanisFiyat,
        BigDecimal guncelDeger,
        BigDecimal karZararTl,
        BigDecimal karZararYuzde,
        LocalDate ilkAlimTarihi
) {}
