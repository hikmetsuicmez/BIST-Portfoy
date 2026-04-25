package com.portfolio.service.impl;

import com.portfolio.dto.request.TemmettuRequest;
import com.portfolio.dto.response.HisseTemmettuDto;
import com.portfolio.dto.response.TemmettuDto;
import com.portfolio.dto.response.TemmettuOzetDto;
import com.portfolio.dto.response.YillikTemmettuDto;
import com.portfolio.entity.Hisse;
import com.portfolio.entity.Kullanici;
import com.portfolio.entity.Temmettu;
import com.portfolio.exception.BusinessException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.HisseRepository;
import com.portfolio.repository.PozisyonRepository;
import com.portfolio.repository.TemmettuRepository;
import com.portfolio.security.SecurityUtil;
import com.portfolio.service.TemmettuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TemmettuServiceImpl implements TemmettuService {

    private static final BigDecimal VARSAYILAN_STOPAJ = new BigDecimal("0.10");

    private final TemmettuRepository temmettuRepository;
    private final HisseRepository hisseRepository;
    private final PozisyonRepository pozisyonRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public TemmettuDto temmettuKaydet(TemmettuRequest request) {
        Hisse hisse = hisseRepository.findBySembol(request.sembol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + request.sembol()));

        Kullanici kullanici = securityUtil.getCurrentKullanici();

        if (temmettuRepository.existsByHisseIdAndKullaniciIdAndYil(hisse.getId(), kullanici.getId(), request.yil())) {
            throw new BusinessException("Bu hisse için " + request.yil() + " yılı temettüsü zaten girilmiş");
        }

        BigDecimal lot = pozisyonRepository.findByHisseIdAndKullaniciId(hisse.getId(), kullanici.getId())
                .map(p -> p.getToplamLot())
                .orElse(BigDecimal.ZERO);

        if (lot.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Temettü kaydı: {} için lot sıfır veya pozisyon yok (kullanici={})", hisse.getSembol(), kullanici.getId());
        }

        BigDecimal stopaj = request.stopajOrani() != null ? request.stopajOrani() : VARSAYILAN_STOPAJ;
        Temmettu temmettu = hesaplaVeOlustur(hisse, kullanici, request, lot, stopaj);

        return TemmettuDto.from(temmettuRepository.save(temmettu));
    }

    @Override
    @Transactional
    public TemmettuDto temmettuGuncelle(Long id, TemmettuRequest request) {
        Temmettu mevcut = temmettuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Temettü bulunamadı: " + id));

        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        if (!mevcut.getKullanici().getId().equals(kullaniciId)) {
            throw new BusinessException("Bu temettüyü güncelleme yetkiniz yok");
        }

        Hisse hisse = hisseRepository.findBySembol(request.sembol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + request.sembol()));

        BigDecimal lot = mevcut.getLot() != null ? mevcut.getLot() : BigDecimal.ZERO;
        BigDecimal stopaj = request.stopajOrani() != null ? request.stopajOrani() : VARSAYILAN_STOPAJ;

        BigDecimal hisseBasiNet = request.hisseBasiTBrut()
                .multiply(BigDecimal.ONE.subtract(stopaj))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal toplamBrut = lot.multiply(request.hisseBasiTBrut()).setScale(4, RoundingMode.HALF_UP);
        BigDecimal toplamNet = lot.multiply(hisseBasiNet).setScale(4, RoundingMode.HALF_UP);

        mevcut.setHisse(hisse);
        mevcut.setYil(request.yil());
        mevcut.setHisseBasiTBrut(request.hisseBasiTBrut());
        mevcut.setStopajOrani(stopaj);
        mevcut.setHisseBasiNet(hisseBasiNet);
        mevcut.setOdemeTarihi(request.odemeTarihi());
        mevcut.setToplamBrut(toplamBrut);
        mevcut.setToplamNet(toplamNet);
        mevcut.setNotlar(request.notlar());

        return TemmettuDto.from(temmettuRepository.save(mevcut));
    }

    @Override
    @Transactional
    public void temmettuSil(Long id) {
        Temmettu temmettu = temmettuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Temettü bulunamadı: " + id));

        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        if (!temmettu.getKullanici().getId().equals(kullaniciId)) {
            throw new BusinessException("Bu temettüyü silme yetkiniz yok");
        }

        temmettuRepository.delete(temmettu);
        log.debug("Temettü silindi: id={}", id);
    }

    @Override
    public List<TemmettuDto> tumTemmettuler() {
        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        return temmettuRepository.findByKullaniciIdOrderByOdemeTarihiDesc(kullaniciId)
                .stream()
                .map(TemmettuDto::from)
                .toList();
    }

    @Override
    public List<TemmettuDto> hisseTemmettulerini(String sembol) {
        Hisse hisse = hisseRepository.findBySembol(sembol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + sembol));

        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        return temmettuRepository.findByKullaniciIdAndHisseId(kullaniciId, hisse.getId())
                .stream()
                .map(TemmettuDto::from)
                .toList();
    }

    @Override
    public TemmettuOzetDto temmettuOzet() {
        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        int buYil = LocalDate.now().getYear();

        BigDecimal toplamNetTumZamanlar = temmettuRepository.toplamNetTemmettu(kullaniciId);
        BigDecimal toplamNetBuYil = temmettuRepository.toplamNetTemmettuByYil(kullaniciId, buYil);

        List<Temmettu> tumTemmettuler = temmettuRepository.findByKullaniciIdOrderByOdemeTarihiDesc(kullaniciId);

        List<YillikTemmettuDto> yillikOzet = tumTemmettuler.stream()
                .collect(Collectors.groupingBy(Temmettu::getYil))
                .entrySet().stream()
                .map(e -> {
                    List<Temmettu> yilTemmettu = e.getValue();
                    BigDecimal net = yilTemmettu.stream()
                            .map(t -> t.getToplamNet() != null ? t.getToplamNet() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal brut = yilTemmettu.stream()
                            .map(t -> t.getToplamBrut() != null ? t.getToplamBrut() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new YillikTemmettuDto(e.getKey(), net, brut, yilTemmettu.size());
                })
                .sorted(Comparator.comparingInt(YillikTemmettuDto::yil).reversed())
                .toList();

        List<HisseTemmettuDto> hisseOzet = tumTemmettuler.stream()
                .collect(Collectors.groupingBy(t -> t.getHisse().getSembol()))
                .entrySet().stream()
                .map(e -> {
                    List<Temmettu> hisseTemmetu = e.getValue();
                    BigDecimal net = hisseTemmetu.stream()
                            .map(t -> t.getToplamNet() != null ? t.getToplamNet() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Temmettu son = hisseTemmetu.get(0);
                    return new HisseTemmettuDto(
                            son.getHisse().getSembol(),
                            son.getHisse().getSirketAdi(),
                            net,
                            son.getYil()
                    );
                })
                .sorted(Comparator.comparing(HisseTemmettuDto::toplamNet).reversed())
                .toList();

        return new TemmettuOzetDto(
                toplamNetTumZamanlar,
                toplamNetBuYil,
                tumTemmettuler.size(),
                yillikOzet,
                hisseOzet
        );
    }

    @Override
    public BigDecimal toplamNet() {
        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        return temmettuRepository.toplamNetTemmettu(kullaniciId);
    }

    private Temmettu hesaplaVeOlustur(Hisse hisse, Kullanici kullanici, TemmettuRequest request,
                                       BigDecimal lot, BigDecimal stopaj) {
        BigDecimal hisseBasiNet = request.hisseBasiTBrut()
                .multiply(BigDecimal.ONE.subtract(stopaj))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal toplamBrut = lot.multiply(request.hisseBasiTBrut()).setScale(4, RoundingMode.HALF_UP);
        BigDecimal toplamNet = lot.multiply(hisseBasiNet).setScale(4, RoundingMode.HALF_UP);

        return Temmettu.builder()
                .hisse(hisse)
                .kullanici(kullanici)
                .yil(request.yil())
                .hisseBasiTBrut(request.hisseBasiTBrut())
                .stopajOrani(stopaj)
                .hisseBasiNet(hisseBasiNet)
                .odemeTarihi(request.odemeTarihi())
                .lot(lot)
                .toplamBrut(toplamBrut)
                .toplamNet(toplamNet)
                .notlar(request.notlar())
                .build();
    }
}
