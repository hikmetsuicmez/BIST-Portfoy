package com.portfolio.service;

import com.portfolio.dto.response.GunsonuSonucDto;
import com.portfolio.entity.*;
import com.portfolio.exception.BusinessException;
import com.portfolio.repository.*;
import com.portfolio.service.impl.GunsonuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GunsonuServiceTest {

    @Mock private PozisyonRepository pozisyonRepository;
    @Mock private KapanisFiyatRepository kapanisFiyatRepository;
    @Mock private GunlukDegisimRepository gunlukDegisimRepository;
    @Mock private OzetGunlukRepository ozetGunlukRepository;

    @InjectMocks private GunsonuServiceImpl gunsonuService;

    private Hisse thyao;
    private Hisse garan;

    @BeforeEach
    void setup() {
        thyao = Hisse.builder().id(1L).sembol("THYAO").sirketAdi("Türk Hava Yolları").build();
        garan = Hisse.builder().id(2L).sembol("GARAN").sirketAdi("Garanti BBVA").build();
    }

    // ---------------------------------------------------------------
    // Temel Hesaplama Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Günsonu: Tek pozisyon için doğru kar/zarar hesaplanmalı")
    void gunsonuTekPozisyon_dogruKarZararHesabi() {
        LocalDate tarih = LocalDate.of(2024, 1, 15);

        PortfoyPozisyon pozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .toplamLot(new BigDecimal("100"))
                .ortalamaMaliyet(new BigDecimal("250.00"))
                .toplamMaliyet(new BigDecimal("25000.00"))
                .build();

        KapanisFiyat bugunFiyat = KapanisFiyat.builder()
                .hisse(thyao).tarih(tarih)
                .kapanisFiyat(new BigDecimal("279.00"))
                .build();

        KapanisFiyat dunkuFiyat = KapanisFiyat.builder()
                .hisse(thyao).tarih(tarih.minusDays(1))
                .kapanisFiyat(new BigDecimal("270.00"))
                .build();

        stubBasariliGunsonu(tarih, List.of(pozisyon), 1L, bugunFiyat, List.of(dunkuFiyat));

        GunsonuSonucDto sonuc = gunsonuService.gunsonuCalistir(tarih);

        assertThat(sonuc.basarili()).isTrue();
        assertThat(sonuc.islemSayisi()).isEqualTo(1);

        PortfoyGunlukDegisim degisim = getKaydedilecekDegisim();

        // guncelDeger = 100 * 279 = 27900
        assertThat(degisim.getGuncelDeger()).isEqualByComparingTo("27900.00");
        // toplamKarZararTl = 27900 - 25000 = 2900
        assertThat(degisim.getToplamKarZararTl()).isEqualByComparingTo("2900.00");
        // toplamKarZararYuzde = (2900 / 25000) * 100 = 11.60%
        assertThat(degisim.getToplamKarZararYuzde()).isEqualByComparingTo("11.6000");
        // gunlukDegisimTl = (279 - 270) * 100 = 900
        assertThat(degisim.getGunlukDegisimTl()).isEqualByComparingTo("900.00");
        // gunlukDegisimYuzde: önce *100 (900), sonra /270 → 3.3333 (hassasiyet korunur)
        assertThat(degisim.getGunlukDegisimYuzde()).isEqualByComparingTo("3.3333");
    }

    @Test
    @DisplayName("Günsonu: Zarar durumunda negatif değerler doğru hesaplanmalı")
    void gunsonuZararDurumu_negatifDegerlerDoğru() {
        LocalDate tarih = LocalDate.of(2024, 1, 15);

        PortfoyPozisyon pozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .toplamLot(new BigDecimal("100"))
                .ortalamaMaliyet(new BigDecimal("250.00"))
                .toplamMaliyet(new BigDecimal("25000.00"))
                .build();

        KapanisFiyat bugunFiyat = KapanisFiyat.builder()
                .hisse(thyao).tarih(tarih)
                .kapanisFiyat(new BigDecimal("220.00"))
                .build();

        stubBasariliGunsonu(tarih, List.of(pozisyon), 1L, bugunFiyat, List.of());

        gunsonuService.gunsonuCalistir(tarih);

        PortfoyGunlukDegisim degisim = getKaydedilecekDegisim();

        // guncelDeger = 100 * 220 = 22000
        assertThat(degisim.getGuncelDeger()).isEqualByComparingTo("22000.00");
        // toplamKarZararTl = 22000 - 25000 = -3000
        assertThat(degisim.getToplamKarZararTl()).isEqualByComparingTo("-3000.00");
        // toplamKarZararYuzde = (-3000 / 25000) * 100 = -12%
        assertThat(degisim.getToplamKarZararYuzde()).isEqualByComparingTo("-12.0000");
    }

    @Test
    @DisplayName("Günsonu: Başabaş durumunda kar/zarar sıfır olmalı")
    void gunsonuBasabas_karZararSifir() {
        LocalDate tarih = LocalDate.of(2024, 1, 15);

        PortfoyPozisyon pozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .toplamLot(new BigDecimal("100"))
                .ortalamaMaliyet(new BigDecimal("250.00"))
                .toplamMaliyet(new BigDecimal("25000.00"))
                .build();

        KapanisFiyat bugunFiyat = KapanisFiyat.builder()
                .hisse(thyao).tarih(tarih)
                .kapanisFiyat(new BigDecimal("250.00"))
                .build();

        stubBasariliGunsonu(tarih, List.of(pozisyon), 1L, bugunFiyat, List.of());

        GunsonuSonucDto sonuc = gunsonuService.gunsonuCalistir(tarih);

        assertThat(sonuc.basarili()).isTrue();
        assertThat(sonuc.toplamKarZararTl()).isEqualByComparingTo("0");

        PortfoyGunlukDegisim degisim = getKaydedilecekDegisim();
        assertThat(degisim.getToplamKarZararTl()).isEqualByComparingTo("0");
        assertThat(degisim.getToplamKarZararYuzde()).isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("Günsonu: İki pozisyon için toplam değer ve kar/zarar doğru toplanmalı")
    void gunsonuIkiPozisyon_toplamDegerDogruHesaplaniyor() {
        LocalDate tarih = LocalDate.of(2024, 1, 15);

        PortfoyPozisyon thyaoPozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .toplamLot(new BigDecimal("100"))
                .ortalamaMaliyet(new BigDecimal("250"))
                .toplamMaliyet(new BigDecimal("25000"))
                .build();

        PortfoyPozisyon garanPozisyon = PortfoyPozisyon.builder()
                .hisse(garan)
                .toplamLot(new BigDecimal("200"))
                .ortalamaMaliyet(new BigDecimal("120"))
                .toplamMaliyet(new BigDecimal("24000"))
                .build();

        KapanisFiyat thyaoFiyat = KapanisFiyat.builder()
                .hisse(thyao).tarih(tarih).kapanisFiyat(new BigDecimal("279")).build();

        KapanisFiyat garanFiyat = KapanisFiyat.builder()
                .hisse(garan).tarih(tarih).kapanisFiyat(new BigDecimal("125")).build();

        when(ozetGunlukRepository.existsByTarih(tarih)).thenReturn(false);
        when(pozisyonRepository.findByToplamLotGreaterThan(BigDecimal.ZERO))
                .thenReturn(List.of(thyaoPozisyon, garanPozisyon));
        when(kapanisFiyatRepository.findByHisseIdAndTarih(1L, tarih)).thenReturn(Optional.of(thyaoFiyat));
        when(kapanisFiyatRepository.findByHisseIdAndTarih(2L, tarih)).thenReturn(Optional.of(garanFiyat));
        when(kapanisFiyatRepository.findPreviousByHisseId(anyLong(), eq(tarih), any(Pageable.class)))
                .thenReturn(List.of());
        when(gunlukDegisimRepository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);
        when(ozetGunlukRepository.findTop30ByOrderByTarihDesc()).thenReturn(List.of());
        when(ozetGunlukRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        GunsonuSonucDto sonuc = gunsonuService.gunsonuCalistir(tarih);

        assertThat(sonuc.islemSayisi()).isEqualTo(2);
        // toplamDeger = 100*279 + 200*125 = 27900 + 25000 = 52900
        assertThat(sonuc.toplamDeger()).isEqualByComparingTo("52900");
        // toplamKarZararTl = 52900 - (25000 + 24000) = 3900
        assertThat(sonuc.toplamKarZararTl()).isEqualByComparingTo("3900");
    }

    // ---------------------------------------------------------------
    // Validasyon Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Günsonu: Aynı gün iki kez çalıştırılmamalı — BusinessException fırlatılmalı")
    void gunsonuAyniGunIkinci_hataVerir() {
        LocalDate tarih = LocalDate.of(2024, 1, 15);
        when(ozetGunlukRepository.existsByTarih(tarih)).thenReturn(true);

        assertThatThrownBy(() -> gunsonuService.gunsonuCalistir(tarih))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten tamamlanmış");
    }

    @Test
    @DisplayName("Günsonu: Eksik fiyat varsa hata ve sembol adı mesajda belirtilmeli")
    void gunsonuEksikFiyat_hataVerirVeSembolBelirtilir() {
        LocalDate tarih = LocalDate.of(2024, 1, 15);

        PortfoyPozisyon pozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .toplamLot(new BigDecimal("100"))
                .build();

        when(ozetGunlukRepository.existsByTarih(tarih)).thenReturn(false);
        when(pozisyonRepository.findByToplamLotGreaterThan(BigDecimal.ZERO)).thenReturn(List.of(pozisyon));
        when(kapanisFiyatRepository.findByHisseIdAndTarih(1L, tarih)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gunsonuService.gunsonuCalistir(tarih))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("THYAO");
    }

    @Test
    @DisplayName("Günsonu: Aktif pozisyon yokken çalıştırılırsa hata vermeli")
    void gunsonuAktifPozisyonYok_hataVerir() {
        LocalDate tarih = LocalDate.of(2024, 1, 15);

        when(ozetGunlukRepository.existsByTarih(tarih)).thenReturn(false);
        when(pozisyonRepository.findByToplamLotGreaterThan(BigDecimal.ZERO)).thenReturn(List.of());

        assertThatThrownBy(() -> gunsonuService.gunsonuCalistir(tarih))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Aktif pozisyon");
    }

    // ---------------------------------------------------------------
    // İlk Gün Senaryosu
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Günsonu: İlk gün — önceki kapanış null, günlük değişim hesaplanmamalı")
    void gunsonuIlkGun_oncekiFiyatNullGunlukDegisimHesaplanmaz() {
        LocalDate tarih = LocalDate.of(2024, 1, 2);

        PortfoyPozisyon pozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .toplamLot(new BigDecimal("100"))
                .ortalamaMaliyet(new BigDecimal("250"))
                .toplamMaliyet(new BigDecimal("25000"))
                .build();

        KapanisFiyat bugunFiyat = KapanisFiyat.builder()
                .hisse(thyao).tarih(tarih)
                .kapanisFiyat(new BigDecimal("260"))
                .build();

        stubBasariliGunsonu(tarih, List.of(pozisyon), 1L, bugunFiyat, List.of());

        gunsonuService.gunsonuCalistir(tarih);

        PortfoyGunlukDegisim degisim = getKaydedilecekDegisim();

        assertThat(degisim.getOncekiKapanisFiyat()).isNull();
        assertThat(degisim.getGunlukDegisimTl()).isNull();
        assertThat(degisim.getGunlukDegisimYuzde()).isNull();
        // Toplam kar/zarar yine de hesaplanmalı: (260-250)*100 = 1000
        assertThat(degisim.getToplamKarZararTl()).isEqualByComparingTo("1000");
    }

    // ---------------------------------------------------------------
    // Kayıt Doğrulama Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Günsonu: Her çalıştırmada saveAll ve ozetRepo.save çağrılmalı")
    void gunsonu_kaydIslemleriCagrilmali() {
        LocalDate tarih = LocalDate.of(2024, 1, 15);

        PortfoyPozisyon pozisyon = PortfoyPozisyon.builder()
                .hisse(thyao)
                .toplamLot(new BigDecimal("100"))
                .ortalamaMaliyet(new BigDecimal("250"))
                .toplamMaliyet(new BigDecimal("25000"))
                .build();

        KapanisFiyat bugunFiyat = KapanisFiyat.builder()
                .hisse(thyao).tarih(tarih).kapanisFiyat(new BigDecimal("279"))
                .build();

        stubBasariliGunsonu(tarih, List.of(pozisyon), 1L, bugunFiyat, List.of());

        gunsonuService.gunsonuCalistir(tarih);

        verify(gunlukDegisimRepository, times(1)).saveAll(anyList());
        verify(ozetGunlukRepository, times(1)).save(any(PortfoyOzetGunluk.class));
    }

    // ---------------------------------------------------------------
    // Yardımcı Metotlar
    // ---------------------------------------------------------------

    private void stubBasariliGunsonu(LocalDate tarih,
                                     List<PortfoyPozisyon> pozisyonlar,
                                     Long hisseId,
                                     KapanisFiyat bugunFiyat,
                                     List<KapanisFiyat> oncekiFiyatlar) {
        when(ozetGunlukRepository.existsByTarih(tarih)).thenReturn(false);
        when(pozisyonRepository.findByToplamLotGreaterThan(BigDecimal.ZERO)).thenReturn(pozisyonlar);
        when(kapanisFiyatRepository.findByHisseIdAndTarih(hisseId, tarih))
                .thenReturn(Optional.of(bugunFiyat));
        when(kapanisFiyatRepository.findPreviousByHisseId(eq(hisseId), eq(tarih), any(Pageable.class)))
                .thenReturn(oncekiFiyatlar);
        when(gunlukDegisimRepository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);
        when(ozetGunlukRepository.findTop30ByOrderByTarihDesc()).thenReturn(List.of());
        when(ozetGunlukRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    }

    @SuppressWarnings("unchecked")
    private PortfoyGunlukDegisim getKaydedilecekDegisim() {
        ArgumentCaptor<List<PortfoyGunlukDegisim>> captor = ArgumentCaptor.forClass(List.class);
        verify(gunlukDegisimRepository).saveAll(captor.capture());
        return captor.getValue().get(0);
    }
}
