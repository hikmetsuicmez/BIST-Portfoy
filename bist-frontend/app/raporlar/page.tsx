import Link from 'next/link';
import { BarChart3, ArrowLeftRight, TrendingUp, ChevronRight } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';

const raporlar = [
  {
    href: '/portfoy',
    icon: BarChart3,
    title: 'Portföy Özet',
    description: 'Anlık pozisyonlar ve portföy durumu',
  },
  {
    href: '/raporlar/islem-gecmisi',
    icon: ArrowLeftRight,
    title: 'İşlem Geçmişi',
    description: 'Tarih aralığına göre filtrelenmiş işlem listesi',
  },
  {
    href: '/raporlar/performans',
    icon: TrendingUp,
    title: 'Performans Analizi',
    description: 'Dönem bazlı portföy performans raporu',
  },
];

export default function RaporlarPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Raporlar</h1>
        <p className="text-sm text-muted-foreground">Portföy analiz ve raporları</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {raporlar.map(({ href, icon: Icon, title, description }) => (
          <Card key={href} className="hover:bg-muted/30 transition-colors">
            <CardHeader>
              <div className="flex items-start justify-between">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                  <Icon className="h-5 w-5" />
                </div>
                <ChevronRight className="h-4 w-4 text-muted-foreground mt-1" />
              </div>
              <CardTitle className="mt-3 text-base">{title}</CardTitle>
              <CardDescription>{description}</CardDescription>
            </CardHeader>
            <CardContent>
              <Button asChild variant="outline" size="sm">
                <Link href={href}>Görüntüle</Link>
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
