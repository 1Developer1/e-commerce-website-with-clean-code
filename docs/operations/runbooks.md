# Operasyonel Runbook'lar — E-Commerce Platform

Bu belge, canlı ortamda (production) yaşanabilecek kritik olaylara müdahale prosedürlerini içerir.

---

## 🔴 Runbook 1: Circuit Breaker OPEN Durumu

**Alert:** `resilience4j.circuitbreaker.state == OPEN`

**Belirtiler:**
- Ödeme/Kargo istekleri anında fallback dönüyor
- Logda `[Fallback] Exception captured by Resilience4j` mesajları

**Aksiyon:**
1. Dış servisin (Ödeme/Kargo API) durumunu kontrol et
2. `curl -s https://payment-api.example.com/health` ile bağlantıyı test et
3. Dış servis çalışıyorsa → Devre 15 saniye sonra otomatik half-open'a geçecek
4. Dış servis çökmüşse → Servis ekibine bildir, süre uzayabilir
5. **Yapma:** Pod'u yeniden başlatma — Circuit Breaker tasarım gereği koruma sağlıyor

---

## 🟡 Runbook 2: Veritabanı Connection Pool Tükenmesi

**Alert:** `hikaricp.connections.active == hikaricp.connections.max`

**Belirtiler:**
- İstekler yavaşlıyor veya timeout alıyor
- Logda `HikariPool - Connection is not available` hataları

**Aksiyon:**
1. `GET /actuator/metrics/hikaricp.connections.active` ile aktif bağlantı sayısını kontrol et
2. Yavaş sorguları tespit et: `pg_stat_activity` (PostgreSQL) veya H2 console
3. Geçici çözüm: `DB_POOL_MAX` env variable'ını artır (maks 20)
4. Kalıcı çözüm: Yavaş sorguyu optimize et veya caching ekle
5. **Yapma:** Pool boyutunu 50'nin üzerine çıkarma — DB bağlantı limiti var

---

## 🔴 Runbook 3: Pod CrashLoopBackOff

**Alert:** Kubernetes event: `CrashLoopBackOff`

**Belirtiler:**
- Pod sürekli restart ediyor
- `kubectl get pods` → STATUS: CrashLoopBackOff

**Aksiyon:**
1. `kubectl logs <pod-name> --previous` ile son çökme logunu oku
2. Olası nedenler:
   - **OOMKilled:** Memory limit aşıldı → `resources.limits.memory` artır
   - **Application Error:** Başlangıçta exception → kodu kontrol et
   - **Config Error:** Environment variable eksik → `kubectl describe pod` ile env'leri doğrula
3. `kubectl rollout undo deployment/ecommerce-app` ile önceki versiyona geri dön
4. **Yapma:** `kubectl delete pod` ile silme — K8s zaten yeniden başlatıyor

---

## 🟡 Runbook 4: Yüksek Latency (>2s P99)

**Alert:** `http_server_requests_seconds{quantile="0.99"} > 2`

**Belirtiler:**
- Kullanıcılar yavaşlıktan şikayet ediyor
- Grafana'da P99 latency spike

**Aksiyon:**
1. Hangi endpoint yavaş: `/actuator/metrics/http.server.requests` → URI tag'lerini kontrol et
2. Trace ID ile Jaeger/Zipkin'de isteğin yolculuğunu incele
3. Darboğaz tespiti:
   - **DB sorgusu yavaş:** Index ekle veya query optimize et
   - **Dış API yavaş:** TimeLimiter devrede mi kontrol et
   - **GC Pause:** JVM GC loglarını incele
4. Geçici çözüm: Replika sayısını artır (`kubectl scale --replicas=4`)

---

## 🔴 Runbook 5: Rate Limit Aşımı (HTTP 429 Artışı)

**Alert:** HTTP 429 response oranı > %5

**Belirtiler:**
- Meşru kullanıcılar "Too Many Requests" hatası alıyor
- Logda `[RateLimiter] Rate limit exceeded for IP` mesajları

**Aksiyon:**
1. Hangi IP'lerin en çok 429 aldığını loglardan tespit et
2. Bot/scraper ise → IP'yi Web Application Firewall (WAF) ile engelle
3. Meşru trafik artışıysa → Rate limit'i geçici olarak artır
4. DDoS saldırısıysa → CDN/WAF seviyesinde engelleme uygula

---

## 🟢 Runbook 6: Routine Deployment

**Prosedür:**
1. `mvn clean verify` — tüm testler geçmeli
2. `docker build -t ecommerce-app:vX.Y.Z .`
3. `docker push registry.example.com/ecommerce-app:vX.Y.Z`
4. `kubectl set image deployment/ecommerce-app ecommerce-app=registry.example.com/ecommerce-app:vX.Y.Z`
5. `kubectl rollout status deployment/ecommerce-app` — RollingUpdate tamamlanmasını bekle
6. Smoke test: `curl -H "Authorization: Bearer $TOKEN" https://api.example.com/products`
7. Sorun varsa: `kubectl rollout undo deployment/ecommerce-app`
