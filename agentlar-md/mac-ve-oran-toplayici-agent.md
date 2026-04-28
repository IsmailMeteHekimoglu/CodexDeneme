# MacVeOranToplayiciAgent

## Amac

Bugunun uygun futbol maclarini ve temel iddaa oranlarini toplar.

## Bagli Java Sinifi

`src/main/java/com/futbolanaliz/agentlar/MacVeOranToplayiciAgent.java`

## Sorumluluklar

- Oncelikli organizasyon filtresine gore maclari secmek.
- Ev sahibi, deplasman, lig, baslama saati ve temel oranlari kaydetmek.
- Ham veriyi `kaynaklar/iddaa-verileri` altinda izlenebilir tutmak.

## Girdi

- Iddaa ham futbol verisi.
- TopLigKontrolAgent tarafindan uretilen organizasyon listesi.

## Cikti

- Analize uygun mac listesi.
- Mac bazli 1/X/2 oranlari ve varsa ek pazar oranlari.

## Karar Kurallari

- Eksik takim veya oran bilgisi olan maclar final oneride kullanilmamalidir.
- Sadece bugunun futbol maclari islenmelidir.
- Organizasyon filtresi disindaki maclar varsayilan akisa alinmamalidir.
