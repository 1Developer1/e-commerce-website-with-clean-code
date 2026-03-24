import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppProviders } from './providers';
import { ProtectedRoute } from '../infrastructure/auth/ProtectedRoute';

// Pages
import { LoginPage } from '../features/auth/pages/LoginPage';
import { CatalogPage } from '../features/catalog/pages/CatalogPage';
import { NotFoundPage } from '../features/common/pages/NotFoundPage';

import '../styles/reset.css';
import '../styles/tokens.css';
import '../styles/typography.css';
import '../styles/animations.css';

export default function App() {
  return (
    <AppProviders>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Routes */}
          <Route
            path="/catalog"
            element={
              <ProtectedRoute>
                <CatalogPage />
              </ProtectedRoute>
            }
          />

          {/* Default redirect */}
          <Route path="/" element={<Navigate to="/catalog" replace />} />

          {/* 404 Not Found */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </BrowserRouter>
    </AppProviders>
  );
}
