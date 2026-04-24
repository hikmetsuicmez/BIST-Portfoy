'use client';

import { useState, useEffect, useCallback } from 'react';
import api from '@/lib/api';
import type { PozisyonDto, PortfoyOzetDto, ApiResponse } from '@/types';

export function usePortfoy() {
  const [pozisyonlar, setPozisyonlar] = useState<PozisyonDto[]>([]);
  const [ozet, setOzet] = useState<PortfoyOzetDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPortfoy = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [pozRes, ozetRes] = await Promise.all([
        api.get<ApiResponse<PozisyonDto[]>>('/portfoy/pozisyonlar'),
        api.get<ApiResponse<PortfoyOzetDto>>('/portfoy/ozet'),
      ]);
      setPozisyonlar(pozRes.data.data);
      setOzet(ozetRes.data.data);
    } catch (err) {
      setError('Portföy verisi yüklenemedi');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPortfoy();
  }, [fetchPortfoy]);

  return { pozisyonlar, ozet, loading, error, refetch: fetchPortfoy };
}
