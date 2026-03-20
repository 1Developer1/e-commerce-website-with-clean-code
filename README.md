# Clean Architecture E-Commerce System 🛍️

Java 17 ile geliştirilmiş, **Robert C. Martin'in Clean Architecture** prensiplerini, SOLID tasarımını, **SRE (Site Reliability Engineering)** kalıplarını ve **güvenlik/gözlemlenebilirlik** katmanlarını uygulayan üretim hazırlığına sahip bir E-Ticaret backend sistemi.

---

## 🏛️ Mimari Felsefe

Projenin temel amacı, **Enterprise Business Rules (Entity)** ve **Application Business Rules (Use Case)** katmanlarını dış teknik detaylardan (Database, Web Framework, API) tamamen izole etmektir.

- **Dependency Rule (Bağımlılık Kuralı):** Kaynak kodu bağımlılıkları *yalnızca içe doğru* akar. İç katmanlar dış katmanlar hakkında hiçbir şey bilmez.
- **Screaming Architecture:** Klasör yapısı `Models/Views/Controllers` yerine iş alanlarını (Cart, Order, Payment...) haykırır.
- **Deferred Decisions:** Veritabanı ve Web Framework seçimleri en sona ertelendi. Sistem ilk olarak tamamen In-Memory Repository'ler ile geliştirildi.

### Katman Mimarisi

```
┌──────────────────────────────────────────────────────────┐
│                  Frameworks & Drivers                     │
│  Spring Boot · JPA · HttpClient · Resilience4j · JWT     │
├──────────────────────────────────────────────────────────┤
│                  Interface Adapters                       │
│  Controllers · Presenters · JPA Adapters · Event Handlers│
├──────────────────────────────────────────────────────────┤
│                  Application Use Cases                    │
│  CreateProduct · PlaceOrder · PayOrder · AddToCart · ...  │
├──────────────────────────────────────────────────────────┤
│                  Enterprise Entities                      │
│  Product · Order · Cart · User · Money (Value Object)    │
└──────────────────────────────────────────────────────────┘
         ↑ Bağımlılıklar her zaman yukarıdan aşağıya akar
```

---

## 📦 Modüler Yapı (Bounded Contexts)

```
com.ecommerce/
├── cart/           # Sepet yönetimi (Entity, UseCase, Adapter, API, Facade)
├── discount/       # İndirim kupon sistemi
├── order/          # Sipariş oluşturma ve durum yönetimi
├── payment/        # Ödeme işleme (Strategy Pattern: Kredi Kartı / Havale)
├── product/        # Ürün kataloğu (CRUD)
├── shipping/       # Kargo takip ve gönderim (External API entegrasyonu)
├── user/           # Kullanıcı bilgileri
├── shared/         # Ortak bileşenler (Money Value Object, EventBus, DomainEvent)
└── infrastructure/ # Glue Code (Spring Config, Security, Tracing, Web)
```

Her modül kendi içinde aşağıdaki Clean Architecture katmanlarına ayrılmıştır:

```
module/
├── entity/                     # Domain Entity (Saf Java, 0 framework bağımlılığı)
├── usecase/                    # Use Case + Input/Output + Port Interfaces
├── adapter/
│   ├── in/controller/          # Inbound Adapter (REST Controller)
│   ├── in/presenter/           # Presenter (UseCase Output → ViewModel)
│   ├── in/event/               # Event Handler (Inbound Event Subscriber)
│   └── out/persistence/        # Outbound Adapter (Repository Implementation)
├── api/                        # Public Module API (Facade Interface)
└── internal/                   # Facade Implementation (Encapsulated)
```

---

## 🔌 API Endpoint'leri

### Kimlik Doğrulama
| Metot | Endpoint | Açıklama | Yetki |
|-------|----------|----------|-------|
| `POST` | `/auth/login` | JWT token üretir | 🔓 Public |

### Ürünler
| Metot | Endpoint | Açıklama | Yetki |
|-------|----------|----------|-------|
| `POST` | `/products` | Yeni ürün oluşturur (`@Valid` input validation) | 🔒 JWT |
| `GET` | `/products?page=0&size=20` | Ürünleri sayfalayarak listeler (max 100/sayfa) | 🔒 JWT |

### Sepet
| Metot | Endpoint | Açıklama | Yetki |
|-------|----------|----------|-------|
| `POST` | `/cart` | Sepete ürün ekler | 🔒 JWT |
| `POST` | `/cart/discount` | İndirim kuponu uygular | 🔒 JWT |

