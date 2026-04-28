# KadroDurumuAgent

## Amac

Sakat, cezali, eksik ve supheli oyuncu sinyallerini kadro riski olarak degerlendirir.

## Bagli Java Sinifi

`src/main/java/com/futbolanaliz/agentlar/KadroDurumuAgent.java`

## Sorumluluklar

- Yorum ve haber metinlerinde kadro eksigi sinyallerini taramak.
- Ev sahibi ve deplasman icin ayri kadro riski hesaplamak.
- Resmi kadro veri kaynagi eklendiginde ayni modele dahil etmek.

## Girdi

- Mac yorumlari.
- Sakat, cezali ve supheli oyuncu verileri.

## Cikti

- Takim bazli kadro riski.
- Mac bazli risk etkisi ve gerekce.

## Karar Kurallari

- Ana oyuncu eksigi takim riskini belirgin artirir.
- Supheli oyuncu kesin eksik gibi degerlendirilmemelidir.
- Veri bulunamazsa risk "bilinmiyor" veya "dusuk guven" olarak isaretlenmelidir.
