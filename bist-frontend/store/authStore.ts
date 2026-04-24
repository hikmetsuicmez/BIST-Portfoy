'use client';

import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { KullaniciDto } from '@/types';

interface AuthState {
  token: string | null;
  kullanici: KullaniciDto | null;
  login: (token: string, kullanici: KullaniciDto) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      kullanici: null,
      login: (token, kullanici) => {
        if (typeof window !== 'undefined') {
          localStorage.setItem('jwt_token', token);
        }
        set({ token, kullanici });
      },
      logout: () => {
        if (typeof window !== 'undefined') {
          localStorage.removeItem('jwt_token');
        }
        set({ token: null, kullanici: null });
      },
      isAuthenticated: () => !!get().token,
    }),
    { name: 'auth-storage' }
  )
);
