-- =============================================
-- MIGRATION 6: Multi-User Desteği
-- =============================================

-- portfoy_islemleri: kullanici_id sütunu ekle
ALTER TABLE portfoy_islemleri
    ADD COLUMN IF NOT EXISTS kullanici_id BIGINT REFERENCES kullanici(id);
UPDATE portfoy_islemleri SET kullanici_id = 1 WHERE kullanici_id IS NULL;
ALTER TABLE portfoy_islemleri
    ALTER COLUMN kullanici_id SET NOT NULL;

-- portfoy_pozisyonlari: kullanici_id sütunu ekle, eski unique kaldır, yeni composite unique ekle
ALTER TABLE portfoy_pozisyonlari
    ADD COLUMN IF NOT EXISTS kullanici_id BIGINT REFERENCES kullanici(id);
UPDATE portfoy_pozisyonlari SET kullanici_id = 1 WHERE kullanici_id IS NULL;
ALTER TABLE portfoy_pozisyonlari
    ALTER COLUMN kullanici_id SET NOT NULL;
ALTER TABLE portfoy_pozisyonlari DROP CONSTRAINT IF EXISTS uk_pozisyon_hisse;
ALTER TABLE portfoy_pozisyonlari ADD CONSTRAINT uk_pozisyon_hisse_kullanici
    UNIQUE (hisse_id, kullanici_id);

-- portfoy_gunluk_degisim: kullanici_id sütunu ekle, eski unique kaldır, yeni composite unique ekle
ALTER TABLE portfoy_gunluk_degisim
    ADD COLUMN IF NOT EXISTS kullanici_id BIGINT REFERENCES kullanici(id);
UPDATE portfoy_gunluk_degisim SET kullanici_id = 1 WHERE kullanici_id IS NULL;
ALTER TABLE portfoy_gunluk_degisim
    ALTER COLUMN kullanici_id SET NOT NULL;
ALTER TABLE portfoy_gunluk_degisim DROP CONSTRAINT IF EXISTS uk_gunluk_tarih_hisse;
ALTER TABLE portfoy_gunluk_degisim ADD CONSTRAINT uk_gunluk_tarih_hisse_kullanici
    UNIQUE (tarih, hisse_id, kullanici_id);

-- portfoy_ozet_gunluk: kullanici_id sütunu ekle, eski unique kaldır, yeni composite unique ekle
ALTER TABLE portfoy_ozet_gunluk
    ADD COLUMN IF NOT EXISTS kullanici_id BIGINT REFERENCES kullanici(id);
UPDATE portfoy_ozet_gunluk SET kullanici_id = 1 WHERE kullanici_id IS NULL;
ALTER TABLE portfoy_ozet_gunluk
    ALTER COLUMN kullanici_id SET NOT NULL;
ALTER TABLE portfoy_ozet_gunluk DROP CONSTRAINT IF EXISTS portfoy_ozet_gunluk_tarih_key;
ALTER TABLE portfoy_ozet_gunluk ADD CONSTRAINT uk_ozet_tarih_kullanici
    UNIQUE (tarih, kullanici_id);

-- kullanici: aktif ve email_dogrulandi alanları ekle
ALTER TABLE kullanici
    ADD COLUMN IF NOT EXISTS aktif BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS email_dogrulandi BOOLEAN DEFAULT TRUE;

-- İndeksler
CREATE INDEX IF NOT EXISTS idx_islemler_kullanici ON portfoy_islemleri(kullanici_id);
CREATE INDEX IF NOT EXISTS idx_pozisyonlar_kullanici ON portfoy_pozisyonlari(kullanici_id);
CREATE INDEX IF NOT EXISTS idx_gunluk_kullanici ON portfoy_gunluk_degisim(kullanici_id);
CREATE INDEX IF NOT EXISTS idx_ozet_kullanici ON portfoy_ozet_gunluk(kullanici_id);
