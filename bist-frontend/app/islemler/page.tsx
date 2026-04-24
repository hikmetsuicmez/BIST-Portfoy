'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { EmptyState } from '@/components/shared/EmptyState';
import api from '@/lib/api';
import { formatTL, formatLot, formatTarihKisa } from '@/lib/utils';
import type { IslemDto, ApiResponse } from '@/types';

export default function IslemlerPage() {
  const [islemler, setIslemler] = useState<IslemDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [silId, setSilId] = useState<number | null>(null);

  const fetchIslemler = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get<ApiResponse<IslemDto[]>>('/islemler');
      setIslemler(res.data.data);
    } catch {
      toast.error('İşlemler yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchIslemler(); }, [fetchIslemler]);

  const handleSil = async () => {
    if (!silId) return;
    try {
      await api.delete(`/islemler/${silId}`);
      toast.success('İşlem silindi');
      setIslemler((prev) => prev.filter((i) => i.id !== silId));
    } catch {
      toast.error('İşlem silinemedi');
    } finally {
      setSilId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">İşlemler</h1>
          <p className="text-sm text-muted-foreground">Tüm alım/satım işlemleriniz</p>
        </div>
        <Button asChild>
          <Link href="/islemler/yeni">
            <Plus className="mr-2 h-4 w-4" />
            Yeni İşlem
          </Link>
        </Button>
      </div>

      <Card>
        <CardContent className="p-0">
          {loading ? (
            <div className="space-y-2 p-4">
              {[...Array(6)].map((_, i) => <Skeleton key={i} className="h-10 w-full" />)}
            </div>
          ) : islemler.length === 0 ? (
            <EmptyState icon="💼" title="Henüz işlem yok" description="Yeni işlem ekleyerek başlayın" />
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
                    <th className="px-4 py-3"></th>
                  </tr>
                </thead>
                <tbody>
                  {islemler.map((i) => (
                    <tr key={i.id} className="border-b border-border last:border-0 hover:bg-muted/50">
                      <td className="px-4 py-3 tabular-nums">{formatTarihKisa(i.tarih)}</td>
                      <td className="px-4 py-3">
                        <Badge variant="outline" className="font-mono font-bold">
                          {i.sembol}
                        </Badge>
                      </td>
                      <td className="px-4 py-3">
                        <Badge
                          variant={i.islemTuru === 'ALIM' ? 'default' : 'destructive'}
                          className="text-xs"
                        >
                          {i.islemTuru}
                        </Badge>
                      </td>
                      <td className="px-4 py-3 tabular-nums">{formatLot(i.lot)}</td>
                      <td className="px-4 py-3 tabular-nums">{formatTL(i.fiyat)}</td>
                      <td className="px-4 py-3 tabular-nums">{formatTL(i.komisyon)}</td>
                      <td className="px-4 py-3 tabular-nums font-semibold">{formatTL(i.toplamTutar)}</td>
                      <td className="px-4 py-3 max-w-[140px] truncate text-muted-foreground">{i.notlar ?? '-'}</td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-1">
                          <Button asChild variant="ghost" size="icon" className="h-7 w-7">
                            <Link href={`/islemler/${i.id}/duzenle`}>
                              <Pencil className="h-3.5 w-3.5" />
                            </Link>
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-7 w-7 text-red-600 hover:text-red-600"
                            onClick={() => setSilId(i.id)}
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      <AlertDialog open={silId !== null} onOpenChange={(o) => !o && setSilId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>İşlemi Sil</AlertDialogTitle>
            <AlertDialogDescription>
              Bu işlemi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>İptal</AlertDialogCancel>
            <AlertDialogAction onClick={handleSil} className="bg-red-600 hover:bg-red-700">
              Evet, Sil
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
