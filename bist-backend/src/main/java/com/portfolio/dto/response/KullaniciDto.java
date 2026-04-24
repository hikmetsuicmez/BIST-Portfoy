package com.portfolio.dto.response;

import com.portfolio.entity.Kullanici;

public record KullaniciDto(
        Long id,
        String ad,
        String email
) {
    public static KullaniciDto from(Kullanici kullanici) {
        return new KullaniciDto(kullanici.getId(), kullanici.getAd(), kullanici.getEmail());
    }
}
