# Dusuk Riskli Net Favori

## Durum

Favori takim oran, yorum, takim gucu ve kadro sinyallerinde ayni yonde destekleniyor.

## Beklenen Davranis

- `TakimGucuAnalizAgent` favoriyi one cikarir.
- `MacYorumAnalizAgent` favori yonlu tahmin uretir.
- `OranRiskAnalizAgent` dusuk risk etiketi verir.
- `BahisOneriAgent` temkinli bahis turunu final listeye alir.

## Kabul Kriteri

- Risk seviyesi `DUSUK` olmalidir.
- Guven puani `70` veya uzeri olmalidir.
- Final gerekce birden fazla sinyalin uyumunu belirtmelidir.
