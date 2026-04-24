package com.portfolio.dto.request;

import com.portfolio.entity.IslemTuru;

import java.time.LocalDate;

public record IslemFiltre(
        String sembol,
        LocalDate baslangic,
        LocalDate bitis,
        IslemTuru tur
) {}
