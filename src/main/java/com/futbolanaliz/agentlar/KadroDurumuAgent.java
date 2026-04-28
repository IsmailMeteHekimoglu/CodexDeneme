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

        return AgentSonucu.basarili(ad(), analizler.size() + " maç için kadro durumu riski değerlendirildi.");
    }

    public List<KadroDurumuAnalizi> getAnalizler() {
        return new ArrayList<KadroDurumuAnalizi>(analizler);
    }

    private KadroDurumuAnalizi analizOlustur(Mac mac) {
        int evRiski = 10;
        int deplasmanRiski = 10;
        MacSonuAnalizi yorumAnalizi = yorumAnaliziBul(mac);

        if (yorumAnalizi != null) {
            for (MacYorumu yorum : yorumAnalizi.getYorumlar()) {
                String metin = normalize(yorum.getYorumMetni());
                if (takimVeRiskVar(metin, mac.getEvSahibi())) {
                    evRiski += 15;
                }
                if (takimVeRiskVar(metin, mac.getDeplasman())) {
                    deplasmanRiski += 15;
                }
            }
        }

        String ozet = "Resmi sakat/cezalı listesi henüz entegre değil; mevcut yorum metinlerinde kadro riski sinyali tarandı.";
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

    private boolean takimVeRiskVar(String metin, String takimAdi) {
        String takim = normalize(takimAdi);
        return metin.contains(takim)
                && (metin.contains("sakat") || metin.contains("cezalı") || metin.contains("cezali") || metin.contains("eksik") || metin.contains("şüpheli"));
    }

    private String normalize(String deger) {
        return deger == null ? "" : deger.toLowerCase(TURKCE);
    }

    private int sinirla(int puan) {
        return Math.max(0, Math.min(100, puan));
    }
}
