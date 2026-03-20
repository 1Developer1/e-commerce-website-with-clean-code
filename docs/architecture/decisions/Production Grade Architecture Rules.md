# Release It! — Production-Grade Architecture Rules  
*(LLM için zorunlu mimari kurallar listesi)*

Bu belge, bir LLM’in tasarladığı her mimari, API, servis, bileşen, altyapı veya yazılım parçasında **mutlaka uyması gereken üretim-odaklı kuralları** içerir.  
Kaynak: *Release It! Second Edition – Michael Nygard*

---

# 1. STABILITY PATTERNS (Zorunlu Uygulanacak)

## ✅ Timeouts
- Tüm dış isteklerde timeout zorunludur.  
- Sonsuz bekleme yasaktır.  
- Timeout değerleri üretim ölçümlerine göre belirlenmelidir.

## ✅ Circuit Breaker
- Her entegrasyon noktası circuit breaker ile korunmalıdır.  
- Kapalı / Açık / Yarı-açık durumları izlenebilir olmalıdır.

## ✅ Bulkheads (Kaynak İzolasyonu)
- Thread pool’lar, connection pool’lar hizmet başına izole edilmelidir.  
- Bir bileşenin çöküşü diğerlerini etkileyemez.

## ✅ Fail Fast
- Hazır olmayan işlem başlamamalı.  
- Kaynak müsait değilse anında hata dönülmeli.

## ✅ Let It Crash
- Sorunlu bileşenler kullanıcıyı oyalamadan çöksün ve yeniden başlasın.  
- Sessiz bozulma (silent failure) yasaktır.

## ✅ Steady State
- Sistem mümkün olduğunca insan müdahalesine ihtiyaç duymadan dengede kalmalıdır.  
- Üretim ortamında elle değişiklik yapmak minimize edilmelidir.

## ✅ Shed Load (Yük Reddetme)
- Sistem kapasitesini aşınca kontrollü şekilde istekleri reddetmelidir.  
- Degrade mod / gracefully degrade uygulanmalıdır.

## ✅ Create Back Pressure
- İç kuyruklar dolduğunda üreticiler otomatik olarak yavaşlatılmalıdır.  
- Sınırsız buffer yasaktır.

## ✅ Governor (Hız Sınırlama)
- Otomasyon kendini hızla çoğaltarak sisteme zarar veremez.  
- Rate limiting zorunludur.

## ✅ Handshaking
- Servisler hazır değilse “hazır değilim” diyebilmelidir.  
- İletişim readiness durumuna göre şekillenmelidir.

## ✅ Test Harnesses
- Kaos testleri (latency injection, dependency failure simulation) zorunludur.  
- Üretim-vari yük testleri yapılmalıdır.

## ✅ Decoupling Middleware
- Servisler protokolden olabildiğince soyutlanmalıdır.  
- Sıkı bağlı (tightly-coupled) sistemler yasaktır.

---

# 2. STABILITY ANTIPATTERNS (Kesinlikle Kaçınılacak)

## ❌ Integration Points
- Dış sistemler güvenilir varsayılmayacak.  
- Her entegrasyon risk yüzeyi olarak ele alınacak.

## ❌ Chain Reactions
- Tek failure diğer bileşenleri tetikleyemez.  
- Kaynak paylaşımı kontrollü olmalıdır.

## ❌ Cascading Failures
- Bir bileşen yavaşlarsa diğerleri de duramaz.  
- Breaker + timeout zorunludur.

## ❌ Users (Dogpile Etkisi Dahil)
- Kullanıcı trafiği öngörülemezdir; limitlenmelidir.  
- Bot / scraper etkisine hazırlıklı olunmalıdır.

## ❌ Blocked Threads
- Sonsuz bekleyen thread veya connection yasaktır.

## ❌ Self-Denial Attacks
- Sistem kendi operasyonlarıyla kendini öldüremez.  
- İç job’lar (cron, batch, reindex) kontrollü olmalıdır.

## ❌ Scaling Effects
- Lineer ölçeklenme varsayımı yasaktır.  
- Bağlantı ve concurrency sayıları non-linear büyür.

## ❌ Unbalanced Capacities
- Servislerin kapasiteleri dengeli olmalıdır.  
- Zayıf halka tüm sistemi batıramaz.

## ❌ Dogpile
- Cache invalidation veya restart sonrası yük yığılması engellenmelidir.

