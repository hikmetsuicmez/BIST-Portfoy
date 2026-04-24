'use client';

import { usePathname } from 'next/navigation';
import { AppSidebar } from '@/components/layout/AppSidebar';
import { AuthGuard } from '@/components/layout/AuthGuard';
import { SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar';

const PUBLIC_PATHS = ['/login'];

export function ConditionalLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isPublic = PUBLIC_PATHS.includes(pathname);

  if (isPublic) {
    return <>{children}</>;
  }

  return (
    <AuthGuard>
      <SidebarProvider>
        <div className="flex min-h-screen w-full">
          <AppSidebar />
          <main className="flex flex-1 flex-col min-w-0">
            <div className="flex items-center border-b border-border px-4 py-2">
              <SidebarTrigger />
            </div>
            <div className="flex-1 p-6">{children}</div>
          </main>
        </div>
      </SidebarProvider>
    </AuthGuard>
  );
}
