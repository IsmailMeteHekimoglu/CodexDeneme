package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.KadroDurumuAnalizi;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.MacSonuAnalizi;
import com.futbolanaliz.modeller.MacYorumu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KadroDurumuAgent implements Agent {
    private static final Locale TURKCE = new Locale("tr", "TR");

    private final List<Mac> maclar;
    private final List<MacSonuAnalizi> yorumAnalizleri;
    private final List<KadroDurumuAnalizi> analizler = new ArrayList<KadroDurumuAnalizi>();

    public KadroDurumuAgent(List<Mac> maclar, List<MacSonuAnalizi> yorumAnalizleri) {
        this.maclar = maclar == null ? new ArrayList<Mac>() : new ArrayList<Mac>(maclar);
        this.yorumAnalizleri = yorumAnalizleri == null ? new ArrayList<MacSonuAnalizi>() : new ArrayList<MacSonuAnalizi>(yorumAnalizleri);
    }

    @Override
    public String ad() {
        return "Kadro Durumu Agent";
    }

    @Override
    public AgentSonucu calistir() {
        analizler.clear();

        for (Mac mac : maclar) {
            analizler.add(analizOlustur(mac));
        }

        return AgentSonucu.basarili(ad(), analizler.size() + " mac icin kadro durumu riski degerlendirildi.");
    }

    public List<KadroDurumuAnalizi> getAnalizler() {
        return new ArrayList<KadroDurumuAnalizi>(analizler);
    }

    private KadroDurumuAnalizi analizOlustur(Mac mac) {
        int evRiski = 10;
        int deplasmanRiski = 10;
        int evSinyalSayisi = 0;
        int deplasmanSinyalSayisi = 0;
        MacSonuAnalizi yorumAnalizi = yorumAnaliziBul(mac);

        if (yorumAnalizi != null) {
            for (MacYorumu yorum : yorumAnalizi.getYorumlar()) {
                String metin = normalize(yorum.getYorumMetni());
                int evEki = kadroRiskEki(metin, mac.getEvSahibi());
                int deplasmanEki = kadroRiskEki(metin, mac.getDeplasman());
                if (evEki > 0) {
                    evSinyalSayisi++;
                    evRiski += evEki;
                }
                if (deplasmanEki > 0) {
                    deplasmanSinyalSayisi++;
                    deplasmanRiski += deplasmanEki;
                }
            }
        }

        String ozet = "Kadro taramasi; yorum/haber metinlerinde sakatlik, ceza, eksik, rotasyon ve supheli oyuncu sinyallerini aradi. "
                + "Resmi sakat/cezali listesi entegre degil. Sinyal sayisi ev/deplasman: "
                + evSinyalSayisi + "/" + deplasmanSinyalSayisi + ".";
        return new KadroDurumuAnalizi(mac, sinirla(evRiski), sinirla(deplasmanRiski), ozet);
    }

    private MacSonuAnalizi yorumAnaliziBul(Mac mac) {
        for (MacSonuAnalizi analiz : yorumAnalizleri) {
            if (analiz.getMac().getIddaaEventId() == mac.getIddaaEventId()) {
                return analiz;
            }
        }

        return null;
    }

    private int kadroRiskEki(String metin, String takimAdi) {
        String takim = normalize(takimAdi);
        if (!metin.contains(takim)) {
            return 0;
        }

        int risk = 0;
        if (metin.contains("sakat") || metin.contains("injury") || metin.contains("injured")) {
            risk += 14;
        }
        if (metin.contains("cezali") || metin.contains("kart cezal") || metin.contains("suspended")) {
            risk += 14;
        }
        if (metin.contains("eksik") || metin.contains("yok") || metin.contains("oynamayacak") || metin.contains("missing")) {
            risk += 10;
        }
        if (metin.contains("supheli") || metin.contains("belirsiz") || metin.contains("doubtful")) {
            risk += 8;
        }
        if (metin.contains("rotasyon") || metin.contains("dinlend") || metin.contains("yedek") || metin.contains("rotation")) {
            risk += 7;
        }
        return Math.min(25, risk);
    }

    private String normalize(String deger) {
        return deger == null ? "" : deger.toLowerCase(TURKCE);
    }

    private int sinirla(int puan) {
        return Math.max(0, Math.min(100, puan));
    }
}
