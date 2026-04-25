package com.portfolio.security;

import com.portfolio.entity.Kullanici;
import com.portfolio.exception.BusinessException;
import com.portfolio.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final KullaniciRepository kullaniciRepository;

    public Kullanici getCurrentKullanici() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı"));
    }

    public Long getCurrentKullaniciId() {
        return getCurrentKullanici().getId();
    }
}
