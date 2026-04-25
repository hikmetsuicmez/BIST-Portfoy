package com.portfolio.controller;

import com.portfolio.dto.response.*;
import com.portfolio.security.SecurityUtil;
import com.portfolio.service.GunsonuService;
import com.portfolio.service.PozisyonService;
import com.portfolio.service.TemmettuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/portfoy")
@RequiredArgsConstructor
public class PortfoyController {

    private final PozisyonService pozisyonService;
    private final GunsonuService gunsonuService;
    private final TemmettuService temmettuService;
    private final SecurityUtil securityUtil;

    @GetMapping("/pozisyonlar")
    public ResponseEntity<ApiResponse<List<PozisyonDto>>> pozisyonlar() {
        return ResponseEntity.ok(ApiResponse.success(pozisyonService.tumPozisyonlariGetir()));
    }

    @GetMapping("/ozet")
    public ResponseEntity<ApiResponse<PortfoyOzetDto>> portfoyOzeti() {
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

        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        List<PortfoyOzetGunlukDto> gecmis = gunsonuService.gecmisGunsonulariniGetir(kullaniciId);
        BigDecimal gunlukDegisimTl = gecmis.isEmpty() ? null : gecmis.get(0).gunlukDegisimTl();
        BigDecimal gunlukDegisimYuzde = gecmis.isEmpty() ? null : gecmis.get(0).gunlukDegisimYuzde();

        BigDecimal toplamTemmettuGeliri = temmettuService.toplamNet();
        BigDecimal temmettuDahilGetiriYuzde = toplamMaliyet.compareTo(BigDecimal.ZERO) > 0
                ? toplamKarZararTl.add(toplamTemmettuGeliri)
                        .divide(toplamMaliyet, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        PortfoyOzetDto ozet = new PortfoyOzetDto(
                LocalDate.now(),
                toplamMaliyet,
                toplamDeger,
                toplamKarZararTl,
                toplamKarZararYuzde,
                gunlukDegisimTl,
                gunlukDegisimYuzde,
                pozisyonlar.size(),
                pozisyonlar,
                toplamTemmettuGeliri,
                temmettuDahilGetiriYuzde
        );

        return ResponseEntity.ok(ApiResponse.success(ozet));
    }

    @GetMapping("/gecmis")
    public ResponseEntity<ApiResponse<List<PortfoyOzetGunlukDto>>> gecmisSnapshots() {
        Long kullaniciId = securityUtil.getCurrentKullaniciId();
        return ResponseEntity.ok(ApiResponse.success(gunsonuService.gecmisGunsonulariniGetir(kullaniciId)));
    }

    @GetMapping("/hisse/{sembol}")
    public ResponseEntity<ApiResponse<HissePozisyonDetayDto>> hissePozisyonDetayi(@PathVariable String sembol) {
        return ResponseEntity.ok(ApiResponse.success(pozisyonService.hissePozisyonDetayiGetir(sembol)));
    }
}
