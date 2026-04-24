'use client';

import { SidebarTrigger } from '@/components/ui/sidebar';

interface DashboardHeaderProps {
  title: string;
  description?: string;
}

export function DashboardHeader({ title, description }: DashboardHeaderProps) {
  return (
    <div className="flex items-center gap-3 border-b border-border px-4 py-3">
      <SidebarTrigger />
      <div>
        <h1 className="text-lg font-semibold leading-tight">{title}</h1>
        {description && (
          <p className="text-xs text-muted-foreground">{description}</p>
        )}
      </div>
    </div>
  );
}
