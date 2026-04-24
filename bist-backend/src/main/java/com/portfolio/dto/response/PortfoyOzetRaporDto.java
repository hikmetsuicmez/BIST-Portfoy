package com.portfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PortfoyOzetRaporDto(
        LocalDate olusturmaTarihi,
        BigDecimal toplamMaliyet,
        BigDecimal toplamDeger,
        BigDecimal toplamKarZararTl,
        BigDecimal toplamKarZararYuzde,
        List<PozisyonDto> pozisyonlar
) {}
