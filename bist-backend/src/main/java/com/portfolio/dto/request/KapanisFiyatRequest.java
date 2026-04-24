package com.portfolio.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record KapanisFiyatRequest(
        @NotBlank String sembol,
        @NotNull LocalDate tarih,
        @NotNull @DecimalMin("0.0001") BigDecimal kapanisFiyat,
        BigDecimal acilisFiyat,
        BigDecimal yuksekFiyat,
        BigDecimal dusukFiyat,
        Long hacim
) {}
