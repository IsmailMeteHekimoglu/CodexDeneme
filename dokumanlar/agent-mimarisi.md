# Agent Mimarisi

Başlangıç mimarisi sekiz agenttan oluşur ve planlanan tüm subagentlar oluşturulmuştur:

1. `AnaYoneticiAgent`: Tüm agent akışını başlatır, sonuçları toplar ve final karar sürecini yönetir.
2. `TopLigKontrolAgent`: Her çalıştırmada top 10 ligleri, Avrupa kupalarını ve seçili ülke kupalarını iddaa.com lig kataloğundan doğrular.
3. `MacVeOranToplayiciAgent`: Günün maçlarını ve iddaa oranlarını toplar.
4. `MacYorumAnalizAgent`: Maç yorumlarını kontrol eder ve maç sonu 1/X/2 tahmini için yorum destekli analiz üretir.
5. `TakimGucuAnalizAgent`: Takımların göreli güç puanını hesaplar.
6. `KadroDurumuAgent`: Sakat, cezalı ve şüpheli oyuncu sinyallerini kadro riski olarak değerlendirir.
7. `OranRiskAnalizAgent`: Bahis oranlarını takım gücü, yorum ve kadro riskiyle birlikte yorumlar.
8. `BahisOneriAgent`: En düşük riskli bahis önerilerini üretir.

## Çalışma Sırası

1. Öncelikli lig/kupa listesi doğrulanır.
2. iddaa.com üzerinden bugünün uygun maçları ve temel oranları çekilir.
3. Maç yorumları aranır ve maç sonu tahmini üretilir.
4. Takım gücü puanı hesaplanır.
5. Kadro riski değerlendirilir.
6. Oran risk puanı hesaplanır.
7. Final bahis önerileri düşük/orta risk eşiğine göre listelenir.

## Notlar

- `MacYorumAnalizAgent` yorum bulamazsa oran dengesine göre fallback analiz üretir.
- `TakimGucuAnalizAgent` ilk sürümde oran dengesi ve iç saha avantajını kullanır; ileride lig sıralaması ve form servisleriyle zenginleştirilecek.
- `KadroDurumuAgent` ilk sürümde yorum metinlerinde sakat, cezalı, eksik ve şüpheli sinyallerini tarar; resmi kadro veri kaynağı eklendiğinde aynı modele beslenecek.
