# ADR 019: Backend Guvenlik Iyilestirmeleri ve API Tutarliligi

**Tarih:** 2026-03-29
**Durum:** Accepted (Kabul Edildi)

## Baglam (Context)
Projenin kapsamli bir kod incelemesi sonucunda birden fazla guvenlik acigi, tamamlanmamis ozellik ve API tutarsizligi tespit edilmistir. Sorunlar oncelik sirasina gore 4 fazda ele alinmistir:

- **Faz 1 (Kritik):** Odeme gateway'inde HTTP 4xx yanitlarinin basari olarak kabul edilmesi, istemcinin odeme tutarini manipule edebilmesi, kargo adresinin kaydedilmemesi, gecersiz JWT token'larin sessizce gecmesi
- **Faz 2 (Tamamlanmamis Ozellikler):** Mock authentication yerine gercek kullanici kayit/giris sistemi, urun silme 404 yaniti, sepet detay bilgileri
- **Faz 3 (API Tutarsizliklari):** Farkli timestamp formatlari, yanlis sayfalama metadata, eksik CORS header'lari, Turkce enum label'lari
- **Faz 4 (Konfigürasyon):** Hardcoded resilience4j parametreleri, CORS wildcard origin

## Karar (Decision)

### Faz 1 — Guvenlik Duzeltmeleri
1. **CreditCardAdapter:** HTTP status kontrolu `>= 500` yerine `>= 200 && < 300` olarak duzeltildi. Yalnizca 2xx yanitlar basari sayilir.
2. **PayOrderUseCase:** Istemciden `amount`/`currency` alimi kaldirildi. Siparis tutari `OrderQueryPort` uzerinden backend'den cekilir. Ayrica siparis sahiplik kontrolu (userId eslesmesi) eklendi.
3. **Order Entity:** `recipientName` ve `shippingAddress` alanlari eklendi. `create()` ve `restore()` factory metodlari, `PlaceOrderInput`, `OrderJpaEntity` ve `OrderPresenter` guncellendi.
4. **JwtAuthenticationFilter:** Gecersiz token'lar icin `try/catch` ile `401 Unauthorized` donusu ve SLF4J loglama eklendi.

### Faz 2 — Tamamlanmamis Ozellikler
1. **Gercek Auth Sistemi:** `RegisterUserUseCase` ve `LoginUseCase` olusturuldu. BCrypt ile sifre hashleme, `TokenGeneratorPort` ile JWT uretimi. `AuthController` mock'tan gercek implementasyona donusturuldu. `V2__users_schema.sql` Flyway migration eklendi.
2. **ProductController.deleteProduct:** Boolean donus degeri kontrol edilerek 404 yaniti dondurulmesi saglandi.
3. **CartPresenter:** `unitPrice`, `itemTotal`, `totalPrice` ve `currency` bilgileri eklendi.

### Faz 3 — API Tutarliligi
1. **Timestamp Formatlari:** Tum presenter'larda `yyyy-MM-dd'T'HH:mm:ss` (ISO 8601) standardina gecildi.
2. **ProductPresenter:** Yanlis `totalCount` (sayfa elemani) yerine `totalElements` ve `totalPages` kullanildi. `X-Total-Count` ve `X-Total-Pages` HTTP header'lari eklendi.
3. **OrderPresenter:** Turkce `formatStatus()` metodu kaldirildi, ham enum string'ler donuyor.

### Faz 4 — Konfigürasyon
1. **Resilience4j:** CircuitBreaker, Bulkhead, TimeLimiter, Retry parametreleri `application.yml`'e tasindi.
2. **CORS:** `allowedOriginPatterns("*")` yerine `application.yml`'den okunan `cors.allowed-origins` konfigürasyonuna gecildi.

### Ek Duzeltmeler
- `ScenarioRunner`'a `@Profile("demo")` eklendi — test sirasinda Spring context'i kirletmesini onlemek icin.
- `OrderQueryPort` interface'i olusturularak odeme modulunun siparis modulune dogrudan bagimliliginin onune gecildi (Clean Architecture).
- `TokenGeneratorPort` interface'i olusturularak kullanici use case'lerinin altyapi katmanina bagimliliginin onune gecildi.

## Sonuclar (Consequences)
**Pozitif:**
* Odeme manipulasyonu ve gecersiz odeme onaylari gibi kritik guvenlik aciklari kapatildi.
* Gercek kullanici kayit/giris sistemi ile mock auth kaldirildi.
* API response'lari tutarli ve tahmin edilebilir hale geldi (ISO 8601 timestamp, dogru sayfalama).
* Konfigürasyon disarilastirildi, ortam bazli ayar degisikligi kolaylasti.
* Port interface'leri ile moduller arasi bagimsizlik korundu (Clean Architecture).

**Negatif:**
* `PayOrderRequest` breaking change iceriyor — eski istemciler `amount`/`currency` gondermemeli.
* `Order.restore()` ve `PlaceOrderInput` imza degisiklikleri mevcut testlerin guncellenmesini gerektirdi.
* `ScenarioRunner` artik sadece `--spring.profiles.active=demo` ile calisir.

## Uyumluluk (Compliance)
* Tum moduller arasi iletisim port interface'leri uzerinden yapilmalidir (`OrderQueryPort`, `TokenGeneratorPort`).
* Yeni endpoint'ler icin timestamp formati mutlaka `yyyy-MM-dd'T'HH:mm:ss` olmalidir.
* Odeme tutari her zaman backend'den (siparis kaydinden) cekilmeli, istemciye guvenilmemelidir.
* CORS origin'leri production'da `CORS_ALLOWED_ORIGINS` env variable ile sinirlandirilmalidir.
