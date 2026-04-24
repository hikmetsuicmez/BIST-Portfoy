package com.portfolio.controller;

import com.portfolio.dto.request.KapanisFiyatRequest;
import com.portfolio.dto.response.ApiResponse;
import com.portfolio.dto.response.KapanisFiyatDto;
import com.portfolio.service.KapanisFiyatService;
import com.portfolio.service.YahooFinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/kapanis-fiyatlari")
@RequiredArgsConstructor
public class KapanisFiyatController {

    private final KapanisFiyatService kapanisFiyatService;
    private final YahooFinanceService yahooFinanceService;

    @PostMapping
    public ResponseEntity<ApiResponse<KapanisFiyatDto>> fiyatKaydet(
            @Valid @RequestBody KapanisFiyatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(kapanisFiyatService.tekFiyatKaydet(request)));
    }

    @PostMapping("/toplu")
    public ResponseEntity<ApiResponse<List<KapanisFiyatDto>>> topluFiyatKaydet(
            @Valid @RequestBody List<KapanisFiyatRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(kapanisFiyatService.topluFiyatKaydet(requests)));
    }

    @GetMapping("/{tarih}")
    public ResponseEntity<ApiResponse<List<KapanisFiyatDto>>> tariheGoreFiyatlar(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tarih) {
        return ResponseEntity.ok(ApiResponse.success(kapanisFiyatService.tariheGoreFiyatlariGetir(tarih)));
    }

    @GetMapping("/{sembol}/{tarih}")
    public ResponseEntity<ApiResponse<KapanisFiyatDto>> sembolVeTariheGoreFiyat(
            @PathVariable String sembol,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tarih) {
        return ResponseEntity.ok(ApiResponse.success(
                kapanisFiyatService.sembolVeTariheGoreFiyatGetir(sembol, tarih)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KapanisFiyatDto>> fiyatGuncelle(
            @PathVariable Long id,
            @Valid @RequestBody KapanisFiyatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(kapanisFiyatService.fiyatGuncelle(id, request)));
    }

    @PostMapping("/yahoo-cek")
    public ResponseEntity<ApiResponse<Map<String, Object>>> yahooCek(
            @RequestBody(required = false) Map<String, String> body) {
        LocalDate tarih = Optional.ofNullable(body)
                .map(b -> b.get("tarih"))
                .filter(t -> t != null && !t.isBlank())
                .map(LocalDate::parse)
                .orElse(LocalDate.now());
        int guncellenen = yahooFinanceService.fiyatlariCek(tarih);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("tarih", tarih.toString(), "guncellenenHisseSayisi", guncellenen)));
    }
}
