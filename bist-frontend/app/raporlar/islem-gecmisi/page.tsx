'use client';

import { useState, useCallback } from 'react';
import { ChevronLeft, Search } from 'lucide-react';
import Link from 'next/link';
import { toast } from 'sonner';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { EmptyState } from '@/components/shared/EmptyState';
import api from '@/lib/api';
import { formatTL, formatLot, formatTarihKisa, bugunTarih } from '@/lib/utils';
import type { IslemDto, ApiResponse } from '@/types';

function oneMonthAgo() {
  const d = new Date();
  d.setMonth(d.getMonth() - 1);
  return d.toISOString().split('T')[0];
}

export default function IslemGecmisiPage() {
  const [baslangic, setBaslangic] = useState(oneMonthAgo());
  const [bitis, setBitis] = useState(bugunTarih());
  const [sembol, setSembol] = useState('');
  const [islemTuru, setIslemTuru] = useState('');
  const [islemler, setIslemler] = useState<IslemDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [aramaDone, setAramaDone] = useState(false);

  const fetchIslemler = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (baslangic) params.append('baslangic', baslangic);
      if (bitis) params.append('bitis', bitis);
      if (sembol) params.append('sembol', sembol.toUpperCase());
      if (islemTuru) params.append('islemTuru', islemTuru);
      const res = await api.get<ApiResponse<IslemDto[]>>(`/islemler?${params}`);
      setIslemler(res.data.data);
      setAramaDone(true);
    } catch {
      toast.error('İşlemler yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, [baslangic, bitis, sembol, islemTuru]);

  const toplamAlim = islemler.filter((i) => i.islemTuru === 'ALIM').reduce((s, i) => s + i.toplamTutar, 0);
  const toplamSatim = islemler.filter((i) => i.islemTuru === 'SATIM').reduce((s, i) => s + i.toplamTutar, 0);
  const toplamKomisyon = islemler.reduce((s, i) => s + i.komisyon, 0);

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Link href="/raporlar" className="text-muted-foreground hover:text-foreground">
          <ChevronLeft className="h-5 w-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold">İşlem Geçmişi Raporu</h1>
          <p className="text-sm text-muted-foreground">Tarih aralığına göre filtrele</p>
        </div>
      </div>

      {/* Filtreler */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap gap-4 items-end">
            <div className="space-y-1">
              <Label>Başlangıç</Label>
              <Input type="date" value={baslangic} onChange={(e) => setBaslangic(e.target.value)} className="w-40" />
            </div>
            <div className="space-y-1">
              <Label>Bitiş</Label>
              <Input type="date" value={bitis} onChange={(e) => setBitis(e.target.value)} className="w-40" />
            </div>
            <div className="space-y-1">
              <Label>Sembol</Label>
              <Input
                placeholder="THYAO"
                value={sembol}
                onChange={(e) => setSembol(e.target.value)}
                className="w-28 uppercase"
              />
            </div>
            <div className="space-y-1">
              <Label>İşlem Türü</Label>
              <select
                value={islemTuru}
                onChange={(e) => setIslemTuru(e.target.value)}
                className="h-9 rounded-md border border-input bg-background px-3 text-sm text-foreground w-28"
              >
                <option value="">Tümü</option>
                <option value="ALIM">ALIM</option>
                <option value="SATIM">SATIM</option>
              </select>
            </div>
            <Button onClick={fetchIslemler} disabled={loading}>
              <Search className="mr-2 h-4 w-4" />
              Filtrele
            </Button>
            <Button
              variant="outline"
              onClick={() => { setSembol(''); setIslemTuru(''); setBaslangic(oneMonthAgo()); setBitis(bugunTarih()); }}
            >
              Temizle
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Özet */}
      {aramaDone && (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          {[
            { label: 'Toplam İşlem', val: String(islemler.length) },
            { label: 'Toplam Alım', val: formatTL(toplamAlim) },
            { label: 'Toplam Satım', val: formatTL(toplamSatim) },
            { label: 'Toplam Komisyon', val: formatTL(toplamKomisyon) },
          ].map(({ label, val }) => (
            <Card key={label}>
              <CardContent className="p-4">
                <p className="text-xs text-muted-foreground">{label}</p>
                <p className="mt-1 text-base font-bold">{val}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Tablo */}
      <Card>
        <CardContent className="p-0">
          {loading ? (
            <div className="space-y-2 p-4">
              {[...Array(6)].map((_, i) => <Skeleton key={i} className="h-10 w-full" />)}
            </div>
          ) : !aramaDone ? (
            <EmptyState icon="🔍" title="Filtre uygulayarak arama yapın" />
          ) : islemler.length === 0 ? (
            <EmptyState icon="📋" title="Sonuç bulunamadı" />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border text-left text-xs text-muted-foreground">
                    <th className="px-4 py-3">Tarih</th>
                    <th className="px-4 py-3">Sembol</th>
                    <th className="px-4 py-3">Tür</th>
                    <th className="px-4 py-3">Lot</th>
                    <th className="px-4 py-3">Fiyat</th>
                    <th className="px-4 py-3">Komisyon</th>
                    <th className="px-4 py-3">Toplam</th>
                    <th className="px-4 py-3">Notlar</th>
                  </tr>
                </thead>
                <tbody>
                  {islemler.map((i) => (
                    <tr key={i.id} className="border-b border-border last:border-0 hover:bg-muted/50">
                      <td className="px-4 py-3 tabular-nums">{formatTarihKisa(i.tarih)}</td>
                      <td className="px-4 py-3">
                        <Badge variant="outline" className="font-mono font-bold">{i.sembol}</Badge>
                      </td>
                      <td className="px-4 py-3">
                        <Badge variant={i.islemTuru === 'ALIM' ? 'default' : 'destructive'} className="text-xs">
                          {i.islemTuru}
                        </Badge>
                      </td>
                      <td className="px-4 py-3 tabular-nums">{formatLot(i.lot)}</td>
                      <td className="px-4 py-3 tabular-nums">{formatTL(i.fiyat)}</td>
                      <td className="px-4 py-3 tabular-nums">{formatTL(i.komisyon)}</td>
                      <td className="px-4 py-3 tabular-nums font-semibold">{formatTL(i.toplamTutar)}</td>
                      <td className="px-4 py-3 text-muted-foreground">{i.notlar ?? '-'}</td>
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
