package com.portfolio.service;

import com.portfolio.dto.request.HisseRequest;
import com.portfolio.dto.response.HisseDto;
import com.portfolio.dto.response.KapanisFiyatDto;

import java.util.List;

public interface HisseService {
    List<HisseDto> tumHisseleriGetir();
    HisseDto sembolIleGetir(String sembol);
    HisseDto yeniHisseEkle(HisseRequest request);
    List<KapanisFiyatDto> fiyatGecmisiniGetir(String sembol, int gunSayisi);
}
