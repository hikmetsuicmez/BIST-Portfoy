-- =============================================
-- MIGRATION 1: Kullanıcı ve Hisse Tabloları
-- =============================================

CREATE TABLE kullanici (
    id            BIGSERIAL PRIMARY KEY,
    ad            VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    sifre_hash    VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE hisseler (
    id            BIGSERIAL PRIMARY KEY,
    sembol        VARCHAR(10)  NOT NULL UNIQUE,
    sirket_adi    VARCHAR(200) NOT NULL,
    sektor        VARCHAR(100),
    piyasa        VARCHAR(20)  DEFAULT 'BIST',
    aktif         BOOLEAN      DEFAULT TRUE,
    created_at    TIMESTAMPTZ  DEFAULT NOW()
);

COMMENT ON TABLE kullanici IS 'Tek kullanıcı sistemi. Sadece bir kayıt bulunur.';
COMMENT ON TABLE hisseler IS 'BIST hisse senetleri master listesi';
COMMENT ON COLUMN hisseler.sembol IS 'Borsa kodu: THYAO, GARAN, AKBNK vb.';
