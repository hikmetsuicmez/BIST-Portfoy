package com.portfolio.service;

import com.portfolio.dto.request.IslemFiltre;
import com.portfolio.dto.request.IslemRequest;
import com.portfolio.dto.response.IslemDto;

import java.util.List;

public interface IslemService {
    IslemDto islemEkle(IslemRequest request);
    IslemDto islemGetir(Long id);
    IslemDto islemGuncelle(Long id, IslemRequest request);
    void islemSil(Long id);
    List<IslemDto> islemleriGetir(IslemFiltre filtre);
}
