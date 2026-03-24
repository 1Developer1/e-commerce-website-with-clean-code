import { Link } from 'react-router-dom';
import styles from './NotFoundPage.module.css';

/**
 * 404 Not Found page — shown for any unmatched route.
 */
export function NotFoundPage() {
  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.code}>404</div>
        <h1 className={styles.title}>Sayfa Bulunamadı</h1>
        <p className={styles.message}>
          Aradığınız sayfa mevcut değil veya taşınmış olabilir.
        </p>
        <Link to="/catalog" className={styles.homeLink}>
          Ana Sayfaya Dön
        </Link>
      </div>
    </div>
  );
}
