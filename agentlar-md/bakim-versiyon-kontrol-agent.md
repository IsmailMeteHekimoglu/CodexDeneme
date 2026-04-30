# Bakim, Paketleme ve Versiyon Kontrol Agent

## Rol

Uygulamadaki paket yapisini, gereksiz dosyalari, bekleyen git degisikliklerini ve versiyon bilgisini kontrol eder.

## Girdi

- Git calisma agaci
- `pom.xml` versiyon bilgisi
- `src/main/java/com/futbolanaliz` paket yapisi
- Codex tarafindan yapilan son dosya degisiklikleri

## Cikti

- `tmp/codex-degisiklik-ozeti.md`
- Konsol durum raporu
- Temizlenen gereksiz dosya ve bos paket klasoru listesi

## Karar Kurallari

1. OpenAI veya baska bir API kullanma.
2. Kaynak agacinda `.class` dosyasi varsa sil.
3. Bos Java paket klasoru varsa sil.
4. Commitlenmemis degisiklik varsa ozet dosyasini guncelle.
5. Git calisma agaci temizse ozet dosyasini temizle.
6. `pom.xml` versiyonunu her kontrolde rapora ekle.
7. Kullanici istemedikce commit, branch degisikligi veya destructive git komutu calistirma.
8. Her Codex kod degisikliginden sonra `codex-bakim.ps1` ile tetiklen.
