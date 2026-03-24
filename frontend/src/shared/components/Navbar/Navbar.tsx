import { useAuth } from '../../../infrastructure/auth/authContext';
import styles from './Navbar.module.css';

/**
 * Top navigation bar — shared layout component.
 * Shows app branding, cart indicator, and logout.
 */
export function Navbar() {
  const { handleLogout } = useAuth();

  return (
    <nav className={styles.navbar} aria-label="Ana navigasyon">
      <div className={styles.container}>
        <div className={styles.brand}>
          <span className={styles.logo}>🛒</span>
          <span className={styles.brandText}>Clean Commerce</span>
        </div>

        <div className={styles.actions}>
          <button
            className={styles.logoutButton}
            onClick={handleLogout}
            aria-label="Çıkış yap"
          >
            Çıkış
          </button>
        </div>
      </div>
    </nav>
  );
}
