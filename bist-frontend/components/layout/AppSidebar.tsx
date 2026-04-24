'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import {
  LayoutDashboard,
  Briefcase,
  ArrowLeftRight,
  TrendingDown,
  Moon,
  BarChart3,
  LogOut,
  ChevronRight,
} from 'lucide-react';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';
import { useAuthStore } from '@/store/authStore';
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar';

const navItems = [
  { href: '/', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/portfoy', label: 'Portföy', icon: Briefcase },
  { href: '/islemler', label: 'İşlemler', icon: ArrowLeftRight },
  { href: '/kapanis-fiyatlari', label: 'Kapanış Fiyatları', icon: TrendingDown },
  { href: '/gunsonu', label: 'Günsonu', icon: Moon },
  { href: '/raporlar', label: 'Raporlar', icon: BarChart3 },
];

export function AppSidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const { kullanici, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    toast.success('Çıkış yapıldı');
    router.push('/login');
  };

  return (
    <Sidebar>
      <SidebarHeader className="border-b border-sidebar-border p-4">
        <div className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
            <BarChart3 className="h-4 w-4" />
          </div>
          <div>
            <p className="text-sm font-semibold text-sidebar-foreground">BIST Portföy</p>
            <p className="text-xs text-muted-foreground">Takip Sistemi</p>
          </div>
        </div>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
              {navItems.map((item) => {
                const isActive =
                  item.href === '/' ? pathname === '/' : pathname.startsWith(item.href);
                return (
                  <SidebarMenuItem key={item.href}>
                    <SidebarMenuButton asChild isActive={isActive}>
                      <Link href={item.href} className="flex items-center gap-3">
                        <item.icon className="h-4 w-4" />
                        <span>{item.label}</span>
                        {isActive && <ChevronRight className="ml-auto h-3 w-3" />}
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                );
              })}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter className="border-t border-sidebar-border p-4">
        <div className="flex items-center justify-between">
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-sidebar-foreground">
              {kullanici?.ad ?? 'Kullanıcı'}
            </p>
            <p className="truncate text-xs text-muted-foreground">{kullanici?.email}</p>
          </div>
          <button
            onClick={handleLogout}
            className={cn(
              'ml-2 flex h-8 w-8 items-center justify-center rounded-md',
              'text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
              'transition-colors'
            )}
            title="Çıkış Yap"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </SidebarFooter>
    </Sidebar>
  );
}
