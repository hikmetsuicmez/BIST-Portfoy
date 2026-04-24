import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import { Toaster } from '@/components/ui/sonner';
import { ConditionalLayout } from '@/components/layout/ConditionalLayout';
import './globals.css';

const geist = Geist({ subsets: ['latin'], variable: '--font-geist-sans' });
const geistMono = Geist_Mono({ subsets: ['latin'], variable: '--font-geist-mono' });

export const metadata: Metadata = {
  title: 'BIST Portföy',
  description: 'BIST Portföy Takip Sistemi',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="tr" className="dark">
      <body className={`${geist.variable} ${geistMono.variable} font-sans antialiased`}>
        <ConditionalLayout>{children}</ConditionalLayout>
        <Toaster richColors position="top-right" />
      </body>
    </html>
  );
}
