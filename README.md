# Clean Architecture E-Commerce System 🛍️

Java 17 ile geliştirilmiş, **SOLID tasarımını, **SRE (Site Reliability Engineering)** kalıplarını ve **güvenlik/gözlemlenebilirlik** katmanlarını uygulayan üretim hazırlığına sahip bir E-Ticaret backend sistemi.

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

> **API Version:** `v1` — Tüm endpoint'ler `/api/v1/` prefix'i altındadır.
> **Swagger UI:** Uygulama çalışırken [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) adresinden interaktif API dokümantasyonuna erişilebilir.

### Kimlik Doğrulama
| Metot | Endpoint | Açıklama | HTTP Kodu | Yetki |
|-------|----------|----------|-----------|-------|
| `POST` | `/auth/login` | JWT token üretir | `200 OK` | 🔓 Public |

### Ürünler
| Metot | Endpoint | Açıklama | HTTP Kodu | Yetki |
|-------|----------|----------|-----------|-------|
| `POST` | `/api/v1/products` | Yeni ürün oluşturur (`@Valid`) | `201 Created` | 🔒 JWT |
| `GET` | `/api/v1/products?page=0&size=20` | Ürünleri sayfalayarak listeler | `200 OK` | 🔒 JWT |
| `PUT` | `/api/v1/products/{id}` | Ürünü günceller (`@Valid`) | `200 OK` | 🔒 JWT |
| `DELETE` | `/api/v1/products/{id}` | Ürünü siler | `204 No Content` | 🔒 JWT |

### Sepet
| Metot | Endpoint | Açıklama | HTTP Kodu | Yetki |
|-------|----------|----------|-----------|-------|
| `GET` | `/api/v1/cart` | Sepeti görüntüler | `200 OK` | 🔒 JWT |
| `POST` | `/api/v1/cart/items` | Sepete ürün ekler | `201 Created` | 🔒 JWT |
| `POST` | `/api/v1/cart/discounts` | İndirim kuponu uygular | `200 OK` | 🔒 JWT |

### Siparişler
| Metot | Endpoint | Açıklama | HTTP Kodu | Yetki |
|-------|----------|----------|-----------|-------|
| `POST` | `/api/v1/orders` | Sepetten sipariş oluşturur | `201 Created` | 🔒 JWT |
| `GET` | `/api/v1/orders?page=0&size=20` | Siparişleri listeler | `200 OK` | 🔒 JWT |
| `GET` | `/api/v1/orders/{id}` | Sipariş detayını getirir | `200 OK` | 🔒 JWT |

### Ödeme
| Metot | Endpoint | Açıklama | HTTP Kodu | Yetki |
|-------|----------|----------|-----------|-------|
| `POST` | `/api/v1/payments` | Siparişi öder (Strategy: CREDIT_CARD / BANK_TRANSFER) | `200 OK` / `422` | 🔒 JWT |

### Operasyonel (Actuator)
| Metot | Endpoint | Açıklama | Yetki |
|-------|----------|----------|-------|
| `GET` | `/actuator/health/liveness` | Liveness probe (K8s) | 🔓 Public |
| `GET` | `/actuator/health/readiness` | Readiness probe (K8s) | 🔓 Public |
| `GET` | `/actuator/prometheus` | Prometheus metrics scraping | 🔓 Public |
| `GET` | `/swagger-ui.html` | OpenAPI / Swagger UI | 🔓 Public |

> **Not:** `userId` artık request body'den alınmıyor. JWT token'dan `@AuthenticationPrincipal` ile otomatik olarak çıkarılıyor.

---

## 🌐 Web API Standartları

Bu projede uygulanan Web API tasarım standartları, REST konvansiyonları ve üretim seviyesi kalite kontrolleri aşağıda açıklanmıştır.

### 1. API Versiyonlama (URI-based Versioning)
Tüm iş domain endpoint'leri `/api/v1/` prefix'i altındadır. Bu, gelecekte breaking change yapıldığında mevcut istemcilerin (`v1`) korunmasını ve yeni istemcilerin (`/api/v2/`) yönlendirilmesini sağlar. Altyapı ve kimlik doğrulama endpoint'leri (`/auth/**`, `/actuator/**`) versiyonlanmaz çünkü bunlar uygulama semantiğinden bağımsız, platform seviyesi servislerdir.

### 2. HTTP Durum Kodları (RFC 7231)
Her HTTP metodu, RFC 7231 standardına uygun durum kodu döner:

