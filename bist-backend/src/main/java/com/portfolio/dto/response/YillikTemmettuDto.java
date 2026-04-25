package com.portfolio.dto.response;

import java.math.BigDecimal;

public record YillikTemmettuDto(
        Integer yil,
        BigDecimal toplamNet,
        BigDecimal toplamBrut,
        Integer islemSayisi
) {}
