'use client';

import { useCallback, useEffect, useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { toast } from 'sonner';
import { BadgeDollarSign, Loader2, Plus, Trash2 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Skeleton } from '@/components/ui/skeleton';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { EmptyState } from '@/components/shared/EmptyState';
import api from '@/lib/api';
import { useHisseler } from '@/hooks/useHisseler';
import { formatTL, formatTarihKisa } from '@/lib/utils';
import type {
  TemmettuDto,
  TemmettuOzetDto,
  PozisyonDto,
  ApiResponse,
} from '@/types';

// ─── Schema ───────────────────────────────────────────────────────────────────
const temmettuSchema = z.object({
  sembol: z.string().min(1, 'Hisse seçin'),
  yil: z.coerce
    .number()
    .int()
    .min(2020, 'En az 2020')
    .max(2030, 'En fazla 2030'),
  odemeTarihi: z.string().min(1, 'Tarih seçin'),
  hisseBasiTBrut: z.coerce.number().positive('0\'dan büyük olmalı'),
  stopajOrani: z.coerce.number(),
  notlar: z.string().optional(),
});

type TemmettuForm = z.infer<typeof temmettuSchema>;

// ─── Yeni Temettü Dialog ───────────────────────────────────────────────────────
function YeniTemmettuDialog({ onSaved }: { onSaved: () => void }) {
  const { hisseler } = useHisseler();
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [pozisyon, setPozisyon] = useState<PozisyonDto | null>(null);

  const {
    register,
    handleSubmit,
    watch,
    control,
    reset,
    formState: { errors },
  } = useForm<TemmettuForm>({
    resolver: zodResolver(temmettuSchema),
    defaultValues: { stopajOrani: 0.1, yil: new Date().getFullYear() },
  });

  const sembol = watch('sembol');
  const brut = watch('hisseBasiTBrut') ?? 0;
  const stopaj = watch('stopajOrani') ?? 0.1;

  const hisseBasiNet = brut * (1 - stopaj);
  const lot = pozisyon?.toplamLot ?? 0;
  const tahminiNet = hisseBasiNet * lot;
  const tahminiBrut = brut * lot;

  useEffect(() => {
    if (!sembol) { setPozisyon(null); return; }
    api
      .get<ApiResponse<PozisyonDto>>(`/portfoy/hisse/${sembol}`)
      .then((r) => setPozisyon(r.data.data))
      .catch(() => setPozisyon(null));
  }, [sembol]);

  const onSubmit = async (data: TemmettuForm) => {
    setSaving(true);
    try {
      await api.post('/temettular', {
        sembol: data.sembol,
        yil: data.yil,
        odemeTarihi: data.odemeTarihi,
        hisseBasiTBrut: data.hisseBasiTBrut,
        stopajOrani: data.stopajOrani,
        notlar: data.notlar || undefined,
      });
      toast.success('Temettü kaydedildi');
      reset();
      setOpen(false);
      onSaved();
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message ?? 'Kayıt başarısız';
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm">
          <Plus className="mr-2 h-4 w-4" />
          Yeni Temettü
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Yeni Temettü Ekle</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Hisse */}
          <div className="space-y-1">
            <Label>Hisse</Label>
            <Controller
              name="sembol"
              control={control}
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value ?? ''}>
                  <SelectTrigger>
                    <SelectValue placeholder="Hisse seçin" />
                  </SelectTrigger>
                  <SelectContent>
                    {hisseler.map((h) => (
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

          <div className="grid grid-cols-2 gap-3">
            {/* Yıl */}
            <div className="space-y-1">
              <Label htmlFor="yil">Yıl</Label>
              <Input id="yil" type="number" min={2020} max={2030} {...register('yil')} />
              {errors.yil && (
                <p className="text-xs text-red-600">{errors.yil.message}</p>
              )}
            </div>
            {/* Ödeme Tarihi */}
            <div className="space-y-1">
              <Label htmlFor="odemeTarihi">Ödeme Tarihi</Label>
              <Input id="odemeTarihi" type="date" {...register('odemeTarihi')} />
              {errors.odemeTarihi && (
                <p className="text-xs text-red-600">{errors.odemeTarihi.message}</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            {/* H.B. Brüt */}
            <div className="space-y-1">
              <Label htmlFor="hisseBasiTBrut">H.B. Brüt (₺)</Label>
              <Input
                id="hisseBasiTBrut"
                type="number"
                step="0.0001"
                placeholder="0,00"
                {...register('hisseBasiTBrut')}
              />
              {errors.hisseBasiTBrut && (
                <p className="text-xs text-red-600">{errors.hisseBasiTBrut.message}</p>
              )}
            </div>
            {/* Stopaj */}
            <div className="space-y-1">
              <Label>Stopaj Oranı</Label>
              <Controller
                name="stopajOrani"
                control={control}
                render={({ field }) => (
                  <Select
                    onValueChange={(v) => field.onChange(parseFloat(v))}
                    value={String(field.value ?? 0.1)}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="0.1">%10 Stopaj</SelectItem>
                      <SelectItem value="0">%0 Stopaj</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>
          </div>

          {/* Notlar */}
          <div className="space-y-1">
            <Label htmlFor="notlar">Notlar (opsiyonel)</Label>
            <Textarea id="notlar" rows={2} {...register('notlar')} />
          </div>

          {/* Önizleme */}
          {brut > 0 && (
            <div className="rounded-lg border border-border bg-muted/40 p-3 text-sm space-y-1">
              {pozisyon ? (
                <>
                  <p className="text-muted-foreground">
                    Mevcut pozisyon:{' '}
                    <span className="font-semibold text-foreground">
                      {pozisyon.toplamLot.toLocaleString('tr-TR')} lot
                    </span>
                  </p>
                  <p className="text-muted-foreground">
                    H.B. Net:{' '}
                    <span className="font-semibold text-foreground">
                      {formatTL(hisseBasiNet)}
                    </span>
                  </p>
                  <p className="text-muted-foreground">
                    Tahmini Brüt:{' '}
                    <span className="font-semibold text-foreground">
                      {formatTL(tahminiBrut)}
                    </span>
                  </p>
                  <p className="text-muted-foreground">
                    Tahmini Net:{' '}
                    <span className="font-semibold text-green-600">
                      {formatTL(tahminiNet)}
                    </span>
                  </p>
                </>
              ) : (
                <p className="text-muted-foreground">
                  H.B. Net:{' '}
                  <span className="font-semibold text-foreground">
                    {formatTL(hisseBasiNet)}
                  </span>
                </p>
              )}
            </div>
          )}

          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              İptal
            </Button>
            <Button type="submit" disabled={saving}>
              {saving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Kaydet
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ─── Ana Sayfa ─────────────────────────────────────────────────────────────────
export default function TemmettuPage() {
  const [ozet, setOzet] = useState<TemmettuOzetDto | null>(null);
  const [tumTemmettuler, setTumTemmettuler] = useState<TemmettuDto[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    try {
      const [ozetRes, listRes] = await Promise.all([
        api.get<ApiResponse<TemmettuOzetDto>>('/temettular/ozet'),
        api.get<ApiResponse<TemmettuDto[]>>('/temettular'),
      ]);
      setOzet(ozetRes.data.data);
      setTumTemmettuler(listRes.data.data);
    } catch {
      toast.error('Temettü verileri yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  const handleDelete = async (id: number) => {
    try {
      await api.delete(`/temettular/${id}`);
      toast.success('Temettü silindi');
      fetchAll();
    } catch {
      toast.error('Silme işlemi başarısız');
    }
  };

  const buYilOrtalamaYillik =
    ozet && ozet.yillikOzet.length > 0
      ? ozet.yillikOzet.reduce((s, y) => s + y.toplamNet, 0) / ozet.yillikOzet.length
      : null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Temettü</h1>
          <p className="text-sm text-muted-foreground">Temettü gelirleri ve özet</p>
        </div>
        <YeniTemmettuDialog onSaved={fetchAll} />
      </div>

      {/* BÖLÜM A — Özet Kartlar */}
      <div className="grid grid-cols-2 gap-4 xl:grid-cols-4">
        {[
          {
            label: 'Toplam Temettü',
            sublabel: 'Tüm zamanlar',
            val: ozet?.toplamNetTumZamanlar,
            icon: <BadgeDollarSign className="h-4 w-4" />,
          },
          {
            label: 'Bu Yıl Temettü',
            sublabel: String(new Date().getFullYear()),
            val: ozet?.toplamNetBuYil,
            icon: <BadgeDollarSign className="h-4 w-4" />,
          },
          {
            label: 'İşlem Sayısı',
            sublabel: 'Kayıt',
            val: null,
            count: ozet?.temmettuSayisi,
            icon: <BadgeDollarSign className="h-4 w-4" />,
          },
          {
            label: 'Ortalama Yıllık',
            sublabel: 'Net temettü',
            val: buYilOrtalamaYillik,
            icon: <BadgeDollarSign className="h-4 w-4" />,
          },
        ].map(({ label, sublabel, val, count, icon }) => (
          <Card key={label}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-1 pt-3 px-4">
              <CardTitle className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                {label}
              </CardTitle>
              <span className="text-muted-foreground">{icon}</span>
            </CardHeader>
            <CardContent className="px-4 pb-3">
              {loading ? (
                <Skeleton className="h-7 w-28" />
              ) : count !== undefined ? (
                <p className="text-2xl font-bold">{count ?? 0}</p>
              ) : (
                <p className="text-2xl font-bold text-green-600">
                  {val != null ? formatTL(val) : '-'}
                </p>
              )}
              <p className="text-xs text-muted-foreground mt-0.5">{sublabel}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* BÖLÜM B — Sekmeli Görünüm */}
      <Card>
        <CardContent className="p-0">
          <Tabs defaultValue="tumler" className="w-full">
            <div className="px-4 pt-4 border-b border-border">
              <TabsList className="mb-0">
                <TabsTrigger value="tumler">Tüm Temettüler</TabsTrigger>
                <TabsTrigger value="yillik">Yıllık Özet</TabsTrigger>
                <TabsTrigger value="hisse">Hisse Bazlı</TabsTrigger>
              </TabsList>
            </div>

            {/* Sekme 1: Tüm Temettüler */}
            <TabsContent value="tumler" className="mt-0">
              {loading ? (
                <div className="space-y-2 p-4">
                  {[...Array(5)].map((_, i) => <Skeleton key={i} className="h-10 w-full" />)}
                </div>
              ) : tumTemmettuler.length === 0 ? (
                <EmptyState icon="💰" title="Henüz temettü kaydı yok" description="Yeni Temettü butonuyla ekleyin" />
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border text-left text-xs text-muted-foreground">
                        <th className="px-4 py-3">Tarih</th>
                        <th className="px-4 py-3">Sembol</th>
                        <th className="px-4 py-3">Yıl</th>
                        <th className="px-4 py-3">H.B. Brüt</th>
                        <th className="px-4 py-3">H.B. Net</th>
                        <th className="px-4 py-3">Lot</th>
                        <th className="px-4 py-3">Toplam Net</th>
                        <th className="px-4 py-3"></th>
                      </tr>
                    </thead>
                    <tbody>
                      {tumTemmettuler.map((t) => (
                        <tr
                          key={t.id}
                          className="border-b border-border last:border-0 hover:bg-muted/50 transition-colors"
                        >
                          <td className="px-4 py-3 tabular-nums text-muted-foreground">
                            {formatTarihKisa(t.odemeTarihi)}
                          </td>
                          <td className="px-4 py-3 font-bold font-mono">{t.hisseSembol}</td>
                          <td className="px-4 py-3 tabular-nums">{t.yil}</td>
                          <td className="px-4 py-3 tabular-nums">{formatTL(t.hisseBasiTBrut)}</td>
                          <td className="px-4 py-3 tabular-nums">{formatTL(t.hisseBasiNet)}</td>
                          <td className="px-4 py-3 tabular-nums">
                            {t.lot.toLocaleString('tr-TR')}
                          </td>
                          <td className="px-4 py-3 tabular-nums font-semibold text-green-600">
                            {formatTL(t.toplamNet)}
                          </td>
                          <td className="px-4 py-3">
                            <AlertDialog>
                              <AlertDialogTrigger asChild>
                                <Button variant="ghost" size="sm" className="h-7 w-7 p-0 text-muted-foreground hover:text-red-600">
                                  <Trash2 className="h-3.5 w-3.5" />
                                </Button>
                              </AlertDialogTrigger>
                              <AlertDialogContent>
                                <AlertDialogHeader>
                                  <AlertDialogTitle>Temettü silinsin mi?</AlertDialogTitle>
                                  <AlertDialogDescription>
                                    {t.hisseSembol} — {t.yil} yılı temettüsü kalıcı olarak silinecek.
                                  </AlertDialogDescription>
                                </AlertDialogHeader>
                                <AlertDialogFooter>
                                  <AlertDialogCancel>İptal</AlertDialogCancel>
                                  <AlertDialogAction
                                    className="bg-red-600 hover:bg-red-700"
                                    onClick={() => handleDelete(t.id)}
                                  >
                                    Evet, Sil
                                  </AlertDialogAction>
                                </AlertDialogFooter>
                              </AlertDialogContent>
                            </AlertDialog>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </TabsContent>

            {/* Sekme 2: Yıllık Özet */}
            <TabsContent value="yillik" className="mt-0">
              {loading ? (
                <div className="space-y-2 p-4">
                  {[...Array(3)].map((_, i) => <Skeleton key={i} className="h-10 w-full" />)}
                </div>
              ) : !ozet || ozet.yillikOzet.length === 0 ? (
                <EmptyState icon="📅" title="Yıllık özet verisi yok" />
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border text-left text-xs text-muted-foreground">
                        <th className="px-4 py-3">Yıl</th>
                        <th className="px-4 py-3">İşlem Sayısı</th>
                        <th className="px-4 py-3">Toplam Brüt</th>
                        <th className="px-4 py-3">Toplam Net</th>
                      </tr>
                    </thead>
                    <tbody>
                      {[...ozet.yillikOzet]
                        .sort((a, b) => b.yil - a.yil)
                        .map((y) => (
                          <tr
                            key={y.yil}
                            className="border-b border-border last:border-0 hover:bg-muted/50 transition-colors"
                          >
                            <td className="px-4 py-3 font-semibold">{y.yil}</td>
                            <td className="px-4 py-3 tabular-nums">{y.islemSayisi}</td>
                            <td className="px-4 py-3 tabular-nums">{formatTL(y.toplamBrut)}</td>
                            <td className="px-4 py-3 tabular-nums font-semibold text-green-600">
                              {formatTL(y.toplamNet)}
                            </td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              )}
            </TabsContent>

            {/* Sekme 3: Hisse Bazlı */}
            <TabsContent value="hisse" className="mt-0">
              {loading ? (
                <div className="space-y-2 p-4">
                  {[...Array(4)].map((_, i) => <Skeleton key={i} className="h-10 w-full" />)}
                </div>
              ) : !ozet || ozet.hisseOzet.length === 0 ? (
                <EmptyState icon="📊" title="Hisse bazlı veri yok" />
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border text-left text-xs text-muted-foreground">
                        <th className="px-4 py-3">Sembol</th>
                        <th className="px-4 py-3">Şirket</th>
                        <th className="px-4 py-3">Toplam Net Temettü</th>
                      </tr>
                    </thead>
                    <tbody>
                      {/* Sembol bazlı topla (aynı sembol birden fazla yıl olabilir) */}
                      {Object.values(
                        ozet.hisseOzet.reduce<
                          Record<string, { sembol: string; sirketAdi: string; toplamNet: number }>
                        >((acc, h) => {
                          if (!acc[h.sembol]) {
                            acc[h.sembol] = { sembol: h.sembol, sirketAdi: h.sirketAdi, toplamNet: 0 };
                          }
                          acc[h.sembol].toplamNet += h.toplamNet;
                          return acc;
                        }, {})
                      )
                        .sort((a, b) => b.toplamNet - a.toplamNet)
                        .map((h) => (
                          <tr
                            key={h.sembol}
                            className="border-b border-border last:border-0 hover:bg-muted/50 transition-colors"
                          >
                            <td className="px-4 py-3 font-bold font-mono">{h.sembol}</td>
                            <td className="px-4 py-3 text-muted-foreground">{h.sirketAdi}</td>
                            <td className="px-4 py-3 tabular-nums font-semibold text-green-600">
                              {formatTL(h.toplamNet)}
                            </td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}
