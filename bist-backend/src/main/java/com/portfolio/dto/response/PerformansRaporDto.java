package com.portfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PerformansRaporDto(
        LocalDate baslangic,
        LocalDate bitis,
        BigDecimal baslangicDegeri,
        BigDecimal bitisDegeri,
        BigDecimal toplamGetiriTl,
        BigDecimal toplamGetiriYuzde,
        List<PortfoyOzetGunlukDto> gunlukSnapshots
) {}
