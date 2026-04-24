package com.portfolio.service;

import com.portfolio.dto.response.HissePozisyonDetayDto;
import com.portfolio.dto.response.PozisyonDto;
import com.portfolio.entity.Hisse;
import com.portfolio.entity.PortfoyIslem;

import java.util.List;

public interface PozisyonService {
    void pozisyonGuncelle(Hisse hisse, PortfoyIslem islem);
    void pozisyonuYenidenHesapla(Hisse hisse);
    List<PozisyonDto> tumPozisyonlariGetir();
    PozisyonDto hissePozisyonuGetir(String sembol);
    HissePozisyonDetayDto hissePozisyonDetayiGetir(String sembol);
}
