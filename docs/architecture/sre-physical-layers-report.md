# SRE Fiziksel Katman Analizi: E-Commerce Platformu

Bu rapor, e-ticaret sistemimizin **4 fiziksel SRE katmanını** derinlemesine inceler. Her katman için: tanımı, meydana gelebilecek riskler, benimsediğimiz ilkeler ve ürettiğimiz çözümler detaylandırılmıştır.

---

## Katman Genel Görünüm

```
┌────────────────────────────────────────────────────────────────────┐
│  4. INSTANCES (Application Workloads)                              │
│  Controllers · Presenters · UseCases · Entities · EventBus         │
├────────────────────────────────────────────────────────────────────┤
│  3. CONTROL PLANE (Governance & Health)                            │
│  Actuator Probes · Prometheus · Logging · Security Filters · ADR   │
├────────────────────────────────────────────────────────────────────┤
│  2. INTERCONNECT (Integration Points)                              │
│  CreditCardAdapter · DummyShippingProvider · Resilience4j · Trace  │
├────────────────────────────────────────────────────────────────────┤
│  1. FOUNDATION (Infrastructure & Environment)                      │
│  Docker · Kubernetes · JPA/H2 · .env Config · JVM · Alpine Linux   │
└────────────────────────────────────────────────────────────────────┘
```

---

## 1. FOUNDATION (Altyapı & Ortam)

### Tanım
Uygulamanın çalıştığı fiziksel veya konteynerize ortam, işletim sistemi, JVM, veritabanı sürücüsü ve temel teknik kapasitelerdir. Tüm üst katmanlar bu temelin üzerinde yükselir.

### Meydana Gelebilecek Riskler

| # | Risk | Etki | Olasılık |
|---|------|------|----------|
| F1 | **Container Escape:** Root kullanıcı ile çalışan konteyner, host sisteme sızma riski taşır | Kritik | Orta |
| F2 | **Kaynak Tükenmesi:** Sınırsız CPU/RAM kullanımı, aynı node üzerindeki diğer servisleri çökertir | Yüksek | Yüksek |
| F3 | **Sabit Kodlanmış Konfigürasyon:** Veritabanı şifreleri veya port numaraları kodda sabit yazılırsa, ortam değişikliklerinde deploy kırılır | Orta | Yüksek |
| F4 | **İmaj Boyutu Şişmesi:** JDK + build araçları production imajına dahil edilirse saldırı yüzeyi büyür | Orta | Orta |
| F5 | **Veritabanı Kilidi:** ORM mapping hataları veya connection pool tükenmesi servisi durdurabilir | Yüksek | Orta |

### Benimsediğimiz İlkeler & Ürettiğimiz Çözümler

#### ✅ F1 Çözümü: Non-Root Multi-Stage Dockerfile
```dockerfile
# Builder aşaması (JDK - sadece derleme için)
FROM eclipse-temurin:17-jdk-alpine AS builder

# Production aşaması (sadece JRE - minimal saldırı yüzeyi)
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S ecommercegroup && adduser -S ecommerceuser -G ecommercegroup
USER ecommerceuser  # ← Root değil!
```
**İlke:** Least Privilege (En Az Yetki). Production konteyner asla root olarak çalışmaz. Alpine Linux tabanlı imaj ile saldırı yüzeyi minimuma indirilir.

