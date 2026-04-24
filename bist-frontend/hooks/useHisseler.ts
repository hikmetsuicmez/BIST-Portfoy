'use client';

import { useState, useEffect } from 'react';
import api from '@/lib/api';
import type { HisseDto, ApiResponse } from '@/types';

export function useHisseler() {
  const [hisseler, setHisseler] = useState<HisseDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<ApiResponse<HisseDto[]>>('/hisseler')
      .then((res) => setHisseler(res.data.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  return { hisseler, loading };
}