### Siparişler
| Metot | Endpoint | Açıklama | Yetki |
|-------|----------|----------|-------|
| `POST` | `/orders` | Sepetten sipariş oluşturur | 🔒 JWT |

### Ödeme
| Metot | Endpoint | Açıklama | Yetki |
|-------|----------|----------|-------|
| `POST` | `/payments` | Siparişi öder (Strategy: CREDIT_CARD / BANK_TRANSFER) | 🔒 JWT |

### Operasyonel (Actuator)
| Metot | Endpoint | Açıklama | Yetki |
|-------|----------|----------|-------|
| `GET` | `/actuator/health/liveness` | Liveness probe (K8s) | 🔓 Public |
| `GET` | `/actuator/health/readiness` | Readiness probe (K8s) | 🔓 Public |
| `GET` | `/actuator/prometheus` | Prometheus metrics scraping | 🔓 Public |

> **Not:** `userId` artık request body'den alınmıyor. JWT token'dan `@AuthenticationPrincipal` ile otomatik olarak çıkarılıyor.

---

## 🔒 Güvenlik (Authentication & Authorization)

```
Client Request → JwtAuthenticationFilter → SecurityFilterChain → Controller
                      │
                      ├── Authorization header'dan Bearer token çıkarır
                      ├── JwtUtil ile token'ı doğrular (HMAC256)
                      ├── userId'yi SecurityContext'e yerleştirir
                      └── Geçersizse → 401 Unauthorized
```

| Bileşen | Dosya | Sorumluluk |
|---------|-------|-----------|
| `JwtUtil` | `infrastructure/security/` | Token üretme ve doğrulama |
| `JwtAuthenticationFilter` | `infrastructure/security/` | HTTP request filtresi |
| `SecurityConfig` | `infrastructure/security/` | Endpoint koruma kuralları |
| `AuthController` | `infrastructure/web/` | `/auth/login` mock endpoint |

**Korunan endpoint'ler:** `/products/**`, `/cart/**`, `/orders/**`, `/payments/**`
**Açık endpoint'ler:** `/auth/**`, `/actuator/**`, `/h2-console/**`

---

## 📊 Gözlemlenebilirlik (Observability)

### Metrikler (Micrometer + Prometheus)
- Spring Boot Actuator üzerinden `/actuator/prometheus` endpoint'i
- JVM metrikleri, HTTP istek sayıları, veritabanı bağlantı havuzu durumu
- Grafana dashboard'ları ile görselleştirmeye hazır

### Dağıtık İzleme (OpenTelemetry)
- Micrometer Tracing + OpenTelemetry bridge
- Logback JSON loglarına `traceId` ve `spanId` otomatik eklenir
- **W3C Trace Context Propagation:** `CreditCardAdapter` ve `DummyShippingProvider` dış HTTP çağrılarına `traceparent` header'ı otomatik enjekte eder

```
[İstemci] → [API Gateway] → [PlaceOrderUseCase] → [CreditCardAdapter] → [Dış Ödeme API'si]
   │              │                  │                      │
   traceId────────┼──────────────────┼──────────────────────┘
                  │                  │           traceparent header
```

### JSON Loglama (Logstash Encoder)
```json
{
  "timestamp": "2026-03-14T21:00:00.000Z",
  "level": "INFO",
  "logger": "c.e.payment.adapter.CreditCardAdapter",
  "message": "Dış Ödeme API'si çağrılıyor...",
  "traceId": "abc123def456",
  "spanId": "789xyz"
}
```

---

## 🛡️ SRE & Production Readiness

### Resilience4j Zırhları (Outbound Adapters)

Her dış API çağrısı (`CreditCardAdapter`, `DummyShippingProvider`) 4 katmanlı koruma ile sarılıdır:

```
Request → TimeLimiter (3s) → Retry (3 deneme) → Bulkhead (max 10 eşzamanlı) → CircuitBreaker (50% eşik) → Fallback
```

| Kalıp | Konfigürasyon | Amacı |
|-------|--------------|-------|
| **TimeLimiter** | 3 saniye timeout | Yavaş API'lerden korunma |
| **Retry** | 3 deneme, 500ms backoff | Geçici hataları tolere etme |
| **Bulkhead** | Maks 10 eşzamanlı çağrı | Thread havuzunu koruma |
| **CircuitBreaker** | %50 hata eşiği, 15s açık kalma | Çökmüş servislere istek göndermeme |

### Production Grade Rules (Release It!)

