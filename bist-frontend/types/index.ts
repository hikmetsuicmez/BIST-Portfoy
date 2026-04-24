// ====== AUTH ======
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  kullanici: KullaniciDto;
}

export interface KullaniciDto {
  id: number;
  ad: string;
  email: string;
}

// ====== HİSSE ======
export interface HisseDto {
  id: number;
  sembol: string;
  sirketAdi: string;
  sektor: string;
  piyasa: string;
  aktif: boolean;
}

export interface KapanisFiyatDto {
  id: number;
  sembol: string;
  tarih: string;
  kapanisFiyat: number;
  acilisFiyat?: number;
  yuksekFiyat?: number;
  dusukFiyat?: number;
  hacim?: number;
}

export interface KapanisFiyatRequest {
  sembol: string;
  tarih: string;
  kapanisFiyat: number;
  acilisFiyat?: number;
  yuksekFiyat?: number;
  dusukFiyat?: number;
}

// ====== İŞLEM ======
export type IslemTuru = 'ALIM' | 'SATIM';

export interface IslemDto {
  id: number;
  sembol: string;
  sirketAdi: string;
  islemTuru: IslemTuru;
  tarih: string;
  lot: number;
  fiyat: number;
  komisyon: number;
  toplamTutar: number;
  notlar?: string;
}

export interface IslemRequest {
  sembol: string;
  islemTuru: IslemTuru;
  tarih: string;
  lot: number;
  fiyat: number;
  komisyon?: number;
  notlar?: string;
}

// ====== PORTFÖY ======
export interface PozisyonDto {
  sembol: string;
  sirketAdi: string;
  sektor: string;
  toplamLot: number;
  ortalamaMaliyet: number;
  toplamMaliyet: number;
  sonKapanisFiyat?: number;
  guncelDeger?: number;
  karZararTl?: number;
  karZararYuzde?: number;
  ilkAlimTarihi: string;
}

export interface PortfoyOzetDto {
  toplamMaliyet: number;
  toplamDeger: number;
  toplamKarZararTl: number;
  toplamKarZararYuzde: number;
  pozisyonSayisi: number;
  sonGunsonuTarihi?: string;
  gunlukDegisimTl?: number;
  gunlukDegisimYuzde?: number;
}

// ====== GÜNSONU ======
export interface GunsonuSonucDto {
  tarih: string;
  islemSayisi: number;
  toplamDeger: number;
  basarili: boolean;
  mesaj?: string;
}

export interface GunsonuDurumDto {
  tarih: string;
  tamamlandi: boolean;
  tamamlanmaZamani?: string;
  pozisyonSayisi?: number;
}

export interface EksikFiyatDto {
  sembol: string;
  sirketAdi: string;
}

// ====== RAPOR ======
export interface OzetGunlukDto {
  tarih: string;
  toplamMaliyet: number;
  toplamDeger: number;
  toplamKarZararTl: number;
  toplamKarZararYuzde: number;
  gunlukDegisimTl?: number;
  gunlukDegisimYuzde?: number;
  tamamlanmaZamani?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}
