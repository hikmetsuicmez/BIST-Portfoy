-- =============================================
-- MIGRATION 4: Rapor Tablosu
-- =============================================

CREATE TYPE rapor_turu AS ENUM (
    'PORTFOY_OZET',
    'ISLEM_GECMISI',
    'PERFORMANS_ANALIZI',
    'HISSE_DETAY'
);

CREATE TABLE raporlar (
    id              BIGSERIAL PRIMARY KEY,
    rapor_turu      rapor_turu   NOT NULL,
    baslik          VARCHAR(200) NOT NULL,
    parametreler    JSONB,
    dosya_yolu      VARCHAR(500),
    created_at      TIMESTAMPTZ  DEFAULT NOW()
);

COMMENT ON COLUMN raporlar.parametreler IS 'Rapor filtre parametreleri. Örn: {"baslangic":"2024-01-01","bitis":"2024-12-31"}';
