# Agent Cikti Formatlari

Bu dokuman agentlarin ortak sonuc dilini tanimlar. Java tarafinda model siniflari kullanilsa bile disari aktarilan JSON bu sozlesmeye yakin kalmalidir.

## Ortak Analiz Sonucu

`MacYorumAnalizAgent`, `TakimGucuAnalizAgent`, `KadroDurumuAgent` ve `OranRiskAnalizAgent` icin temel format:

```json
{
  "agent": "OranRiskAnalizAgent",
  "basarili": true,
  "macId": 2831052,
  "riskSeviyesi": "DUSUK",
  "riskPuani": 31,
  "guvenPuani": 72,
  "tahmin": "CIFTE_SANS_1X",
  "oneCikanTaraf": "evSahibi",
  "gerekce": "Oran, takim gucu ve yorum sinyalleri ayni yonde.",
  "uyarilar": [],
  "detaylar": {}
}
```

## Bahis Onerisi

`BahisOneriAgent` final format:

```json
{
  "macId": 2831052,
  "karsilasma": "Neom SC - AL Hazem FC",
  "bahisTuru": "CIFTE_SANS_1X",
  "secim": "1X",
  "oranDegeri": 1.22,
  "riskPuani": 31,
  "guvenPuani": 72,
  "riskSeviyesi": "DUSUK",
  "gerekce": "Ev sahibi sinyalleri daha guclu, secim riski azaltir.",
  "uyarilar": []
}
```

## Zorunlu Kurallar

- `riskPuani` ve `guvenPuani` her zaman `0-100` araliginda olmalidir.
- `riskSeviyesi`, risk puanina gore tutarli olmalidir.
- `gerekce` bos birakilmamalidir.
- Eksik veri varsa `uyarilar` alaninda belirtilmelidir.
- Final oneride kesinlik veya garanti ifade edilmemelidir.
