'use client';

import { useEffect, useState, useCallback } from 'react';
import { toast } from 'sonner';
import { CheckCircle, Loader2, Search, Zap } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { EmptyState } from '@/components/shared/EmptyState';
import api from '@/lib/api';
import { bugunTarih, formatTL, formatTarihKisa } from '@/lib/utils';
import type { PozisyonDto, KapanisFiyatDto, ApiResponse } from '@/types';

interface FiyatRow {
  sembol: string;
  sirketAdi: string;
  kapanisFiyat: string;
  acilisFiyat: string;
  yuksekFiyat: string;
  dusukFiyat: string;
  kaydedildi: boolean;
  mevcutKapanisStr?: string;
}

export default function KapanisFiyatlariPage() {
  const [tarih, setTarih] = useState(bugunTarih());
  const [rows, setRows] = useState<FiyatRow[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [otoCekiyor, setOtoCekiyor] = useState(false);

  const fetchVeriler = useCallback(async () => {
    setLoading(true);
    try {
      const [pozRes, fiyatRes] = await Promise.all([
        api.get<ApiResponse<PozisyonDto[]>>('/portfoy/pozisyonlar'),
        api.get<ApiResponse<KapanisFiyatDto[]>>(`/kapanis-fiyatlari/${tarih}`).catch(() => ({ data: { data: [] } })),
      ]);

      const mevcutMap = new Map<string, KapanisFiyatDto>(
        (fiyatRes.data.data as KapanisFiyatDto[]).map((f) => [f.sembol, f])
      );

      const newRows: FiyatRow[] = (pozRes.data.data as PozisyonDto[]).map((p) => {
        const m = mevcutMap.get(p.sembol);
        return {
          sembol: p.sembol,
          sirketAdi: p.sirketAdi,
          kapanisFiyat: m ? String(m.kapanisFiyat) : '',
          acilisFiyat: m?.acilisFiyat ? String(m.acilisFiyat) : '',
          yuksekFiyat: m?.yuksekFiyat ? String(m.yuksekFiyat) : '',
          dusukFiyat: m?.dusukFiyat ? String(m.dusukFiyat) : '',
          kaydedildi: !!m,
          mevcutKapanisStr: p.sonKapanisFiyat ? formatTL(p.sonKapanisFiyat) : undefined,
        };
      });
      setRows(newRows);
    } catch {
      toast.error('Veriler yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, [tarih]);

  useEffect(() => { fetchVeriler(); }, [fetchVeriler]);

  const updateRow = (sembol: string, field: keyof FiyatRow, value: string) => {
    setRows((prev) => prev.map((r) => r.sembol === sembol ? { ...r, [field]: value } : r));
  };

  const handleOtoCek = async () => {
    setOtoCekiyor(true);
    try {
      const res = await api.post<ApiResponse<{ guncellenenSayisi: number }>>('/kapanis-fiyatlari/yahoo-cek', { tarih });
      const sayi = res.data.data?.guncellenenSayisi ?? 0;
      toast.success(`${sayi} hisse güncellendi`);
      await fetchVeriler();
    } catch {
      toast.error('Fiyat çekme başarısız');
    } finally {
      setOtoCekiyor(false);
    }
  };

  const handleKaydet = async () => {
    const eksik = rows.filter((r) => !r.kapanisFiyat || isNaN(Number(r.kapanisFiyat)));
    if (eksik.length > 0) {
      toast.error(`Kapanış fiyatı eksik: ${eksik.map((r) => r.sembol).join(', ')}`);
      return;
    }
    setSaving(true);
    try {
      const payload = rows.map((r) => ({
        sembol: r.sembol,
        tarih,
        kapanisFiyat: Number(r.kapanisFiyat),
        acilisFiyat: r.acilisFiyat ? Number(r.acilisFiyat) : undefined,
        yuksekFiyat: r.yuksekFiyat ? Number(r.yuksekFiyat) : undefined,
        dusukFiyat: r.dusukFiyat ? Number(r.dusukFiyat) : undefined,
      }));
      await api.post('/kapanis-fiyatlari/toplu', payload);
      toast.success('Fiyatlar kaydedildi!');
      setRows((prev) => prev.map((r) => ({ ...r, kaydedildi: true })));
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Kayıt başarısız';
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Kapanış Fiyatları</h1>
        <p className="text-sm text-muted-foreground">Günsonu öncesi fiyat girişi</p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-end gap-3">
            <div className="space-y-1">
              <Label>Tarih</Label>
              <Input
                type="date"
                value={tarih}
                onChange={(e) => setTarih(e.target.value)}
                className="w-44"
              />
            </div>
            <Button variant="outline" onClick={fetchVeriler} disabled={loading}>
              <Search className="mr-2 h-4 w-4" />
              Getir
            </Button>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="space-y-2 p-4">
              {[...Array(5)].map((_, i) => <Skeleton key={i} className="h-12 w-full" />)}
            </div>
          ) : rows.length === 0 ? (
            <EmptyState icon="📈" title="Portföyde pozisyon bulunamadı" />
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-border text-left text-xs text-muted-foreground">
                      <th className="px-4 py-3">Sembol</th>
                      <th className="px-4 py-3">Şirket</th>
                      <th className="px-4 py-3 text-right text-muted-foreground/60">Önceki</th>
                      <th className="px-4 py-3">Açılış</th>
                      <th className="px-4 py-3">
                        Kapanış <span className="text-red-500">*</span>
                      </th>
                      <th className="px-4 py-3">Yüksek</th>
                      <th className="px-4 py-3">Düşük</th>
                      <th className="px-4 py-3">Durum</th>
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map((r) => (
                      <tr key={r.sembol} className="border-b border-border last:border-0">
                        <td className="px-4 py-2 font-mono font-bold">{r.sembol}</td>
                        <td className="px-4 py-2 text-muted-foreground">{r.sirketAdi}</td>
                        <td className="px-4 py-2 text-right text-xs text-muted-foreground/60 tabular-nums">
                          {r.mevcutKapanisStr ?? '-'}
                        </td>
                        {(['acilisFiyat', 'kapanisFiyat', 'yuksekFiyat', 'dusukFiyat'] as const).map((f) => (
                          <td key={f} className="px-4 py-2">
                            <Input
                              type="number"
                              step="0.0001"
                              placeholder="0.00"
                              value={(r as unknown as Record<string, string>)[f]}
                              onChange={(e) => updateRow(r.sembol, f, e.target.value)}
                              className="h-8 w-28 tabular-nums"
                            />
                          </td>
                        ))}
                        <td className="px-4 py-2">
                          {r.kaydedildi ? (
                            <CheckCircle className="h-4 w-4 text-green-600" />
                          ) : (
                            <span className="text-xs text-muted-foreground">Bekliyor</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="border-t border-border p-4 flex gap-3">
                <Button onClick={handleKaydet} disabled={saving || otoCekiyor}>
                  {saving ? (
                    <><Loader2 className="mr-2 h-4 w-4 animate-spin" />Kaydediliyor...</>
                  ) : (
                    'Tüm Fiyatları Kaydet'
                  )}
                </Button>
                <Button variant="outline" onClick={handleOtoCek} disabled={otoCekiyor || saving}>
                  {otoCekiyor ? (
                    <><Loader2 className="mr-2 h-4 w-4 animate-spin" />Fiyatlar çekiliyor...</>
                  ) : (
                    <><Zap className="mr-2 h-4 w-4" />Fiyatları Otomatik Çek</>
                  )}
                </Button>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
