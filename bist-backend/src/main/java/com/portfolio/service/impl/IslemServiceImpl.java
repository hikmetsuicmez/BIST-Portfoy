package com.portfolio.service.impl;

import com.portfolio.dto.request.IslemFiltre;
import com.portfolio.dto.request.IslemRequest;
import com.portfolio.dto.response.IslemDto;
import com.portfolio.entity.Hisse;
import com.portfolio.entity.IslemTuru;
import com.portfolio.entity.Kullanici;
import com.portfolio.entity.PortfoyIslem;
import com.portfolio.entity.PortfoyPozisyon;
import com.portfolio.exception.BusinessException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.HisseRepository;
import com.portfolio.repository.IslemRepository;
import com.portfolio.repository.PozisyonRepository;
import com.portfolio.security.SecurityUtil;
import com.portfolio.service.IslemService;
import com.portfolio.service.PozisyonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IslemServiceImpl implements IslemService {

    private final IslemRepository islemRepository;
    private final HisseRepository hisseRepository;
    private final PozisyonRepository pozisyonRepository;
    private final PozisyonService pozisyonService;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public IslemDto islemEkle(IslemRequest request) {
        Hisse hisse = hisseRepository.findBySembol(request.sembol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + request.sembol()));

        BigDecimal komisyon = request.komisyon() != null ? request.komisyon() : BigDecimal.ZERO;

        if (request.islemTuru() == IslemTuru.SATIM) {
            dogrulaYeterliLot(hisse, request.lot(), securityUtil.getCurrentKullaniciId());
        }

        Kullanici kullanici = securityUtil.getCurrentKullanici();

        BigDecimal toplamTutar = hesaplaToplamTutar(request.islemTuru(), request.lot(), request.fiyat(), komisyon);

        PortfoyIslem islem = PortfoyIslem.builder()
                .kullanici(kullanici)
                .hisse(hisse)
                .islemTuru(request.islemTuru())
                .tarih(request.tarih())
                .lot(request.lot())
                .fiyat(request.fiyat())
                .komisyon(komisyon)
                .toplamTutar(toplamTutar)
                .notlar(request.notlar())
                .build();

        PortfoyIslem kaydedilen = islemRepository.save(islem);
        pozisyonService.pozisyonGuncelle(hisse, kaydedilen);

        log.debug("İşlem eklendi: {} {} {} lot @ {}", hisse.getSembol(), request.islemTuru(), request.lot(), request.fiyat());
        return IslemDto.from(kaydedilen);
    }

    @Override
    public IslemDto islemGetir(Long id) {
        return islemRepository.findById(id)
                .map(IslemDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("İşlem bulunamadı: " + id));
    }

    @Override
    @Transactional
    public IslemDto islemGuncelle(Long id, IslemRequest request) {
        PortfoyIslem mevcutIslem = islemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("İşlem bulunamadı: " + id));

        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        if (!mevcutIslem.getKullanici().getId().equals(kullaniciId)) {
            throw new BusinessException("Bu işlemi güncelleme yetkiniz yok");
        }

        Hisse hisse = hisseRepository.findBySembol(request.sembol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + request.sembol()));

        BigDecimal komisyon = request.komisyon() != null ? request.komisyon() : BigDecimal.ZERO;
        BigDecimal toplamTutar = hesaplaToplamTutar(request.islemTuru(), request.lot(), request.fiyat(), komisyon);

        mevcutIslem.setHisse(hisse);
        mevcutIslem.setIslemTuru(request.islemTuru());
        mevcutIslem.setTarih(request.tarih());
        mevcutIslem.setLot(request.lot());
        mevcutIslem.setFiyat(request.fiyat());
        mevcutIslem.setKomisyon(komisyon);
        mevcutIslem.setToplamTutar(toplamTutar);
        mevcutIslem.setNotlar(request.notlar());

        PortfoyIslem kaydedilen = islemRepository.save(mevcutIslem);
        pozisyonService.pozisyonuYenidenHesapla(hisse);

        return IslemDto.from(kaydedilen);
    }

    @Override
    @Transactional
    public void islemSil(Long id) {
        PortfoyIslem islem = islemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("İşlem bulunamadı: " + id));

        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        if (!islem.getKullanici().getId().equals(kullaniciId)) {
            throw new BusinessException("Bu işlemi silme yetkiniz yok");
        }

        Hisse hisse = islem.getHisse();
        islemRepository.delete(islem);
        pozisyonService.pozisyonuYenidenHesapla(hisse);
        log.debug("İşlem silindi: id={}, hisse={}", id, hisse.getSembol());
    }

    @Override
    public List<IslemDto> islemleriGetir(IslemFiltre filtre) {
        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        Stream<PortfoyIslem> stream;

        if (filtre.baslangic() != null && filtre.bitis() != null) {
            stream = islemRepository.findByKullaniciIdAndTarihAraligi(kullaniciId, filtre.baslangic(), filtre.bitis()).stream();
        } else if (filtre.sembol() != null && !filtre.sembol().isBlank()) {
            Hisse hisse = hisseRepository.findBySembol(filtre.sembol().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + filtre.sembol()));
            stream = islemRepository.findByKullaniciIdAndHisseId(kullaniciId, hisse.getId()).stream();
        } else {
            stream = islemRepository.findByKullaniciId(kullaniciId).stream();
        }

        if (filtre.tur() != null) {
            stream = stream.filter(i -> i.getIslemTuru() == filtre.tur());
        }

        return stream.map(IslemDto::from).toList();
    }

    private void dogrulaYeterliLot(Hisse hisse, BigDecimal satilacakLot, Long kullaniciId) {
        Optional<PortfoyPozisyon> pozisyon = pozisyonRepository.findByHisseIdAndKullaniciId(hisse.getId(), kullaniciId);
        BigDecimal mevcutLot = pozisyon.map(PortfoyPozisyon::getToplamLot).orElse(BigDecimal.ZERO);
        if (mevcutLot.compareTo(satilacakLot) < 0) {
            throw new BusinessException("Yetersiz lot: mevcut=" + mevcutLot + ", satılmak istenen=" + satilacakLot);
        }
    }

    private BigDecimal hesaplaToplamTutar(IslemTuru tur, BigDecimal lot, BigDecimal fiyat, BigDecimal komisyon) {
        BigDecimal islemTutari = lot.multiply(fiyat);
        return tur == IslemTuru.ALIM
                ? islemTutari.add(komisyon)
                : islemTutari.subtract(komisyon);
    }
}
