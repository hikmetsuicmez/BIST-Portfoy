# BIST Portföy Takip Sistemi

Borsa İstanbul hisse senetleri için kişisel portföy takip uygulaması. Alım-satım işlemlerini kayıt altına alır, günsonu kapanış fiyatlarıyla pozisyonları günceller ve performans raporları üretir.

---

## Özellikler

- **Portföy Yönetimi** — Hisse alım/satım işlemleri, ortalama maliyet ve kar/zarar takibi
- **Günsonu Motoru** — Seçilen tarihe ait kapanış fiyatlarıyla tüm pozisyonları otomatik güncelleme
- **Yahoo Finance Entegrasyonu** — Hisse fiyatlarını otomatik çekme ve doğrulama
- **Raporlar** — Portföy özeti, işlem geçmişi ve performans analizi
- **JWT Kimlik Doğrulama** — Güvenli oturum yönetimi
- **Karanlık / Aydınlık Tema** — Sistem temasını takip eden arayüz
- **Tek Kullanıcı Mimarisi** — Kişisel kullanıma yönelik tasarım

---

## Teknoloji Yığını

### Backend
| Teknoloji | Versiyon | Kullanım |
|---|---|---|
| Java | 21 | Platform |
| Spring Boot | 3.x | REST API çatısı |
| Spring Security + JWT | — | Kimlik doğrulama |
| PostgreSQL | — | Ana veritabanı |
| Flyway | — | Veritabanı migration |
| Yahoo Finance API | — | Fiyat verisi |

### Frontend
| Teknoloji | Versiyon | Kullanım |
|---|---|---|
| Next.js | 15 | React çatısı |
| TypeScript | 5 | Tip güvenliği |
| Tailwind CSS | 4 | Stil |
| shadcn/ui + Radix UI | — | Bileşen kütüphanesi |
| Recharts | 2 | Grafikler |
| Zustand | 5 | State yönetimi |
| React Hook Form + Zod | — | Form ve validasyon |
| Axios | — | HTTP istemcisi |

---

## Kurulum

### Gereksinimler

- Java 21+
- Node.js 20+
- PostgreSQL 15+

---

### Backend

```bash
cd bist-backend
```

`.env` dosyasını düzenleyin:

```env
DB_URL=jdbc:postgresql://localhost:5432/bist_portfoy
DB_USERNAME=postgres
DB_PASSWORD=sifreniz
JWT_SECRET=gizli-anahtar-buraya
```

Veritabanını oluşturun:

```sql
CREATE DATABASE bist_portfoy;
```

Uygulamayı başlatın (Flyway migration'lar otomatik çalışır):

```bash
./mvnw spring-boot:run
```

API `http://localhost:8080` adresinde çalışmaya başlar.

---

### Frontend

```bash
cd bist-frontend
npm install
```

`.env.local` dosyası oluşturun:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

Geliştirme sunucusunu başlatın:

```bash
npm run dev
```

Uygulama `http://localhost:3000` adresinde açılır.

---

## Ekran Görüntüleri

> Ekran görüntüleri eklenecek

| Portföy Özeti | İşlem Listesi | Günsonu | Raporlar |
|---|---|---|---|
| ![portfoy](docs/screenshots/portfoy.png) | ![islemler](docs/screenshots/islemler.png) | ![gunsonu](docs/screenshots/gunsonu.png) | ![raporlar](docs/screenshots/raporlar.png) |

---

## API Uç Noktaları

| Grup | Endpoint | Açıklama |
|---|---|---|
| Auth | `POST /api/auth/login` | Giriş, JWT döner |
| Hisseler | `GET /api/hisseler` | Tüm hisseler |
| İşlemler | `POST /api/islemler` | Yeni işlem ekle |
| Portföy | `GET /api/portfoy/ozet` | Portföy özeti |
| Günsonu | `POST /api/gunsonu/calistir` | Günsonu motorunu çalıştır |
| Raporlar | `GET /api/raporlar/performans` | Performans raporu |

---

## Proje Yapısı

```
BIST-Portfoy-Clean/
├── bist-backend/          # Spring Boot REST API
│   └── src/main/java/com/portfolio/
│       ├── controller/    # REST endpoint'leri
│       ├── service/       # İş mantığı
│       ├── entity/        # JPA varlıkları
│       ├── repository/    # Veritabanı erişimi
│       ├── security/      # JWT filtresi
│       └── dto/           # Request / Response sınıfları
└── bist-frontend/         # Next.js arayüzü
    └── app/
        ├── portfoy/       # Portföy sayfaları
        ├── islemler/      # İşlem yönetimi
        ├── gunsonu/       # Günsonu işlemi
        └── raporlar/      # Raporlar
```

---

## Lisans

[MIT](LICENSE)
