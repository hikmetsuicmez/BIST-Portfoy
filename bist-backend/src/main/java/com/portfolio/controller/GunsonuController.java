package com.portfolio.controller;

import com.portfolio.dto.request.GunsonuRequest;
import com.portfolio.dto.response.ApiResponse;
import com.portfolio.dto.response.GunsonuDurumDto;
import com.portfolio.dto.response.GunsonuSonucDto;
import com.portfolio.dto.response.PortfoyOzetGunlukDto;
import com.portfolio.service.GunsonuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/gunsonu")
@RequiredArgsConstructor
public class GunsonuController {

    private final GunsonuService gunsonuService;

    @PostMapping("/calistir")
    public ResponseEntity<ApiResponse<GunsonuSonucDto>> gunsonuCalistir(
            @Valid @RequestBody GunsonuRequest request) {
        return ResponseEntity.ok(ApiResponse.success(gunsonuService.gunsonuCalistir(request.tarih())));
    }

    @GetMapping("/durum/{tarih}")
    public ResponseEntity<ApiResponse<GunsonuDurumDto>> gunsonuDurum(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tarih) {
        return ResponseEntity.ok(ApiResponse.success(gunsonuService.gunsonuDurumGetir(tarih)));
    }

    @GetMapping("/gecmis")
    public ResponseEntity<ApiResponse<List<PortfoyOzetGunlukDto>>> gecmisGunsonular() {
        return ResponseEntity.ok(ApiResponse.success(gunsonuService.gecmisGunsonulariniGetir()));
    }

    @GetMapping("/eksik-fiyatlar/{tarih}")
    public ResponseEntity<ApiResponse<List<String>>> eksikFiyatlar(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tarih) {
        return ResponseEntity.ok(ApiResponse.success(gunsonuService.eksikFiyatlariGetir(tarih)));
    }
}
