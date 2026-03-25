import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

type Theme = 'light' | 'dark';

interface UIContextValue {
  isCartOpen: boolean;
  theme: Theme;
  openCart: () => void;
  closeCart: () => void;
  toggleCart: () => void;
  toggleTheme: () => void;
}

const UIContext = createContext<UIContextValue | undefined>(undefined);

export function UIProvider({ children }: { children: ReactNode }) {
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [theme, setTheme] = useState<Theme>(() => {
    return (localStorage.getItem('app-theme') as Theme) || 'dark';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('app-theme', theme);
  }, [theme]);

  return (
    <UIContext.Provider
      value={{
        isCartOpen,
        theme,
        openCart: () => setIsCartOpen(true),
        closeCart: () => setIsCartOpen(false),
        toggleCart: () => setIsCartOpen((prev) => !prev),
        toggleTheme: () => setTheme(prev => prev === 'dark' ? 'light' : 'dark'),
      }}
    >
      {children}
    </UIContext.Provider>
  );
}

export function useUI() {
  const context = useContext(UIContext);
  if (!context) {
    throw new Error('useUI must be used within a UIProvider');
  }
  return context;
}
