package com.portfolio.service.impl;

import com.portfolio.dto.response.HissePozisyonDetayDto;
import com.portfolio.dto.response.IslemDto;
import com.portfolio.dto.response.KapanisFiyatDto;
import com.portfolio.dto.response.PozisyonDto;
import com.portfolio.entity.*;
import com.portfolio.exception.BusinessException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.*;
import com.portfolio.service.PozisyonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PozisyonServiceImpl implements PozisyonService {

    private final PozisyonRepository pozisyonRepository;
    private final HisseRepository hisseRepository;
    private final IslemRepository islemRepository;
    private final KapanisFiyatRepository kapanisFiyatRepository;

    @Override
    @Transactional
    public void pozisyonGuncelle(Hisse hisse, PortfoyIslem islem) {
        PortfoyPozisyon pozisyon = pozisyonRepository.findByHisseId(hisse.getId())
                .orElseGet(() -> PortfoyPozisyon.builder().hisse(hisse).build());

        if (IslemTuru.ALIM.equals(islem.getIslemTuru())) {
            islemleAlimiIsle(pozisyon, islem);
        } else {
            islemleSatimIsle(pozisyon, islem);
        }

        pozisyon.setSonIslemTarihi(islem.getTarih());
        pozisyonRepository.save(pozisyon);
        log.debug("Pozisyon güncellendi: {} -> lot={}", hisse.getSembol(), pozisyon.getToplamLot());
    }

    @Override
    @Transactional
    public void pozisyonuYenidenHesapla(Hisse hisse) {
        List<PortfoyIslem> islemler = islemRepository.findByHisseIdOrderByTarihDesc(hisse.getId());
        // Tarihe göre artan sırala (en eski önce)
        List<PortfoyIslem> siraliIslemler = islemler.stream()
                .sorted((a, b) -> a.getTarih().compareTo(b.getTarih()))
                .toList();

        PortfoyPozisyon pozisyon = pozisyonRepository.findByHisseId(hisse.getId())
                .orElseGet(() -> PortfoyPozisyon.builder().hisse(hisse).build());

        pozisyon.setToplamLot(BigDecimal.ZERO);
        pozisyon.setOrtalamaMaliyet(BigDecimal.ZERO);
        pozisyon.setToplamMaliyet(BigDecimal.ZERO);
        pozisyon.setIlkAlimTarihi(null);
        pozisyon.setSonIslemTarihi(null);

        for (PortfoyIslem islem : siraliIslemler) {
            if (IslemTuru.ALIM.equals(islem.getIslemTuru())) {
                islemleAlimiIsle(pozisyon, islem);
            } else {
                islemleSatimIsle(pozisyon, islem);
            }
            pozisyon.setSonIslemTarihi(islem.getTarih());
        }

        pozisyonRepository.save(pozisyon);
    }

    @Override
    public List<PozisyonDto> tumPozisyonlariGetir() {
        return pozisyonRepository.findByToplamLotGreaterThan(BigDecimal.ZERO)
                .stream()
                .map(this::toPozisyonDto)
                .toList();
    }

    @Override
    public PozisyonDto hissePozisyonuGetir(String sembol) {
        Hisse hisse = hisseRepository.findBySembol(sembol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + sembol));

        PortfoyPozisyon pozisyon = pozisyonRepository.findByHisseId(hisse.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pozisyon bulunamadı: " + sembol));

        return toPozisyonDto(pozisyon);
    }

    @Override
    public HissePozisyonDetayDto hissePozisyonDetayiGetir(String sembol) {
        Hisse hisse = hisseRepository.findBySembol(sembol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + sembol));

        PortfoyPozisyon pozisyon = pozisyonRepository.findByHisseId(hisse.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pozisyon bulunamadı: " + sembol));

        BigDecimal sonFiyat = sonKapanisFiyatGetir(hisse.getId());
        BigDecimal guncelDeger = sonFiyat != null
                ? pozisyon.getToplamLot().multiply(sonFiyat)
                : BigDecimal.ZERO;
        BigDecimal karZararTl = guncelDeger.subtract(pozisyon.getToplamMaliyet());
        BigDecimal karZararYuzde = hesaplaYuzde(karZararTl, pozisyon.getToplamMaliyet());

        List<IslemDto> islemler = islemRepository.findByHisseIdOrderByTarihDesc(hisse.getId())
                .stream().map(IslemDto::from).toList();

        List<KapanisFiyatDto> fiyatlar = kapanisFiyatRepository
                .findByHisseIdOrderByTarihDesc(hisse.getId(), PageRequest.of(0, 30))
                .stream().map(KapanisFiyatDto::from).toList();

        return new HissePozisyonDetayDto(
                hisse.getSembol(), hisse.getSirketAdi(), hisse.getSektor(),
                pozisyon.getToplamLot(), pozisyon.getOrtalamaMaliyet(), pozisyon.getToplamMaliyet(),
                sonFiyat, guncelDeger, karZararTl, karZararYuzde,
                pozisyon.getIlkAlimTarihi(), islemler, fiyatlar
        );
    }

    private void islemleAlimiIsle(PortfoyPozisyon pozisyon, PortfoyIslem islem) {
        BigDecimal yeniToplamMaliyet = pozisyon.getToplamMaliyet()
                .add(islem.getLot().multiply(islem.getFiyat()))
                .add(islem.getKomisyon());
        BigDecimal yeniToplamLot = pozisyon.getToplamLot().add(islem.getLot());
        BigDecimal yeniOrtalama = yeniToplamLot.compareTo(BigDecimal.ZERO) > 0
                ? yeniToplamMaliyet.divide(yeniToplamLot, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        pozisyon.setToplamLot(yeniToplamLot);
        pozisyon.setToplamMaliyet(yeniToplamMaliyet);
        pozisyon.setOrtalamaMaliyet(yeniOrtalama);

        if (pozisyon.getIlkAlimTarihi() == null) {
            pozisyon.setIlkAlimTarihi(islem.getTarih());
        }
    }

    private void islemleSatimIsle(PortfoyPozisyon pozisyon, PortfoyIslem islem) {
        if (pozisyon.getToplamLot().compareTo(islem.getLot()) < 0) {
            throw new BusinessException("Yetersiz lot: mevcut=" + pozisyon.getToplamLot()
                    + ", satılmak istenen=" + islem.getLot());
        }
        BigDecimal yeniToplamLot = pozisyon.getToplamLot().subtract(islem.getLot());
        // Ortalama maliyet değişmez, toplam maliyet orantılı düşer
        BigDecimal yeniToplamMaliyet = yeniToplamLot.compareTo(BigDecimal.ZERO) > 0
                ? yeniToplamLot.multiply(pozisyon.getOrtalamaMaliyet())
                : BigDecimal.ZERO;

        pozisyon.setToplamLot(yeniToplamLot);
        pozisyon.setToplamMaliyet(yeniToplamMaliyet);
    }

    private PozisyonDto toPozisyonDto(PortfoyPozisyon pozisyon) {
        BigDecimal sonFiyat = sonKapanisFiyatGetir(pozisyon.getHisse().getId());
        BigDecimal guncelDeger = sonFiyat != null
                ? pozisyon.getToplamLot().multiply(sonFiyat)
                : BigDecimal.ZERO;
        BigDecimal karZararTl = guncelDeger.subtract(pozisyon.getToplamMaliyet());
        BigDecimal karZararYuzde = hesaplaYuzde(karZararTl, pozisyon.getToplamMaliyet());

        return new PozisyonDto(
                pozisyon.getHisse().getSembol(),
                pozisyon.getHisse().getSirketAdi(),
                pozisyon.getHisse().getSektor(),
                pozisyon.getToplamLot(),
                pozisyon.getOrtalamaMaliyet(),
                pozisyon.getToplamMaliyet(),
                sonFiyat,
                guncelDeger,
                karZararTl,
                karZararYuzde,
                pozisyon.getIlkAlimTarihi()
        );
    }

    private BigDecimal sonKapanisFiyatGetir(Long hisseId) {
        return kapanisFiyatRepository
                .findByHisseIdOrderByTarihDesc(hisseId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(kf -> kf.getKapanisFiyat())
                .orElse(null);
    }

    private BigDecimal hesaplaYuzde(BigDecimal pay, BigDecimal payda) {
        if (payda == null || payda.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return pay.divide(payda, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
}
