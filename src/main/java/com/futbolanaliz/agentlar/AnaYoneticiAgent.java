package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.BahisOnerisi;
import com.futbolanaliz.modeller.KadroDurumuAnalizi;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.MacSonuAnalizi;
import com.futbolanaliz.modeller.MacYorumu;
import com.futbolanaliz.modeller.Oran;
import com.futbolanaliz.modeller.OranRiskAnalizi;
import com.futbolanaliz.modeller.TakimGucuAnalizi;
import com.futbolanaliz.modeller.TopLig;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AnaYoneticiAgent implements Agent {
    private static final DateTimeFormatter TARIH_FORMATI = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter SAAT_FORMATI = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String ad() {
        return "Ana Yönetici Agent";
    }

    @Override
    public AgentSonucu calistir() {
        LocalDate bugun = LocalDate.now();
        TopLigKontrolAgent topLigKontrolAgent = new TopLigKontrolAgent();

        System.out.println("Futbol Analiz Agent başlatıldı.");
        System.out.println("Tarih: " + bugun.format(TARIH_FORMATI));
        System.out.println("Çalışan agent: " + ad());
        System.out.println("");

        AgentSonucu topLigSonucu = topLigKontrolAgent.calistir();
        agentSonucunuYazdir(topLigSonucu);
        topLigleriYazdir(topLigKontrolAgent.getKontrolEdilenLigler());
        System.out.println("");

        MacVeOranToplayiciAgent macVeOranToplayiciAgent = new MacVeOranToplayiciAgent(topLigKontrolAgent.getKontrolEdilenLigler());
        agentSonucunuYazdir(macVeOranToplayiciAgent.calistir());
        System.out.println("");
        maclariYazdir(macVeOranToplayiciAgent.getToplananMaclar());

        MacYorumAnalizAgent macYorumAnalizAgent = new MacYorumAnalizAgent(macVeOranToplayiciAgent.getToplananMaclar());
        agentSonucunuYazdir(macYorumAnalizAgent.calistir());
        System.out.println("");
        macSonuAnalizleriniYazdir(macYorumAnalizAgent.getAnalizler());

        TakimGucuAnalizAgent takimGucuAnalizAgent = new TakimGucuAnalizAgent(macVeOranToplayiciAgent.getToplananMaclar());
        agentSonucunuYazdir(takimGucuAnalizAgent.calistir());
        takimGucuAnalizleriniYazdir(takimGucuAnalizAgent.getAnalizler());

        KadroDurumuAgent kadroDurumuAgent = new KadroDurumuAgent(macVeOranToplayiciAgent.getToplananMaclar(), macYorumAnalizAgent.getAnalizler());
        agentSonucunuYazdir(kadroDurumuAgent.calistir());
        kadroDurumuAnalizleriniYazdir(kadroDurumuAgent.getAnalizler());

        OranRiskAnalizAgent oranRiskAnalizAgent = new OranRiskAnalizAgent(
                macVeOranToplayiciAgent.getToplananMaclar(),
                macYorumAnalizAgent.getAnalizler(),
                takimGucuAnalizAgent.getAnalizler(),
                kadroDurumuAgent.getAnalizler()
        );
        agentSonucunuYazdir(oranRiskAnalizAgent.calistir());
        oranRiskAnalizleriniYazdir(oranRiskAnalizAgent.getAnalizler());

        BahisOneriAgent bahisOneriAgent = new BahisOneriAgent(oranRiskAnalizAgent.getAnalizler());
        agentSonucunuYazdir(bahisOneriAgent.calistir());
        bahisOnerileriniYazdir(bahisOneriAgent.getOneriler());

        return AgentSonucu.basarili(
                ad(),
                "Ana yönetici agent planlanan tüm subagent akışını tamamladı."
        );
    }

    private void agentSonucunuYazdir(AgentSonucu sonuc) {
        System.out.println("Alt agent: " + sonuc.getAgentAdi());
        System.out.println("Durum: " + sonuc.getMesaj());
    }

    private void topLigleriYazdir(List<TopLig> ligler) {
        System.out.println("Kontrol Edilen Öncelikli Lig ve Kupalar");
        System.out.println("--------------------------------------");

        for (TopLig lig : ligler) {
            System.out.println(lig.getOncelikSirasi() + ". " + lig.getLigAdi() + " | " + lig.getKategori() + " | iddaa Lig ID: " + lig.getIddaaLigId());
        }
    }

    private void maclariYazdir(List<Mac> maclar) {
        System.out.println("Bugünün iddaa.com Futbol Maçları ve Oranları");
        System.out.println("-------------------------------------------");

        if (maclar.isEmpty()) {
            System.out.println("Bugün doğrulanan öncelikli lig/kupa filtresine uyan maç bulunamadı.");
            return;
        }

        for (Mac mac : maclar) {
            System.out.println(mac.getSaat().format(SAAT_FORMATI) + " | " + mac.getLig() + " | " + mac.getKarsilasmaAdi());

            for (Oran oran : mac.getOranlar()) {
                System.out.println("  - " + oran.getBahisTuru().getGorunenAd() + ": " + oran.formatliDeger());
            }

            System.out.println("");
        }
    }

    private void macSonuAnalizleriniYazdir(List<MacSonuAnalizi> analizler) {
        System.out.println("Yorum Destekli Maç Sonu Analizi");
        System.out.println("-------------------------------");

        if (analizler.isEmpty()) {
            System.out.println("Analiz üretilecek maç bulunamadı.");
            return;
        }

        for (MacSonuAnalizi analiz : analizler) {
            Mac mac = analiz.getMac();
            System.out.println(mac.getSaat().format(SAAT_FORMATI) + " | " + mac.getKarsilasmaAdi());
            System.out.println("  - Maç sonu tahmini: " + analiz.getMacSonuTahmini());
            System.out.println("  - Güven puanı: " + analiz.getGuvenPuani() + "/100");
            System.out.println("  - Gerekçe: " + analiz.getGerekce());

            if (!analiz.getYorumlar().isEmpty()) {
                MacYorumu yorum = analiz.getYorumlar().get(0);
                System.out.println("  - Örnek yorum kaynağı: " + yorum.getKaynak() + " | " + yorum.getYazar());
                System.out.println("  - Örnek yorum: " + kisalt(yorum.getYorumMetni(), 180));
            }

            System.out.println("");
        }
    }

    private void takimGucuAnalizleriniYazdir(List<TakimGucuAnalizi> analizler) {
        System.out.println("");
        System.out.println("Takım Gücü Analizi");
        System.out.println("------------------");

        for (TakimGucuAnalizi analiz : analizler) {
            System.out.println(analiz.getMac().getKarsilasmaAdi() + " | Ev: " + analiz.getEvSahibiGucPuani() + " / Dep: " + analiz.getDeplasmanGucPuani() + " | Öne çıkan: " + analiz.getOneCikanTaraf());
        }
        System.out.println("");
    }

    private void kadroDurumuAnalizleriniYazdir(List<KadroDurumuAnalizi> analizler) {
        System.out.println("Kadro Durumu Analizi");
        System.out.println("--------------------");

        for (KadroDurumuAnalizi analiz : analizler) {
            System.out.println(analiz.getMac().getKarsilasmaAdi() + " | Ev risk: " + analiz.getEvSahibiRiskPuani() + " / Dep risk: " + analiz.getDeplasmanRiskPuani());
            System.out.println("  - " + analiz.getDurumOzeti());
        }
        System.out.println("");
    }

    private void oranRiskAnalizleriniYazdir(List<OranRiskAnalizi> analizler) {
        System.out.println("Oran Risk Analizi");
        System.out.println("-----------------");

        for (OranRiskAnalizi analiz : analizler) {
            System.out.println(analiz.getMac().getKarsilasmaAdi() + " | " + analiz.getSecim() + " | Oran: " + analiz.formatliOranDegeri() + " | Risk: " + analiz.getRiskPuani() + " | Güven: " + analiz.getGuvenPuani());
        }
        System.out.println("");
    }

    private void bahisOnerileriniYazdir(List<BahisOnerisi> oneriler) {
        System.out.println("Final Bahis Önerileri");
        System.out.println("---------------------");

        if (oneriler.isEmpty()) {
            System.out.println("Düşük/orta risk eşiğini geçen öneri bulunamadı.");
            return;
        }

        for (BahisOnerisi oneri : oneriler) {
            System.out.println(oneri.getMac().getSaat().format(SAAT_FORMATI) + " | " + oneri.getMac().getKarsilasmaAdi());
            System.out.println("  - Öneri: " + oneri.getSecim() + " | Oran: " + oneri.formatliOranDegeri());
            System.out.println("  - Risk/Güven: " + oneri.getRiskPuani() + "/" + oneri.getGuvenPuani());
            System.out.println("  - Gerekçe: " + oneri.getGerekce());
        }
        System.out.println("");
    }

    private String kisalt(String metin, int enFazlaKarakter) {
        if (metin == null || metin.length() <= enFazlaKarakter) {
            return metin;
        }

        return metin.substring(0, enFazlaKarakter - 3) + "...";
    }
}
