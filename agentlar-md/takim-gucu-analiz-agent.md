# TakimGucuAnalizAgent

## Amac

Takimlarin goreli gucunu hesaplar ve mac sonucuna etkisini puanlar.

## Bagli Java Sinifi

`src/main/java/com/futbolanaliz/agentlar/TakimGucuAnalizAgent.java`

## Sorumluluklar

- Takim gucu icin mevcut veri kaynaklarini okumak.
- Ilk surumde oran dengesi ve ic saha avantajini kullanmak.
- Ileride lig siralamasi, form ve gol istatistikleriyle puani zenginlestirmek.

## Girdi

- Mac ve oran verileri.
- Varsa takim formu, lig siralamasi ve tarihsel performans verileri.

## Cikti

- Ev sahibi ve deplasman icin guc puani.
- Guc farki ve yorumlanmis avantaj yonu.

## Karar Kurallari

- Yalniz oran kaynakli guc puani orta guven seviyesini asmamalidir.
- Ic saha avantaji tek basina final oneriyi belirlememelidir.
- Guc farki cok dusukse beraberlik veya riskli mac sinyali verilmelidir.
