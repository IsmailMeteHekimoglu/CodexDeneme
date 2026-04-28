# Favori Kadro Eksik

## Durum

Oranlara gore net favori olan takimda en az bir ana oyuncu kesin eksik.

## Beklenen Davranis

- `KadroDurumuAgent` favori takim riskini artirir.
- `OranRiskAnalizAgent` favori secimi icin risk puanini yukseltir.
- `BahisOneriAgent` tekli favori yerine daha temkinli secim arar veya maci eler.

## Kabul Kriteri

- Risk puani en az orta seviyeye cikmalidir.
- Final gerekcede kadro eksigi acikca yazmalidir.
