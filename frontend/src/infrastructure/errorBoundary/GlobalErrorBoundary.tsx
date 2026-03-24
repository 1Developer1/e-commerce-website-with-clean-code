import { Component, type ReactNode, type ErrorInfo } from 'react';
import styles from './GlobalErrorBoundary.module.css';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

/**
 * Global Error Boundary — SRE principle: Graceful Degradation.
 * Catches any unhandled React rendering errors and shows a friendly message
 * instead of a blank white screen.
 */
export class GlobalErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('[GlobalErrorBoundary] Caught error:', error, errorInfo);
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div className={styles.container}>
          <div className={styles.card}>
            <div className={styles.icon}>⚠️</div>
            <h1 className={styles.title}>Beklenmeyen Bir Hata Oluştu</h1>
            <p className={styles.message}>
              Bir şeyler ters gitti. Lütfen sayfayı yenilemeyi deneyin.
            </p>
            {this.state.error && (
              <details className={styles.details}>
                <summary>Teknik Detay</summary>
                <pre>{this.state.error.message}</pre>
              </details>
            )}
            <button className={styles.retryButton} onClick={this.handleRetry}>
              Tekrar Dene
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
