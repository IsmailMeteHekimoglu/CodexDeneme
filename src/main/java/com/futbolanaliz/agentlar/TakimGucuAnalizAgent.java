package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.BahisTuru;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.Oran;
import com.futbolanaliz.modeller.TakimGucuAnalizi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TakimGucuAnalizAgent implements Agent {
    private static final Locale TURKCE = new Locale("tr", "TR");

    private final List<Mac> maclar;
    private final List<TakimGucuAnalizi> analizler = new ArrayList<TakimGucuAnalizi>();

    public TakimGucuAnalizAgent(List<Mac> maclar) {
        this.maclar = maclar == null ? new ArrayList<Mac>() : new ArrayList<Mac>(maclar);
    }

    @Override
    public String ad() {
        return "Takim Gucu Analiz Agent";
    }

    @Override
    public AgentSonucu calistir() {
        analizler.clear();

        for (Mac mac : maclar) {
            analizler.add(analizOlustur(mac));
        }

        return AgentSonucu.basarili(ad(), analizler.size() + " mac icin takim gucu analizi uretildi.");
    }

    public List<TakimGucuAnalizi> getAnalizler() {
        return new ArrayList<TakimGucuAnalizi>(analizler);
    }

    private TakimGucuAnalizi analizOlustur(Mac mac) {
        int evPuani = 50;
        int deplasmanPuani = 48;
        int icSahaAvantaji = icSahaAvantajiPuani(mac);
        int ligKupaDengesi = ligKupaDengesiPuani(mac);
        Oran evOrani = oranBul(mac, BahisTuru.MAC_SONUCU_1);
        Oran deplasmanOrani = oranBul(mac, BahisTuru.MAC_SONUCU_2);

        evPuani += icSahaAvantaji;
        deplasmanPuani -= Math.max(0, icSahaAvantaji - 1);
        evPuani += ligKupaDengesi;
        deplasmanPuani += ligKupaDengesi;

        if (evOrani != null && deplasmanOrani != null) {
            int fark = oranFarkiPuani(evOrani.getDeger(), deplasmanOrani.getDeger());
            evPuani += fark;
            deplasmanPuani -= fark;
        }

        if (!mac.getMacOncesiTahmin().isEmpty()) {
            String tahmin = normalize(mac.getMacOncesiTahmin());
            if (tahmin.contains("1") || tahmin.contains(normalize(mac.getEvSahibi()))) {
                evPuani += 3;
            }
            if (tahmin.contains("2") || tahmin.contains(normalize(mac.getDeplasman()))) {
                deplasmanPuani += 3;
            }
            if (tahmin.contains("x") || tahmin.contains("beraber")) {
                evPuani -= 1;
                deplasmanPuani -= 1;
            }
        }

        String oneCikanTaraf = "Dengeli";
        if (evPuani - deplasmanPuani >= 6) {
            oneCikanTaraf = "1";
        } else if (deplasmanPuani - evPuani >= 6) {
            oneCikanTaraf = "2";
        }

        String gerekce = "Takim gucu; mac sonucu oran dengesi, ic saha/deplasman etkisi, lig/kupa baglami ve varsa mac oncesi tahmin sinyali birlikte degerlendirilerek hesaplandi.";
        return new TakimGucuAnalizi(mac, sinirla(evPuani), sinirla(deplasmanPuani), oneCikanTaraf, gerekce);
    }

    private int icSahaAvantajiPuani(Mac mac) {
        String lig = normalize(mac.getLig());
        if (lig.contains("hazirlik") || lig.contains("friendly") || lig.contains("tarafsiz")) {
            return 1;
        }
        if (lig.contains("kupa") || lig.contains("cup") || lig.contains("play-off") || lig.contains("playoff")) {
            return 2;
        }
        return 4;
    }

    private int ligKupaDengesiPuani(Mac mac) {
        String lig = normalize(mac.getLig());
        if (lig.contains("final") || lig.contains("yari final") || lig.contains("yari-final") || lig.contains("play-off") || lig.contains("playoff")) {
            return 2;
        }
        if (lig.contains("kupa") || lig.contains("cup")) {
            return 1;
        }
        return 0;
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

    private String normalize(String deger) {
        return deger == null ? "" : deger.toLowerCase(TURKCE);
    }
}
