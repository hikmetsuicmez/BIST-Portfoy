package com.portfolio.service.impl;

import com.portfolio.dto.request.LoginRequest;
import com.portfolio.dto.response.KullaniciDto;
import com.portfolio.dto.response.LoginResponse;
import com.portfolio.entity.Kullanici;
import com.portfolio.exception.BusinessException;
import com.portfolio.repository.KullaniciRepository;
import com.portfolio.security.JwtUtil;
import com.portfolio.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final KullaniciRepository kullaniciRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Kullanici kullanici = kullaniciRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı"));

        String token = jwtUtil.generateToken(kullanici.getEmail());
        log.debug("Kullanıcı giriş yaptı: {}", kullanici.getEmail());

        return LoginResponse.of(token, KullaniciDto.from(kullanici));
    }

    @Override
    public KullaniciDto getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Kullanici kullanici = kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Oturum kullanıcısı bulunamadı"));
        return KullaniciDto.from(kullanici);
    }
}
