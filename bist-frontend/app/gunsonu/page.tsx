'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { toast } from 'sonner';
import { CheckCircle, AlertTriangle, Loader2, Play } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import { KarZararBadge, YuzdeBadge } from '@/components/shared/KarZararBadge';
import { EmptyState } from '@/components/shared/EmptyState';
import api from '@/lib/api';
import { bugunTarih, formatTL, formatTarih, formatTarihKisa } from '@/lib/utils';
import type { GunsonuDurumDto, GunsonuSonucDto, OzetGunlukDto, ApiResponse } from '@/types';

export default function GunsonuPage() {
  const [tarih, setTarih] = useState(bugunTarih());
  const [durum, setDurum] = useState<GunsonuDurumDto | null>(null);
  const [durumLoading, setDurumLoading] = useState(false);
  const [calistiriliyor, setCalistiriliyor] = useState(false);
  const [sonuc, setSonuc] = useState<GunsonuSonucDto | null>(null);
  const [gecmis, setGecmis] = useState<OzetGunlukDto[]>([]);
  const [gecmisLoading, setGecmisLoading] = useState(true);

  const fetchDurum = async (t: string) => {
    setDurumLoading(true);
    setSonuc(null);
    try {
      const res = await api.get<ApiResponse<GunsonuDurumDto>>(`/gunsonu/durum/${t}`);
      setDurum(res.data.data);
    } catch {
      setDurum(null);
    } finally {
      setDurumLoading(false);
    }
  };

  useEffect(() => {
    fetchDurum(tarih);
    setGecmisLoading(true);
    api
      .get<ApiResponse<OzetGunlukDto[]>>('/gunsonu/gecmis')
      .then((r) => setGecmis(r.data.data))
      .catch(() => {})
      .finally(() => setGecmisLoading(false));
  }, []);

  const handleTarihChange = (t: string) => {
    setTarih(t);
    fetchDurum(t);
  };

  const handleCalistir = async () => {
    setCalistiriliyor(true);
    try {
      const res = await api.post<ApiResponse<GunsonuSonucDto>>('/gunsonu/calistir', { tarih });
      setSonuc(res.data.data);
      toast.success('Günsonu tamamlandı!');
      fetchDurum(tarih);
      const gecmisRes = await api.get<ApiResponse<OzetGunlukDto[]>>('/gunsonu/gecmis');
      setGecmis(gecmisRes.data.data);
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Günsonu başarısız';
      toast.error(msg);
    } finally {
      setCalistiriliyor(false);
    }
  };

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h1 className="text-2xl font-bold">Günsonu</h1>
        <p className="text-sm text-muted-foreground">Günlük portföy değerleme süreci</p>
      </div>

      {/* Günsonu Çalıştır */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Günsonu Çalıştır</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1">
            <Label>Tarih</Label>
            <Input
              type="date"
              value={tarih}
              onChange={(e) => handleTarihChange(e.target.value)}
              className="w-44"
            />
          </div>

          {/* Durum */}
          {durumLoading ? (
            <Skeleton className="h-16 w-full" />
          ) : durum?.tamamlandi ? (
            <div className="flex items-center gap-3 rounded-lg border border-green-600/30 bg-green-600/10 px-4 py-3">
              <CheckCircle className="h-5 w-5 text-green-600 shrink-0" />
              <div>
                <p className="text-sm font-medium text-green-600">
                  {formatTarih(tarih)} günsonu tamamlandı
                </p>
                {durum.tamamlanmaZamani && (
                  <p className="text-xs text-muted-foreground">
                    Tamamlanma: {new Date(durum.tamamlanmaZamani).toLocaleString('tr-TR')}
                  </p>
                )}
              </div>
            </div>
          ) : (
            <div className="flex items-start gap-3 rounded-lg border border-yellow-600/30 bg-yellow-600/10 px-4 py-3">
              <AlertTriangle className="h-5 w-5 text-yellow-500 shrink-0 mt-0.5" />
              <div className="flex-1">
                <p className="text-sm font-medium text-yellow-500">
                  {formatTarih(tarih)} günsonu henüz tamamlanmadı
                </p>
                <p className="text-xs text-muted-foreground mt-1">
                  Devam etmeden önce tüm kapanış fiyatlarının girildiğinden emin olun.
                </p>
              </div>
              <Button asChild size="sm" variant="outline" className="border-yellow-600/50 text-yellow-500 shrink-0">
                <Link href="/kapanis-fiyatlari">Fiyat Gir</Link>
              </Button>
            </div>
          )}

          {/* Sonuç */}
          {sonuc && (
            <div className="rounded-lg border border-green-600/30 bg-green-600/10 px-4 py-3 space-y-1">
              <p className="text-sm font-semibold text-green-600">Günsonu tamamlandı!</p>
              <p className="text-sm">Toplam Değer: <span className="font-bold">{formatTL(sonuc.toplamDeger)}</span></p>
              {sonuc.mesaj && <p className="text-xs text-muted-foreground">{sonuc.mesaj}</p>}
            </div>
          )}

          <Button
            onClick={handleCalistir}
            disabled={calistiriliyor || !!durum?.tamamlandi}
          >
            {calistiriliyor ? (
              <><Loader2 className="mr-2 h-4 w-4 animate-spin" />Hesaplanıyor...</>
            ) : (
              <><Play className="mr-2 h-4 w-4" />Günsonu Çalıştır</>
            )}
          </Button>
        </CardContent>
      </Card>

      {/* Geçmiş */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Günsonu Geçmişi</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {gecmisLoading ? (
            <div className="space-y-2 p-4">
              {[...Array(5)].map((_, i) => <Skeleton key={i} className="h-10 w-full" />)}
            </div>
          ) : gecmis.length === 0 ? (
            <EmptyState icon="📅" title="Henüz günsonu kaydı yok" />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border text-left text-xs text-muted-foreground">
                    <th className="px-4 py-3">Tarih</th>
                    <th className="px-4 py-3">Toplam Değer</th>
                    <th className="px-4 py-3">Toplam K/Z</th>
                    <th className="px-4 py-3">Günlük Değişim</th>
                    <th className="px-4 py-3">Durum</th>
                  </tr>
                </thead>
                <tbody>
                  {gecmis.map((g) => (
                    <tr key={g.tarih} className="border-b border-border last:border-0 hover:bg-muted/50">
                      <td className="px-4 py-3 tabular-nums">{formatTarihKisa(g.tarih)}</td>
                      <td className="px-4 py-3 tabular-nums font-semibold">{formatTL(g.toplamDeger)}</td>
                      <td className="px-4 py-3">
                        <KarZararBadge value={g.toplamKarZararTl} showIcon={false} />
                      </td>
                      <td className="px-4 py-3 flex items-center gap-2">
                        <KarZararBadge value={g.gunlukDegisimTl} showIcon />
                        <YuzdeBadge value={g.gunlukDegisimYuzde} />
                      </td>
                      <td className="px-4 py-3">
                        <Badge variant="outline" className="text-green-600 border-green-600/30">
                          Tamamlandı
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
