import styles from './Skeleton.module.css';

interface SkeletonProps {
  width?: string;
  height?: string;
  borderRadius?: string;
  count?: number;
}

/**
 * Skeleton loader — shows pulsing placeholder while data loads.
 * SRE: Prevents layout shift and communicates loading state.
 */
export function Skeleton({ width = '100%', height = '20px', borderRadius = 'var(--radius-sm)', count = 1 }: SkeletonProps) {
  return (
    <>
      {Array.from({ length: count }).map((_, i) => (
        <div
          key={i}
          className={styles.skeleton}
          style={{ width, height, borderRadius }}
          aria-hidden="true"
        />
      ))}
    </>
  );
}

/**
 * ProductCard-shaped skeleton for catalog loading state.
 */
export function ProductCardSkeleton() {
  return (
    <div className={styles.cardSkeleton}>
      <Skeleton height="180px" borderRadius="0" />
      <div className={styles.cardContent}>
        <Skeleton width="70%" height="22px" />
        <Skeleton width="40%" height="26px" />
      </div>
      <div className={styles.cardActions}>
        <Skeleton height="40px" borderRadius="var(--radius-md)" />
      </div>
    </div>
  );
}

export function ProductGridSkeleton({ count = 6 }: { count?: number }) {
  return (
    <div className={styles.grid}>
      {Array.from({ length: count }).map((_, i) => (
        <ProductCardSkeleton key={i} />
      ))}
    </div>
  );
}
