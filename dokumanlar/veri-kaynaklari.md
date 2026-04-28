# Veri Kaynaklari

Bu dokuman hangi verinin nereden geldigini ve hangi agent tarafindan kullanildigini tarif eder.

## Iddaa Mac ve Oran Verisi

- Klasor: `kaynaklar/iddaa-verileri`
- Ornek dosya: `bugun-futbol-raw.json`
- Kullanan agent: `MacVeOranToplayiciAgent`
- Kullanilan alanlar: mac id, takim adlari, tarih/saat, lig/organizasyon kodu, marketler ve oranlar.
- Guncelleme sikligi: Her analiz calismasindan once yenilenmesi hedeflenir.

## Takim Verileri

- Klasor: `kaynaklar/takim-verileri`
- Kullanan agent: `TakimGucuAnalizAgent`
- Beklenen alanlar: lig siralamasi, son mac formu, gol ortalamasi, ic saha/deplasman performansi.
- Durum: Ilk surumde oran dengesi ve ic saha avantaji ile fallback hesaplama yapilir.

## Sakat ve Cezali Verileri

- Klasor: `kaynaklar/sakat-cezali-verileri`
- Kullanan agent: `KadroDurumuAgent`
- Beklenen alanlar: takim, oyuncu, durum, kesinlik, kaynak, guncelleme zamani.
- Durum: Resmi veri yoksa yorum metinlerinden sinyal taranir.

## Yorum ve Haber Verileri

- Klasor onerisi: `kaynaklar/yorum-verileri`
- Kullanan agent: `MacYorumAnalizAgent`
- Beklenen alanlar: mac id, kaynak, metin, yayin zamani, guven puani.
- Durum: Yorum yoksa oran dengesine dayali fallback analiz kullanilir.

## Veri Guven Kurallari

- Tek kaynakli veri yuksek guven sayilmaz.
- Guncel olmayan kadro ve yorum verileri final riskini artirir.
- Ham veri ile islenmis veri ayri tutulmalidir.
- Kaynak dosya adi veya kaynak URL bilgisi sonuc gerekcesinde izlenebilir olmalidir.
