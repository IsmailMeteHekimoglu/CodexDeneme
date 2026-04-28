# Oran Favori Yorum Ters

## Durum

Oran favorisi ev sahibi, ancak yorum ve haber sinyalleri deplasman lehine.

## Beklenen Davranis

- `MacYorumAnalizAgent` yorum sinyalini deplasman yonlu isaretler.
- `OranRiskAnalizAgent` oran ve yorum uyumsuzlugu nedeniyle riski artirir.
- `BahisOneriAgent` maci dusuk riskli listeye almamalidir.

## Kabul Kriteri

- Risk seviyesi `ORTA` veya `YUKSEK` olmalidir.
- Gerekcede "oran ve yorum uyumsuzlugu" belirtilmelidir.
