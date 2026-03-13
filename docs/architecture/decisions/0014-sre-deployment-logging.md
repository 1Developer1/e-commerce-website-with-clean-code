# ADR 0014: SRE, Kontrol Düzlemi ve Uygulama Yaşam Döngüsü (Production Readiness)

**Tarih:** 2026-03-09
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Uygulamanın çekirdek iş mantığını (Clean Architecture) ve taktiksel SRE zırhlarını (Resilience4j) tamamladık. Ancak sistemin bulut (Cloud/Kubernetes) ortamlarında güvenli bir şekilde koşturulması, hataların izlenebilmesi ve ortam değişkenlerinin yönetilebilmesi için altyapısal bileşenlere ihtiyaç var. "12-Factor App" ilkeleri ve "Release It!" (Nygard) standartları gereği hard-coded yapılandırmalardan ve okunaksız log formatlarından kurtulmamız gerekiyor.

Değerlendirilen Seçenekler (Loglama):
1. `System.out.println` (Mevcut yapı, log toplayıcılar için uygun değil).
2. SLF4J + Logback ile makine okunabilir (JSON) loglama.

Değerlendirilen Seçenekler (Konfigürasyon):
1. Hard-coded port ve URL'ler kullanmaya devam etmek.
2. `dotenv-java` kullanarak Spring profillerine ihtiyaç duymadan ortam değişkenlerini (ENV) çekmek.

## Karar (Decision)
* Uygulama genelindeki tüm standart konsol çıktıları tamamen kaldırılarak, **SLF4J API ve Logback (Logstash Encoder) tabanlı JSON loglamaya geçeceğiz.**
* Uygulamanın koşturulduğu porta, havuz ayarlarına vb. dair tüm yapılandırmalar **`.env`** (Environment Variables) üzerinden okunacak.
* Uygulamanın orkestratörlerle (Kubernetes) uyum içinde çalışması için **Distroless/Alpine tabanlı, Root yetkisi olmayan Multi-Stage Dockerfile** ve Liveness/Readiness Probe'larını tanımlayan **Kubernetes Deployment Manifest** (deployment.yaml) dosyası kullanılacak.

## Sonuçlar (Consequences)
**Pozitif:**
* Tüm loglar ELK (Elasticsearch, Logstash, Kibana) veya Datadog gibi merkezi loglama sistemlerine doğrudan JSON olarak gönderilebilecek. Hata ayıklama analiz edilebilir hale gelecek.
* Uygulama `build` almadan ortam (Local, Staging, Prod) değiştirebilecek.
* K8s veya Docker Swarm üzerinde pod kilitlenmeleri anında tespit edilip orkestratör tarafından otomatik restart (Self-Healing) edilebilecek.
* Zararlı işlemler yapılması riskine karşı Non-root konteyner kullanımı sayesinde güvenlik (Defense-in-depth) artacak.

**Negatif:**
* Loglama için JSON parsing maliyeti eklendi.
* `.env` dosyasının yanlışlıkla Git'e (Eğer ignore edilmezse) pushlanma ihtimali var. 

## Uyumluluk (Compliance)
* `System.out.println` kullanımı kod inceleme (Code Review) adımlarında reddedilecektir. Sadece `org.slf4j.Logger` kullanılabilir.
* Her Use Case veya altyapı servisi eklenirken CPU ve Memory `request/limit` hesaplamaları dikkate alınarak K8s manifestoları güncellenmelidir.
