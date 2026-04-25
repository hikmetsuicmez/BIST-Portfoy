package com.portfolio.dto.response;

import com.portfolio.entity.Temmettu;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TemmettuDto(
        Long id,
        String hisseSembol,
        String hisseSirketAdi,
        Integer yil,
        BigDecimal hisseBasiTBrut,
        BigDecimal hisseBasiNet,
        BigDecimal stopajOrani,
        LocalDate odemeTarihi,
        BigDecimal lot,
        BigDecimal toplamBrut,
        BigDecimal toplamNet,
        String notlar
) {
    public static TemmettuDto from(Temmettu t) {
        return new TemmettuDto(
                t.getId(),
                t.getHisse().getSembol(),
                t.getHisse().getSirketAdi(),
                t.getYil(),
                t.getHisseBasiTBrut(),
                t.getHisseBasiNet(),
                t.getStopajOrani(),
                t.getOdemeTarihi(),
                t.getLot(),
                t.getToplamBrut(),
                t.getToplamNet(),
                t.getNotlar()
        );
    }
}
