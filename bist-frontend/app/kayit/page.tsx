'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { toast } from 'sonner';
import { BarChart3, Loader2 } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import api from '@/lib/api';
import { useAuthStore } from '@/store/authStore';
import type { LoginResponse, ApiResponse } from '@/types';

const kayitSchema = z
  .object({
    ad: z.string().min(2, 'Ad en az 2 karakter olmalı'),
    email: z.string().email('Geçerli e-posta girin'),
    password: z.string().min(8, 'Şifre en az 8 karakter'),
    passwordConfirm: z.string(),
  })
  .refine((data) => data.password === data.passwordConfirm, {
    message: 'Şifreler eşleşmiyor',
    path: ['passwordConfirm'],
  });

type KayitForm = z.infer<typeof kayitSchema>;

export default function KayitPage() {
  const router = useRouter();
  const login = useAuthStore((s) => s.login);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<KayitForm>({ resolver: zodResolver(kayitSchema) });

  const onSubmit = async (data: KayitForm) => {
    setLoading(true);
    try {
      const res = await api.post<ApiResponse<LoginResponse>>('/auth/register', {
        ad: data.ad,
        email: data.email,
        password: data.password,
      });
      const { token, kullanici } = res.data.data;
      login(token, kullanici);
      toast.success(`Hoş geldiniz, ${kullanici.ad}!`);
      router.push('/');
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Kayıt işlemi başarısız';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="w-full max-w-sm">
        <CardHeader className="space-y-1 text-center">
          <div className="flex justify-center mb-2">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary text-primary-foreground">
              <BarChart3 className="h-6 w-6" />
            </div>
          </div>
          <CardTitle className="text-2xl">BIST Portföy</CardTitle>
          <CardDescription>Yeni hesap oluşturun</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="ad">Ad Soyad</Label>
              <Input
                id="ad"
                type="text"
                placeholder="Adınız Soyadınız"
                {...register('ad')}
              />
              {errors.ad && (
                <p className="text-xs text-red-600">{errors.ad.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="email">E-posta</Label>
              <Input
                id="email"
                type="email"
                placeholder="investor@portfolio.local"
                {...register('email')}
              />
              {errors.email && (
                <p className="text-xs text-red-600">{errors.email.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Şifre</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                {...register('password')}
              />
              {errors.password && (
                <p className="text-xs text-red-600">{errors.password.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="passwordConfirm">Şifre Tekrar</Label>
              <Input
                id="passwordConfirm"
                type="password"
                placeholder="••••••••"
                {...register('passwordConfirm')}
              />
              {errors.passwordConfirm && (
                <p className="text-xs text-red-600">{errors.passwordConfirm.message}</p>
              )}
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Kayıt yapılıyor...
                </>
              ) : (
                'Kayıt Ol'
              )}
            </Button>
          </form>
          <div className="mt-4 text-center text-sm text-muted-foreground">
            Zaten hesabınız var mı?{' '}
            <Link href="/login" className="text-primary font-medium hover:underline">
              Giriş yapın →
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
