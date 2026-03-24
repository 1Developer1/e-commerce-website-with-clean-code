import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import * as authService from './authService';

interface AuthContextType {
  token: string | null;
  userId: string | null;
  isLoggedIn: boolean;
  handleLogin: () => Promise<void>;
  handleLogout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

/**
 * AuthProvider wraps the entire app, providing authentication state.
 * Token is initialized from localStorage for persistence across refreshes.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(authService.getToken);
  const [userId, setUserId] = useState<string | null>(authService.getUserId);

  const handleLogin = useCallback(async () => {
    const response = await authService.login();
    setToken(response.token);
    setUserId(response.userId);
  }, []);

  const handleLogout = useCallback(() => {
    authService.logout();
    setToken(null);
    setUserId(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        token,
        userId,
        isLoggedIn: token !== null,
        handleLogin,
        handleLogout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Custom hook to access authentication context.
 * Must be used within AuthProvider.
 */
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
