-- =============================================
-- MIGRATION 5: Başlangıç (Seed) Verisi
-- =============================================

-- Varsayılan kullanıcı (şifre: "portfolio123" bcrypt hash)
INSERT INTO kullanici (ad, email, sifre_hash) VALUES
('Yatırımcı', 'investor@portfolio.local',
 '$2a$10$tGxluXz467gvC.dwN7D89.O9o18KrDb3UeGaK5g0H86jhE4O6fge.');

-- BIST Hisse Senetleri Seed Verisi
INSERT INTO hisseler (sembol, sirket_adi, sektor) VALUES
('THYAO',  'Türk Hava Yolları A.O.',                'Havacılık'),
('GARAN',  'Garanti BBVA Bankası A.Ş.',              'Bankacılık'),
('AKBNK',  'Akbank T.A.Ş.',                          'Bankacılık'),
('EREGL',  'Ereğli Demir ve Çelik Fab. T.A.Ş.',     'Metal'),
('ASELS',  'Aselsan Elektronik San. ve Tic. A.Ş.',  'Savunma'),
('BIMAS',  'BİM Birleşik Mağazalar A.Ş.',            'Perakende'),
('KCHOL',  'Koç Holding A.Ş.',                       'Holding'),
('SISE',   'Türkiye Şişe ve Cam Fab. A.Ş.',          'Cam'),
('TOASO',  'Tofaş Türk Otomobil Fab. A.Ş.',          'Otomotiv'),
('FROTO',  'Ford Otomotiv Sanayi A.Ş.',               'Otomotiv'),
('TUPRS',  'Tüpraş-Türkiye Petrol Rafinerileri A.Ş.','Enerji'),
('SAHOL',  'Hacı Ömer Sabancı Holding A.Ş.',         'Holding'),
('TTKOM',  'Türk Telekomunikasyon A.Ş.',              'Telekomünikasyon'),
('ISCTR',  'Türkiye İş Bankası A.Ş. (C)',             'Bankacılık'),
('HALKB',  'Türkiye Halk Bankası A.Ş.',               'Bankacılık'),
('VAKBN',  'Türkiye Vakıflar Bankası T.A.O.',         'Bankacılık'),
('PETKM',  'Petkim Petrokimya Holding A.Ş.',          'Kimya'),
('ARCLK',  'Arçelik A.Ş.',                            'Dayanıklı Tüketim'),
('KOZAL',  'Koza Altın İşletmeleri A.Ş.',             'Madencilik'),
('PGSUS',  'Pegasus Hava Taşımacılığı A.Ş.',          'Havacılık');

-- Örnek kapanış fiyatları (2024-01-15 tarihi için)
INSERT INTO kapanis_fiyatlari (hisse_id, tarih, kapanis_fiyat, acilis_fiyat, yuksek_fiyat, dusuk_fiyat)
SELECT h.id, '2024-01-15', kf.kapanis, kf.acilis, kf.yuksek, kf.dusuk
FROM hisseler h
JOIN (VALUES
    ('THYAO',  279.00,  275.00, 282.00, 274.00),
    ('GARAN',  122.50,  120.00, 124.00, 119.50),
    ('AKBNK',   45.80,   45.00,  46.20,  44.80),
    ('EREGL',   62.25,   61.00,  63.50,  60.75),
    ('ASELS',  195.00,  192.00, 197.00, 191.00),
    ('BIMAS',  432.00,  428.00, 435.00, 427.00),
    ('KCHOL',  178.00,  175.00, 180.00, 174.50),
    ('SISE',    46.50,   46.00,  47.00,  45.80),
    ('TOASO',  387.50,  383.00, 390.00, 382.00),
    ('FROTO', 1025.00, 1010.00,1035.00,1008.00),
    ('TUPRS',  215.00,  212.00, 217.50, 211.00),
    ('SAHOL',   73.80,   72.50,  74.50,  72.20),
    ('TTKOM',   28.76,   28.50,  29.00,  28.40),
    ('ISCTR',   19.45,   19.20,  19.60,  19.10),
    ('HALKB',   16.80,   16.50,  17.00,  16.40),
    ('VAKBN',   22.30,   22.00,  22.60,  21.90),
    ('PETKM',   38.40,   38.00,  38.90,  37.80),
    ('ARCLK',  213.50,  210.00, 215.00, 209.00),
    ('KOZAL', 1480.00, 1460.00,1495.00,1458.00),
    ('PGSUS',  753.00,  745.00, 760.00, 744.00)
) AS kf(sembol, kapanis, acilis, yuksek, dusuk) ON h.sembol = kf.sembol;
