package com.portfolio.service;

import com.portfolio.dto.request.TemmettuRequest;
import com.portfolio.dto.response.TemmettuDto;
import com.portfolio.entity.Hisse;
import com.portfolio.entity.Kullanici;
import com.portfolio.entity.PortfoyPozisyon;
import com.portfolio.entity.Temmettu;
import com.portfolio.exception.BusinessException;
import com.portfolio.repository.HisseRepository;
import com.portfolio.repository.PozisyonRepository;
import com.portfolio.repository.TemmettuRepository;
import com.portfolio.security.SecurityUtil;
import com.portfolio.service.impl.TemmettuServiceImpl;
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
class TemmettuServiceTest {

    @Mock private TemmettuRepository temmettuRepository;
    @Mock private HisseRepository hisseRepository;
    @Mock private PozisyonRepository pozisyonRepository;
    @Mock private SecurityUtil securityUtil;

    @InjectMocks private TemmettuServiceImpl temmettuService;

    private static final Long KULLANICI_ID = 1L;

    private Hisse thyao;
    private Kullanici kullanici;

    @BeforeEach
    void setup() {
        thyao = Hisse.builder().id(1L).sembol("THYAO").sirketAdi("Türk Hava Yolları").build();
        kullanici = Kullanici.builder().id(KULLANICI_ID).email("test@test.com").ad("Test").build();
    }

    // ---------------------------------------------------------------
    // Doğru Hesaplama Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Temettü: Brüt, net ve toplam tutarlar doğru hesaplanmalı")
    void temmettuKaydet_dogruHesaplama() {
        // 100 lot @ 2.50 TL brüt, %10 stopaj → net = 2.25
        // toplamBrut = 100 * 2.50 = 250.00
        // toplamNet  = 100 * 2.25 = 225.00
        TemmettuRequest request = new TemmettuRequest(
                "THYAO", 2024,
                new BigDecimal("2.50"),
                new BigDecimal("0.10"),
                LocalDate.of(2024, 4, 15),
                null
        );

        PortfoyPozisyon pozisyon = PortfoyPozisyon.builder()
                .hisse(thyao).kullanici(kullanici)
                .toplamLot(new BigDecimal("100"))
                .build();

        stubBasariliKayit(request, pozisyon);

        TemmettuDto sonuc = temmettuService.temmettuKaydet(request);

        ArgumentCaptor<Temmettu> captor = ArgumentCaptor.forClass(Temmettu.class);
        verify(temmettuRepository).save(captor.capture());
        Temmettu kaydedilen = captor.getValue();

        assertThat(kaydedilen.getHisseBasiNet()).isEqualByComparingTo("2.2500");
        assertThat(kaydedilen.getToplamBrut()).isEqualByComparingTo("250.0000");
        assertThat(kaydedilen.getToplamNet()).isEqualByComparingTo("225.0000");
        assertThat(kaydedilen.getLot()).isEqualByComparingTo("100");
        assertThat(kaydedilen.getStopajOrani()).isEqualByComparingTo("0.10");
    }

    @Test
    @DisplayName("Temettü: Varsayılan stopaj oranı (%10) null gelince kullanılmalı")
    void temmettuKaydet_varsayilanStopaj() {
        TemmettuRequest request = new TemmettuRequest(
                "THYAO", 2024,
                new BigDecimal("3.00"),
                null,  // stopaj null → varsayılan 0.10
                LocalDate.of(2024, 4, 15),
                null
        );

        PortfoyPozisyon pozisyon = PortfoyPozisyon.builder()
                .hisse(thyao).kullanici(kullanici)
                .toplamLot(new BigDecimal("200"))
                .build();

        stubBasariliKayit(request, pozisyon);

        temmettuService.temmettuKaydet(request);

        ArgumentCaptor<Temmettu> captor = ArgumentCaptor.forClass(Temmettu.class);
        verify(temmettuRepository).save(captor.capture());
        Temmettu kaydedilen = captor.getValue();

        // hisseBasiNet = 3.00 * (1 - 0.10) = 2.70
        assertThat(kaydedilen.getStopajOrani()).isEqualByComparingTo("0.10");
        assertThat(kaydedilen.getHisseBasiNet()).isEqualByComparingTo("2.7000");
        // toplamNet = 200 * 2.70 = 540
        assertThat(kaydedilen.getToplamNet()).isEqualByComparingTo("540.0000");
    }

    @Test
    @DisplayName("Temettü: Lot ile çarpım hassasiyeti korunmalı (kesirli lot)")
    void temmettuKaydet_lotCarpimHassasiyeti() {
        TemmettuRequest request = new TemmettuRequest(
                "THYAO", 2024,
                new BigDecimal("1.1234"),
                new BigDecimal("0.15"),
                LocalDate.of(2024, 4, 15),
                null
        );

        PortfoyPozisyon pozisyon = PortfoyPozisyon.builder()
                .hisse(thyao).kullanici(kullanici)
                .toplamLot(new BigDecimal("150.5000"))
                .build();

        stubBasariliKayit(request, pozisyon);

        temmettuService.temmettuKaydet(request);

        ArgumentCaptor<Temmettu> captor = ArgumentCaptor.forClass(Temmettu.class);
        verify(temmettuRepository).save(captor.capture());
        Temmettu kaydedilen = captor.getValue();

        // hisseBasiNet = 1.1234 * (1 - 0.15) = 1.1234 * 0.85 = 0.95489 → 0.9549
        assertThat(kaydedilen.getHisseBasiNet()).isEqualByComparingTo("0.9549");
        // toplamBrut = 150.5 * 1.1234 = 169.0717
        assertThat(kaydedilen.getToplamBrut()).isEqualByComparingTo("169.0717");
        // toplamNet = 150.5 * 0.9549 = 143.7124...
        assertThat(kaydedilen.getToplamNet()).isEqualByComparingTo("143.7125");
    }

