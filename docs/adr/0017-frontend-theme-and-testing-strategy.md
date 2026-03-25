# ADR 017: Tema Yönetimi ve Entegrasyon Test Çerçevesi

**Tarih:** 2026-03-25
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Faz 5 itibarıyla uygulamanın kullanıcı deneyimini artıracak Aydınlık/Karanlık (Light/Dark Mode) tema ihtiyacı ve SRE hedeflerine uygun, test edilebilirliğini garantileyecek bir Test Harness'a ihtiyaç duyulmuştur. React uygulamaları için popüler olan pek çok test aracı bulunmakla beraber (Jest, Vitest), Vite kullanan modern bir dev-server ortamında uyumluluk kıymetlidir.

Ayrıca JSDOM kullanıldığında CSS property ve modern dom rendering hataları yaşanmış, sorunlar test frameworkünü kilitleme eğilimi göstermiştir.

## Karar (Decision)
1. **Tema Yönetimi:** CSS değişkenleri (`var(--)`) üzerine inşa edilecek olup, Document root (`<html>`) etiketine eklenen `data-theme` attribute'u ile yönetilecektir. Bu karara global state üzerinden Context API eklenerek kullanıcının kararı `localStorage`'da saklanacaktır.
2. **Test Çerçevesi:** Jest yerine **Vite ile bütünleşik ve daha hızlı çalışan Vitest** test çatısı olarak kullanılacaktır.
3. **DOM Ortamı:** Uyumluluk sıkıntıları (CSS-color ESM hataları) nedeniyle `jsdom` terkedilerek, çok daha optimize ve Vite dostu olan **`happy-dom`** test environments olarak kullanılacaktır.

## Sonuçlar (Consequences)
**Pozitif:**
* CSS Custom Properties, JS'in yüklenmesini beklemeden çok hızlı boyanabildiği (paint) için performans harikadır, Tailwind gibi üçüncü parti derleyicilere ihtiyaç bırakmaz.
* Vitest, Vite'in konfigürasyonunu (resolve, alias vb) birebir kullandığından `tsconfig` ikiliklerine neden olmaz. Happy-dom sorunsuz render sağlar.

**Negatif:**
* Mocking bağımlılıklarında (MSW veya Axios mocking vb) JSDOM ile yazılmış eski dokümantasyonlar, Happy-DOM'da minimal değişiklikler gerektirebilir (ki axiosClient mock ile aşıldı).

## Uyumluluk (Compliance)
* Tüm renk kodları `tokens.css` içinde `html[data-theme='...']` altına variable olarak konmalıdır. Hardcode renk verilmemelidir.
* Testler (`npm test`) CI ortamında mutlu (happy) sonlanabilmesi için backend HTTP çağrılarını dış dünyaya sızdırmayacak şekilde mocklanmalıdır.