| Kural | Uygulama | Bileşen |
|-------|----------|--------|
| **Rate Limiting** | IP başı 20 req/sec, HTTP 429 + `Retry-After` | `RateLimitingFilter` |
| **Pagination** | `findAll(page, size)`, MAX 100/sayfa | `ListProductsUseCase` |
| **Back Pressure** | EventBus max 50 subscriber/event | `SimpleEventBus` |
| **Input Validation** | `@Valid` + JSR-380 (`@NotBlank`, `@Positive`) | `CreateProductRequest` |
| **Audit Logging** | POST/PUT/DELETE → userId + path + ip | `AuditLoggingInterceptor` |
| **Log Rotation** | 50MB/dosya, 30 gün, 1GB toplam cap | `logback.xml` |
| **Secrets** | `${JWT_SECRET}` env variable + K8s Secret | `application.yml` |
| **Connection Pool** | HikariCP: max=10, min=5, timeout=3s | `application.yml` |
| **Graceful Shutdown** | `server.shutdown: graceful` + 30s timeout | `application.yml` |

### Konteyner & Kubernetes

```dockerfile
# Multi-stage build, Non-root, Alpine-based
FROM eclipse-temurin:17-jdk-alpine AS builder
FROM eclipse-temurin:17-jre-alpine AS runtime
RUN adduser -D ecommerceuser
USER ecommerceuser
```

```yaml
# k8s/deployment.yaml — Zero-Downtime Deployment
strategy:
  type: RollingUpdate
  rollingUpdate: { maxUnavailable: 0, maxSurge: 1 }
resources:
  limits: { cpu: "500m", memory: "512Mi" }
livenessProbe: /actuator/health/liveness
readinessProbe: /actuator/health/readiness
```

```yaml
# k8s/hpa.yaml — Fail-Safe Autoscaling
minReplicas: 2
maxReplicas: 5
scaleDown:
  stabilizationWindowSeconds: 300  # 5 dk bekle
  policies: [{ type: Pods, value: 1 }]  # Tek seferde maks 1 pod azalt
```

---

## 🧩 Tasarım Kalıpları (Design Patterns)

| Kalıp | Kullanım Yeri |
|-------|--------------|
| **Strategy** | `PaymentGateway` → CreditCard / BankTransfer seçimi |
| **Facade** | `CartService` / `ShippingService` → modül içi detayları gizler |
| **Repository** | `OrderRepository`, `CartRepository`, `ProductRepository` |
| **Mapper** | `OrderPersistenceAdapter` → Domain ↔ JPA Entity dönüşümü |
| **DTO** | `CartDto`, `ShipmentDto` → katmanlar arası veri taşıma |
| **Domain Events** | `PaymentSucceededEvent` → `OrderPaidEventHandler` / `ShippingHandler` |
| **Factory Method** | `Order.create()`, `Product.create()`, `Cart.create()` |
| **Value Object** | `Money` → immutable, equals/hashCode override |
| **Decorator** | Resilience4j Circuit → Retry → Bulkhead → TimeLimiter zinciri |
| **Presenter** | `ProductPresenter`, `OrderPresenter` → UseCase Output → ViewModel |
| **Filter Chain** | `JwtAuthenticationFilter` → Spring Security |

---

## 🛠️ Mimari Kalite Koruması

Projenin "architectural drift" (mimari kayma) riskine karşı otomatik korumaları vardır:

| Araç | Ne Yapar? |
|------|----------|
| **ArchUnit** | Döngüsel bağımlılıkları, katman ihlallerini ve isimlendirme kurallarını birim testi olarak doğrular |
| **PMD 7** | Statik kod analizi (karmaşıklık, hata kalıpları, best practices) — Özel `pmd-ruleset.xml` ile yapılandırılmış |
| **Checkstyle** | Kod stili tutarlılığını denetler (Google Java Style) |
| **Fitness Functions** | SLA eşik testleri (sipariş oluşturma süresi vb.) |

```bash
# Tüm kalite kontrollerini çalıştır
mvn clean checkstyle:check pmd:check test

# JDepend bağımlılık metrikleri
mvn jdepend:generate
```

---

## 📚 Mimari Karar Kayıtları (ADR)

Proje boyunca alınan her önemli mimari karar belgelenmiştir:

