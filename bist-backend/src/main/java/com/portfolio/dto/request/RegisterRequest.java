package com.portfolio.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Ad zorunludur")
        String ad,

        @NotBlank(message = "Email zorunludur")
        @Email(message = "Geçerli bir email adresi giriniz")
        String email,

        @NotBlank(message = "Şifre zorunludur")
        @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
        String password
) {}
