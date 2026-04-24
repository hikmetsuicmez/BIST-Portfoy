package com.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.entity.Hisse;
import com.portfolio.entity.KapanisFiyat;
import com.portfolio.entity.PortfoyPozisyon;
import com.portfolio.repository.KapanisFiyatRepository;
import com.portfolio.repository.PozisyonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class YahooFinanceService {

    private final PozisyonRepository pozisyonRepository;
    private final KapanisFiyatRepository kapanisFiyatRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String YAHOO_V8_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/%s.IS?interval=1d&range=1d";

    @Transactional
    public int fiyatlariCek(LocalDate tarih) {
        List<PortfoyPozisyon> aktifPozisyonlar = pozisyonRepository.findByToplamLotGreaterThan(BigDecimal.ZERO);
        int guncellenenSayisi = 0;

        for (PortfoyPozisyon pozisyon : aktifPozisyonlar) {
            Hisse hisse = pozisyon.getHisse();
            String sembol = hisse.getSembol();
            try {
                FiyatBilgisi fiyat = fiyatBilgisiGetir(sembol);
                if (fiyat == null) {
                    log.warn("[Yahoo] Fiyat alınamadı: {}", sembol);
                    continue;
                }
                upsertKapanisFiyat(hisse, tarih, fiyat);
                guncellenenSayisi++;
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("[Yahoo] Döngü kesildi, {} hisse işlendi", guncellenenSayisi);
                break;
            } catch (Exception e) {
                log.error("[Yahoo] Hata [{}]: {}", sembol, e.getMessage());
            }
        }

        log.info("[Yahoo] {} → {} hisse güncellendi", tarih, guncellenenSayisi);
        return guncellenenSayisi;
    }

    public BigDecimal tekHisseFiyatCek(String sembol) {
        FiyatBilgisi fiyat = fiyatBilgisiGetir(sembol);
        return fiyat != null ? fiyat.kapanis() : null;
    }

    private FiyatBilgisi fiyatBilgisiGetir(String sembol) {
        try {
            String url = String.format(YAHOO_V8_URL, sembol.toUpperCase());

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode root = MAPPER.readTree(response.getBody());
            JsonNode result = root.path("chart").path("result");
            if (result.isMissingNode() || result.isEmpty()) {
                log.warn("[Yahoo] Sonuç boş: {}", sembol);
                return null;
            }

            JsonNode meta = result.get(0).path("meta");
            double kapanis = meta.path("regularMarketPrice").asDouble(0);
            if (kapanis == 0) {
                log.warn("[Yahoo] Fiyat sıfır geldi: {}", sembol);
                return null;
            }

            JsonNode quoteArr  = result.get(0).path("indicators").path("quote");
            JsonNode quoteData = (quoteArr.isArray() && !quoteArr.isEmpty()) ? quoteArr.get(0) : null;

            double acilis, yuksek, dusuk;
            long   hacim;
            if (quoteData != null && quoteData.isObject()) {
                acilis = sonDegerVeyaVarsayilan(quoteData.path("open"),   kapanis);
                yuksek = sonDegerVeyaVarsayilan(quoteData.path("high"),   kapanis);
                dusuk  = sonDegerVeyaVarsayilan(quoteData.path("low"),    kapanis);
                hacim  = (long) sonDegerVeyaVarsayilan(quoteData.path("volume"), 0);
            } else {
                log.debug("[Yahoo] OHLCV bulunamadı, sadece kapanış kaydedilecek: {}", sembol);
                acilis = kapanis;
                yuksek = kapanis;
                dusuk  = kapanis;
                hacim  = 0L;
            }

            return new FiyatBilgisi(
                    BigDecimal.valueOf(acilis),
                    BigDecimal.valueOf(yuksek),
                    BigDecimal.valueOf(dusuk),
                    BigDecimal.valueOf(kapanis),
                    hacim);

        } catch (Exception e) {
            log.warn("[Yahoo] API hatası [{}]: {}", sembol, e.getMessage());
            return null;
        }
    }

    // Son non-null elemanı döner — bugünün verisi dizinin sonunda olabilir.
    private double sonDegerVeyaVarsayilan(JsonNode arrayNode, double varsayilan) {
        if (arrayNode != null && arrayNode.isArray()) {
            for (int i = arrayNode.size() - 1; i >= 0; i--) {
                JsonNode elem = arrayNode.get(i);
                if (elem != null && !elem.isNull()) {
                    return elem.asDouble(varsayilan);
                }
            }
        }
        return varsayilan;
    }

    private void upsertKapanisFiyat(Hisse hisse, LocalDate tarih, FiyatBilgisi fiyat) {
        Optional<KapanisFiyat> mevcutOpt = kapanisFiyatRepository.findByHisseIdAndTarih(hisse.getId(), tarih);

        if (mevcutOpt.isPresent()) {
            KapanisFiyat mevcut = mevcutOpt.get();
            mevcut.setKapanisFiyat(fiyat.kapanis());
            mevcut.setAcilisFiyat(fiyat.acilis());
            mevcut.setYuksekFiyat(fiyat.yuksek());
            mevcut.setDusukFiyat(fiyat.dusuk());
            mevcut.setHacim(fiyat.hacim());
            kapanisFiyatRepository.save(mevcut);
        } else {
            kapanisFiyatRepository.save(KapanisFiyat.builder()
                    .hisse(hisse)
                    .tarih(tarih)
                    .kapanisFiyat(fiyat.kapanis())
                    .acilisFiyat(fiyat.acilis())
                    .yuksekFiyat(fiyat.yuksek())
                    .dusukFiyat(fiyat.dusuk())
                    .hacim(fiyat.hacim())
                    .build());
        }

        log.debug("[Yahoo] {} kapanış: {} TL ({})", hisse.getSembol(), fiyat.kapanis(), tarih);
    }

    private record FiyatBilgisi(
            BigDecimal acilis,
            BigDecimal yuksek,
            BigDecimal dusuk,
            BigDecimal kapanis,
            Long hacim) {}
}
