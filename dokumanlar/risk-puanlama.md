# Risk Puanlama

Bu dokuman agentlarin ayni risk dilini kullanmasi icin temel puanlama cercevesini tanimlar.

## Puan Araliklari

- `0-34`: Dusuk risk
- `35-69`: Orta risk
- `70-100`: Yuksek risk

Guven puani ters yonde yorumlanir:

- `0-39`: Dusuk guven
- `40-69`: Orta guven
- `70-100`: Yuksek guven

## Baslangic Puani

Her mac icin risk puani `50` ile baslar.

## Oran Etkisi

- Favori oran 1.20 altindaysa: `+12` risk. Deger dusuk, surpriz etkisi yuksek.
- Favori oran 1.20-1.45 arasindaysa: `-8` risk.
- Favori oran 1.46-1.85 arasindaysa: `-4` risk.
- Favori oran 1.86 ustundeyse: `+6` risk.
- 1/X/2 oranlari birbirine cok yakinsa: `+10` risk.

## Takim Gucu Etkisi

- Takim gucu favoriyle ayni yondeyse: `-8` risk, `+8` guven.
- Takim gucu favoriye tersse: `+15` risk, `-12` guven.
- Guc farki 10 puandan dusukse: `+8` risk.

## Yorum Etkisi

- En az iki kaynak ayni sonucu destekliyorsa: `-8` risk, `+10` guven.
- Yorum sinyali oran favorisine tersse: `+12` risk, `-10` guven.
- Yorum yoksa ve fallback kullaniliyorsa: `+8` risk, `-12` guven.

## Kadro Etkisi

- Favori takimda ana oyuncu eksigi varsa: `+14` risk.
- Favori takimda coklu eksik varsa: `+10` ek risk.
- Rakipte kritik eksik varsa: `-6` risk.
- Sadece supheli oyuncu haberi varsa: `+4` risk.
- Kadro verisi yoksa: `+5` risk, `-8` guven.

## Oneri Esikleri

- Final listeye yuksek riskli mac alinmaz.
- Dusuk riskli maclar ana oneridir.
- Orta riskli maclar yalnizca guven puani `65` ve ustuyse gosterilir.
- Veri eksigi olan maclarda gerekce ve uyari zorunludur.
