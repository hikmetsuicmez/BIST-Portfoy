package com.portfolio.service.impl;

import com.portfolio.dto.response.*;
import com.portfolio.entity.Hisse;
import com.portfolio.entity.IslemTuru;
import com.portfolio.entity.PortfoyIslem;
import com.portfolio.entity.PortfoyPozisyon;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.*;
import com.portfolio.service.PozisyonService;
import com.portfolio.service.RaporService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RaporServiceImpl implements RaporService {

    private final PozisyonRepository pozisyonRepository;
    private final IslemRepository islemRepository;
    private final KapanisFiyatRepository kapanisFiyatRepository;
    private final OzetGunlukRepository ozetGunlukRepository;
    private final HisseRepository hisseRepository;
    private final PozisyonService pozisyonService;

    @Override
    public PortfoyOzetRaporDto portfoyOzetRaporu() {
        List<PozisyonDto> pozisyonlar = pozisyonService.tumPozisyonlariGetir();

        BigDecimal toplamMaliyet = pozisyonlar.stream()
                .map(PozisyonDto::toplamMaliyet)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal toplamDeger = pozisyonlar.stream()
                .map(p -> p.guncelDeger() != null ? p.guncelDeger() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal toplamKarZararTl = toplamDeger.subtract(toplamMaliyet);
        BigDecimal toplamKarZararYuzde = toplamMaliyet.compareTo(BigDecimal.ZERO) > 0
                ? toplamKarZararTl.divide(toplamMaliyet, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new PortfoyOzetRaporDto(
                LocalDate.now(),
                toplamMaliyet,
                toplamDeger,
                toplamKarZararTl,
                toplamKarZararYuzde,
                pozisyonlar
        );
    }

    @Override
    public IslemGecmisiRaporDto islemGecmisiRaporu(LocalDate baslangic, LocalDate bitis) {
        List<PortfoyIslem> islemler = islemRepository.findByTarihAraligi(baslangic, bitis);

        BigDecimal toplamAlim = islemler.stream()
                .filter(i -> i.getIslemTuru() == IslemTuru.ALIM)
                .map(PortfoyIslem::getToplamTutar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal toplamSatim = islemler.stream()
                .filter(i -> i.getIslemTuru() == IslemTuru.SATIM)
                .map(PortfoyIslem::getToplamTutar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new IslemGecmisiRaporDto(
                baslangic,
                bitis,
                islemler.size(),
                toplamAlim,
                toplamSatim,
                islemler.stream().map(IslemDto::from).toList()
        );
    }

    @Override
    public PerformansRaporDto performansRaporu(LocalDate baslangic, LocalDate bitis) {
        List<PortfoyOzetGunlukDto> snapshots = ozetGunlukRepository
                .findByTarihAraligi(baslangic, bitis)
                .stream()
                .map(PortfoyOzetGunlukDto::from)
                .toList();

        BigDecimal baslangicDegeri = snapshots.isEmpty() ? BigDecimal.ZERO : snapshots.get(0).toplamDeger();
        BigDecimal bitisDegeri = snapshots.isEmpty() ? BigDecimal.ZERO : snapshots.get(snapshots.size() - 1).toplamDeger();
        BigDecimal toplamGetiriTl = bitisDegeri.subtract(baslangicDegeri);
        BigDecimal toplamGetiriYuzde = baslangicDegeri.compareTo(BigDecimal.ZERO) > 0
                ? toplamGetiriTl.divide(baslangicDegeri, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new PerformansRaporDto(
                baslangic,
                bitis,
                baslangicDegeri,
                bitisDegeri,
                toplamGetiriTl,
                toplamGetiriYuzde,
                snapshots
        );
    }

    @Override
    public HisseDetayRaporDto hisseDetayRaporu(String sembol) {
        Hisse hisse = hisseRepository.findBySembol(sembol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + sembol));

        PortfoyPozisyon pozisyon = pozisyonRepository.findByHisseId(hisse.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pozisyon bulunamadı: " + sembol));

        List<IslemDto> islemler = islemRepository.findByHisseIdOrderByTarihDesc(hisse.getId())
                .stream().map(IslemDto::from).toList();

        List<KapanisFiyatDto> fiyatlar = kapanisFiyatRepository
                .findByHisseIdOrderByTarihDesc(hisse.getId(), PageRequest.of(0, 60))
                .stream().map(KapanisFiyatDto::from).toList();

        BigDecimal sonFiyat = fiyatlar.isEmpty() ? null : fiyatlar.get(0).kapanisFiyat();
        BigDecimal guncelDeger = sonFiyat != null
                ? pozisyon.getToplamLot().multiply(sonFiyat)
                : BigDecimal.ZERO;
        BigDecimal karZararTl = guncelDeger.subtract(pozisyon.getToplamMaliyet());
        BigDecimal karZararYuzde = pozisyon.getToplamMaliyet().compareTo(BigDecimal.ZERO) > 0
                ? karZararTl.divide(pozisyon.getToplamMaliyet(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new HisseDetayRaporDto(
                hisse.getSembol(), hisse.getSirketAdi(), hisse.getSektor(),
                pozisyon.getToplamLot(), pozisyon.getOrtalamaMaliyet(), pozisyon.getToplamMaliyet(),
                sonFiyat, guncelDeger, karZararTl, karZararYuzde,
                islemler, fiyatlar
        );
    }
}
