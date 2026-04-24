package com.portfolio.dto.response;

import com.portfolio.entity.Hisse;

public record HisseDto(
        Long id,
        String sembol,
        String sirketAdi,
        String sektor,
        String piyasa,
        Boolean aktif
) {
    public static HisseDto from(Hisse hisse) {
        return new HisseDto(
                hisse.getId(),
                hisse.getSembol(),
                hisse.getSirketAdi(),
                hisse.getSektor(),
                hisse.getPiyasa(),
                hisse.getAktif()
        );
    }
}