| # | Karar | Konum |
|---|-------|-------|
| 001 | Clean Architecture benimsenmesi | `docs/adr/` |
| 002 | Modüler monolit yapısı | `docs/adr/` |
| 003 | Altyapı kararlarının ertelenmesi | `docs/adr/` |
| 004 | Mimari uygulama stratejisi | `docs/adr/` |
| 005 | Product ve Cart refactoring | `docs/adr/` |
| 006 | Discount modülü eklenmesi | `docs/adr/` |
| 007 | Payment Strategy Pattern | `docs/adr/` |
| 008 | DTO Pattern kullanımı | `docs/adr/` |
| 009 | Domain Events ayrıştırma | `docs/adr/` |
| 010 | Module Facade Pattern | `docs/adr/` |
| 011 | Strict Module Encapsulation | `docs/adr/` |
| 012 | Shipping modül tasarımı | `docs/adr/` |
| 013 | Strict Module Encapsulation v2 | `docs/adr/` |
| 014 | SRE Deployment & Logging | `docs/architecture/decisions/` |
| 015 | Spring Boot & JPA Integration | `docs/architecture/decisions/` |
| 016 | Trace Propagation & Presenter | `docs/architecture/decisions/` |
| 017 | Production Grade Rules | `docs/architecture/decisions/` |
| 018 | Test Harness (WireMock) | `docs/architecture/decisions/` |

---

## 🚀 Quickstart

### Gereksinimler
- Java 17+
- Maven 3.8+

### Yerel Çalıştırma (Maven)

```bash
# 1. Repoyu klonla
git clone <repo-url> && cd e-commerce-app

# 2. Ortam değişkenlerini ayarla
cp .env.example .env

# 3. Derle ve çalıştır
mvn clean package -DskipTests
mvn spring-boot:run
```

### Docker ile Çalıştırma

```bash
# Alpine imajını oluştur
docker build -t ecommerce-app:latest .

# Çalıştır
docker run -p 8080:8080 --env-file .env ecommerce-app:latest
```

### Kubernetes'e Deploy

```bash
kubectl apply -f k8s/deployment.yaml
```

### Token Alma ve API Kullanımı

```bash
# 1. JWT token al
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token')

# 2. Ürün oluştur
curl -X POST http://localhost:8080/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"MacBook Pro","description":"Laptop","price":1999.99,"currency":"USD","stockQuantity":10}'

# 3. Ürünleri listele
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/products
```

---

## 🧪 Test Suite

```bash
# Tüm testleri çalıştır
mvn clean test

# Sadece Architecture testleri
mvn test -Dtest=ArchitectureTest

# Sadece Fitness testleri
mvn test -Dtest=FitnessTest

# WireMock Chaos testleri (gerçek HTTP seviyesi)
mvn test "-Dtest=CreditCardAdapterWireMockTest"
mvn test "-Dtest=DummyShippingProviderWireMockTest"

# Rate Limiter integration test
mvn test "-Dtest=RateLimitingFilterTest"
```

| Test Kategorisi | Dosya | Ne Test Eder? |
|----------------|-------|--------------|
| **Unit Tests** | `*UseCaseTest.java` | İş mantığı (mock repository ile) |
| **Architecture** | `ArchitectureTest.java` | Katman ihlalleri, döngüsel bağımlılıklar |
| **Fitness** | `FitnessTest.java` | Performans SLA eşikleri |
| **Chaos (Override)** | `CreditCardAdapterTest.java` | CircuitBreaker, TimeLimiter, Retry (method override) |
| **Chaos (WireMock)** | `CreditCardAdapterWireMockTest.java` | Gerçek HTTP: 500 crash, latency, connection refused, corrupt response |
| **Chaos (WireMock)** | `DummyShippingProviderWireMockTest.java` | Gerçek HTTP: 500, slow response, fallback tracking |
| **Integration** | `RateLimitingFilterTest.java` | Rate Limiter: HTTP 429 + Retry-After header |

---

## 📁 Teknoloji Stack

| Katman | Teknoloji |
|--------|----------|
| **Language** | Java 17 |
| **Build** | Maven |
| **Web Framework** | Spring Boot 3.x |
| **Persistence** | Spring Data JPA + H2 (In-Memory) / PostgreSQL |
| **Validation** | Jakarta Bean Validation (JSR-380) — `@Valid`, `@NotBlank`, `@Positive` |
| **Security** | Spring Security + JWT (java-jwt) |
| **Resilience** | Resilience4j (CircuitBreaker, Retry, Bulkhead, TimeLimiter, **RateLimiter**) |
| **Observability** | Micrometer + Prometheus + OpenTelemetry |
| **Logging** | SLF4J + Logback (RollingFileAppender + Logstash JSON Encoder) |
| **Testing** | JUnit 5 + Mockito + ArchUnit + WireMock |
| **Code Quality** | PMD 7 + Checkstyle + JDepend |
| **Containerization** | Docker (Multi-stage Alpine) + Kubernetes (Deployment + HPA) |
