package com.portfolio.controller;

import com.portfolio.dto.response.*;
import com.portfolio.service.RaporService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/raporlar")
@RequiredArgsConstructor
public class RaporController {

    private final RaporService raporService;

    @GetMapping("/portfoy-ozet")
    public ResponseEntity<ApiResponse<PortfoyOzetRaporDto>> portfoyOzetRaporu() {
        return ResponseEntity.ok(ApiResponse.success(raporService.portfoyOzetRaporu()));
    }

    @GetMapping("/islem-gecmisi")
    public ResponseEntity<ApiResponse<IslemGecmisiRaporDto>> islemGecmisiRaporu(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baslangic,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bitis) {
        return ResponseEntity.ok(ApiResponse.success(raporService.islemGecmisiRaporu(baslangic, bitis)));
    }

    @GetMapping("/performans")
    public ResponseEntity<ApiResponse<PerformansRaporDto>> performansRaporu(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baslangic,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bitis) {
        return ResponseEntity.ok(ApiResponse.success(raporService.performansRaporu(baslangic, bitis)));
    }

    @GetMapping("/hisse/{sembol}")
    public ResponseEntity<ApiResponse<HisseDetayRaporDto>> hisseDetayRaporu(@PathVariable String sembol) {
        return ResponseEntity.ok(ApiResponse.success(raporService.hisseDetayRaporu(sembol)));
    }
}
