package com.portfolio.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record TemmettuOzetDto(
        BigDecimal toplamNetTumZamanlar,
        BigDecimal toplamNetBuYil,
        Integer temmettuSayisi,
        List<YillikTemmettuDto> yillikOzet,
        List<HisseTemmettuDto> hisseOzet
) {}
