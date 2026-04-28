package com.futbolanaliz.servisler;

import com.futbolanaliz.modeller.BahisOnerisi;
import com.futbolanaliz.modeller.KadroDurumuAnalizi;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.MacSonuAnalizi;
import com.futbolanaliz.modeller.MacYorumu;
import com.futbolanaliz.modeller.Oran;
import com.futbolanaliz.modeller.OranRiskAnalizi;
import com.futbolanaliz.modeller.TakimGucuAnalizi;
import com.futbolanaliz.modeller.TopLig;

import java.util.Collection;

public class TokenTahminServisi {
    private static final int KARAKTER_BASINA_TOKEN_ORANI = 4;

    public int tahminEt(String metin) {
        if (metin == null || metin.isEmpty()) {
            return 0;
        }

        return (int) Math.ceil(metin.length() / (double) KARAKTER_BASINA_TOKEN_ORANI);
    }

    public int tahminEt(String... metinler) {
        int toplam = 0;
        if (metinler == null) {
            return toplam;
        }

        for (String metin : metinler) {
            toplam += tahminEt(metin);
        }
        return toplam;
    }

    public int tahminEt(Collection<?> degerler) {
        if (degerler == null || degerler.isEmpty()) {
            return 0;
        }

        int toplam = 0;
        for (Object deger : degerler) {
            toplam += tahminEt(nesneyiMetneCevir(deger));
        }
        return toplam;
    }

    public int agentTahmini(String agentAdi, String mesaj, Collection<?> girdi, Collection<?> cikti) {
        return tahminEt(agentAdi, mesaj) + tahminEt(girdi) + tahminEt(cikti);
    }

    private String nesneyiMetneCevir(Object deger) {
        if (deger == null) {
            return "";
        }

        if (deger instanceof Mac) {
            Mac mac = (Mac) deger;
            StringBuilder metin = new StringBuilder();
            metin.append(mac.getIddaaEventId()).append(" ")
                    .append(mac.getLig()).append(" ")
                    .append(mac.getTarih()).append(" ")
                    .append(mac.getSaat()).append(" ")
                    .append(mac.getEvSahibi()).append(" ")
                    .append(mac.getDeplasman());
            for (Oran oran : mac.getOranlar()) {
                metin.append(" ").append(oran.getBahisTuru().getGorunenAd()).append(" ").append(oran.formatliDeger());
            }
            return metin.toString();
        }

        if (deger instanceof TopLig) {
            TopLig lig = (TopLig) deger;
            return lig.getOncelikSirasi() + " " + lig.getLigAdi() + " " + lig.getKategori() + " " + lig.getIddaaLigId();
        }

        if (deger instanceof MacSonuAnalizi) {
            MacSonuAnalizi analiz = (MacSonuAnalizi) deger;
            StringBuilder metin = new StringBuilder();
            metin.append(nesneyiMetneCevir(analiz.getMac())).append(" ")
                    .append(analiz.getMacSonuTahmini()).append(" ")
                    .append(analiz.getGuvenPuani()).append(" ")
                    .append(analiz.getGerekce());
            for (MacYorumu yorum : analiz.getYorumlar()) {
                metin.append(" ").append(nesneyiMetneCevir(yorum));
            }
            return metin.toString();
        }

        if (deger instanceof MacYorumu) {
            MacYorumu yorum = (MacYorumu) deger;
            return yorum.getKaynak() + " " + yorum.getYazar() + " " + yorum.getYorumMetni() + " "
                    + yorum.getOnerilenBahisTuru() + " " + yorum.getOran();
        }

        if (deger instanceof TakimGucuAnalizi) {
            TakimGucuAnalizi analiz = (TakimGucuAnalizi) deger;
            return nesneyiMetneCevir(analiz.getMac()) + " " + analiz.getEvSahibiGucPuani() + " "
                    + analiz.getDeplasmanGucPuani() + " " + analiz.getOneCikanTaraf() + " " + analiz.getGerekce();
        }

        if (deger instanceof KadroDurumuAnalizi) {
            KadroDurumuAnalizi analiz = (KadroDurumuAnalizi) deger;
            return nesneyiMetneCevir(analiz.getMac()) + " " + analiz.getEvSahibiRiskPuani() + " "
                    + analiz.getDeplasmanRiskPuani() + " " + analiz.getDurumOzeti();
        }

        if (deger instanceof OranRiskAnalizi) {
            OranRiskAnalizi analiz = (OranRiskAnalizi) deger;
            return nesneyiMetneCevir(analiz.getMac()) + " " + analiz.getSecim() + " "
                    + analiz.formatliOranDegeri() + " " + analiz.getRiskPuani() + " "
                    + analiz.getGuvenPuani() + " " + analiz.getGerekce();
        }

        if (deger instanceof BahisOnerisi) {
            BahisOnerisi oneri = (BahisOnerisi) deger;
            return nesneyiMetneCevir(oneri.getMac()) + " " + oneri.getSecim() + " "
                    + oneri.formatliOranDegeri() + " " + oneri.getRiskPuani() + " "
                    + oneri.getGuvenPuani() + " " + oneri.getGerekce();
        }

        return String.valueOf(deger);
    }
}
