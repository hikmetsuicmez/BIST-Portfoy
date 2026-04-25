package com.portfolio.service.impl;

import com.portfolio.dto.response.GunsonuDurumDto;
import com.portfolio.dto.response.GunsonuSonucDto;
import com.portfolio.dto.response.PortfoyOzetGunlukDto;
import com.portfolio.entity.*;
import com.portfolio.exception.BusinessException;
import com.portfolio.repository.*;
import com.portfolio.service.GunsonuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GunsonuServiceImpl implements GunsonuService {

    private final PozisyonRepository pozisyonRepository;
    private final KapanisFiyatRepository kapanisFiyatRepository;
    private final GunlukDegisimRepository gunlukDegisimRepository;
    private final OzetGunlukRepository ozetGunlukRepository;
    private final KullaniciRepository kullaniciRepository;

    @Override
    @Transactional
    public GunsonuSonucDto gunsonuCalistir(LocalDate tarih, Long kullaniciId) {
        dogrulaGunsonuCalisamaz(tarih, kullaniciId);
        dogrulaFiyatlarGirilmis(tarih, kullaniciId);

        Kullanici kullanici = kullaniciRepository.findById(kullaniciId)
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı: " + kullaniciId));

        List<PortfoyPozisyon> pozisyonlar = pozisyonRepository
                .findByKullaniciIdAndToplamLotGreaterThan(kullaniciId, BigDecimal.ZERO);
        if (pozisyonlar.isEmpty()) {
            throw new BusinessException("Aktif pozisyon bulunamadı");
        }

        List<PortfoyGunlukDegisim> degisimler = new ArrayList<>();
        BigDecimal toplamMaliyet = BigDecimal.ZERO;
        BigDecimal toplamDeger = BigDecimal.ZERO;

        for (PortfoyPozisyon pozisyon : pozisyonlar) {
            KapanisFiyat bugunFiyat = kapanisFiyatRepository
                    .findByHisseIdAndTarih(pozisyon.getHisse().getId(), tarih)
                    .orElseThrow(() -> new BusinessException("Fiyat bulunamadı: " + pozisyon.getHisse().getSembol()));

            KapanisFiyat oncekiFiyat = kapanisFiyatRepository
                    .findPreviousByHisseId(pozisyon.getHisse().getId(), tarih, PageRequest.of(0, 1))
                    .stream().findFirst().orElse(null);

            BigDecimal guncelDeger = pozisyon.getToplamLot().multiply(bugunFiyat.getKapanisFiyat());
            BigDecimal toplamKarZararTl = guncelDeger.subtract(pozisyon.getToplamMaliyet());
            BigDecimal toplamKarZararYuzde = pozisyon.getToplamMaliyet().compareTo(BigDecimal.ZERO) > 0
                    ? toplamKarZararTl.divide(pozisyon.getToplamMaliyet(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            BigDecimal gunlukDegisimTl = null;
            BigDecimal gunlukDegisimYuzde = null;
            if (oncekiFiyat != null) {
                BigDecimal fiyatFark = bugunFiyat.getKapanisFiyat().subtract(oncekiFiyat.getKapanisFiyat());
                gunlukDegisimTl = fiyatFark.multiply(pozisyon.getToplamLot());
                gunlukDegisimYuzde = oncekiFiyat.getKapanisFiyat().compareTo(BigDecimal.ZERO) > 0
                        ? fiyatFark.multiply(BigDecimal.valueOf(100)).divide(oncekiFiyat.getKapanisFiyat(), 4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
            }

            PortfoyGunlukDegisim degisim = PortfoyGunlukDegisim.builder()
                    .tarih(tarih)
                    .kullanici(kullanici)
                    .hisse(pozisyon.getHisse())
                    .lot(pozisyon.getToplamLot())
                    .ortalamaMaliyet(pozisyon.getOrtalamaMaliyet())
                    .toplamMaliyet(pozisyon.getToplamMaliyet())
                    .kapanisFiyat(bugunFiyat.getKapanisFiyat())
                    .oncekiKapanisFiyat(oncekiFiyat != null ? oncekiFiyat.getKapanisFiyat() : null)
                    .guncelDeger(guncelDeger)
                    .gunlukDegisimTl(gunlukDegisimTl)
                    .gunlukDegisimYuzde(gunlukDegisimYuzde)
                    .toplamKarZararTl(toplamKarZararTl)
                    .toplamKarZararYuzde(toplamKarZararYuzde)
                    .build();

            degisimler.add(degisim);
            toplamMaliyet = toplamMaliyet.add(pozisyon.getToplamMaliyet());
            toplamDeger = toplamDeger.add(guncelDeger);
        }

        gunlukDegisimRepository.saveAll(degisimler);
        ozetKaydet(tarih, kullanici, toplamMaliyet, toplamDeger, pozisyonlar.size());

        log.debug("Günsonu tamamlandı: tarih={}, kullanici={}, pozisyon={}, toplamDeger={}",
                tarih, kullaniciId, pozisyonlar.size(), toplamDeger);

        BigDecimal toplamKarZararTl = toplamDeger.subtract(toplamMaliyet);
        return GunsonuSonucDto.builder()
                .tarih(tarih)
                .islemSayisi(pozisyonlar.size())
                .toplamDeger(toplamDeger)
                .toplamKarZararTl(toplamKarZararTl)
                .basarili(true)
                .build();
    }

    @Override
    public GunsonuDurumDto gunsonuDurumGetir(LocalDate tarih, Long kullaniciId) {
        List<String> eksikFiyatlar = eksikFiyatlariGetir(tarih, kullaniciId);

        return ozetGunlukRepository.findByTarihAndKullaniciId(tarih, kullaniciId)
                .map(ozet -> new GunsonuDurumDto(
                        tarih,
                        ozet.getGunsonuTamamlandi(),
                        ozet.getTamamlanmaZamani(),
                        ozet.getToplamDeger(),
                        eksikFiyatlar
                ))
                .orElseGet(() -> new GunsonuDurumDto(tarih, false, null, null, eksikFiyatlar));
    }

    @Override
    public List<PortfoyOzetGunlukDto> gecmisGunsonulariniGetir(Long kullaniciId) {
        return ozetGunlukRepository.findTop30ByKullaniciIdOrderByTarihDesc(kullaniciId)
                .stream()
                .map(PortfoyOzetGunlukDto::from)
                .toList();
    }

    @Override
    public List<String> eksikFiyatlariGetir(LocalDate tarih, Long kullaniciId) {
        return pozisyonRepository.findByKullaniciIdAndToplamLotGreaterThan(kullaniciId, BigDecimal.ZERO)
                .stream()
                .filter(p -> kapanisFiyatRepository.findByHisseIdAndTarih(p.getHisse().getId(), tarih).isEmpty())
                .map(p -> p.getHisse().getSembol())
                .toList();
    }

    private void dogrulaGunsonuCalisamaz(LocalDate tarih, Long kullaniciId) {
        if (ozetGunlukRepository.existsByTarihAndKullaniciId(tarih, kullaniciId)) {
            throw new BusinessException("Bu tarih için günsonu zaten tamamlanmış: " + tarih);
        }
    }

    private void dogrulaFiyatlarGirilmis(LocalDate tarih, Long kullaniciId) {
        List<String> eksik = eksikFiyatlariGetir(tarih, kullaniciId);
        if (!eksik.isEmpty()) {
            throw new BusinessException("Fiyat girilmemiş hisseler: " + String.join(", ", eksik));
        }
    }

    private void ozetKaydet(LocalDate tarih, Kullanici kullanici, BigDecimal toplamMaliyet,
                             BigDecimal toplamDeger, int pozisyonSayisi) {
        BigDecimal toplamKarZararTl = toplamDeger.subtract(toplamMaliyet);
        BigDecimal toplamKarZararYuzde = toplamMaliyet.compareTo(BigDecimal.ZERO) > 0
                ? toplamKarZararTl.divide(toplamMaliyet, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        PortfoyOzetGunluk onceki = ozetGunlukRepository
                .findTop30ByKullaniciIdOrderByTarihDesc(kullanici.getId())
                .stream().filter(o -> o.getTarih().isBefore(tarih)).findFirst().orElse(null);

        BigDecimal gunlukDegisimTl = onceki != null ? toplamDeger.subtract(onceki.getToplamDeger()) : null;
        BigDecimal gunlukDegisimYuzde = (onceki != null && onceki.getToplamDeger().compareTo(BigDecimal.ZERO) > 0)
                ? gunlukDegisimTl.divide(onceki.getToplamDeger(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : null;

        PortfoyOzetGunluk ozet = PortfoyOzetGunluk.builder()
                .tarih(tarih)
                .kullanici(kullanici)
                .toplamMaliyet(toplamMaliyet)
                .toplamDeger(toplamDeger)
                .toplamKarZararTl(toplamKarZararTl)
                .toplamKarZararYuzde(toplamKarZararYuzde)
                .gunlukDegisimTl(gunlukDegisimTl)
                .gunlukDegisimYuzde(gunlukDegisimYuzde)
                .pozisyonSayisi(pozisyonSayisi)
                .gunsonuTamamlandi(true)
                .tamamlanmaZamani(OffsetDateTime.now())
                .build();

        ozetGunlukRepository.save(ozet);
    }
}
