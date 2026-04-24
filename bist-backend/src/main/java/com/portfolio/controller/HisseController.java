package com.portfolio.controller;

import com.portfolio.dto.request.HisseRequest;
import com.portfolio.dto.response.ApiResponse;
import com.portfolio.dto.response.HisseDto;
import com.portfolio.dto.response.KapanisFiyatDto;
import com.portfolio.service.HisseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hisseler")
@RequiredArgsConstructor
public class HisseController {

    private final HisseService hisseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HisseDto>>> tumHisseler() {
        return ResponseEntity.ok(ApiResponse.success(hisseService.tumHisseleriGetir()));
    }

    @GetMapping("/{sembol}")
    public ResponseEntity<ApiResponse<HisseDto>> hisseGetir(@PathVariable String sembol) {
        return ResponseEntity.ok(ApiResponse.success(hisseService.sembolIleGetir(sembol)));
    }

    @GetMapping("/{sembol}/fiyatlar")
    public ResponseEntity<ApiResponse<List<KapanisFiyatDto>>> fiyatGecmisi(
            @PathVariable String sembol,
            @RequestParam(defaultValue = "30") int gun) {
        return ResponseEntity.ok(ApiResponse.success(hisseService.fiyatGecmisiniGetir(sembol, gun)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HisseDto>> hisseEkle(@Valid @RequestBody HisseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hisse eklendi", hisseService.yeniHisseEkle(request)));
    }
}
