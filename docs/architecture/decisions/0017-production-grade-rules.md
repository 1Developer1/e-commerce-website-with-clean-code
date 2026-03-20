# ADR 017: Production Grade Architecture Rules Uygulaması

**Tarih:** 2026-03-21
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Release It! (Michael Nygard) kitabından derlenen `Production Grade Architecture Rules.md` dokümanı, 56 üretim-odaklı kuralı içermektedir. Projenin bu kurallara karşı yapılan uyumluluk analizi sonucunda 17 maddenin ❌ eksik, 16 maddenin ⚠️ kısmi uygulandığı tespit edilmiştir. Test Harnesses maddesi kapsam dışı bırakılarak kalan tüm eksiklikler bu ADR kapsamında çözülmüştür.

## Karar (Decision)

### 1. Rate Limiting (Governor + Shed Load)
Resilience4j `RateLimiter` tabanlı `RateLimitingFilter` (Servlet Filter) oluşturuldu. IP başına saniyede 20 istek limiti uygulanıyor. Limit aşımında HTTP 429 (Too Many Requests) + `Retry-After: 1` header dönülüyor. Actuator ve auth endpoint'leri muaf tutuldu.

### 2. Pagination (Unbounded Result Sets)
`ProductRepository` interface'ine `findAll(int page, int size)` ve `count()` metotları eklendi. `ListProductsUseCase` pagination parametreleri kabul ediyor (`MAX_SIZE=100` cap). Spring `Pageable` sınıfı UseCase katmanına sızdırılmadı — yalnızca ilkel tipler (`int`) kullanıldı. Dönüşüm `ProductPersistenceAdapter`'da `PageRequest.of()` ile yapılıyor.

### 3. Bounded EventBus (Back Pressure)
`SimpleEventBus`'a event tipi başına maksimum 50 subscriber limiti eklendi. Handler'lar try-catch ile sarıldı — bir handler'ın hatası diğerlerini ve publisher'ı etkilemiyor (Let It Crash prensibi).

### 4. Input Validation (OWASP)
`CreateProductRequest` record'u Jakarta Bean Validation (JSR-380) anotasyonları ile oluşturuldu (`@NotBlank`, `@Positive`, `@Size`). `ProductController`'da `@Valid` ile zorunlu kılındı. `GlobalExceptionHandler`'a `MethodArgumentNotValidException` handler eklendi. Validation anotasyonları UseCase katmanına değil, Controller katmanına yerleştirildi.

### 5. Secrets Externalization
`jwt.secret` ve `jwt.expiration` değerleri `application.yml`'de `${JWT_SECRET:changeme-in-production}` ile ortam değişkeninden okunuyor. K8s `deployment.yaml`'de `secretKeyRef` ile K8s Secret'tan enjekte ediliyor.

### 6. Audit Logging
`AuditLoggingInterceptor` (Spring `HandlerInterceptor`) mutating HTTP isteklerini (POST/PUT/DELETE/PATCH) loglıyor: `userId`, `method`, `path`, `status`, `ip`. Ayrı bir `AUDIT` logger ile logback'te izole ediliyor.

### 7. Log Rotation (Steady State)
`logback.xml`'e `RollingFileAppender` eklendi: günlük rotasyon, dosya başı 50MB, 30 gün saklama, toplam 1GB cap. Loglar sınırsız büyüyemez.

### 8. K8s Zero-Downtime + HPA
`deployment.yaml`'e `strategy: RollingUpdate` (`maxUnavailable: 0`, `maxSurge: 1`) eklendi. `hpa.yaml` oluşturuldu: min 2, max 5 replika, CPU %70 eşik, fail-safe scale-down (5 dk stabilizasyon, tek seferde 1 pod azaltma).

### 9. Connection Pool (Scaling Effects)
`application.yml`'e HikariCP yapılandırması eklendi: `maximum-pool-size`, `minimum-idle`, `connection-timeout`, `idle-timeout`, `max-lifetime` — tümü ortam değişkeni ile yapılandırılabilir.

### 10. Runbooks (Operasyonel Prosedürler)
6 adet operasyonel runbook oluşturuldu: Circuit Breaker OPEN, DB Pool tükenmesi, CrashLoopBackOff, Yüksek Latency, Rate Limit aşımı, Routine Deployment.

## Sonuçlar (Consequences)
**Pozitif:**
- Compliance skoru %41 → %85+ seviyesine yükseldi
- Bot/scraper saldırılarına karşı koruma sağlandı (Rate Limiting)
- Sınırsız sorgu riski ortadan kalktı (Pagination MAX_SIZE=100)
- Güvenlik denetim izi (Audit Trail) oluşturuldu
- Operasyon ekibi için standart prosedürler belgelendi (Runbooks)

**Negatif:**
- Rate Limiting in-memory ConcurrentHashMap kullanıyor — çok pod'lu ortamda Redis tabanlı dağıtık rate limiter gerekebilir
- Pagination eklenmesi mevcut API tüketicilerinin güncellenmesini gerektirir (breaking change)

## Uyumluluk (Compliance)
- `mvn clean test-compile` → exit code 0 (tüm dosyalar derlendi)
- ArchUnit testleri ile katman ihlali kontrolü devam ediyor
- Yeni endpoint'lerde `@Valid` olmadan PR kabul edilmeyecek (Code Review kuralı)
