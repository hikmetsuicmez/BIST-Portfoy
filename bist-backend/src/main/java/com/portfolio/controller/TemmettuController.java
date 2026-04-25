package com.portfolio.controller;

import com.portfolio.dto.request.TemmettuRequest;
import com.portfolio.dto.response.ApiResponse;
import com.portfolio.dto.response.TemmettuDto;
import com.portfolio.dto.response.TemmettuOzetDto;
import com.portfolio.service.TemmettuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/temettular")
@RequiredArgsConstructor
public class TemmettuController {

    private final TemmettuService temmettuService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TemmettuDto>>> tumTemmettuler() {
        return ResponseEntity.ok(ApiResponse.success(temmettuService.tumTemmettuler()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TemmettuDto>> temmettuKaydet(
            @Valid @RequestBody TemmettuRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(temmettuService.temmettuKaydet(request)));
    }

    @GetMapping("/ozet")
    public ResponseEntity<ApiResponse<TemmettuOzetDto>> temmettuOzet() {
        return ResponseEntity.ok(ApiResponse.success(temmettuService.temmettuOzet()));
    }

    @GetMapping("/hisse/{sembol}")
    public ResponseEntity<ApiResponse<List<TemmettuDto>>> hisseTemmettulerini(
            @PathVariable String sembol) {
        return ResponseEntity.ok(ApiResponse.success(temmettuService.hisseTemmettulerini(sembol)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TemmettuDto>> temmettuGuncelle(
            @PathVariable Long id,
            @Valid @RequestBody TemmettuRequest request) {
        return ResponseEntity.ok(ApiResponse.success(temmettuService.temmettuGuncelle(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> temmettuSil(@PathVariable Long id) {
        temmettuService.temmettuSil(id);
        return ResponseEntity.noContent().build();
    }
}
