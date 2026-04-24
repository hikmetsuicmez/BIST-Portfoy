package com.portfolio.service;

import com.portfolio.dto.request.IslemFiltre;
import com.portfolio.dto.request.IslemRequest;
import com.portfolio.dto.response.IslemDto;
import com.portfolio.entity.*;
import com.portfolio.exception.BusinessException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.*;
import com.portfolio.service.impl.IslemServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IslemServiceTest {

    @Mock private IslemRepository islemRepository;
    @Mock private HisseRepository hisseRepository;
    @Mock private PozisyonRepository pozisyonRepository;
    @Mock private PozisyonService pozisyonService;

    @InjectMocks private IslemServiceImpl islemService;

    private Hisse thyao;

    @BeforeEach
    void setup() {
        thyao = Hisse.builder().id(1L).sembol("THYAO").sirketAdi("Türk Hava Yolları").build();
    }

    // ---------------------------------------------------------------
    // Alım İşlemi Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Alım işlemi: Başarıyla kaydedilmeli ve pozisyon güncellenmeli")
    void alimIslem_basariylaKaydedilir() {
        IslemRequest request = new IslemRequest(
                "THYAO", IslemTuru.ALIM,
                LocalDate.of(2024, 1, 10),
                new BigDecimal("100"),
                new BigDecimal("250"),
                new BigDecimal("12.50"),
                null
        );

        PortfoyIslem kaydedilen = PortfoyIslem.builder()
                .id(1L)
                .hisse(thyao)
                .islemTuru(IslemTuru.ALIM)
                .tarih(request.tarih())
                .lot(request.lot())
                .fiyat(request.fiyat())
                .komisyon(request.komisyon())
                .toplamTutar(new BigDecimal("25012.50"))
                .build();

        when(hisseRepository.findBySembol("THYAO")).thenReturn(Optional.of(thyao));
        when(islemRepository.save(any(PortfoyIslem.class))).thenReturn(kaydedilen);

        IslemDto sonuc = islemService.islemEkle(request);

        assertThat(sonuc).isNotNull();
        assertThat(sonuc.sembol()).isEqualTo("THYAO");
        assertThat(sonuc.islemTuru()).isEqualTo(IslemTuru.ALIM);
        verify(pozisyonService, times(1)).pozisyonGuncelle(eq(thyao), any(PortfoyIslem.class));
    }

    @Test
    @DisplayName("Alım: Toplam tutar doğru hesaplanmalı — lot*fiyat + komisyon")
    void alimToplamTutar_dogruHesaplaniyor() {
        IslemRequest request = new IslemRequest(
                "THYAO", IslemTuru.ALIM,
                LocalDate.of(2024, 1, 10),
                new BigDecimal("100"),
                new BigDecimal("250"),
                new BigDecimal("12.50"),
                null
        );

        PortfoyIslem kaydedilen = PortfoyIslem.builder()
                .id(1L).hisse(thyao)
                .islemTuru(IslemTuru.ALIM)
                .tarih(request.tarih())
                .lot(request.lot())
                .fiyat(request.fiyat())
                .komisyon(request.komisyon())
                .toplamTutar(new BigDecimal("25012.50"))
                .build();

        when(hisseRepository.findBySembol("THYAO")).thenReturn(Optional.of(thyao));
        when(islemRepository.save(any(PortfoyIslem.class))).thenReturn(kaydedilen);

        ArgumentCaptor<PortfoyIslem> captor = ArgumentCaptor.forClass(PortfoyIslem.class);

        islemService.islemEkle(request);

        verify(islemRepository).save(captor.capture());
        PortfoyIslem islem = captor.getValue();

        // toplamTutar = 100 * 250 + 12.50 = 25012.50
        assertThat(islem.getToplamTutar()).isEqualByComparingTo("25012.50");
    }

    @Test
    @DisplayName("Alım: Komisyon null ise sıfır olarak kabul edilmeli")
    void alimKomisyonNull_sifirKabulEdilir() {
        IslemRequest request = new IslemRequest(
                "THYAO", IslemTuru.ALIM,
                LocalDate.of(2024, 1, 10),
                new BigDecimal("100"),
                new BigDecimal("250"),
                null,  // komisyon null
                null
        );

        PortfoyIslem kaydedilen = PortfoyIslem.builder()
                .id(1L).hisse(thyao)
                .islemTuru(IslemTuru.ALIM)
                .tarih(request.tarih())
                .lot(request.lot())
                .fiyat(request.fiyat())
                .komisyon(BigDecimal.ZERO)
                .toplamTutar(new BigDecimal("25000"))
                .build();

        when(hisseRepository.findBySembol("THYAO")).thenReturn(Optional.of(thyao));
        when(islemRepository.save(any(PortfoyIslem.class))).thenReturn(kaydedilen);

        ArgumentCaptor<PortfoyIslem> captor = ArgumentCaptor.forClass(PortfoyIslem.class);
        islemService.islemEkle(request);

        verify(islemRepository).save(captor.capture());
        PortfoyIslem islem = captor.getValue();

        // komisyon null → 0; toplamTutar = 100 * 250 = 25000
        assertThat(islem.getKomisyon()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(islem.getToplamTutar()).isEqualByComparingTo("25000");
    }

    // ---------------------------------------------------------------
    // Satım İşlemi Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Satım işlemi: Toplam tutar doğru hesaplanmalı — lot*fiyat - komisyon")
    void satimToplamTutar_komisyonDusulur() {
        PortfoyPozisyon mevcutPozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .toplamLot(new BigDecimal("200"))
                .ortalamaMaliyet(new BigDecimal("275"))
                .toplamMaliyet(new BigDecimal("55000"))
                .build();

        IslemRequest request = new IslemRequest(
                "THYAO", IslemTuru.SATIM,
                LocalDate.of(2024, 1, 12),
                new BigDecimal("100"),
                new BigDecimal("320"),
                new BigDecimal("16"),
                null
        );

        PortfoyIslem kaydedilen = PortfoyIslem.builder()
                .id(2L).hisse(thyao)
                .islemTuru(IslemTuru.SATIM)
                .tarih(request.tarih())
                .lot(request.lot())
                .fiyat(request.fiyat())
                .komisyon(new BigDecimal("16"))
                .toplamTutar(new BigDecimal("31984"))
                .build();

        when(hisseRepository.findBySembol("THYAO")).thenReturn(Optional.of(thyao));
        when(pozisyonRepository.findByHisseId(1L)).thenReturn(Optional.of(mevcutPozisyon));
        when(islemRepository.save(any(PortfoyIslem.class))).thenReturn(kaydedilen);

        ArgumentCaptor<PortfoyIslem> captor = ArgumentCaptor.forClass(PortfoyIslem.class);
        islemService.islemEkle(request);

        verify(islemRepository).save(captor.capture());
        PortfoyIslem islem = captor.getValue();

        // toplamTutar = 100 * 320 - 16 = 31984
        assertThat(islem.getToplamTutar()).isEqualByComparingTo("31984");
    }

    @Test
    @DisplayName("Satım: Yeterli lot varken işlem başarılı olmalı")
    void satimYeterliLot_basarili() {
        PortfoyPozisyon mevcutPozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .toplamLot(new BigDecimal("200"))
                .ortalamaMaliyet(new BigDecimal("275"))
                .toplamMaliyet(new BigDecimal("55000"))
                .build();

        IslemRequest request = new IslemRequest(
                "THYAO", IslemTuru.SATIM,
                LocalDate.of(2024, 1, 12),
                new BigDecimal("100"),
                new BigDecimal("320"),
                BigDecimal.ZERO,
                null
        );

        PortfoyIslem kaydedilen = PortfoyIslem.builder()
                .id(2L).hisse(thyao)
                .islemTuru(IslemTuru.SATIM)
                .tarih(request.tarih())
                .lot(request.lot())
                .fiyat(request.fiyat())
                .komisyon(BigDecimal.ZERO)
                .toplamTutar(new BigDecimal("32000"))
                .build();

        when(hisseRepository.findBySembol("THYAO")).thenReturn(Optional.of(thyao));
        when(pozisyonRepository.findByHisseId(1L)).thenReturn(Optional.of(mevcutPozisyon));
        when(islemRepository.save(any(PortfoyIslem.class))).thenReturn(kaydedilen);

        assertThatNoException().isThrownBy(() -> islemService.islemEkle(request));
        verify(pozisyonService, times(1)).pozisyonGuncelle(eq(thyao), any(PortfoyIslem.class));
    }

    @Test
    @DisplayName("Satım: Yetersiz lot varken işlem reddedilmeli — BusinessException")
    void satimYetersizLot_hataVerir() {
        PortfoyPozisyon mevcutPozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .toplamLot(new BigDecimal("50"))
                .build();

        IslemRequest request = new IslemRequest(
                "THYAO", IslemTuru.SATIM,
                LocalDate.of(2024, 1, 12),
                new BigDecimal("100"),
                new BigDecimal("320"),
                BigDecimal.ZERO,
                null
        );

        when(hisseRepository.findBySembol("THYAO")).thenReturn(Optional.of(thyao));
        when(pozisyonRepository.findByHisseId(1L)).thenReturn(Optional.of(mevcutPozisyon));

        assertThatThrownBy(() -> islemService.islemEkle(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Yetersiz lot");

        verify(islemRepository, never()).save(any());
        verify(pozisyonService, never()).pozisyonGuncelle(any(), any());
    }

    @Test
    @DisplayName("Satım: Hiç pozisyon yokken işlem reddedilmeli — BusinessException")
    void satimPozisyonYok_hataVerir() {
        IslemRequest request = new IslemRequest(
                "THYAO", IslemTuru.SATIM,
                LocalDate.of(2024, 1, 12),
                new BigDecimal("100"),
                new BigDecimal("320"),
                BigDecimal.ZERO,
                null
        );

        when(hisseRepository.findBySembol("THYAO")).thenReturn(Optional.of(thyao));
        when(pozisyonRepository.findByHisseId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> islemService.islemEkle(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Yetersiz lot");
    }

    // ---------------------------------------------------------------
    // Validasyon Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Geçersiz hisse sembolü ile işlem — ResourceNotFoundException fırlatılmalı")
    void gecersizSembol_hataVerir() {
        IslemRequest request = new IslemRequest(
                "XXXX", IslemTuru.ALIM,
                LocalDate.of(2024, 1, 10),
                new BigDecimal("100"),
                new BigDecimal("100"),
                BigDecimal.ZERO,
                null
        );

        when(hisseRepository.findBySembol("XXXX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> islemService.islemEkle(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Hisse bulunamadı");
    }

    @Test
    @DisplayName("İşlem getir: Var olmayan ID ile ResourceNotFoundException fırlatılmalı")
    void islemGetir_olmayalId_hataVerir() {
        when(islemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> islemService.islemGetir(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("İşlem bulunamadı");
    }

    // ---------------------------------------------------------------
    // İşlem Silme Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("İşlem sil: Var olmayan ID ile ResourceNotFoundException fırlatılmalı")
    void islemSil_olmayalId_hataVerir() {
        when(islemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> islemService.islemSil(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("İşlem sil: Silme sonrası pozisyon yeniden hesaplanmalı")
    void islemSil_basarili_pozisyonYenidenHesaplaniyor() {
        PortfoyIslem islem = PortfoyIslem.builder()
                .id(1L)
                .hisse(thyao)
                .islemTuru(IslemTuru.ALIM)
                .lot(new BigDecimal("100"))
                .fiyat(new BigDecimal("250"))
                .komisyon(BigDecimal.ZERO)
                .toplamTutar(new BigDecimal("25000"))
                .tarih(LocalDate.of(2024, 1, 10))
                .build();

        when(islemRepository.findById(1L)).thenReturn(Optional.of(islem));
        doNothing().when(islemRepository).delete(islem);
        doNothing().when(pozisyonService).pozisyonuYenidenHesapla(thyao);

        islemService.islemSil(1L);

        verify(islemRepository, times(1)).delete(islem);
        verify(pozisyonService, times(1)).pozisyonuYenidenHesapla(thyao);
    }

    // ---------------------------------------------------------------
    // İşlem Listeleme Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("İşlem listesi: Filtre olmadan tüm işlemler dönmeli")
    void islemListesi_filtresizTumIslemler() {
        IslemFiltre filtre = new IslemFiltre(null, null, null, null);

        PortfoyIslem alim = PortfoyIslem.builder()
                .id(1L).hisse(thyao).islemTuru(IslemTuru.ALIM)
                .lot(new BigDecimal("100")).fiyat(new BigDecimal("250"))
                .komisyon(BigDecimal.ZERO).toplamTutar(new BigDecimal("25000"))
                .tarih(LocalDate.of(2024, 1, 10)).build();

        when(islemRepository.findAllByOrderByTarihDesc()).thenReturn(List.of(alim));

        List<IslemDto> sonuc = islemService.islemleriGetir(filtre);

        assertThat(sonuc).hasSize(1);
        assertThat(sonuc.get(0).sembol()).isEqualTo("THYAO");
    }

    @Test
    @DisplayName("İşlem listesi: Tür filtresi ile sadece ALIM işlemleri gelmeli")
    void islemListesi_turFiltreliSadeceAlim() {
        IslemFiltre filtre = new IslemFiltre(null, null, null, IslemTuru.ALIM);

        PortfoyIslem alim = PortfoyIslem.builder()
                .id(1L).hisse(thyao).islemTuru(IslemTuru.ALIM)
                .lot(new BigDecimal("100")).fiyat(new BigDecimal("250"))
                .komisyon(BigDecimal.ZERO).toplamTutar(new BigDecimal("25000"))
                .tarih(LocalDate.of(2024, 1, 10)).build();

        PortfoyIslem satim = PortfoyIslem.builder()
                .id(2L).hisse(thyao).islemTuru(IslemTuru.SATIM)
                .lot(new BigDecimal("50")).fiyat(new BigDecimal("300"))
                .komisyon(BigDecimal.ZERO).toplamTutar(new BigDecimal("15000"))
                .tarih(LocalDate.of(2024, 1, 12)).build();

        when(islemRepository.findAllByOrderByTarihDesc()).thenReturn(List.of(alim, satim));

        List<IslemDto> sonuc = islemService.islemleriGetir(filtre);

        assertThat(sonuc).hasSize(1);
        assertThat(sonuc.get(0).islemTuru()).isEqualTo(IslemTuru.ALIM);
    }
}
