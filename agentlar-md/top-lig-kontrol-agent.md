# TopLigKontrolAgent

## Amac

Her calistirmada oncelikli ligleri, Avrupa kupalarini ve secili ulke kupalarini dogrular.

## Bagli Java Sinifi

`src/main/java/com/futbolanaliz/agentlar/TopLigKontrolAgent.java`

## Sorumluluklar

- Iddaa lig katalogundan guncel organizasyon bilgisini okumak.
- Top 10 lig, Avrupa kupalari ve secili kupa maclarini ayirmak.
- Mac toplama agentina uygun organizasyon filtresi saglamak.

## Girdi

- Iddaa organizasyon katalogu.
- Projede tanimli oncelikli lig/kupa listesi.

## Cikti

- Uygun lig ve kupa listesi.
- Eksik veya bulunamayan organizasyon uyarilari.

## Karar Kurallari

- Ulke ligleri, Avrupa kupalari ve ulke kupalari ayri kategorilerde tutulmalidir.
- Liste eslesmesi isim farkliliklarina karsi toleransli olmalidir.
- Dogrulanmayan organizasyonlardan gelen maclar dusuk guvenle isaretlenmelidir.
