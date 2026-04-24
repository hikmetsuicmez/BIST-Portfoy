'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { toast } from 'sonner';
import { ChevronLeft, Loader2, AlertTriangle } from 'lucide-react';
import Link from 'next/link';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import api from '@/lib/api';
import { useHisseler } from '@/hooks/useHisseler';
import { formatTL, formatLot } from '@/lib/utils';
import type { IslemDto, IslemRequest, PozisyonDto, KapanisFiyatDto, ApiResponse } from '@/types';

function oncekiGun(t: string) {
  const d = new Date(t);
  d.setUTCDate(d.getUTCDate() - 1);
  return d.toISOString().split('T')[0];
}

const islemSchema = z.object({
  sembol: z.string().min(1, 'Hisse seçin'),
  islemTuru: z.enum(['ALIM', 'SATIM']),
  tarih: z.string().min(1, 'Tarih seçin'),
  lot: z.coerce.number().positive("Lot 0'dan büyük olmalı"),
  fiyat: z.coerce.number().positive("Fiyat 0'dan büyük olmalı"),
  komisyon: z.coerce.number().min(0).optional(),
  notlar: z.string().optional(),
});

type IslemForm = z.infer<typeof islemSchema>;

export default function IslemDuzenlePage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;

  const { hisseler } = useHisseler();
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [mevcutLot, setMevcutLot] = useState<number | null>(null);
  const [kapanisFiyatIpucu, setKapanisFiyatIpucu] = useState<number | null>(null);
  // Mevcut işlemin komisyonu korunur; lot/fiyat değişirse kullanıcı "Otomatik hesapla"ya basabilir
  const [komisyonManuel, setKomisyonManuel] = useState(true);

  const {
    register,
    handleSubmit,
    watch,
    control,
    reset,
    setValue,
    formState: { errors },
  } = useForm<IslemForm>({
    resolver: zodResolver(islemSchema),
  });

  useEffect(() => {
    api
      .get<ApiResponse<IslemDto>>(`/islemler/${id}`)
      .then((r) => {
        const d = r.data.data;
        reset({
          sembol: d.sembol,
          islemTuru: d.islemTuru,
          tarih: d.tarih,
          lot: d.lot,
          fiyat: d.fiyat,
          komisyon: d.komisyon,
          notlar: d.notlar ?? '',
        });
      })
      .catch(() => {
        toast.error('İşlem bulunamadı');
        router.push('/islemler');
      })
      .finally(() => setFetchLoading(false));
  }, [id, reset, router]);

  const lot = watch('lot');
  const fiyat = watch('fiyat');
  const komisyon = watch('komisyon') ?? 0;
  const sembol = watch('sembol');
  const tarih = watch('tarih');
  const islemTuru = watch('islemTuru');

  const toplamTutar = lot && fiyat ? lot * fiyat + (komisyon || 0) : null;

  useEffect(() => {
    if (!sembol) { setMevcutLot(null); return; }
    api
      .get<ApiResponse<PozisyonDto>>(`/portfoy/hisse/${sembol}`)
      .then((r) => setMevcutLot(r.data.data.toplamLot))
      .catch(() => setMevcutLot(null));
  }, [sembol]);

  useEffect(() => {
    if (!sembol || !tarih) { setKapanisFiyatIpucu(null); return; }
    api
      .get<ApiResponse<KapanisFiyatDto>>(`/kapanis-fiyatlari/${sembol}/${oncekiGun(tarih)}`)
      .then((r) => setKapanisFiyatIpucu(r.data.data.kapanisFiyat))
      .catch(() => setKapanisFiyatIpucu(null));
  }, [sembol, tarih]);

  useEffect(() => {
    if (komisyonManuel || !(lot > 0) || !(fiyat > 0)) return;
    setValue('komisyon', parseFloat((lot * fiyat * 0.0015).toFixed(2)));
  }, [lot, fiyat, komisyonManuel, setValue]);

  const onSubmit = async (data: IslemForm) => {
    setLoading(true);
    try {
      const req: IslemRequest = {
        sembol: data.sembol,
        islemTuru: data.islemTuru,
        tarih: data.tarih,
        lot: data.lot,
        fiyat: data.fiyat,
        komisyon: data.komisyon,
        notlar: data.notlar,
      };
      await api.put(`/islemler/${id}`, req);
      toast.success('İşlem güncellendi!');
      router.push('/islemler');
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'İşlem güncellenemedi';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  const yetersizLot =
    islemTuru === 'SATIM' && mevcutLot !== null && lot && lot > mevcutLot;

  if (fetchLoading) {
    return (
      <div className="space-y-6 max-w-2xl">
        <Skeleton className="h-8 w-48" />
        <Card>
          <CardContent className="pt-6 space-y-4">
            {Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-10 w-full" />
            ))}
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <div className="flex items-center gap-3">
        <Link href="/islemler" className="text-muted-foreground hover:text-foreground">
          <ChevronLeft className="h-5 w-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold">İşlemi Düzenle</h1>
          <p className="text-sm text-muted-foreground">İşlem #{id}</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">İşlem Bilgileri</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Hisse</Label>
                <Controller
                  name="sembol"
                  control={control}
                  render={({ field }) => (
                    <Select onValueChange={field.onChange} value={field.value}>
                      <SelectTrigger>
                        <SelectValue placeholder="Hisse seçin" />
                      </SelectTrigger>
                      <SelectContent>
                        {hisseler.filter((h) => h.aktif).map((h) => (
                          <SelectItem key={h.sembol} value={h.sembol}>
                            {h.sembol} — {h.sirketAdi}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.sembol && (
                  <p className="text-xs text-red-600">{errors.sembol.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label>İşlem Türü</Label>
                <Controller
                  name="islemTuru"
                  control={control}
                  render={({ field }) => (
                    <Select onValueChange={field.onChange} value={field.value}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ALIM">ALIM</SelectItem>
                        <SelectItem value="SATIM">SATIM</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Tarih</Label>
                <Input type="date" {...register('tarih')} />
                {errors.tarih && (
                  <p className="text-xs text-red-600">{errors.tarih.message}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label>Lot</Label>
                <Input type="number" step="0.0001" placeholder="0" {...register('lot')} />
                {errors.lot && (
                  <p className="text-xs text-red-600">{errors.lot.message}</p>
                )}
                {yetersizLot && (
                  <p className="text-xs text-red-600 flex items-center gap-1">
                    <AlertTriangle className="h-3 w-3" />
                    Yetersiz lot! Mevcut: {formatLot(mevcutLot!)}
                  </p>
                )}
                {islemTuru === 'SATIM' && mevcutLot !== null && !yetersizLot && (
                  <p className="text-xs text-muted-foreground">
                    Mevcut: {formatLot(mevcutLot)}
                  </p>
                )}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Fiyat (₺)</Label>
                <Input
                  type="number"
                  step="0.0001"
                  placeholder={
                    kapanisFiyatIpucu
                      ? `Örn: ${kapanisFiyatIpucu.toLocaleString('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 4 })} (${tarih ? oncekiGun(tarih).split('-').slice(1).reverse().join('.') : ''} kapanış)`
                      : '0.00'
                  }
                  {...register('fiyat')}
                />
                {errors.fiyat && (
                  <p className="text-xs text-red-600">{errors.fiyat.message}</p>
                )}
                {kapanisFiyatIpucu && (
                  <p className="text-xs text-muted-foreground">
                    Önceki kapanış: ₺{kapanisFiyatIpucu.toLocaleString('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 4 })} — gerçek işlem fiyatınızı girin
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <Label>Komisyon (₺)</Label>
                <Input
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                  {...register('komisyon', { onChange: () => setKomisyonManuel(true) })}
                />
                {lot > 0 && fiyat > 0 && (
                  <p className="text-xs text-muted-foreground">
                    {komisyonManuel ? (
                      <>
                        Manuel komisyon —{' '}
                        <button
                          type="button"
                          className="underline hover:text-foreground"
                          onClick={() => setKomisyonManuel(false)}
                        >
                          Otomatik hesapla
                        </button>
                      </>
                    ) : (
                      'Tahmini komisyon (%0,15) — değiştirebilirsiniz'
                    )}
                  </p>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <Label>Notlar</Label>
              <Textarea
                placeholder="İsteğe bağlı not..."
                {...register('notlar')}
                rows={2}
              />
            </div>

            {toplamTutar !== null && (
              <div className="rounded-lg bg-muted px-4 py-3">
                <span className="text-sm text-muted-foreground">Toplam Tutar: </span>
                <span className="text-base font-bold">{formatTL(toplamTutar)}</span>
                <span className="text-xs text-muted-foreground ml-1">(komisyon dahil)</span>
              </div>
            )}

            <div className="flex gap-3 pt-2">
              <Button type="submit" disabled={loading || !!yetersizLot}>
                {loading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Güncelleniyor...
                  </>
                ) : (
                  'Değişiklikleri Kaydet'
                )}
              </Button>
              <Button variant="outline" asChild>
                <Link href="/islemler">İptal</Link>
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
