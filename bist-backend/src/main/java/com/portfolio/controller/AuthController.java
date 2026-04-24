package com.portfolio.controller;

import com.portfolio.dto.request.LoginRequest;
import com.portfolio.dto.response.ApiResponse;
import com.portfolio.dto.response.KullaniciDto;
import com.portfolio.dto.response.LoginResponse;
import com.portfolio.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<KullaniciDto>> me() {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser()));
    }
}
