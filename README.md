# Futbol Analiz Agent

Bu proje, Türkiye'deki iddaa maçlarını ve oranlarını analiz ederek düşük riskli bahis seçeneklerini bulmayı hedefleyen Java tabanlı agent uygulamasıdır.

Planlanan agent akışı oluşturuldu: ana yönetici, öncelikli organizasyon kontrolü, maç-oran toplama, yorum destekli maç sonu analizi, takım gücü analizi, kadro durumu analizi, oran risk analizi ve final bahis önerisi. Kontrol agentı her çalıştırmada iddaa.com lig kataloğundan top 10 ligleri, Avrupa kupalarını ve seçili ülke kupalarını doğrular; maç-oran toplama agentı yalnızca bu organizasyonlardaki güncel futbol maçlarını ve temel oranları çeker.

## Klasörler

- `src/main/java/com/futbolanaliz/uygulama`: Uygulamanın başlangıç noktası.
- `src/main/java/com/futbolanaliz/agentlar`: Agent sınıfları ve ortak agent sözleşmeleri.
- `src/main/java/com/futbolanaliz/modeller`: Agentlar arasında taşınacak veri modelleri.
- `src/main/java/com/futbolanaliz/servisler`: Dış kaynaklara ve yardımcı iş akışlarına erişen servisler.
- `src/main/java/com/futbolanaliz/analiz`: Risk, oran ve performans analiz mantıkları.
- `src/main/java/com/futbolanaliz/veri`: Veri okuma, yazma ve saklama bileşenleri.
- `src/main/java/com/futbolanaliz/yardimcilar`: Ortak yardımcı sınıflar.
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
