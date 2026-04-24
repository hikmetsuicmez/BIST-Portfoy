package com.portfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HissePozisyonDetayDto(
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
        LocalDate ilkAlimTarihi,
        List<IslemDto> islemler,
        List<KapanisFiyatDto> fiyatGecmisi
) {}
