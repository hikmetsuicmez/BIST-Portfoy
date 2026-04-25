package com.portfolio.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TemmettuRequest(
        @NotBlank(message = "Sembol zorunludur")
        String sembol,

        @NotNull(message = "Yıl zorunludur")
        Integer yil,

        @NotNull(message = "Hisse başı brüt temettü zorunludur")
        @DecimalMin(value = "0.0001", message = "Hisse başı brüt temettü 0'dan büyük olmalıdır")
        BigDecimal hisseBasiTBrut,

        BigDecimal stopajOrani,

        @NotNull(message = "Ödeme tarihi zorunludur")
        LocalDate odemeTarihi,

        String notlar
) {}
