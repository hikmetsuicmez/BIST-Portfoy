-- =============================================
-- MIGRATION 7: Temettü Takip Tablosu
-- =============================================

CREATE TABLE IF NOT EXISTS temettular (
    id                          BIGSERIAL PRIMARY KEY,
    hisse_id                    BIGINT          NOT NULL REFERENCES hisseler(id) ON DELETE RESTRICT,
    kullanici_id                BIGINT          NOT NULL REFERENCES kullanici(id) ON DELETE CASCADE,
    yil                         INTEGER         NOT NULL,
    hisse_basi_brut_temmettu    NUMERIC(10, 4),
    stopaj_orani                NUMERIC(5, 4)   DEFAULT 0.10,
    hisse_basi_net_temmettu     NUMERIC(10, 4),
    odeme_tarihi                DATE            NOT NULL,
    lot                         NUMERIC(12, 4),
    toplam_brut                 NUMERIC(16, 4),
    toplam_net                  NUMERIC(16, 4),
    notlar                      TEXT,
    created_at                  TIMESTAMPTZ     DEFAULT NOW(),
    CONSTRAINT uk_temmettu_hisse_kullanici_yil UNIQUE (hisse_id, kullanici_id, yil)
);

CREATE INDEX IF NOT EXISTS idx_temettular_kullanici    ON temettular(kullanici_id);
CREATE INDEX IF NOT EXISTS idx_temettular_hisse        ON temettular(hisse_id);
CREATE INDEX IF NOT EXISTS idx_temettular_yil          ON temettular(kullanici_id, yil);
