package com.portfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record GunsonuDurumDto(
        LocalDate tarih,
        boolean tamamlandi,
        OffsetDateTime tamamlanmaZamani,
        BigDecimal toplamDeger,
        List<String> eksikFiyatlar
) {}