| İşlem | HTTP Metodu | Durum Kodu | Açıklama |
|-------|-------------|------------|----------|
| Kaynak oluşturma | `POST` | `201 Created` | Yeni kaynak başarıyla oluşturuldu |
| Kaynak okuma | `GET` | `200 OK` | Kaynak başarıyla döndürüldü |
| Kaynak güncelleme | `PUT` | `200 OK` | Kaynak başarıyla güncellendi |
| Kaynak silme | `DELETE` | `204 No Content` | Kaynak silindi, response body yok |
| İş kuralı ihlali | `POST` | `422 Unprocessable Entity` | İstek geçerli ama iş kuralı nedeniyle işlenemez (ör: ödeme başarısız) |
| Doğrulama hatası | Herhangi | `400 Bad Request` | `@Valid` input validation başarısız |
| Yetkisiz erişim | Herhangi | `401 Unauthorized` | JWT token eksik veya geçersiz |
| Kaynak bulunamadı | `GET` | `404 Not Found` | İstenen kaynak mevcut değil |

### 3. Tutarlı Yanıt Formatı (ResponseEntity)
Tüm Controller metotları `ResponseEntity<T>` döner. Bu yaklaşım sayesinde:
- HTTP status code, header ve body üzerinde tam kontrol sağlanır
- Ödeme başarısız olduğunda `200 OK` yerine `422 Unprocessable Entity` döndürülebilir
- Frontend ekipleri status code'a güvenerek akış kontrolü yapabilir

### 4. RESTful URI Tasarımı
URI'ler **isim (noun)** tabanlıdır, **fiil (verb)** içermez:
- ✅ `POST /api/v1/cart/items` (sepete öğe ekle)
- ❌ ~~`POST /cart/add`~~ (fiil içeren eski yapı)
- ✅ `POST /api/v1/cart/discounts` (indirim uygula)
- ❌ ~~`POST /cart/discount`~~ (tekil ve fiil benzeri eski yapı)

