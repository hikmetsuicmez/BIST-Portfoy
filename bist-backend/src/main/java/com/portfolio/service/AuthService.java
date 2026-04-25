package com.portfolio.service;

import com.portfolio.dto.request.LoginRequest;
import com.portfolio.dto.request.RegisterRequest;
import com.portfolio.dto.response.KullaniciDto;
import com.portfolio.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse register(RegisterRequest request);
    KullaniciDto getCurrentUser();
}
