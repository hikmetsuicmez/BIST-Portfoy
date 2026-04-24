package com.portfolio.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GunsonuRequest(@NotNull LocalDate tarih) {}
