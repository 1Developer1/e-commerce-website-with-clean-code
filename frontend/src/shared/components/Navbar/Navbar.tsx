import { useAuth } from '../../../infrastructure/auth/authContext';
import { useUI } from '../../../features/common/context/UIContext';
import { useCart } from '../../../features/cart/hooks/useCart';
import { useNavigate } from 'react-router-dom';
import styles from './Navbar.module.css';

/**
 * Top navigation bar — shared layout component.
 * Shows app branding, cart indicator, and logout.
 */
export function Navbar() {
  const { handleLogout } = useAuth();
  const { toggleCart, theme, toggleTheme } = useUI();
  const { data: cartData } = useCart();
  const navigate = useNavigate();

  const totalItems = cartData?.items?.reduce((sum: number, item: { quantity: number }) => sum + item.quantity, 0) || 0;

  return (
    <nav className={styles.navbar} aria-label="Ana navigasyon">
      <div className={styles.container}>
        <div className={styles.brand}>
          <span className={styles.logo}>🛒</span>
          <span className={styles.brandText}>Clean Commerce</span>
        </div>

        <div className={styles.actions}>
          <button
            className={styles.themeButton}
            onClick={toggleTheme}
            aria-label="Temayı Değiştir"
          >
            {theme === 'dark' ? '☀️' : '🌙'}
          </button>

          <button 
            className={styles.ordersButton}
            onClick={() => navigate('/orders')}
            aria-label="Siparişlerim"
          >
            Siparişlerim
          </button>

          <button 
            className={styles.cartButton}
            onClick={toggleCart}
            aria-label={`Sepeti aç (${totalItems} ürün)`}
          >
            <span className={styles.cartIcon}>🛒</span>
            {totalItems > 0 && <span className={styles.badge}>{totalItems}</span>}
          </button>

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
