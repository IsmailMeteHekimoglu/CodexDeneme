package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.BahisTuru;
import com.futbolanaliz.modeller.LlmYorumAnalizSonucu;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.MacSonuAnalizi;
import com.futbolanaliz.modeller.MacYorumu;
import com.futbolanaliz.modeller.Oran;
import com.futbolanaliz.servisler.LlmYorumAnalizServisi;
import com.futbolanaliz.servisler.MacYorumServisi;
import com.futbolanaliz.servisler.TokenTahminServisi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MacYorumAnalizAgent implements Agent {
    private static final Locale TURKCE = new Locale("tr", "TR");

    private final MacYorumServisi macYorumServisi = new MacYorumServisi();
    private final LlmYorumAnalizServisi llmYorumAnalizServisi = new LlmYorumAnalizServisi();
    private final TokenTahminServisi tokenTahminServisi = new TokenTahminServisi();
    private final List<Mac> maclar;
    private final List<MacSonuAnalizi> analizler = new ArrayList<MacSonuAnalizi>();
    private int sonLlmTokenSayisi;

    public MacYorumAnalizAgent(List<Mac> maclar) {
        this.maclar = maclar == null ? new ArrayList<Mac>() : new ArrayList<Mac>(maclar);
    }

    @Override
    public String ad() {
        return "Mac Yorumu ve Mac Sonu Analiz Agent";
    }

    @Override
    public AgentSonucu calistir() {
        analizler.clear();
        sonLlmTokenSayisi = 0;

        for (Mac mac : maclar) {
            List<MacYorumu> yorumlar = macYorumServisi.yorumlariGetir(mac);
            analizler.add(analizOlustur(mac, yorumlar));
        }

        String mesaj = analizler.size() + " mac icin yorum destekli mac sonu analizi uretildi.";
        return AgentSonucu.basarili(
                ad(),
                mesaj,
                tokenTahminServisi.agentTahmini(ad(), mesaj, maclar, analizler) + sonLlmTokenSayisi
        );
    }

    public List<MacSonuAnalizi> getAnalizler() {
        return new ArrayList<MacSonuAnalizi>(analizler);
    }

    private MacSonuAnalizi analizOlustur(Mac mac, List<MacYorumu> yorumlar) {
        MacSonuAnalizi llmAnalizi = llmAnalizOlustur(mac, yorumlar);
        if (llmAnalizi != null) {
            return llmAnalizi;
        }

        Oran favoriOran = favoriMacSonuOrani(mac);
        String tahmin = tahminAdi(favoriOran);
        int guvenPuani = oranGuvenPuani(favoriOran);
        int evSinyali = 0;
        int deplasmanSinyali = 0;
        int beraberlikSinyali = 0;

        for (MacYorumu yorum : yorumlar) {
            String metin = normalize(yorum.getYorumMetni());
            evSinyali += takimSinyali(metin, mac.getEvSahibi());
            deplasmanSinyali += takimSinyali(metin, mac.getDeplasman());
            beraberlikSinyali += kelimeVarMi(metin, "beraberlik") || kelimeVarMi(metin, "denge") ? 2 : 0;
        }

        if (!yorumlar.isEmpty()) {
            if (evSinyali > deplasmanSinyali && evSinyali > beraberlikSinyali) {
                tahmin = "1";
                guvenPuani += 8;
            } else if (deplasmanSinyali > evSinyali && deplasmanSinyali > beraberlikSinyali) {
                tahmin = "2";
                guvenPuani += 8;
            } else if (beraberlikSinyali > evSinyali && beraberlikSinyali > deplasmanSinyali) {
                tahmin = "X";
                guvenPuani += 6;
            } else {
                guvenPuani += 3;
            }
        }

        guvenPuani = Math.max(25, Math.min(85, guvenPuani));
        return new MacSonuAnalizi(mac, tahmin, guvenPuani, gerekceOlustur(favoriOran, yorumlar, evSinyali, deplasmanSinyali, beraberlikSinyali), yorumlar);
    }

    private MacSonuAnalizi llmAnalizOlustur(Mac mac, List<MacYorumu> yorumlar) {
        try {
            LlmYorumAnalizSonucu sonuc = llmYorumAnalizServisi.macYorumlariniAnalizEt(mac, yorumlar);
            if (sonuc == null) {
                return null;
            }

            sonLlmTokenSayisi += sonuc.getToplamTokenSayisi();
            String gerekce = sonuc.getGerekce()
                    + " LLM token kullanimi: ~"
                    + sonuc.getGirisTokenSayisi()
                    + " giris / ~"
                    + sonuc.getCikisTokenSayisi()
                    + " cikis.";
            return new MacSonuAnalizi(mac, sonuc.getMacSonuTahmini(), sonuc.getGuvenPuani(), gerekce, yorumlar);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Oran favoriMacSonuOrani(Mac mac) {
        Oran favori = null;

        for (Oran oran : mac.getOranlar()) {
            if (!macSonuOraniMi(oran.getBahisTuru())) {
                continue;
            }

            if (favori == null || oran.getDeger().compareTo(favori.getDeger()) < 0) {
                favori = oran;
            }
        }

        return favori;
    }

    private boolean macSonuOraniMi(BahisTuru bahisTuru) {
        return bahisTuru == BahisTuru.MAC_SONUCU_1
                || bahisTuru == BahisTuru.MAC_SONUCU_X
                || bahisTuru == BahisTuru.MAC_SONUCU_2;
    }

    private String tahminAdi(Oran oran) {
        if (oran == null) {
            return "Belirsiz";
        }

        if (oran.getBahisTuru() == BahisTuru.MAC_SONUCU_1) {
            return "1";
        }

        if (oran.getBahisTuru() == BahisTuru.MAC_SONUCU_X) {
            return "X";
        }

        return "2";
    }

    private int oranGuvenPuani(Oran oran) {
        if (oran == null) {
            return 35;
        }

        BigDecimal deger = oran.getDeger();
        if (deger.compareTo(new BigDecimal("1.50")) <= 0) {
            return 70;
        }
        if (deger.compareTo(new BigDecimal("2.00")) <= 0) {
            return 62;
        }
        if (deger.compareTo(new BigDecimal("2.50")) <= 0) {
            return 54;
        }

        return 45;
    }

    private int takimSinyali(String metin, String takimAdi) {
        String takim = normalize(takimAdi);
        int puan = metin.contains(takim) ? 2 : 0;

        if (metin.contains(takim) && (kelimeVarMi(metin, "favori") || kelimeVarMi(metin, "kazan") || kelimeVarMi(metin, "galibiyet"))) {
            puan += 3;
        }

        if (metin.contains(takim) && (kelimeVarMi(metin, "eksik") || kelimeVarMi(metin, "sakat") || kelimeVarMi(metin, "cezali") || kelimeVarMi(metin, "zorlan"))) {
            puan -= 2;
        }

        return puan;
    }

    private boolean kelimeVarMi(String metin, String kelime) {
        return metin.contains(kelime);
    }

    private String gerekceOlustur(Oran favoriOran, List<MacYorumu> yorumlar, int evSinyali, int deplasmanSinyali, int beraberlikSinyali) {
        StringBuilder gerekce = new StringBuilder();

        if (favoriOran == null) {
            gerekce.append("Mac sonucu orani bulunamadi; yorum ve genel sinyal dengesi sinirli yorumlandi.");
        } else {
            gerekce.append("En dusuk mac sonucu orani ")
                    .append(favoriOran.getBahisTuru().getGorunenAd())
                    .append(" tarafinda: ")
                    .append(favoriOran.formatliDeger())
                    .append(".");
        }

        if (yorumlar.isEmpty()) {
            gerekce.append(" iddaa yorum verisi bulunamadi; analiz temel oran dengesine gore uretildi.");
        } else {
            gerekce.append(" ")
                    .append(yorumlar.size())
                    .append(" yorum sinyali incelendi. Ev/deplasman/beraberlik sinyali: ")
                    .append(evSinyali)
                    .append("/")
                    .append(deplasmanSinyali)
                    .append("/")
                    .append(beraberlikSinyali)
                    .append(".");
        }

        return gerekce.toString();
    }

    private String normalize(String deger) {
        return deger == null ? "" : deger.toLowerCase(TURKCE);
    }
}
