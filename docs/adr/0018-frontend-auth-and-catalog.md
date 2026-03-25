# ADR 018: Frontend Kimlik Doğrulama (Auth) ve Rota Yönetimi

**Tarih:** 2026-03-25
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Faz 2 aşamasında kullanıcıların sisteme giriş yapması, sistemin geri kalanındaki Katalog, Sepet ve Ödeme gibi alanlara sadece yetkili (Authenticated) kullanıcıların erişebilmesinin sağlanması gerekmiştir. Backend halihazırda `/api/v1/users/login` endpoint'i sunmakta ve geriye JWT tabanlı bir token dönmektedir. Frontend'in bu token'ı güvenli bir şekilde saklaması, sayfa yenilendiğinde oturumu koruması ve HTTP isteklerine otomatik dahil etmesi (Interceptor) tasarlanmalıdır.

Değerlendirilen Seçenekler:
1. Token'ı sadece bellek (memory) üzerinde tutmak. (Sayfa yenilendiğinde oturum düşer).
2. Token'ı `localStorage` veya `sessionStorage` içinde saklamak.
3. Backend üzerinden HttpOnly Cookie dönmesini beklemek.

## Karar (Decision)
Mevcut Backend mimarisini değiştirmemek ve SPA (Single Page Application) esnekliğini korumak adına **JWT token `localStorage` üzerinden saklanacak ve uygulamanın en üst düzeyinde yer alan bir `AuthProvider` (Context API) ile yönetilecektir.** 

Ayrıca Frontend tarafında Rotalar (Routes) arası erişim güvenliğini sağlamak amacıyla `<ProtectedRoute>` bileşeni tasarlanmıştır. `ProtectedRoute` bileşeni kullanıcının yetkisi olmadığını anlarsa derhal `/login` sayfasına yönlendirme (redirect) yapar.

## Sonuçlar (Consequences)
**Pozitif:**
* React Router ile tam uyumlu çalışan, esnek ve kolay yönetilebilen bir routing stratejisi kazanılmıştır.
* Axios interceptor aracılığı ile her api isteğinin `Authorization` kipine token tek bir merkezden (`localStorage.getItem('token')`) otomatik basılabilmektedir.

**Negatif:**
* LocalStorage kullanımı XSS (Cross-Site Scripting) ataklarına açıktır. Frontend'de form validasyonları ve React'in kendi XSS koruma özellikleri (JSX escape) kullanılarak bu risk asgari düzeye indirilmelidir. 
* Backend tarafında kullanıcı kayıt (Register) API'si henüz olmadığı için Login işleminde varsayılan (test) bilgiler (`test@test.com`) arayüze öntanımlı (placeholder) olarak girilmiştir.

## Uyumluluk (Compliance)
* Tüm korunan API endpointlerine gidecek istekler merkezi `axiosClient.ts` üzerinden geçmek zorundadır. Aksi takdirde Interceptor token'ı basamaz.
* React Router yapısında, public olmayan sayfa bileşenleri mutlaka `<ProtectedRoute>` ile sarmalanmalıdır (Wrap edilmelidir).
