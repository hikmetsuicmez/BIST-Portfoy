package com.portfolio.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record HisseDetayRaporDto(
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
        List<IslemDto> islemler,
        List<KapanisFiyatDto> fiyatGecmisi
) {}