## ❌ Force Multiplier
- Hatalı otomasyon çarpan etkisi üretemez.  
- Autoscaler agresif davranamaz.

## ❌ Slow Responses
- Yavaş istekler sessizce sistem kaynaklarını tüketemez.  
- Timeouts zorunludur.

## ❌ Unbounded Result Sets
- Limitsiz sorgular yasaktır.  
- Pagination zorunludur.

---

# 3. DESIGN FOR PRODUCTION  
*(Katman Bazlı Kurallar)*

## 🧱 FOUNDATION (Temel Katman)
- Network latency, jitter, routing açık şekilde modellenmeli.  
- VM, container, fiziksel host kapasite sınırları bilinmeli.  
- Cloud zone/region failure senaryoları tasarlanmalı.

## 🧩 PROCESSES ON MACHINES
- Kod deterministik build edilmelidir.  
- Konfigürasyon ve kod kesinlikle ayrılmalıdır.  
- Health-check, liveness-check, readiness-check zorunlu.  
- Secrets yönetimi şeffaf ve güvenli olmalıdır.

## 🔗 INTERCONNECT
- DNS fallback aktif olmalı.  
- Load balancing stratejisi belirli olmalı.  
- Service discovery sağlam olmalı.  
- Rate limiting ve demand control zorunludur.  
- Network partition senaryoları düşünülmelidir.

## 🎛 CONTROL PLANE
- Config service zorunlu.  
- Deployment pipeline zero-downtime olacak.  
- Autoscaling politikaları fail-safe olmalı.  
- Orchestration sistemi (K8s/ECS) ile uyumlu çalışılmalı.  
- System-wide transparency sağlanmalı.  

## 🔐 SECURITY
- OWASP Top 10 uygulanmalı.  
- Least privilege prensibi zorunlu.  
- Audit logging zorunludur.  
- Konfigüre edilmiş parolalar asla commit edilmez.  
- Güvenlik sürekli bir süreçtir.

---

# 4. OPERATIONAL RULES  
*(Üretim Disiplini)*

## 📡 Observability
- Log + Metrics + Traces üçlüsü zorunludur.  
- Hata oranı, latency, trafik ve saturation sürekli izlenmelidir.

## 🩺 Diagnostics
- Vital signs: latency, error rate, throughput, saturation.  
- Synthetic checks ile dış bağımlılıklar test edilmeli.

## 🚀 Deployment
- Blue-green / canary deployment zorunlu.  
- Rollback mekanizması hazır olmalıdır.  
- Config versioning zorunludur.

## 📈 Capacity Planning
- Load test zorunlu.  
- Chaos test zorunlu.  
- DB ve cache kapasiteleri eğri şeklinde takip edilmeli.

## 🔥 Resilience Testing
- Dependency failure simulation.  
- Latency injection.  
- DNS failure.  
- Instance kill tests.

## 📘 Runbooks
- Tüm kritik bileşenler için operasyon runbook’u olmalı.  
- Alert’ler aksiyon alınabilir olmalı.  
- Alert gürültüsü minimize edilmelidir.

---

# 5. LLM Kullanımı İçin Kısa Talimat

Bir LLM, mimari üretirken aşağıdaki yasaları **kesinlikle ihlal edemez**:

1. Tüm dış çağrılar timeout’lu olmalı.  
2. Her bağımlılık circuit breaker ile korunmalı.  
3. Tüm kaynaklar bulkhead ile izole edilmeli.  
4. Sınırsız kuyruk, buffer, liste, sorgu yasak.  
5. Yavaş yanıtlar fail fast ile kesilmeli.  
6. Kapasite aşımı durumunda yük reddedilmeli.  
7. Her bileşende health/readiness check olmalı.  
8. DNS, LB, routing sorunları düşünülmeli.  
9. Observability (log/metric/trace) zorunlu.  
10. Deployment zero-downtime olacak.  
11. Security: OWASP Top 10 + least privilege.  
12. Chaos testleri baştan planlanmalı.  
13. Tüm konfigürasyon koddan ayrı tutulmalı.  
14. Retry storm, cascading failure, dogpile engellenmeli.  
15. Sistem minimum insan müdahalesi ile steady-state’te kalmalı.

---

# Sonuç  
Bu dosya, bir LLM’in üreteceği her sistemde **üretim-disiplini, sağlamlık, dayanıklılık ve operasyonel gerçeklik** standartlarını garanti eder.
