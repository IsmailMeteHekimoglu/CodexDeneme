package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.BahisTuru;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.MacSonuAnalizi;
import com.futbolanaliz.modeller.MacYorumu;
import com.futbolanaliz.modeller.Oran;
import com.futbolanaliz.servisler.MacYorumServisi;
import com.futbolanaliz.servisler.TokenTahminServisi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MacYorumAnalizAgent implements Agent {
    private static final Locale TURKCE = new Locale("tr", "TR");

    private final MacYorumServisi macYorumServisi = new MacYorumServisi();
    private final TokenTahminServisi tokenTahminServisi = new TokenTahminServisi();
    private final List<Mac> maclar;
    private final List<MacYorumu> onHazirYorumlar;
    private final List<MacSonuAnalizi> analizler = new ArrayList<MacSonuAnalizi>();

    public MacYorumAnalizAgent(List<Mac> maclar) {
        this.maclar = maclar == null ? new ArrayList<Mac>() : new ArrayList<Mac>(maclar);
        this.onHazirYorumlar = null;
    }

    public MacYorumAnalizAgent(List<Mac> maclar, List<MacYorumu> onHazirYorumlar) {
        this.maclar = maclar == null ? new ArrayList<Mac>() : new ArrayList<Mac>(maclar);
        this.onHazirYorumlar = onHazirYorumlar == null ? null : new ArrayList<MacYorumu>(onHazirYorumlar);
    }

    @Override
    public String ad() {
        return "Mac Yorumu ve Mac Sonu Analiz Agent";
    }

    @Override
    public AgentSonucu calistir() {
        analizler.clear();

        for (Mac mac : maclar) {
            List<MacYorumu> yorumlar = yorumlariSec(mac);
            analizler.add(analizOlustur(mac, yorumlar));
        }

        String mesaj = analizler.size() + " mac icin yorum destekli mac sonu analizi uretildi.";
        return AgentSonucu.basarili(
                ad(),
                mesaj,
                tokenTahminServisi.agentTahmini(ad(), mesaj, maclar, analizler)
        );
    }

    public List<MacSonuAnalizi> getAnalizler() {
        return new ArrayList<MacSonuAnalizi>(analizler);
    }

    private List<MacYorumu> yorumlariSec(Mac mac) {
        if (maclar.size() == 1 && onHazirYorumlar != null) {
            return new ArrayList<MacYorumu>(onHazirYorumlar);
        }
        return macYorumServisi.yorumlariGetir(mac);
    }

    private MacSonuAnalizi analizOlustur(Mac mac, List<MacYorumu> yorumlar) {
        Oran favoriOran = favoriMacSonuOrani(mac);
        String tahmin = tahminAdi(favoriOran);
        int guvenPuani = oranGuvenPuani(favoriOran);
        int evSinyali = 0;
        int deplasmanSinyali = 0;
        int beraberlikSinyali = 0;
        int baglamSinyali = 0;

        for (MacYorumu yorum : yorumlar) {
            String metin = normalize(yorum.getYorumMetni());
            evSinyali += takimSinyali(metin, mac.getEvSahibi());
            deplasmanSinyali += takimSinyali(metin, mac.getDeplasman());
            beraberlikSinyali += kelimeVarMi(metin, "beraberlik") || kelimeVarMi(metin, "denge") ? 2 : 0;
            baglamSinyali += baglamSinyali(metin);
        }

        evSinyali += icSahaSinyali(mac);
        baglamSinyali += ligKupaSinyali(mac);

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
        return new MacSonuAnalizi(mac, tahmin, guvenPuani, gerekceOlustur(favoriOran, yorumlar, evSinyali, deplasmanSinyali, beraberlikSinyali, baglamSinyali), yorumlar);
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

        if (metin.contains(takim) && (kelimeVarMi(metin, "formda") || kelimeVarMi(metin, "iyi form") || kelimeVarMi(metin, "seri") || kelimeVarMi(metin, "moralli"))) {
            puan += 2;
        }

        if (metin.contains(takim) && (kelimeVarMi(metin, "motivasyon") || kelimeVarMi(metin, "hedef") || kelimeVarMi(metin, "puan ihtiyaci") || kelimeVarMi(metin, "tur"))) {
            puan += 2;
        }

        if (metin.contains(takim) && (kelimeVarMi(metin, "eksik") || kelimeVarMi(metin, "sakat") || kelimeVarMi(metin, "cezali") || kelimeVarMi(metin, "zorlan") || kelimeVarMi(metin, "rotasyon"))) {
            puan -= 2;
        }

        return puan;
    }

    private int baglamSinyali(String metin) {
        int puan = 0;
        if (kelimeVarMi(metin, "form") || kelimeVarMi(metin, "seri") || kelimeVarMi(metin, "son mac")) {
            puan += 1;
        }
        if (kelimeVarMi(metin, "sakat") || kelimeVarMi(metin, "cezali") || kelimeVarMi(metin, "eksik")) {
            puan += 1;
        }
        if (kelimeVarMi(metin, "ic saha") || kelimeVarMi(metin, "deplasman")) {
            puan += 1;
        }
        if (kelimeVarMi(metin, "lig") || kelimeVarMi(metin, "kupa") || kelimeVarMi(metin, "puan durumu") || kelimeVarMi(metin, "tur")) {
            puan += 1;
        }
        return puan;
    }

    private int icSahaSinyali(Mac mac) {
        String lig = normalize(mac.getLig());
        if (lig.contains("hazirlik") || lig.contains("friendly") || lig.contains("tarafsiz")) {
            return 0;
        }
        return 2;
    }

    private int ligKupaSinyali(Mac mac) {
        String lig = normalize(mac.getLig());
        if (lig.contains("final") || lig.contains("kupa") || lig.contains("cup") || lig.contains("play-off") || lig.contains("playoff")) {
            return 2;
        }
        return lig.contains("lig") ? 1 : 0;
    }

    private boolean kelimeVarMi(String metin, String kelime) {
        return metin.contains(kelime);
    }

    private String gerekceOlustur(Oran favoriOran, List<MacYorumu> yorumlar, int evSinyali, int deplasmanSinyali, int beraberlikSinyali, int baglamSinyali) {
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
                    .append(". Form/kadro/saha/lig-kupa baglam sinyali: ")
                    .append(baglamSinyali)
                    .append(".");
        }

        return gerekce.toString();
    }

    private String normalize(String deger) {
        return deger == null ? "" : deger.toLowerCase(TURKCE);
    }
}
