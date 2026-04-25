package com.portfolio.service;

import com.portfolio.dto.response.GunsonuDurumDto;
import com.portfolio.dto.response.GunsonuSonucDto;
import com.portfolio.dto.response.PortfoyOzetGunlukDto;

import java.time.LocalDate;
import java.util.List;

public interface GunsonuService {
    GunsonuSonucDto gunsonuCalistir(LocalDate tarih, Long kullaniciId);
    GunsonuDurumDto gunsonuDurumGetir(LocalDate tarih, Long kullaniciId);
    List<PortfoyOzetGunlukDto> gecmisGunsonulariniGetir(Long kullaniciId);
    List<String> eksikFiyatlariGetir(LocalDate tarih, Long kullaniciId);
}
