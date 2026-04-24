-- =============================================
-- MIGRATION 2: Fiyat ve İşlem Tabloları
-- =============================================

CREATE TABLE kapanis_fiyatlari (
    id            BIGSERIAL PRIMARY KEY,
    hisse_id      BIGINT       NOT NULL REFERENCES hisseler(id) ON DELETE RESTRICT,
    tarih         DATE         NOT NULL,
    kapanis_fiyat NUMERIC(12,4) NOT NULL,
    acilis_fiyat  NUMERIC(12,4),
    yuksek_fiyat  NUMERIC(12,4),
    dusuk_fiyat   NUMERIC(12,4),
    hacim         BIGINT,
    created_at    TIMESTAMPTZ  DEFAULT NOW(),
    CONSTRAINT uk_kapanis_hisse_tarih UNIQUE (hisse_id, tarih)
);

CREATE INDEX idx_kapanis_tarih         ON kapanis_fiyatlari(tarih DESC);
CREATE INDEX idx_kapanis_hisse_tarih   ON kapanis_fiyatlari(hisse_id, tarih DESC);

CREATE TYPE islem_turu AS ENUM ('ALIM', 'SATIM');

CREATE TABLE portfoy_islemleri (
    id              BIGSERIAL PRIMARY KEY,
    hisse_id        BIGINT        NOT NULL REFERENCES hisseler(id) ON DELETE RESTRICT,
    islem_turu      islem_turu    NOT NULL,
    tarih           DATE          NOT NULL,
    lot             NUMERIC(12,4) NOT NULL CHECK (lot > 0),
    fiyat           NUMERIC(12,4) NOT NULL CHECK (fiyat > 0),
    komisyon        NUMERIC(10,4) NOT NULL DEFAULT 0 CHECK (komisyon >= 0),
    toplam_tutar    NUMERIC(16,4) NOT NULL,
    notlar          TEXT,
    created_at      TIMESTAMPTZ   DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   DEFAULT NOW()
);

CREATE INDEX idx_islem_hisse ON portfoy_islemleri(hisse_id);
CREATE INDEX idx_islem_tarih ON portfoy_islemleri(tarih DESC);

COMMENT ON COLUMN portfoy_islemleri.toplam_tutar IS 'ALIM: lot*fiyat+komisyon | SATIM: lot*fiyat-komisyon';
