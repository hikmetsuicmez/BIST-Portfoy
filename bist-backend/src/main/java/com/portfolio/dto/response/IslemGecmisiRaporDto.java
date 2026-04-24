package com.portfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record IslemGecmisiRaporDto(
        LocalDate baslangic,
        LocalDate bitis,
        int toplamIslemSayisi,
        BigDecimal toplamAlimTutari,
        BigDecimal toplamSatimTutari,
        List<IslemDto> islemler
) {}
