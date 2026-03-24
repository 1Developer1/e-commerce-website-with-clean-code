import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './authContext';
import type { ReactNode } from 'react';

interface ProtectedRouteProps {
  children: ReactNode;
}

/**
 * Route guard that redirects unauthenticated users to /login.
 * Preserves the originally requested URL as returnUrl in location state,
 * so the user can be redirected back after successful login.
 */
export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isLoggedIn } = useAuth();
  const location = useLocation();

  if (!isLoggedIn) {
    return (
      <Navigate
        to="/login"
        state={{ returnUrl: location.pathname + location.search }}
        replace
      />
    );
  }

  return <>{children}</>;
}
