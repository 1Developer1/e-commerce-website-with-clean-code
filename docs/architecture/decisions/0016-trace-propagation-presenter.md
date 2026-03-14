# ADR 016: Trace Propagation ve Presenter Katmanı

**Tarih:** 2026-03-14
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
1. **Trace Propagation:** `CreditCardAdapter` ve `DummyShippingProvider` adaptörleri `java.net.http.HttpClient` ile dış servislere HTTP çağrısı yaparken W3C `traceparent` header'ı eklemiyordu. Bu, dağıtık izleme zincirini (Distributed Tracing) kopuk bırakıyordu.
2. **Presenter Katmanı:** Controller'lar doğrudan UseCase Output record'larını JSON olarak dönüyordu. Clean Architecture'a göre UseCase çıktıları ile View formatı arasına bir Presenter katmanı (Interface Adapter) konulmalıdır.

## Karar (Decision)
1. `TraceContextPropagator` adlı bir Infrastructure utility sınıfı oluşturuldu. Micrometer Tracing `Tracer`'dan aktif span bilgilerini çekerek W3C traceparent header'ı üretir.
2. `ProductPresenter` ve `OrderPresenter` sınıfları Interface Adapters katmanında oluşturuldu. UseCase Output → ViewModel dönüşümü bu sınıflarda yapılır (tarih formatlama, para birimi gösterimi, durum etiketi lokalizasyonu vb.).

## Sonuçlar (Consequences)
**Pozitif:**
- Grafana/Jaeger'da artık uçtan uca izleme görülebilir.
- Controller'lar sadece yönlendirme yapar, sunum mantığı tamamen Presenter'da.
- Presenter'lar bağımsız test edilebilir.

**Negatif:**
- Her yeni UseCase çıktısı için ilgili Presenter metodu da güncellenmelidir.
- `TraceContextPropagator` Micrometer Tracing kütüphanesine bağımlıdır.

## Uyumluluk (Compliance)
- Controller'larda doğrudan UseCase Output dönen metot bulunursa Code Review'da reddedilecek.
- ArchUnit testi ile Presenter sınıflarının `adapter.in.presenter` paketinde olması zorunlu kılınabilir.
