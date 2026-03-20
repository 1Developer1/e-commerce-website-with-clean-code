# ADR 018: Test Harness — WireMock Kaos Testleri ve Rate Limiter Doğrulaması

**Tarih:** 2026-03-21
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Release It! kitabının "Test Harnesses" kuralı, üretim ortamında yaşanabilecek arızaların **gerçek HTTP seviyesinde** test edilmesini zorunlu kılar. Projemizde mevcut `CreditCardAdapterTest` sınıfı, `simulateExternalPayment()` metodunu override ederek test yapıyordu — bu yaklaşım ağ seviyesindeki hataları (connection refused, corrupt response, DNS failure) geçersiz bırakıyordu.

Değerlendirilen Seçenekler:
1. Mevcut yapı (method override) ile devam etmek — yetersiz
2. **WireMock** ile gerçek HTTP mock server kullanmak — kapsamlı
3. Testcontainers + Toxiproxy — aşırı karmaşık bu aşama için

## Karar (Decision)

### 1. WireMock ile Gerçek HTTP Kaos Testleri
**WireMock Standalone** (zaten pom.xml'de mevcut) kullanarak her outbound adaptör için gerçek HTTP seviyesinde arıza simülasyonu testleri oluşturuldu.

**`CreditCardAdapterWireMockTest`** — 5 Senaryo:
1. **Happy Path:** WireMock 200 → pay() = true
2. **Server Crash (500):** WireMock 500 → Retry 3x doğrulaması → Fallback
3. **Latency Injection (5s):** WireMock `fixedDelay(5000)` → TimeLimiter(3s) keser → Retry → Fallback
4. **Connection Refused:** Kapalı port (localhost:1) → Network Error → Fallback
5. **Corrupt Response:** WireMock `MALFORMED_RESPONSE_CHUNK` → Fallback

**`DummyShippingProviderWireMockTest`** — 3 Senaryo:
1. **Happy Path:** 200 → TRK- tracking code
2. **Server Crash:** 500 → TRK-FALLBACK-PENDING
3. **Slow Response:** 3s gecikme → TimeLimiter(2s) → Fallback

### 2. Rate Limiter Integration Test
**`RateLimitingFilterTest`** — Spring `MockMvc` ile tam entegrasyon testi:
- Aynı endpoint'e 25 ardışık istek gönderilir
- İlk 20 istek → başarılı
- Son 5 istek → HTTP 429 + `Retry-After` header doğrulanır

## Sonuçlar (Consequences)
**Pozitif:**
- Resilience4j zinciri artık gerçek HTTP seviyesinde kanıtlanıyor (ağ, TCP, HTTP katmanları dahil)
- `verify(3, postRequestedFor(...))` ile Retry sayısı tam olarak doğrulanıyor
- Rate Limiter'ın çalıştığı entegrasyon seviyesinde ispatlanıyor

**Negatif:**
- WireMock testleri yavaştır (~10-15s) çünkü gerçek Retry + Timeout bekleme süreleri işler
- RateLimitingFilterTest `@SpringBootTest` gerektirir — tüm context ayağa kalkar

## Uyumluluk (Compliance)
- `mvn test -Dtest=CreditCardAdapterWireMockTest` → exit code 0
- `mvn test -Dtest=DummyShippingProviderWireMockTest` → exit code 0
- Yeni outbound adaptör eklendiğinde WireMock test sınıfı zorunlu olarak oluşturulacak (Code Review kuralı)
