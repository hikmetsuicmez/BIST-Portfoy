'use client';

import { useState, useCallback } from 'react';
import api from '@/lib/api';
import type { GunsonuDurumDto, EksikFiyatDto, GunsonuSonucDto, ApiResponse } from '@/types';

export function useGunsonu(tarih: string) {
  const [durum, setDurum] = useState<GunsonuDurumDto | null>(null);
  const [eksikFiyatlar, setEksikFiyatlar] = useState<EksikFiyatDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [calistiriliyor, setCalistiriliyor] = useState(false);
  const [sonuc, setSonuc] = useState<GunsonuSonucDto | null>(null);

  const fetchDurum = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get<ApiResponse<GunsonuDurumDto>>(`/gunsonu/durum/${tarih}`);
      setDurum(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [tarih]);

  const calistir = useCallback(async () => {
    setCalistiriliyor(true);
    setSonuc(null);
    try {
      const res = await api.post<ApiResponse<GunsonuSonucDto>>('/gunsonu/calistir', { tarih });
      setSonuc(res.data.data);
      await fetchDurum();
    } catch (err) {
      console.error(err);
      throw err;
    } finally {
      setCalistiriliyor(false);
    }
  }, [tarih, fetchDurum]);

  return { durum, eksikFiyatlar, setEksikFiyatlar, loading, calistiriliyor, sonuc, fetchDurum, calistir };
}
