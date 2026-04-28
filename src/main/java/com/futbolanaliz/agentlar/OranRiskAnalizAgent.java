package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.BahisTuru;
import com.futbolanaliz.modeller.KadroDurumuAnalizi;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.MacSonuAnalizi;
import com.futbolanaliz.modeller.Oran;
import com.futbolanaliz.modeller.OranRiskAnalizi;
import com.futbolanaliz.modeller.TakimGucuAnalizi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OranRiskAnalizAgent implements Agent {
    private final List<Mac> maclar;
    private final List<MacSonuAnalizi> yorumAnalizleri;
    private final List<TakimGucuAnalizi> takimGucuAnalizleri;
    private final List<KadroDurumuAnalizi> kadroDurumuAnalizleri;
    private final List<OranRiskAnalizi> analizler = new ArrayList<OranRiskAnalizi>();

    public OranRiskAnalizAgent(List<Mac> maclar, List<MacSonuAnalizi> yorumAnalizleri, List<TakimGucuAnalizi> takimGucuAnalizleri, List<KadroDurumuAnalizi> kadroDurumuAnalizleri) {
        this.maclar = maclar == null ? new ArrayList<Mac>() : new ArrayList<Mac>(maclar);
        this.yorumAnalizleri = yorumAnalizleri == null ? new ArrayList<MacSonuAnalizi>() : new ArrayList<MacSonuAnalizi>(yorumAnalizleri);
        this.takimGucuAnalizleri = takimGucuAnalizleri == null ? new ArrayList<TakimGucuAnalizi>() : new ArrayList<TakimGucuAnalizi>(takimGucuAnalizleri);
        this.kadroDurumuAnalizleri = kadroDurumuAnalizleri == null ? new ArrayList<KadroDurumuAnalizi>() : new ArrayList<KadroDurumuAnalizi>(kadroDurumuAnalizleri);
    }

    @Override
    public String ad() {
        return "Oran Risk Analiz Agent";
    }

    @Override
    public AgentSonucu calistir() {
        analizler.clear();

        for (Mac mac : maclar) {
            Oran onerilenOran = enGuvenliOranBul(mac);
            if (onerilenOran != null) {
                analizler.add(analizOlustur(mac, onerilenOran));
            }
        }

        return AgentSonucu.basarili(ad(), analizler.size() + " maç için oran risk analizi üretildi.");
    }

    public List<OranRiskAnalizi> getAnalizler() {
        return new ArrayList<OranRiskAnalizi>(analizler);
    }

    private OranRiskAnalizi analizOlustur(Mac mac, Oran oran) {
        int risk = temelRiskPuani(oran.getDeger());
        MacSonuAnalizi yorumAnalizi = yorumAnaliziBul(mac);
        TakimGucuAnalizi takimGucuAnalizi = takimGucuAnaliziBul(mac);
        KadroDurumuAnalizi kadroDurumuAnalizi = kadroDurumuAnaliziBul(mac);

        if (yorumAnalizi != null && secimUyumlu(oran.getBahisTuru(), yorumAnalizi.getMacSonuTahmini())) {
            risk -= 8;
        }

        if (takimGucuAnalizi != null && secimUyumlu(oran.getBahisTuru(), takimGucuAnalizi.getOneCikanTaraf())) {
            risk -= 6;
        }

        if (kadroDurumuAnalizi != null) {
            risk += Math.max(kadroDurumuAnalizi.getEvSahibiRiskPuani(), kadroDurumuAnalizi.getDeplasmanRiskPuani()) / 10;
        }

        risk = Math.max(5, Math.min(95, risk));
        int guven = 100 - risk;
        String gerekce = "Risk; oran seviyesi, yorum tahmini, takım gücü ve kadro riski birlikte değerlendirilerek hesaplandı.";
        return new OranRiskAnalizi(mac, oran.getBahisTuru(), oran.getBahisTuru().getGorunenAd(), oran.getDeger(), risk, guven, gerekce);
    }

    private Oran enGuvenliOranBul(Mac mac) {
        Oran enGuvenli = null;

        for (Oran oran : mac.getOranlar()) {
            if (enGuvenli == null || temelRiskPuani(oran.getDeger()) < temelRiskPuani(enGuvenli.getDeger())) {
                enGuvenli = oran;
            }
        }

        return enGuvenli;
    }

    private int temelRiskPuani(BigDecimal oran) {
        if (oran.compareTo(new BigDecimal("1.35")) <= 0) {
            return 24;
        }
        if (oran.compareTo(new BigDecimal("1.80")) <= 0) {
            return 36;
        }
        if (oran.compareTo(new BigDecimal("2.40")) <= 0) {
            return 50;
        }
        if (oran.compareTo(new BigDecimal("3.20")) <= 0) {
            return 65;
        }

        return 78;
    }

    private boolean secimUyumlu(BahisTuru bahisTuru, String secim) {
        return (bahisTuru == BahisTuru.MAC_SONUCU_1 && "1".equals(secim))
                || (bahisTuru == BahisTuru.MAC_SONUCU_X && "X".equals(secim))
                || (bahisTuru == BahisTuru.MAC_SONUCU_2 && "2".equals(secim));
    }

    private MacSonuAnalizi yorumAnaliziBul(Mac mac) {
        for (MacSonuAnalizi analiz : yorumAnalizleri) {
            if (analiz.getMac().getIddaaEventId() == mac.getIddaaEventId()) {
                return analiz;
            }
        }
        return null;
    }

    private TakimGucuAnalizi takimGucuAnaliziBul(Mac mac) {
        for (TakimGucuAnalizi analiz : takimGucuAnalizleri) {
            if (analiz.getMac().getIddaaEventId() == mac.getIddaaEventId()) {
                return analiz;
            }
        }
        return null;
    }

    private KadroDurumuAnalizi kadroDurumuAnaliziBul(Mac mac) {
        for (KadroDurumuAnalizi analiz : kadroDurumuAnalizleri) {
            if (analiz.getMac().getIddaaEventId() == mac.getIddaaEventId()) {
                return analiz;
            }
        }
        return null;
    }
}
