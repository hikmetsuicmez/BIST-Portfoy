package com.portfolio.dto.response;

import java.math.BigDecimal;

public record HisseTemmettuDto(
        String sembol,
        String sirketAdi,
        BigDecimal toplamNet,
        Integer yil
) {}