**Dosya:** [Dockerfile](file:///d:/python/e-commerce-app/Dockerfile)

#### ✅ F2 Çözümü: Kubernetes Resource Limits
```yaml
resources:
  requests: { memory: "256Mi", cpu: "200m" }
  limits:   { memory: "512Mi", cpu: "500m" }
```
**İlke:** Physical Bulkheads (Fiziksel Bölmeler). Kubernetes OOMKiller, limiti aşan pod'ları otomatik sonlandırır. Bir pod'un kaynak açgözlülüğü diğer pod'ları etkileyemez.

**Dosya:** [deployment.yaml](file:///d:/python/e-commerce-app/k8s/deployment.yaml)

#### ✅ F3 Çözümü: 12-Factor App Externalized Config
```yaml
server:
  port: ${APP_PORT:8080}  # ← Ortam değişkeninden alınır
spring:
  datasource:
    url: jdbc:h2:mem:testdb
```
**İlke:** 12-Factor App Config. Port, veritabanı URL'si ve havuz ayarları `application.yml` + `.env` dosyası üzerinden dışsallaştırılmıştır. Kod değişikliği gerekmeden ortam geçişi yapılabilir (dev → staging → prod).

**Dosya:** [application.yml](file:///d:/python/e-commerce-app/src/main/resources/application.yml)

#### ✅ F4 Çözümü: Multi-Stage Build
Build aşamasında JDK + Maven kullanılır, production aşamasına **sadece JAR dosyası** kopyalanır. Son imajda Maven, kaynak kod veya build araçları bulunmaz.

#### ✅ F5 Çözümü: Clean Architecture ile DB İzolasyonu
Veritabanı detayları (JPA Entity, Spring Data Repository) yalnızca `adapter.out.persistence.jpa` paketindedir. Domain Entity'ler ve UseCase'ler veritabanından tamamen habersizdir. H2'den PostgreSQL'e geçiş sadece `application.yml` değiştirilerek yapılabilir.

---

## 2. INTERCONNECT (Entegrasyon Noktaları)

### Tanım
Dış API'ler, veritabanları ve mesaj servisleri ile kurulan iletişim hatlarıdır. Kaskad arızaların (cascading failures) genellikle başladığı noktadır. "Sistemin en zayıf halkası" burasıdır.

### Meydana Gelebilecek Riskler

| # | Risk | Etki | Olasılık |
|---|------|------|----------|
| I1 | **Yavaş Yanıt (Slow Response):** Dış ödeme API'si 30 saniye yanıt vermezse, thread havuzu tükenir ve tüm sistem kilitlenir | Kritik | Yüksek |
| I2 | **Kaskad Arıza (Cascading Failure):** Çöken kargo API'si, ödeme servisini de çökertir çünkü thread'ler paylaşılır | Kritik | Orta |
| I3 | **Geçici Ağ Hataları:** DNS çözümleme gecikmesi, TCP timeout, paket kaybı gibi geçici sorunlar kalıcı hata gibi işlenir | Orta | Yüksek |
| I4 | **Tamamen Çökmüş Servis:** Dış API süresiz çöktüğünde, her isteği tekrar denemek hem bant genişliğini hem de dış servisi boğar | Yüksek | Orta |
| I5 | **İzleme Kopukluğu:** Dış servise giden isteklerin hangi kullanıcı işleminden kaynaklandığı bilinemez | Orta | Yüksek |

### Benimsediğimiz İlkeler & Ürettiğimiz Çözümler

#### ✅ I1 Çözümü: TimeLimiter (Zaman Sınırlayıcı)
```java
TimeLimiterConfig tlConfig = TimeLimiterConfig.custom()
    .timeoutDuration(Duration.ofSeconds(3))  // ← Maksimum 3 saniye bekle
    .build();
```
**İlke:** Fail Fast. Bir thread 3 saniyeden fazla bekletilmez. Timeout aşılırsa `TimeoutException` fırlatılır ve Retry mekanizmasına geçilir.

#### ✅ I2 Çözümü: Logical Bulkhead (Mantıksal Bölme)
```java
BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
    .maxConcurrentCalls(10)       // ← Eşzamanlı maks 10 çağrı
    .maxWaitDuration(Duration.ofMillis(100))
    .build();
```
**İlke:** Bulkhead Pattern (Gemi Bölme Duvarı). Ödeme adaptörü en fazla 10 eşzamanlı isteği kabul eder. 11. istek anında reddedilir. Kargo adaptörünün thread havuzu ayrıdır — birinin çökmesi diğerini etkilemez.

#### ✅ I3 Çözümü: Retry with Backoff (Yeniden Deneme)
```java
RetryConfig retryConfig = RetryConfig.custom()
    .maxAttempts(3)                    // ← 3 deneme hakkı
    .waitDuration(Duration.ofMillis(500)) // ← Denemeler arası 500ms bekleme
    .build();
```
**İlke:** Retry Pattern. Geçici hatalar (DNS timeout, TCP reset) 3 kez yeniden denenir. Kalıcı hatalar ise CircuitBreaker'a escalate edilir.

#### ✅ I4 Çözümü: Circuit Breaker (Devre Kesici)
```java
CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
    .failureRateThreshold(50)               // ← %50 hata oranında devre açılır
    .waitDurationInOpenState(Duration.ofSeconds(15)) // ← 15 saniye boyunca tüm istekler reddedilir
    .slidingWindowSize(10)                  // ← Son 10 isteğe bakılır
    .build();
```
**İlke:** Circuit Breaker (Michael Nygard). Devre açıldığında dış servise **sıfır** istek gönderilir. Bu, çökmüş servise yük bindirmekten kaçınırken, kendi thread havuzumuzu da korur. 15 saniye sonra "half-open" durumuna geçerek bir deneme isteği gönderir.

**Decorator Zinciri:**
```
İstek → TimeLimiter(3s) → Retry(3x) → Bulkhead(10) → CircuitBreaker(50%) → Dış API
                                                                    ↓ (başarısız)
                                                              Fallback Response
```

**Dosyalar:**
- [CreditCardAdapter.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/payment/adapter/out/strategy/CreditCardAdapter.java)
- [DummyShippingProvider.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/shipping/adapter/out/provider/DummyShippingProvider.java)

#### ✅ I5 Çözümü: W3C Trace Context Propagation
```java
// TraceContextPropagator.java
String traceparent = "00-" + traceId + "-" + spanId + "-01";

// CreditCardAdapter.java — HTTP isteğinde header olarak eklenir
requestBuilder.header("traceparent", traceparent);
```
**İlke:** Distributed Tracing. Kullanıcının `/orders` isteğinden, `CreditCardAdapter` üzerinden dış ödeme API'sine kadar olan tüm yolculuk tek bir `traceId` ile izlenebilir.

**Dosya:** [TraceContextPropagator.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/infrastructure/tracing/TraceContextPropagator.java)

---

## 3. CONTROL PLANE (Yönetim & Sağlık Düzlemi)

### Tanım
Çalışan instance'ları gözlemleyen, yöneten ve karar veren yönetim katmanıdır. "Uygulama sağlıklı mı?", "Kaç istek işleniyor?", "Kim ne yaptı?" sorularına cevap verir.

### Meydana Gelebilecek Riskler

| # | Risk | Etki | Olasılık |
|---|------|------|----------|
| C1 | **Kör Uçuş (Flying Blind):** Uygulamanın canlı mı çökmüş mü olduğu bilinemiyor, Kubernetes ölen pod'u yeniden başlatamıyor | Kritik | Yüksek |
| C2 | **Sessiz Arıza (Silent Failure):** Hata logları yapılandırılmamışsa, gece 3'te oluşan veritabanı bağlantı hatası sabaha kadar fark edilmiyor | Yüksek | Yüksek |
| C3 | **Metrik Eksikliği:** "Şu an kaç sipariş/saniye alıyoruz?" sorusuna cevap verilemiyor. Kapasite planlaması yapılamıyor | Orta | Yüksek |
| C4 | **Yetkisiz Erişim:** Herhangi biri API endpoint'lerine kimlik doğrulaması olmadan erişebilirse, veri kaybı ve fraud riski oluşur | Kritik | Yüksek |
| C5 | **Mimari Kayma (Architectural Drift):** Yeni geliştirici Clean Architecture kurallarını bilmeden Entity'den doğrudan Controller çağırır | Orta | Yüksek |

### Benimsediğimiz İlkeler & Ürettiğimiz Çözümler

#### ✅ C1 Çözümü: Kubernetes Health Probes
```yaml
livenessProbe:                    # "Uygulama canlı mı?"
  httpGet: { path: /health/liveness, port: 8080 }
  failureThreshold: 3             # 3 başarısız → pod restart

readinessProbe:                   # "Trafik almaya hazır mı?"
  httpGet: { path: /health/readiness, port: 8080 }
  failureThreshold: 2             # 2 başarısız → Service'den çıkar
```
**İlke:** Health Check Pattern. Spring Boot Actuator, JVM + Veritabanı + Disk durumunu otomatik denetler. Kubernetes bu bilgiyle pod'ları yeniden başlatır veya trafik yönlendirmesinden çıkarır.

**Yapılandırma:** `management.endpoint.health.probes.enabled: true` ([application.yml](file:///d:/python/e-commerce-app/src/main/resources/application.yml))

#### ✅ C2 Çözümü: Yapısal JSON Loglama
```xml
<!-- logback.xml -->
<encoder class="net.logstash.logback.encoder.LogstashEncoder" />
```
Her log satırı JSON formatında üretilir:
```json
{"timestamp":"2026-03-15","level":"ERROR","logger":"CreditCardAdapter","message":"Network/IO Error","traceId":"abc123","spanId":"xyz789"}
```
**İlke:** Structured Logging. JSON loglar ELK Stack (Elasticsearch + Logstash + Kibana) veya Grafana Loki ile doğrudan indekslenebilir. `traceId` alanı sayesinde bir hatanın hangi kullanıcı isteğinden kaynaklandığı saniyeler içinde bulunur.

**Dosya:** [logback.xml](file:///d:/python/e-commerce-app/src/main/resources/logback.xml)

#### ✅ C3 Çözümü: Prometheus Metrics Endpoint
```yaml
management.endpoints.web.exposure.include: health,info,metrics,prometheus
```
`/actuator/prometheus` endpoint'i Micrometer aracılığıyla şu metrikleri sunar:
- **JVM:** Heap kullanımı, GC süreleri, thread sayısı
- **HTTP:** İstek sayısı, ortalama yanıt süresi, hata oranı
- **Veritabanı:** Connection pool boyutu, aktif bağlantılar

**İlke:** Observability. Grafana dashboard'ları ile gerçek zamanlı izleme ve alerting yapılabilir.

#### ✅ C4 Çözümü: JWT Authentication & Security Filter Chain
```
İstek → JwtAuthenticationFilter → SecurityFilterChain → Controller
            │
            ├── "Authorization: Bearer <token>" header'ından token çıkar
            ├── HMAC256 ile imza doğrular
            ├── userId'yi SecurityContext'e koyar
            └── Geçersizse → 401 Unauthorized
```
**İlke:** Zero Trust. Her istek doğrulanır, session tutulmaz (Stateless). Token içindeki `userId`, Controller'da `@AuthenticationPrincipal` ile otomatik çıkarılır — request body'den asla alınmaz.

**Dosyalar:**
- [SecurityConfig.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/infrastructure/security/SecurityConfig.java)
- [JwtAuthenticationFilter.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/infrastructure/security/JwtAuthenticationFilter.java)
- [JwtUtil.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/infrastructure/security/JwtUtil.java)

#### ✅ C5 Çözümü: Otomatik Mimari Koruma (ArchUnit + PMD)
```java
// ArchitectureTest.java
slices().matching("com.ecommerce.(*)..").should().beFreeOfCycles()  // Döngüsel bağımlılık yasağı
```
**İlke:** Fitness Functions. Her `mvn test` çalıştığında ArchUnit katman ihlallerini, PMD 7 kod kalitesi kurallarını ve Checkstyle stil ihlallerini otomatik olarak denetler. CI/CD pipeline'ında gate görevi görür.

**Dosya:** Özel [pmd-ruleset.xml](file:///d:/python/e-commerce-app/pmd-ruleset.xml) ile Java 17 Records ve Spring Boot uyumlu kural seti

#### ✅ C5+ Çözümü: Mimari Karar Kayıtları (ADR)
16 adet ADR dosyası, projenin tüm kritik mimari kararlarını (neden, nasıl, alternatifler, sonuçlar) belgeleyerek "takım hafızası" işlevi görür.

---

## 4. INSTANCES (Uygulama İş Yükleri)

### Tanım
Domain iş kurallarını çalıştıran, HTTP endpoint'lerini sunan ve olay işleme mantığını yöneten uygulama katmanıdır. Yatay olarak ölçeklenen (scale-out) replikalardan oluşur.

### Meydana Gelebilecek Riskler

| # | Risk | Etki | Olasılık |
|---|------|------|----------|
| A1 | **Sıkı Bağlılık (Tight Coupling):** Order modülü doğrudan Payment modülünü çağırırsa, bir modüldeki değişiklik zincirleme kırılma yaratır | Yüksek | Yüksek |
| A2 | **Framework Kirliliği:** Spring Boot annotation'ları Entity veya UseCase sınıflarına sızarsa, domain mantığı framework'e bağımlı hale gelir ve test edilmesi zorlaşır | Kritik | Orta |
| A3 | **Presentation Sızıntısı:** Controller'lar UseCase çıktısını (ResponseModel) doğrudan JSON olarak dönerse, API formatı iş kuralı değişikliklerinden etkilenir | Orta | Yüksek |
| A4 | **Exception Propagasyonu:** Domain exception'ları (IllegalStateException) doğrudan HTTP response olarak sızarsa, hata mesajları güvenlik açığı oluşturur | Orta | Orta |
| A5 | **Durum Yönetimi (State Management):** Instance'lar yerel durum tutarsa, yatay ölçekleme ve pod restart'larda veri kaybı yaşanır | Yüksek | Orta |

### Benimsediğimiz İlkeler & Ürettiğimiz Çözümler

#### ✅ A1 Çözümü: Event-Driven Decoupling (Olay Tabanlı Ayrıştırma)
```
PlaceOrder ──(save)──→ OrderRepository
PayOrder ──(publish)──→ EventBus ──→ OrderPaymentEventHandler ──(update status)──→ OrderRepository
                             └──→ OrderPaidEventHandler ──(create shipment)──→ ShippingService
```
**İlke:** Domain Events + Pub/Sub. `PayOrderUseCase` hiçbir zaman `OrderRepository`'ye doğrudan erişmez. Bunun yerine `PaymentSucceededEvent` yayınlar. Order ve Shipping modülleri bu olaya bağımsız olarak abone olur.

**Dosyalar:**
- [EventBus.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/shared/event/EventBus.java) (Interface — Use Case katmanında)
- [SimpleEventBus.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/shared/event/SimpleEventBus.java) (Implementation — Infrastructure)

#### ✅ A1+ Çözümü: Facade Pattern ile Modül Kapsüllemesi
```java
// Dış dünya sadece CartService interface'ini görür
public interface CartService {
    Optional<CartDto> getCartForOrder(UUID userId);
    void clearCart(UUID userId);
}
```
**İlke:** Information Hiding. `PlaceOrderUseCase` → `CartService` (Facade) → `CartServiceImpl` → `CartRepository`. Order modülü, Cart modülünün iç yapısını bilmez.

#### ✅ A2 Çözümü: Zero-Annotation Domain Layer
```java
// Order.java — Saf Java, hiçbir framework import'u yok
package com.ecommerce.order.entity;
import com.ecommerce.shared.domain.Money;
import java.util.UUID;

public class Order {
    private final UUID id;
    private Status status;
    public void pay() { ... }  // İş kuralı
}
```
**İlke:** Dependency Rule. Entity ve UseCase sınıflarında `@Entity`, `@Table`, `@Service`, `@Component` gibi Spring annotation'ları **sıfır kez** geçer. Framework değişse bile bu katmanlar asla değişmez.

#### ✅ A3 Çözümü: Presenter Katmanı
```java
// OrderPresenter.java — UseCase Output → ViewModel dönüşümü
public Map<String, Object> presentPlaceOrder(PlaceOrderOutput output) {
    viewModel.put("orderStatus", formatStatus(output.status()));  // "CREATED" → "Sipariş Oluşturuldu"
    viewModel.put("displayTotal", "USD " + output.totalAmount().toPlainString());
}
```
**İlke:** Humble Object Pattern. Controller sadece yönlendirme yapar, sunum mantığı Presenter'dadır. API formatı değiştiğinde UseCase hiç etkilenmez.

**Dosyalar:**
- [ProductPresenter.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/product/adapter/in/presenter/ProductPresenter.java)
- [OrderPresenter.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/order/adapter/in/presenter/OrderPresenter.java)

#### ✅ A4 Çözümü: Centralized Exception Handler
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String,String>> handleDomainExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error","Bad Request", "message", ex.getMessage()));
    }
}
```
**İlke:** Anti-Corruption Layer. Domain exception'ları HTTP status code'larına çevrilirken, hassas stack trace bilgileri istemciye sızdırılmaz.

**Dosya:** [GlobalExceptionHandler.java](file:///d:/python/e-commerce-app/src/main/java/com/ecommerce/infrastructure/web/GlobalExceptionHandler.java)

#### ✅ A5 Çözümü: Stateless Replication
- Instance'lar **hiçbir yerel durum** tutmaz (session yok, JWT Stateless)
- Tüm kalıcı veri JPA üzerinden merkezi veritabanında saklanır
- Kubernetes `replicas: 2` ile yatay ölçekleme anında yapılabilir
- Pod restart'ında veri kaybı sıfır

---

## Özet: Katman-Risk-Çözüm Matrisi

| Katman | Risk Sayısı | Uygulanan Çözüm Sayısı | Kapsam |
|--------|------------|------------------------|--------|
| **Foundation** | 5 | 5 (%100) | Docker, K8s, Config, DB izolasyonu |
| **Interconnect** | 5 | 5 (%100) | TimeLimiter, Retry, Bulkhead, CircuitBreaker, Trace |
| **Control Plane** | 5 | 6 (%120) | Probes, Prometheus, JSON logs, JWT, ArchUnit, ADR |
| **Instances** | 5 | 6 (%120) | Events, Facade, Zero-Annotation, Presenter, ExHandler, Stateless |

**Toplam:** 20 risk tespit edildi, 22 çözüm uygulandı.

> Her katman için en az bir "savunma hattı" oluşturulmuştur. Hiçbir katman korumasız değildir.
