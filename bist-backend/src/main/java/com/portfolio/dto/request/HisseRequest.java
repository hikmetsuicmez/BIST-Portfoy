package com.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HisseRequest(
        @NotBlank @Size(max = 10) String sembol,
        @NotBlank @Size(max = 200) String sirketAdi,
        String sektor,
        String piyasa
) {}
