package com.portfolio.service;

import com.portfolio.entity.Kullanici;
import com.portfolio.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true")
public class SchedulerService {

    private final YahooFinanceService yahooFinanceService;
    private final GunsonuService gunsonuService;
    private final KullaniciRepository kullaniciRepository;

    // 2024, 2025 ve 2026 yılları için BIST resmi tatil günleri.
    // Hafta sonu tatilleri @Scheduled cron ifadesindeki MON-FRI kısıtı ile zaten
    // atlanır.
    private static final Set<LocalDate> TURKIYE_TATILLERI = Set.of(
            // 2024
            LocalDate.of(2024, 1, 1), // Yılbaşı
            LocalDate.of(2024, 4, 9), // Ramazan Arefe (yarım gün, BIST erken kapanır)
            LocalDate.of(2024, 4, 10), // Ramazan Bayramı 1
            LocalDate.of(2024, 4, 11), // Ramazan Bayramı 2
            LocalDate.of(2024, 4, 12), // Ramazan Bayramı 3
            LocalDate.of(2024, 4, 23), // Ulusal Egemenlik ve Çocuk Bayramı
            LocalDate.of(2024, 5, 1), // İşçi ve Emekçi Bayramı
            LocalDate.of(2024, 5, 19), // Gençlik ve Spor Bayramı (Pazar - cron zaten atlar)
            LocalDate.of(2024, 6, 17), // Kurban Bayramı 1
            LocalDate.of(2024, 6, 18), // Kurban Bayramı 2
            LocalDate.of(2024, 6, 19), // Kurban Bayramı 3
            LocalDate.of(2024, 6, 20), // Kurban Bayramı 4
            LocalDate.of(2024, 7, 15), // Demokrasi ve Millî Birlik Günü
            LocalDate.of(2024, 8, 30), // Zafer Bayramı
            LocalDate.of(2024, 10, 28), // Cumhuriyet Bayramı arefesi (yarım gün, BIST erken kapanır)
            LocalDate.of(2024, 10, 29), // Cumhuriyet Bayramı
            // 2025
            LocalDate.of(2025, 1, 1), // Yılbaşı
            LocalDate.of(2025, 3, 31), // Ramazan Bayramı 2
            LocalDate.of(2025, 4, 1), // Ramazan Bayramı 3
            LocalDate.of(2025, 4, 23), // Ulusal Egemenlik ve Çocuk Bayramı
            LocalDate.of(2025, 5, 1), // İşçi ve Emekçi Bayramı
            LocalDate.of(2025, 5, 19), // Gençlik ve Spor Bayramı
            LocalDate.of(2025, 6, 5), // Kurban Arefe
            LocalDate.of(2025, 6, 6), // Kurban Bayramı 1
            LocalDate.of(2025, 6, 9), // Kurban Bayramı 4
            LocalDate.of(2025, 7, 15), // Demokrasi ve Millî Birlik Günü
            LocalDate.of(2025, 10, 28), // Cumhuriyet Bayramı arefesi
            LocalDate.of(2025, 10, 29), // Cumhuriyet Bayramı
            // 2026
            LocalDate.of(2026, 1, 1), // Yılbaşı
            LocalDate.of(2026, 3, 19), // Ramazan Bayramı Arefesi (Yarım Gün Tatil)
            LocalDate.of(2026, 3, 20), // Ramazan Bayramı
            LocalDate.of(2026, 3, 21), // Ramazan Bayramı
            LocalDate.of(2026, 3, 22), // Ramazan Bayramı
            LocalDate.of(2026, 4, 23), // Ulusal Egemenlik ve Çocuk Bayramı
            LocalDate.of(2026, 5, 1), // İşçi ve Emekçi Bayramı
            LocalDate.of(2026, 5, 19), // Gençlik ve Spor Bayramı
            LocalDate.of(2026, 5, 26), // Kurban Bayramı arefesi
            LocalDate.of(2026, 5, 27), // Kurban Bayramı 1
            LocalDate.of(2026, 5, 28), // Kurban Bayramı 2
            LocalDate.of(2026, 5, 29), // Kurban Bayramı 3
            LocalDate.of(2026, 7, 15), // Demokrasi ve Millî Birlik Günü
            LocalDate.of(2026, 8, 30), // Zafer Bayramı
            LocalDate.of(2026, 10, 28), // Cumhuriyet Bayramı arefesi
            LocalDate.of(2026, 10, 29) // Cumhuriyet Bayramı
    );

    @Scheduled(cron = "${scheduler.gunsonu-saati:0 30 18 * * MON-FRI}", zone = "Europe/Istanbul")
    public void gunsonuOtomatikCalistir() {
        LocalDate bugun = LocalDate.now();

        if (TURKIYE_TATILLERI.contains(bugun)) {
            log.info("[Scheduler] {} resmi tatil, günsonu atlanıyor", bugun);
            return;
        }

        log.info("[Scheduler] {} günsonu işlemi başlıyor", bugun);
        try {
            int guncellenen = yahooFinanceService.fiyatlariCek(bugun);
            log.info("[Scheduler] {} hisse fiyatı Yahoo Finance'den çekildi", guncellenen);
        } catch (Exception e) {
            log.error("[Scheduler] {} fiyat çekme hatası: {}", bugun, e.getMessage(), e);
        }

        List<Kullanici> aktifKullanicilar = kullaniciRepository.findByAktifTrue();
        for (Kullanici kullanici : aktifKullanicilar) {
            try {
                gunsonuService.gunsonuCalistir(bugun, kullanici.getId());
                log.info("[Scheduler] {} günsonu tamamlandı - kullanici: {}", bugun, kullanici.getId());
            } catch (Exception e) {
                log.error("[Scheduler] Günsonu hatası - kullanici: {}", kullanici.getId(), e);
            }
        }
    }
}
