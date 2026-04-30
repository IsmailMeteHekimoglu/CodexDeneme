# Futbol Analiz Agent

Bu proje, Türkiye'deki iddaa maçlarını ve oranlarını analiz ederek düşük riskli bahis seçeneklerini bulmayı hedefleyen Java tabanlı agent uygulamasıdır.

Planlanan agent akışı oluşturuldu: ana yönetici, öncelikli organizasyon kontrolü, maç-oran toplama, yorum destekli maç sonu analizi, takım gücü analizi, kadro durumu analizi, oran risk analizi ve final bahis önerisi. Kontrol agentı her çalıştırmada iddaa.com lig kataloğundan top 10 ligleri, Avrupa kupalarını ve seçili ülke kupalarını doğrular; maç-oran toplama agentı yalnızca bu organizasyonlardaki güncel futbol maçlarını ve temel oranları çeker.

## Klasörler

- `src/main/java/com/futbolanaliz/uygulama`: Uygulamanın başlangıç noktası.
- `src/main/java/com/futbolanaliz/agentlar`: Agent sınıfları ve ortak agent sözleşmeleri.
- `src/main/java/com/futbolanaliz/modeller`: Agentlar arasında taşınacak veri modelleri.
- `src/main/java/com/futbolanaliz/servisler`: Dış kaynaklara ve yardımcı iş akışlarına erişen servisler.
- `agentlar-md`: Her agent için rol, girdi, çıktı ve karar talimatları.
- `skills`: Tekrar kullanılabilir analiz iş akışları.
- `sozlesmeler`: Agentlar arası JSON veri sözleşmeleri.
- `ornekler`: Standart örnek girdi ve çıktı dosyaları.
- `test-senaryolari`: Risk ve karar davranışını sabitleyen kabul senaryoları.
- `promptlar`: LLM destekli analiz için prompt şablonları.
- `dokumanlar`: Proje notları ve tasarım açıklamaları.
- `kaynaklar`: Toplanan veya üretilen veri dosyaları.
- `varliklar/ikonlar`: Uygulama ikonu ve masaüstü kısayolu görselleri.

## Çalıştırma

Masaüstündeki `Futbol Analiz Agent` kısayolu renkli masaüstü arayüzünü başlatır.

Alternatif olarak proje klasöründe şu dosya çalıştırılabilir:

```bash
calistir.bat
```

Konsol çıktısını görmek için şu dosya kullanılabilir:

```bash
calistir-konsol.bat
```

Maven kuruluysa şu komut da kullanılabilir:

```bash
mvn compile exec:java
```

## Masaüstü Arayüz

İlk masaüstü prototipi tek ekranlıdır:

- Solda bugünün maç listesi bulunur.
- Maç seçildiğinde sağda lig, saat ve oran kartları açılır.
- `Analiz Et` butonu seçili maç için yorum, takım gücü, kadro riski, oran riski ve final öneri agentlarını çalıştırır.
- Sonuç alanında öneri, risk/güven puanı ve analiz gerekçesi gösterilir.

## JDK Ayarı

Bu proje için JDK yolu kullanıcı ortam değişkenlerine eklendi:

```text
JAVA_HOME=C:\Users\IMHek\OneDrive\Documents\JDK\java-1.8.0-openjdk-1.8.0.492.b09-1.win.jdk.x86_64
```

## LLM Destekli Yorum Analizi

Yorum analizi varsayilan olarak yerel kural tabanli akisi kullanir. OpenAI API anahtari tanimliysa mac yorumlari icin LLM destekli analiz devreye girer:

```powershell
$env:OPENAI_API_KEY="sk-..."
```

Varsayilan model `gpt-5-mini` olarak ayarlidir. Farkli model kullanmak icin:

```powershell
$env:FUTBOL_ANALIZ_LLM_MODEL="gpt-5-mini"
```

LLM analizini gecici olarak kapatmak icin:

```powershell
$env:FUTBOL_ANALIZ_LLM="0"
```

API anahtari yoksa veya LLM istegi basarisiz olursa uygulama otomatik olarak eski kural tabanli yoruma geri doner.

Mac analizi iddaa.com mac/oran verileri, iddaa istatistik ekranindan gelen son maclar ve varsa Broadage yorumlariyla zenginlestirilir.

Masaustu arayuzundeki `Ayarlar` butonu ile OpenAI API key, model, LLM acik/kapali ve cache acik/kapali tercihleri kaydedilebilir. Ayarlar `config.properties` dosyasinda tutulur; bu dosya git'e dahil edilmez. Cache sonuclari `kaynaklar/llm-cache/` altina yazilir ve ayni analiz tekrarlandiginda OpenAI istegi yapmadan kullanilir.

## Bakim ve Versiyon Kontrol Agent

`BakimVersiyonKontrolAgent`, Codex ile yapilan her kod degisikliginden sonra calistirilmak uzere tasarlandi. Agent API kullanmaz; yerel git durumu, paket yapisi ve dosya sistemi uzerinden calisir.

Calistirma:

```powershell
powershell -ExecutionPolicy Bypass -File .\codex-bakim.ps1
```

Agent su islemleri yapar:

- Java kaynak agacinda kalan gereksiz `.class` dosyalarini temizler.
- `src/main/java/com/futbolanaliz` altindaki bos paket klasorlerini siler.
- `pom.xml` versiyonunu ve git branch/HEAD bilgisini okur.
- Commitlenmemis degisiklikleri `tmp/codex-degisiklik-ozeti.md` dosyasina yazar.
- Commit sonrasi git calisma agaci temizse ozet dosyasini temizler.
- Kullanici istemedikce commit atmaz, branch degistirmez veya destructive git komutu calistirmaz.
