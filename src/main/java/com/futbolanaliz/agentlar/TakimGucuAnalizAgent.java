package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.BahisTuru;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.Oran;
import com.futbolanaliz.modeller.TakimGucuAnalizi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TakimGucuAnalizAgent implements Agent {
    private final List<Mac> maclar;
    private final List<TakimGucuAnalizi> analizler = new ArrayList<TakimGucuAnalizi>();

    public TakimGucuAnalizAgent(List<Mac> maclar) {
        this.maclar = maclar == null ? new ArrayList<Mac>() : new ArrayList<Mac>(maclar);
    }

    @Override
    public String ad() {
        return "Takım Gücü Analiz Agent";
    }

    @Override
    public AgentSonucu calistir() {
        analizler.clear();

        for (Mac mac : maclar) {
            analizler.add(analizOlustur(mac));
        }

        return AgentSonucu.basarili(ad(), analizler.size() + " maç için takım gücü analizi üretildi.");
    }

    public List<TakimGucuAnalizi> getAnalizler() {
        return new ArrayList<TakimGucuAnalizi>(analizler);
    }

    private TakimGucuAnalizi analizOlustur(Mac mac) {
        int evPuani = 50;
        int deplasmanPuani = 48;
        Oran evOrani = oranBul(mac, BahisTuru.MAC_SONUCU_1);
        Oran deplasmanOrani = oranBul(mac, BahisTuru.MAC_SONUCU_2);

        if (evOrani != null && deplasmanOrani != null) {
            int fark = oranFarkiPuani(evOrani.getDeger(), deplasmanOrani.getDeger());
            evPuani += fark;
            deplasmanPuani -= fark;
        }

        String oneCikanTaraf = "Dengeli";
        if (evPuani - deplasmanPuani >= 6) {
            oneCikanTaraf = "1";
        } else if (deplasmanPuani - evPuani >= 6) {
            oneCikanTaraf = "2";
        }

        String gerekce = "Takım gücü ilk sürümde maç sonucu oran dengesi ve iç saha avantajı üzerinden hesaplandı.";
        return new TakimGucuAnalizi(mac, sinirla(evPuani), sinirla(deplasmanPuani), oneCikanTaraf, gerekce);
    }

    private Oran oranBul(Mac mac, BahisTuru bahisTuru) {
        for (Oran oran : mac.getOranlar()) {
            if (oran.getBahisTuru() == bahisTuru) {
                return oran;
            }
        }

        return null;
    }

    private int oranFarkiPuani(BigDecimal evOrani, BigDecimal deplasmanOrani) {
        BigDecimal fark = deplasmanOrani.subtract(evOrani);
        return Math.max(-14, Math.min(14, fark.multiply(new BigDecimal("8")).intValue()));
    }

    private int sinirla(int puan) {
        return Math.max(0, Math.min(100, puan));
    }
}
