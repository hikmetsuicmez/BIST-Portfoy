package com.portfolio.controller;

import com.portfolio.dto.request.IslemFiltre;
import com.portfolio.dto.request.IslemRequest;
import com.portfolio.dto.response.ApiResponse;
import com.portfolio.dto.response.IslemDto;
import com.portfolio.entity.IslemTuru;
import com.portfolio.service.IslemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/islemler")
@RequiredArgsConstructor
public class IslemController {

    private final IslemService islemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IslemDto>>> islemleriGetir(
            @RequestParam(required = false) String sembol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baslangic,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bitis,
            @RequestParam(required = false) IslemTuru tur) {
        IslemFiltre filtre = new IslemFiltre(sembol, baslangic, bitis, tur);
        return ResponseEntity.ok(ApiResponse.success(islemService.islemleriGetir(filtre)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IslemDto>> islemEkle(@Valid @RequestBody IslemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("İşlem eklendi", islemService.islemEkle(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IslemDto>> islemGetir(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(islemService.islemGetir(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IslemDto>> islemGuncelle(
            @PathVariable Long id,
            @Valid @RequestBody IslemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(islemService.islemGuncelle(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> islemSil(@PathVariable Long id) {
        islemService.islemSil(id);
        return ResponseEntity.noContent().build();
    }
}
