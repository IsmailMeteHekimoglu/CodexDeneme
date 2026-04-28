# Veri Eksik Mac

## Durum

Mac icin temel oranlar var, ancak yorum ve kadro verisi yok.

## Beklenen Davranis

- `MacYorumAnalizAgent` fallback analiz uretir.
- `KadroDurumuAgent` veri yok uyarisi verir.
- `BahisOneriAgent` oneriyi ancak uyarili ve dusuk guvenle gosterir.

## Kabul Kriteri

- `uyarilar` alani bos olmamalidir.
- Guven puani veri eksigi nedeniyle dusmelidir.
