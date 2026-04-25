package com.portfolio.service;

import com.portfolio.dto.request.TemmettuRequest;
import com.portfolio.dto.response.TemmettuDto;
import com.portfolio.dto.response.TemmettuOzetDto;

import java.math.BigDecimal;
import java.util.List;

public interface TemmettuService {
    TemmettuDto temmettuKaydet(TemmettuRequest request);
    TemmettuDto temmettuGuncelle(Long id, TemmettuRequest request);
    void temmettuSil(Long id);
    List<TemmettuDto> tumTemmettuler();
    List<TemmettuDto> hisseTemmettulerini(String sembol);
    TemmettuOzetDto temmettuOzet();
    BigDecimal toplamNet();
}
