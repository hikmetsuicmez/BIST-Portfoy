package com.portfolio.service;

import com.portfolio.dto.response.HisseDetayRaporDto;
import com.portfolio.dto.response.IslemGecmisiRaporDto;
import com.portfolio.dto.response.PerformansRaporDto;
import com.portfolio.dto.response.PortfoyOzetRaporDto;

import java.time.LocalDate;

public interface RaporService {
    PortfoyOzetRaporDto portfoyOzetRaporu();
    IslemGecmisiRaporDto islemGecmisiRaporu(LocalDate baslangic, LocalDate bitis);
    PerformansRaporDto performansRaporu(LocalDate baslangic, LocalDate bitis);
    HisseDetayRaporDto hisseDetayRaporu(String sembol);
}
