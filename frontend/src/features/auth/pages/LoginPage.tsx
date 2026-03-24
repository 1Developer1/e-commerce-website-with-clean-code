import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../../infrastructure/auth/authContext';
import styles from './LoginPage.module.css';

/**
 * Login Page — calls POST /auth/login (mock endpoint).
 * After successful login, redirects to returnUrl or /catalog.
 */
export function LoginPage() {
  const { handleLogin, isLoggedIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const returnUrl = (location.state as { returnUrl?: string })?.returnUrl || '/catalog';

  // Already logged in → redirect
  if (isLoggedIn) {
    navigate(returnUrl, { replace: true });
    return null;
  }

  const onLogin = async () => {
    setLoading(true);
    setError(null);
    try {
      await handleLogin();
      navigate(returnUrl, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Giriş başarısız');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.logo}>🛒</div>
        <h1 className={styles.title}>Clean Commerce</h1>
        <p className={styles.subtitle}>Temiz Mimari ile E-Ticaret</p>

        {error && (
          <div className={styles.error} role="alert">
            {error}
          </div>
        )}

        <button
          className={styles.loginButton}
          onClick={onLogin}
          disabled={loading}
          aria-label="Giriş Yap"
        >
          {loading ? (
            <span className={styles.spinner} aria-hidden="true" />
          ) : (
            'Giriş Yap'
          )}
        </button>
        <p className={styles.hint}>Demo hesap — kimlik bilgisi gerekmez</p>
      </div>
    </div>
  );
}
