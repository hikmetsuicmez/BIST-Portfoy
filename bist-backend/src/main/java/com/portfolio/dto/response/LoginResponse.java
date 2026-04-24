package com.portfolio.dto.response;

public record LoginResponse(
        String token,
        String tokenType,
        KullaniciDto kullanici
) {
    public static LoginResponse of(String token, KullaniciDto kullanici) {
        return new LoginResponse(token, "Bearer", kullanici);
    }
}
