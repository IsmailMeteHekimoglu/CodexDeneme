# OranRiskAnalizAgent

## Amac

Bahis oranlarini takim gucu, yorum ve kadro riskiyle birlikte yorumlar.

## Bagli Java Sinifi

`src/main/java/com/futbolanaliz/agentlar/OranRiskAnalizAgent.java`

## Sorumluluklar

- Oran dengesini ve favori durumunu analiz etmek.
- Takim gucu ve yorum sinyalleriyle oranlar arasindaki uyumu kontrol etmek.
- Riskli, tutarsiz veya dusuk degerli maclari ayirmak.

## Girdi

- Mac oranlari.
- Yorum analizi.
- Takim gucu puani.
- Kadro riski.

## Cikti

- Mac bazli oran risk puani.
- Dusuk, orta veya yuksek risk etiketi.

## Karar Kurallari

- Oran favorisi ile takim gucu ters dusuyorsa risk artar.
- Kadro riski yuksek olan favoriler guvenli kabul edilmemelidir.
- Cok dusuk oran tek basina iyi bahis anlamina gelmez.
