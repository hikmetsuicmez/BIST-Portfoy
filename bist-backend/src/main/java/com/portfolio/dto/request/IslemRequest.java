package com.portfolio.dto.request;

import com.portfolio.entity.IslemTuru;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IslemRequest(
        @NotBlank String sembol,
        @NotNull IslemTuru islemTuru,
        @NotNull LocalDate tarih,
        @NotNull @DecimalMin("0.0001") BigDecimal lot,
        @NotNull @DecimalMin("0.0001") BigDecimal fiyat,
        @DecimalMin("0") BigDecimal komisyon,
        String notlar
) {}
