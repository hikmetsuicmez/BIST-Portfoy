package com.portfolio.service;

import com.portfolio.dto.request.KapanisFiyatRequest;
import com.portfolio.dto.response.KapanisFiyatDto;

import java.time.LocalDate;
import java.util.List;

public interface KapanisFiyatService {
    KapanisFiyatDto tekFiyatKaydet(KapanisFiyatRequest request);
    List<KapanisFiyatDto> topluFiyatKaydet(List<KapanisFiyatRequest> requests);
    List<KapanisFiyatDto> tariheGoreFiyatlariGetir(LocalDate tarih);
    KapanisFiyatDto fiyatGuncelle(Long id, KapanisFiyatRequest request);
    KapanisFiyatDto sembolVeTariheGoreFiyatGetir(String sembol, LocalDate tarih);
}
