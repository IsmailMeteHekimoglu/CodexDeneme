# AnaYoneticiAgent

## Amac

Tum analiz akisini baslatir, alt agent sonuclarini toplar ve final karar surecini yonetir.

## Bagli Java Sinifi

`src/main/java/com/futbolanaliz/agentlar/AnaYoneticiAgent.java`

## Sorumluluklar

- Lig ve kupa uygunluk kontrolunu baslatmak.
- Mac ve oran toplama adimini calistirmak.
- Yorum, takim gucu, kadro riski ve oran riski analizlerini sirayla toplamak.
- Final bahis onerisi agentindan gelen sonucu kullaniciya sunmak.

## Girdi

- Bugunun mac ve oran verileri.
- Oncelikli lig ve kupa listesi.
- Agentlarin uretecegi ara analiz sonuclari.

## Cikti

- Tum alt agentlarin calisma durumlari.
- Final bahis onerisi akisi icin birlesik analiz ozeti.

## Karar Kurallari

- Lig kontrolu basarisizsa mac toplama adimi kisitli veya uyarili calismalidir.
- Kritik veri eksikse final sonucunda risk seviyesi yukseltilmelidir.
- Alt agentlardan gelen basarisiz sonuc, final kararda acikca belirtilmelidir.
