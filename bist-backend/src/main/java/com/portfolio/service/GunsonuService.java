package com.portfolio.service;

import com.portfolio.dto.response.GunsonuDurumDto;
import com.portfolio.dto.response.GunsonuSonucDto;
import com.portfolio.dto.response.PortfoyOzetGunlukDto;

import java.time.LocalDate;
import java.util.List;

public interface GunsonuService {
    GunsonuSonucDto gunsonuCalistir(LocalDate tarih);
    GunsonuDurumDto gunsonuDurumGetir(LocalDate tarih);
    List<PortfoyOzetGunlukDto> gecmisGunsonulariniGetir();
    List<String> eksikFiyatlariGetir(LocalDate tarih);
}
