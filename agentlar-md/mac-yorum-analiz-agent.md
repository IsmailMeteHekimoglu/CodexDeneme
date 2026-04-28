# MacYorumAnalizAgent

## Amac

Mac yorumlarini ve metinsel sinyalleri kullanarak mac sonu 1/X/2 egilimi uretir.

## Bagli Java Sinifi

`src/main/java/com/futbolanaliz/agentlar/MacYorumAnalizAgent.java`

## Sorumluluklar

- Maca ait yorum, haber veya kisa analiz metinlerini okumak.
- Ev sahibi, beraberlik ve deplasman lehine sinyalleri ayirmak.
- Yorum bulunamazsa oran dengesine dayali fallback analiz uretmek.

## Girdi

- Mac listesi ve oranlar.
- Varsa yorum/haber metinleri.

## Cikti

- Mac bazli yorum destekli tahmin.
- Tahmin guveni ve gerekce metni.

## Karar Kurallari

- Tek kaynakli yorum yuksek guven sayilmamalidir.
- Kadro eksigi, form, motivasyon ve fikstur yogunlugu ayri sinyaller olarak degerlendirilmelidir.
- Yorum yoksa sonuc "fallback" olarak isaretlenmelidir.