### 5. OpenAPI / Swagger Dokümantasyonu
`springdoc-openapi-starter-webmvc-ui` entegre edilmiştir. Uygulama çalışırken:
- **Swagger UI:** [`/swagger-ui.html`](http://localhost:8080/swagger-ui.html) — interaktif API keşfi ve test
- **OpenAPI JSON:** `/v3/api-docs` — frontend SDK üretimi (codegen) için makine tarafından okunabilir şema
- Her endpoint `@Operation` ve `@Tag` anotasyonlarıyla belgelenmiştir
- JWT Bearer Authentication şeması Swagger UI üzerinden test edilebilir

### 6. CORS Konfigürasyonu (Cross-Origin Resource Sharing)
`SecurityConfig` üzerinde global CORS politikası tanımlıdır:
- **İzin verilen metotlar:** `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`
- **İzin verilen header'lar:** `Authorization`, `Content-Type`, `X-Requested-With`
- **Expose edilen header'lar:** `X-Total-Count`, `X-Total-Pages` (pagination bilgisi)
- **Pre-flight cache:** 3600 saniye (1 saat)
- Production ortamında `CORS_ALLOWED_ORIGINS` environment variable'ı ile izin verilen alan adları kısıtlanmalıdır.

### 7. Hata Zarfı — RFC 7807 (Problem Detail)
Tüm hata yanıtları standart bir yapıda döner. Bu sayede frontend ekipleri her hata türünü aynı parser ile işleyebilir:

```json
{
  "type": "https://api.ecommerce.com/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields failed validation.",
  "violations": [
    { "field": "name", "message": "Product name is required" }
  ],
  "timestamp": "2026-03-23T22:00:00Z"
}
```

**Güvenlik:** Generic (beklenmeyen) exception mesajları **asla** istemciye sızdırılmaz. Yalnızca `"An unexpected error occurred. Please try again later."` mesajı döner, gerçek hata detayı sunucu loglarına yazılır.

### 8. Input Validation (Girdi Doğrulama)
Controller sınırında Jakarta Bean Validation (JSR-380) ile girdi doğrulanır. Geçersiz veri domain katmanına ulaşmadan reddedilir:

| Anotasyon | Kullanım | Açıklama |
|-----------|----------|----------|
| `@NotBlank` | `name` | Boş veya null olamaz |
| `@Size(min, max)` | `name`, `description`, `currency` | Karakter uzunluğu sınırı |
| `@Positive` | `price`, `stockQuantity` | Sıfırdan büyük olmalı |
| `@Valid` | Controller metot parametresi | Doğrulamayı tetikler |

### 9. Presenter Pattern (ViewModel Dönüşümü)
UseCase çıktıları doğrudan istemciye dönmez. `ProductPresenter`, `OrderPresenter` gibi Presenter sınıfları UseCase Response'unu ViewModel'e (`Map<String, Object>`) dönüştürür. Bu, Clean Architecture'ın **Output Port** prensibine uygun şekilde iç katman verisinin dış katmana sızdırılmasını engeller.

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

SRE'nin "Four Golden Signals" (Dört Altın Sinyal) yaklaşımına göre sistem sağlığı izlenir:

### İzlenen Metrikler ve SRE Anlamları

SRE kültüründe bir sistemi "Kör Uçuş (Blind Flight)" hatasından kurtarmak için metrikler hayati öneme sahiptir. İzlediğimiz temel metriklerin amaçları şunlardır:

1.  **Latency (Gecikme süresi):** Kullanıcının bir isteğe ne kadar sürede yanıt aldığını gösterir. Ortalama gecikme (Mean) yanıltıcı olabilir, bu nedenle p95 ve p99 (yüzdelik dilimler) izlenir. Sistemin en kötü durum performansını belirlemek ve SLA (Hizmet Seviyesi Anlaşması) ihlallerini tespit etmek için kritiktir.
2.  **Traffic (Trafik / İstek Sayısı):** Sistemin saniyede karşıladığı HTTP isteği veya mesaj sayısıdır. Sistemin kapasite sınırlarını zorlayıp zorlamadığını (örneğin Black Friday trafiği) gösterir. Anormal artışlar DDoS saldırısına işaret edebilir.
3.  **Error Rate (Hata Oranı):** Başarısız olan isteklerin (HTTP 5xx) toplam isteklere oranıdır. Bu oranın sıfır olması beklenmez (Hata Bütçesi/Error Budget konsepti). Ancak bu oran aniden yükselirse bir kod hatası (bug) veya bağımlı bir sistemin çökmesi söz konusudur ve derhal müdahale gerektirir.
4.  **Saturation (Doygunluk):** Sistem kaynaklarının (CPU, Memory, Disk G/Ç, Veritabanı Connection Pool kapasitesi) ne kadarının "dolu" olduğunu gösterir. Doygunluk %100'e yaklaşırken performans genellikle aniden düşer (Cliff effect). Bu metrikler felaket baş göstermeden *önceden* donanım artırımına (Scaling) karar vermek için izlenir.

| Metrik | Neden İzlenir? / Neyi İşaret Eder? | Tipik Kaynak (Endpoint) |
|--------|---------------|-------------------|
| **Latency (Gecikme)** | P99 latency artarsa süreç darboğazları ortaya çıkar, kullanıcılar uygulamayı terk etmeye başlar. | `http.server.requests` (Timer) |
| **Traffic (Trafik)** | Ani yük (spike) kapasite planlamasını etkiler. Organik büyüme veya saldırıyı deşifre eder. | `http.server.requests.count` (Counter) |
| **Error Rate (Hata Oranı)** | Hatalar artınca Error Budget tükenir; yeni feature çıkılması durdurulmalı, altyapı iyileştirilmelidir. | `http.server.requests{status=5xx}` |
| **Saturation (DVM, CPU)** | JVM Memory tükenirse uygulamanın OOM Crash yaşayacağını öngörmemizi sağlar. | `jvm.memory.used`, `system.cpu.usage` |
| **DB Pool Durumu** | Aktif bağlantılar limite ulaştığında (Connection Starvation) istekler Timeout'a düşer. | `hikaricp.connections.active` (Gauge) |
| **CircuitBreaker Durumu** | Dışarıya giden bağlantılardaki bozukluklar, içeriyi de kilitlemesin diye Devre Açılır. | `resilience4j.circuitbreaker.state` |
| **JVM GC Pauses** | Garbage Collection eylemi uzadığında thread'ler donar (Stop the world) ve sunucu kilitlenmiş sanılır. | `jvm.gc.pause` |

Tüm metrikler **Prometheus** tarafından `/actuator/prometheus` endpoint'inden toplanır ve **Grafana** dashboard'larında görselleştirilir.

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

> **Neden Distributed Tracing?** Bir sipariş isteği Gateway → OrderUseCase → PaymentAdapter → Dış API arasında gezinir. Bir yerde yavaşlama varsa, `traceId` ile o spesifik isteğin tüm yolculuğunu görebilir ve darboğazı saniyeler içinde tespit edebilirsiniz.

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

### Audit Logging
Mutating HTTP istekleri (POST/PUT/DELETE) otomatik olarak denetim izine kaydedilir:
```
[AUDIT] user=e49d60ea method=POST path=/orders status=200 ip=192.168.1.1
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

### Production Grade Rules (Üretim Seviyesi Kuralları)

Bu proje salt bir CRUD uygulaması değil, Michael Nygard'ın meşhur "Release It!" kitabında tanımladığı üretim ortamı dayanıklılık kalıplarını (Production-Ready Patterns) uygular. Bir yazılımın prod ortamında "hayatta kalmasını" sağlayan bu kurallar ve projemizdeki karşılıkları şunlardır:

*   **Rate Limiting (Hız Sınırlandırıcı):** Kötü niyetli botların veya yanlış yapılandırılmış bir API istemcisinin saniyeler içinde binlerce istek atarak sistemi çökertmesini engeller. Sistem, ani yük (spike) durumunda kapasitesini korumak için belirlenen sınırın üzerindeki istekleri **HTTP 429 Too Many Requests** ile reddeder. (`RateLimitingFilter`)
*   **Pagination (Sayfalama):** Veritabanından yüz binlerce kaydın aynı anda RAM'e çekilmesini (OutOfMemoryError) engeller. İstekler her zaman "sayfa" ve "büyüklük" limitleriyle sınırlanmıştır. (`ListProductsUseCase`)
*   **Back Pressure (Geri Basınç):** Asenkron olay yönetiminde (EventBus), olay üreticilerin hızı tüketicilerin (subscriber) işleme hızını geçerse sistemin şişmesini ve kuyruk boyutunun sonsuza gitmesini önler.
*   **Input Validation (Girdi Doğrulama):** "IOW" (Input is root of all evil) prensibi gereği OWASP zafiyetlerine karşı ilk kalkandır. Formatı bozuk, beklenen boyutları aşan (örn. çok uzun bir string) veya negatif değer taşıyan girdiler, iş mantığına (Domain) ulaşmadan Web (Controller) katmanında tespit edilerek 400 Bad Request fırlatılır.
*   **Audit Logging (Denetim İzi):** Sistemdeki her kritik değişimi (POST, PUT, DELETE) kimin (userId), nereden (IP), ne zaman yaptığını kalıcı olarak loglar. Sistem geriye dönük incelenmek istendiğinde ve yasal uyumlulukta vazgeçilmezdir. (`AuditLoggingInterceptor`)
*   **Log Rotation (Log Döndürme):** Log dosyalarının sonsuza kadar büyüyerek disk alanını tüketmesini ve sunucuyu kilitlemesini engeller (Steady State ilkesi). Boyuta ulaştığında eski loglar arşivlenir.
*   **Secrets Management (Gizli Bilgi Yönetimi):** Parolalar (DB şifresi) ve anahtarlar (JWT Secret) asla kaynak koda gömülmez. Sadece çalışma anında ortam değişkenlerinden (`ENV`) okunur.
*   **Connection Pooling (Bağlantı Havuzu):** Her veritabanı işlemi için yeni bir ağ bağlantısı açmanın devasa maliyetini önler. `HikariCP` kullanarak minimum ve maksimum bağlantı eşikleriyle thread kilitlenmesi riskini azaltır.
*   **Graceful Shutdown (Nazik Kapatılma):** Uygulama güncelleneceği zaman veya pod öldürüldüğünde, Linux çekirdeğinden gelen *SIGTERM* sinyalini yakalar. Yarım kalmış müşteri işlemlerini ve aktif HTTP isteklerini bitirene kadar (veya 30 saniye süreyle) bekler ve işlemi temizce kapatır.

| Kural | Amacı / Etkisi | Uygulanan Bileşen |
|-------|--------------|--------|
| **Rate Limiting** | DDoS ve botları önleme | `RateLimitingFilter` |
| **Pagination** | `OutOfMemoryError` önleme | `ListProductsUseCase` |
| **Back Pressure** | Olay kuyruklarının şişmesini önleme | `SimpleEventBus` |
| **Input Validation** | Veri tutarlılığı ve güvenlik | `CreateProductRequest` |
| **Audit Logging** | Denetlenebilirlik ve Traceability | `AuditLoggingInterceptor` |
| **Log Rotation** | Diskin dolmasını (Disk Exhaustion) önleme | `logback.xml` |
| **Secrets** | Kod güvenliği ve taşınabilirlik | `application.yml`, `.env` |
| **Connection Pool** | Bloklanmış thread ve connection süresi optimizasyonu | `application.yml` |
| **Graceful Shutdown** | Veri kaybı ve kesinti (Downtime) engelleme | `application.yml` |

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
| 019 | Presenter ViewModels & Filter Ordering | `docs/architecture/decisions/` |

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