    // ---------------------------------------------------------------
    // Aynı Yıl Çift Kayıt Engeli Testi
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Temettü: Aynı hisse + yıl için ikinci kayıt reddedilmeli")
    void temmettuKaydet_ayniYilCiftKayitReddedilmeli() {
        TemmettuRequest request = new TemmettuRequest(
                "THYAO", 2024,
                new BigDecimal("2.50"),
                null,
                LocalDate.of(2024, 4, 15),
                null
        );

        when(hisseRepository.findBySembol("THYAO")).thenReturn(Optional.of(thyao));
        when(securityUtil.getCurrentKullanici()).thenReturn(kullanici);
        when(temmettuRepository.existsByHisseIdAndKullaniciIdAndYil(1L, KULLANICI_ID, 2024))
                .thenReturn(true);

        assertThatThrownBy(() -> temmettuService.temmettuKaydet(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("2024")
                .hasMessageContaining("zaten girilmiş");

        verify(temmettuRepository, never()).save(any());
    }

    @Test
    @DisplayName("Temettü: Farklı yıllar için aynı hisseye kayıt yapılabilmeli")
    void temmettuKaydet_farkliYilKabulEdilir() {
        TemmettuRequest request = new TemmettuRequest(
                "THYAO", 2025,
                new BigDecimal("2.80"),
                null,
                LocalDate.of(2025, 4, 10),
                null
        );

        PortfoyPozisyon pozisyon = PortfoyPozisyon.builder()
                .hisse(thyao).kullanici(kullanici)
                .toplamLot(new BigDecimal("100"))
                .build();

        stubBasariliKayit(request, pozisyon);

        assertThatNoException().isThrownBy(() -> temmettuService.temmettuKaydet(request));
        verify(temmettuRepository, times(1)).save(any(Temmettu.class));
    }

    @Test
    @DisplayName("Temettü: Pozisyon yokken kayıt yapılabilmeli (geçmişe yönelik)")
    void temmettuKaydet_pozisyonYoksa_yineKaydedilir() {
        TemmettuRequest request = new TemmettuRequest(
                "THYAO", 2023,
                new BigDecimal("1.50"),
                null,
                LocalDate.of(2023, 5, 1),
                null
        );

        when(hisseRepository.findBySembol("THYAO")).thenReturn(Optional.of(thyao));
        when(securityUtil.getCurrentKullanici()).thenReturn(kullanici);
        when(temmettuRepository.existsByHisseIdAndKullaniciIdAndYil(1L, KULLANICI_ID, 2023))
                .thenReturn(false);
        when(pozisyonRepository.findByHisseIdAndKullaniciId(1L, KULLANICI_ID))
                .thenReturn(Optional.empty());
        when(temmettuRepository.save(any())).thenAnswer(i -> {
            Temmettu t = i.getArgument(0);
            t = Temmettu.builder()
                    .id(1L).hisse(thyao).kullanici(kullanici)
                    .yil(t.getYil()).hisseBasiTBrut(t.getHisseBasiTBrut())
                    .stopajOrani(t.getStopajOrani()).hisseBasiNet(t.getHisseBasiNet())
                    .odemeTarihi(t.getOdemeTarihi())
                    .lot(t.getLot()).toplamBrut(t.getToplamBrut()).toplamNet(t.getToplamNet())
                    .build();
            return t;
        });

        assertThatNoException().isThrownBy(() -> temmettuService.temmettuKaydet(request));

        ArgumentCaptor<Temmettu> captor = ArgumentCaptor.forClass(Temmettu.class);
        verify(temmettuRepository).save(captor.capture());
        // Lot 0 olduğunda toplamNet = 0
        assertThat(captor.getValue().getLot()).isEqualByComparingTo("0");
        assertThat(captor.getValue().getToplamNet()).isEqualByComparingTo("0.0000");
    }

    // ---------------------------------------------------------------
    // Yardımcı Metotlar
    // ---------------------------------------------------------------

    private void stubBasariliKayit(TemmettuRequest request, PortfoyPozisyon pozisyon) {
        when(hisseRepository.findBySembol("THYAO")).thenReturn(Optional.of(thyao));
        when(securityUtil.getCurrentKullanici()).thenReturn(kullanici);
        when(temmettuRepository.existsByHisseIdAndKullaniciIdAndYil(1L, KULLANICI_ID, request.yil()))
                .thenReturn(false);
        when(pozisyonRepository.findByHisseIdAndKullaniciId(1L, KULLANICI_ID))
                .thenReturn(Optional.of(pozisyon));
        when(temmettuRepository.save(any())).thenAnswer(i -> {
            Temmettu t = i.getArgument(0);
            return Temmettu.builder()
                    .id(1L).hisse(thyao).kullanici(kullanici)
                    .yil(t.getYil()).hisseBasiTBrut(t.getHisseBasiTBrut())
                    .stopajOrani(t.getStopajOrani()).hisseBasiNet(t.getHisseBasiNet())
                    .odemeTarihi(t.getOdemeTarihi())
                    .lot(t.getLot()).toplamBrut(t.getToplamBrut()).toplamNet(t.getToplamNet())
                    .build();
        });
    }
}
