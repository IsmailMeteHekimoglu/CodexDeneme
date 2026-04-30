# Bakim ve Versiyon Kontrol Skill

## Amac

Codex her kod ekleme veya degistirme turundan sonra bu skill'i uygular. Skill OpenAI/API cagrisi yapmaz; yalnizca yerel dosya sistemi, git durumu ve Java derlemesiyle calisir.

## Tetikleme

- Java kaynak kodu degistiginde
- Paket veya dosya eklendiginde
- Prompt, README, pom veya script degistiginde
- Commit oncesi son kontrol istendiginde
- Commit sonrasi bekleyen ozetin temizlenmesi gerektiginde

## Calisma Komutu

```powershell
.\codex-bakim.ps1
```

## Kurallar

- Kaynak agacinda kalan gereksiz `.class` dosyalarini temizle.
- `src/main/java/com/futbolanaliz` altindaki bos paket klasorlerini sil.
- Paket yapisini kontrol et ve ozetle.
- Commitlenmemis degisiklikleri `tmp/codex-degisiklik-ozeti.md` dosyasina yaz.
- Git calisma agaci temizse bu ozet dosyasini temizle.
- Versiyon bilgisini `pom.xml` uzerinden oku ve ozete ekle.
- Kullanici acikca istemedikce commit atma, branch degistirme veya git reset yapma.
- API kullanma; bu is Codex'in yerel token butcesiyle yurutulur.
- Commit özeti için uygun bir baslik ve detayli aciklama ekle.

## Beklenen Cikti

- Java derleme hatasiz olmali.
- Bakim agent'i calismali.
- Bekleyen degisiklik varsa `tmp/codex-degisiklik-ozeti.md` guncellenmeli.
- Bekleyen degisiklik yoksa ozet dosyasi temizlenmeli.
- pom.xml'deki versiyon bilgisi yeni değerle guncellenmeli.
- Gereksiz ve boş klasorler veya dosyalar temizlenmeli.
- Commit basligi ve aciklamasi eklenmeli, ancak commit atilmamalidir.

