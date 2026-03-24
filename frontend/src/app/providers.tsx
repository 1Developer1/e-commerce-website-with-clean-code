import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '../infrastructure/auth/authContext';
import { GlobalErrorBoundary } from '../infrastructure/errorBoundary/GlobalErrorBoundary';
import type { ReactNode } from 'react';

/**
 * React Query client with SRE defaults:
 * - GET requests retry 3 times with exponential backoff
 * - Data stays fresh for 30 seconds (staleTime)
 * - Mutations never auto-retry
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 3,
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 8000),
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,
    },
  },
});

/**
 * AppProviders wraps the application with all required context providers.
 * Order: ErrorBoundary > QueryClient > Auth
 */
export function AppProviders({ children }: { children: ReactNode }) {
  return (
    <GlobalErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          {children}
        </AuthProvider>
      </QueryClientProvider>
    </GlobalErrorBoundary>
  );
}
