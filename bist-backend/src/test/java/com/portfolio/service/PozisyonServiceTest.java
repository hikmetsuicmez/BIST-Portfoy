package com.portfolio.service;

import com.portfolio.entity.*;
import com.portfolio.exception.BusinessException;
import com.portfolio.repository.*;
import com.portfolio.security.SecurityUtil;
import com.portfolio.service.impl.PozisyonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PozisyonServiceTest {

    @Mock private PozisyonRepository pozisyonRepository;
    @Mock private HisseRepository hisseRepository;
    @Mock private IslemRepository islemRepository;
    @Mock private KapanisFiyatRepository kapanisFiyatRepository;
    @Mock private SecurityUtil securityUtil;

    @InjectMocks private PozisyonServiceImpl pozisyonService;

    private static final Long KULLANICI_ID = 1L;

    private Hisse thyao;
    private Kullanici kullanici;

    @BeforeEach
    void setup() {
        thyao = Hisse.builder().id(1L).sembol("THYAO").sirketAdi("Türk Hava Yolları").build();
        kullanici = Kullanici.builder().id(KULLANICI_ID).email("test@test.com").ad("Test").build();
    }

    // ---------------------------------------------------------------
    // Alım İşlemi Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Alım: Yeni pozisyon oluşturulmalı — lot ve maliyet doğru kaydedilmeli")
    void alimYeniPozisyon_dogruHesap() {
        PortfoyIslem alim = PortfoyIslem.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .islemTuru(IslemTuru.ALIM)
                .lot(new BigDecimal("100"))
                .fiyat(new BigDecimal("250"))
                .komisyon(new BigDecimal("12.50"))
                .toplamTutar(new BigDecimal("25012.50"))
                .tarih(LocalDate.of(2024, 1, 10))
                .build();

        when(pozisyonRepository.findByHisseIdAndKullaniciId(1L, KULLANICI_ID)).thenReturn(Optional.empty());
        when(pozisyonRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        pozisyonService.pozisyonGuncelle(thyao, alim);

        ArgumentCaptor<PortfoyPozisyon> captor = ArgumentCaptor.forClass(PortfoyPozisyon.class);
        verify(pozisyonRepository).save(captor.capture());

        PortfoyPozisyon pozisyon = captor.getValue();
        assertThat(pozisyon.getToplamLot()).isEqualByComparingTo("100");
        // toplamMaliyet = 0 + (100 * 250) + 12.50 = 25012.50
        assertThat(pozisyon.getToplamMaliyet()).isEqualByComparingTo("25012.50");
        // ortalamaMaliyet = 25012.50 / 100 = 250.1250
        assertThat(pozisyon.getOrtalamaMaliyet()).isEqualByComparingTo("250.1250");
        // İlk alım tarihi setlenmiş olmalı
        assertThat(pozisyon.getIlkAlimTarihi()).isEqualTo(LocalDate.of(2024, 1, 10));
    }

    @Test
    @DisplayName("Alım: Mevcut pozisyona ağırlıklı ortalama maliyet doğru güncellenmeli")
    void alimMevcutPozisyon_agirlikliOrtalamaDoğru() {
        PortfoyPozisyon mevcut = PortfoyPozisyon.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .toplamLot(new BigDecimal("100"))
                .ortalamaMaliyet(new BigDecimal("250"))
                .toplamMaliyet(new BigDecimal("25000"))
                .ilkAlimTarihi(LocalDate.of(2024, 1, 5))
                .build();

        PortfoyIslem yeniAlim = PortfoyIslem.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .islemTuru(IslemTuru.ALIM)
                .lot(new BigDecimal("100"))
                .fiyat(new BigDecimal("300"))
                .komisyon(BigDecimal.ZERO)
                .toplamTutar(new BigDecimal("30000"))
                .tarih(LocalDate.of(2024, 1, 10))
                .build();

        when(pozisyonRepository.findByHisseIdAndKullaniciId(1L, KULLANICI_ID)).thenReturn(Optional.of(mevcut));
        when(pozisyonRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        pozisyonService.pozisyonGuncelle(thyao, yeniAlim);

        ArgumentCaptor<PortfoyPozisyon> captor = ArgumentCaptor.forClass(PortfoyPozisyon.class);
        verify(pozisyonRepository).save(captor.capture());

        PortfoyPozisyon pozisyon = captor.getValue();
        assertThat(pozisyon.getToplamLot()).isEqualByComparingTo("200");
        // toplamMaliyet = 25000 + (100 * 300) + 0 = 55000
        assertThat(pozisyon.getToplamMaliyet()).isEqualByComparingTo("55000");
        // ortalamaMaliyet = 55000 / 200 = 275.0000
        assertThat(pozisyon.getOrtalamaMaliyet()).isEqualByComparingTo("275.0000");
        // ilkAlimTarihi değişmemeli
        assertThat(pozisyon.getIlkAlimTarihi()).isEqualTo(LocalDate.of(2024, 1, 5));
    }

    @Test
    @DisplayName("Alım: Farklı lot büyüklüklerinde ağırlıklı ortalama doğru hesaplanmalı")
    void alimFarklıLotlar_agirlikliOrtalamaDoğru() {
        // Mevcut: 100 lot @ 250 = 25000
        PortfoyPozisyon mevcut = PortfoyPozisyon.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .toplamLot(new BigDecimal("100"))
                .ortalamaMaliyet(new BigDecimal("250"))
                .toplamMaliyet(new BigDecimal("25000"))
                .build();

        // Yeni alım: 200 lot @ 300 = 60000 → toplam 85000 / 300 = 283.3333
        PortfoyIslem yeniAlim = PortfoyIslem.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .islemTuru(IslemTuru.ALIM)
                .lot(new BigDecimal("200"))
                .fiyat(new BigDecimal("300"))
                .komisyon(BigDecimal.ZERO)
                .toplamTutar(new BigDecimal("60000"))
                .tarih(LocalDate.now())
                .build();

        when(pozisyonRepository.findByHisseIdAndKullaniciId(1L, KULLANICI_ID)).thenReturn(Optional.of(mevcut));
        when(pozisyonRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        pozisyonService.pozisyonGuncelle(thyao, yeniAlim);

        ArgumentCaptor<PortfoyPozisyon> captor = ArgumentCaptor.forClass(PortfoyPozisyon.class);
        verify(pozisyonRepository).save(captor.capture());

        PortfoyPozisyon pozisyon = captor.getValue();
        assertThat(pozisyon.getToplamLot()).isEqualByComparingTo("300");
        assertThat(pozisyon.getToplamMaliyet()).isEqualByComparingTo("85000");
        // 85000 / 300 = 283.3333
        assertThat(pozisyon.getOrtalamaMaliyet()).isEqualByComparingTo("283.3333");
    }

    @Test
    @DisplayName("Alım: Komisyon maliyete eklenmeli — ortalama maliyet yükselmeli")
    void alimKomisyonlu_maliyeteEklenir() {
        PortfoyIslem alim = PortfoyIslem.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .islemTuru(IslemTuru.ALIM)
                .lot(new BigDecimal("100"))
                .fiyat(new BigDecimal("250"))
                .komisyon(new BigDecimal("50"))
                .toplamTutar(new BigDecimal("25050"))
                .tarih(LocalDate.now())
                .build();

        when(pozisyonRepository.findByHisseIdAndKullaniciId(1L, KULLANICI_ID)).thenReturn(Optional.empty());
        when(pozisyonRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        pozisyonService.pozisyonGuncelle(thyao, alim);

        ArgumentCaptor<PortfoyPozisyon> captor = ArgumentCaptor.forClass(PortfoyPozisyon.class);
        verify(pozisyonRepository).save(captor.capture());

        // komisyon dahil: 25050 / 100 = 250.5000
        assertThat(captor.getValue().getOrtalamaMaliyet()).isEqualByComparingTo("250.5000");
    }

    // ---------------------------------------------------------------
    // Satım İşlemi Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Satım: Lot azaltılmalı, toplam maliyet orantılı düşmeli, ortalama değişmemeli")
    void satimIslem_lotAzaltilirMaliyetDuser() {
        PortfoyPozisyon mevcut = PortfoyPozisyon.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .toplamLot(new BigDecimal("200"))
                .ortalamaMaliyet(new BigDecimal("275"))
                .toplamMaliyet(new BigDecimal("55000"))
                .build();

        PortfoyIslem satim = PortfoyIslem.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .islemTuru(IslemTuru.SATIM)
                .lot(new BigDecimal("100"))
                .fiyat(new BigDecimal("320"))
                .komisyon(BigDecimal.ZERO)
                .toplamTutar(new BigDecimal("32000"))
                .tarih(LocalDate.now())
                .build();

        when(pozisyonRepository.findByHisseIdAndKullaniciId(1L, KULLANICI_ID)).thenReturn(Optional.of(mevcut));
        when(pozisyonRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        pozisyonService.pozisyonGuncelle(thyao, satim);

        ArgumentCaptor<PortfoyPozisyon> captor = ArgumentCaptor.forClass(PortfoyPozisyon.class);
        verify(pozisyonRepository).save(captor.capture());

        PortfoyPozisyon pozisyon = captor.getValue();
        assertThat(pozisyon.getToplamLot()).isEqualByComparingTo("100");
        // toplamMaliyet = 100 * 275 = 27500
        assertThat(pozisyon.getToplamMaliyet()).isEqualByComparingTo("27500.0000");
        // Ortalama maliyet satımda değişmemeli
        assertThat(pozisyon.getOrtalamaMaliyet()).isEqualByComparingTo("275");
    }

    @Test
    @DisplayName("Satım: Tüm lot satılırsa toplam maliyet sıfır olmalı")
    void satimTumLot_maliyetSifirOlur() {
        PortfoyPozisyon mevcut = PortfoyPozisyon.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .toplamLot(new BigDecimal("100"))
                .ortalamaMaliyet(new BigDecimal("250"))
                .toplamMaliyet(new BigDecimal("25000"))
                .build();

        PortfoyIslem satim = PortfoyIslem.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .islemTuru(IslemTuru.SATIM)
                .lot(new BigDecimal("100"))
                .fiyat(new BigDecimal("300"))
                .komisyon(BigDecimal.ZERO)
                .toplamTutar(new BigDecimal("30000"))
                .tarih(LocalDate.now())
                .build();

        when(pozisyonRepository.findByHisseIdAndKullaniciId(1L, KULLANICI_ID)).thenReturn(Optional.of(mevcut));
        when(pozisyonRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        pozisyonService.pozisyonGuncelle(thyao, satim);

        ArgumentCaptor<PortfoyPozisyon> captor = ArgumentCaptor.forClass(PortfoyPozisyon.class);
        verify(pozisyonRepository).save(captor.capture());

        assertThat(captor.getValue().getToplamLot()).isEqualByComparingTo("0");
        assertThat(captor.getValue().getToplamMaliyet()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Satım: Yetersiz lot varken satım yapılamaz — BusinessException fırlatılmalı")
    void satimYetersizLot_hataVerir() {
        PortfoyPozisyon mevcut = PortfoyPozisyon.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .toplamLot(new BigDecimal("50"))
                .ortalamaMaliyet(new BigDecimal("275"))
                .toplamMaliyet(new BigDecimal("13750"))
                .build();

        PortfoyIslem satim = PortfoyIslem.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .islemTuru(IslemTuru.SATIM)
                .lot(new BigDecimal("100"))
                .fiyat(new BigDecimal("320"))
                .komisyon(BigDecimal.ZERO)
                .toplamTutar(new BigDecimal("32000"))
                .tarih(LocalDate.now())
                .build();

        when(pozisyonRepository.findByHisseIdAndKullaniciId(1L, KULLANICI_ID)).thenReturn(Optional.of(mevcut));

        assertThatThrownBy(() -> pozisyonService.pozisyonGuncelle(thyao, satim))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Yetersiz lot");

        verify(pozisyonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Satım: Pozisyon hiç yokken satım yapılamaz — BusinessException fırlatılmalı")
    void satimPozisyonYok_hataVerir() {
        PortfoyPozisyon bosEPozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                // toplamLot default = 0
                .build();

        PortfoyIslem satim = PortfoyIslem.builder()
                .hisse(thyao)
                .kullanici(kullanici)
                .islemTuru(IslemTuru.SATIM)
                .lot(new BigDecimal("100"))
                .fiyat(new BigDecimal("300"))
                .komisyon(BigDecimal.ZERO)
                .toplamTutar(new BigDecimal("30000"))
                .tarih(LocalDate.now())
                .build();

        when(pozisyonRepository.findByHisseIdAndKullaniciId(1L, KULLANICI_ID)).thenReturn(Optional.of(bosEPozisyon));

        assertThatThrownBy(() -> pozisyonService.pozisyonGuncelle(thyao, satim))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Yetersiz lot");
    }
}
