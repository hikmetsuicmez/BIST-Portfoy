-- =============================================
-- MIGRATION 3: Portföy Pozisyon ve Snapshot Tabloları
-- =============================================

CREATE TABLE portfoy_pozisyonlari (
    id                  BIGSERIAL PRIMARY KEY,
    hisse_id            BIGINT        NOT NULL REFERENCES hisseler(id) ON DELETE RESTRICT,
    toplam_lot          NUMERIC(12,4) NOT NULL DEFAULT 0,
    ortalama_maliyet    NUMERIC(12,4) NOT NULL DEFAULT 0,
    toplam_maliyet      NUMERIC(16,4) NOT NULL DEFAULT 0,
    ilk_alim_tarihi     DATE,
    son_islem_tarihi    DATE,
    updated_at          TIMESTAMPTZ   DEFAULT NOW(),
    CONSTRAINT uk_pozisyon_hisse UNIQUE (hisse_id)
);

CREATE TABLE portfoy_gunluk_degisim (
    id                      BIGSERIAL PRIMARY KEY,
    tarih                   DATE          NOT NULL,
    hisse_id                BIGINT        NOT NULL REFERENCES hisseler(id),

    lot                     NUMERIC(12,4) NOT NULL,
    ortalama_maliyet        NUMERIC(12,4) NOT NULL,
    toplam_maliyet          NUMERIC(16,4) NOT NULL,

    kapanis_fiyat           NUMERIC(12,4) NOT NULL,
    onceki_kapanis_fiyat    NUMERIC(12,4),

    guncel_deger            NUMERIC(16,4) NOT NULL,
    gunluk_degisim_tl       NUMERIC(16,4),
    gunluk_degisim_yuzde    NUMERIC(8,4),
    toplam_kar_zarar_tl     NUMERIC(16,4),
    toplam_kar_zarar_yuzde  NUMERIC(8,4),

    created_at              TIMESTAMPTZ   DEFAULT NOW(),
    CONSTRAINT uk_gunluk_tarih_hisse UNIQUE (tarih, hisse_id)
);

CREATE TABLE portfoy_ozet_gunluk (
    id                      BIGSERIAL PRIMARY KEY,
    tarih                   DATE          NOT NULL UNIQUE,
    toplam_maliyet          NUMERIC(16,4) NOT NULL,
    toplam_deger            NUMERIC(16,4) NOT NULL,
    toplam_kar_zarar_tl     NUMERIC(16,4) NOT NULL,
    toplam_kar_zarar_yuzde  NUMERIC(8,4)  NOT NULL,
    gunluk_degisim_tl       NUMERIC(16,4),
    gunluk_degisim_yuzde    NUMERIC(8,4),
    pozisyon_sayisi         INTEGER,
    gunsonu_tamamlandi      BOOLEAN       DEFAULT FALSE,
    tamamlanma_zamani       TIMESTAMPTZ,
    created_at              TIMESTAMPTZ   DEFAULT NOW()
);

CREATE INDEX idx_gunluk_tarih         ON portfoy_gunluk_degisim(tarih DESC);
CREATE INDEX idx_gunluk_hisse_tarih   ON portfoy_gunluk_degisim(hisse_id, tarih DESC);
CREATE INDEX idx_ozet_tarih           ON portfoy_ozet_gunluk(tarih DESC);
