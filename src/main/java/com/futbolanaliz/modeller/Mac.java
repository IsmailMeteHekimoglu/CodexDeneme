package com.futbolanaliz.modeller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mac {
    private final long iddaaEventId;
    private final long broadageId;
    private final String lig;
    private final LocalDate tarih;
    private final LocalTime saat;
    private final String evSahibi;
    private final String deplasman;
    private final List<Oran> oranlar;
    private final String durum;
    private final Integer evSahibiSkor;
    private final Integer deplasmanSkor;
    private final String dakika;
    private final String macOncesiTahmin;

    public Mac(long iddaaEventId, long broadageId, String lig, LocalDate tarih, LocalTime saat, String evSahibi, String deplasman, List<Oran> oranlar) {
        this(iddaaEventId, broadageId, lig, tarih, saat, evSahibi, deplasman, oranlar, "Planlandi", null, null, "", "");
    }

    public Mac(long iddaaEventId, long broadageId, String lig, LocalDate tarih, LocalTime saat, String evSahibi, String deplasman, List<Oran> oranlar,
               String durum, Integer evSahibiSkor, Integer deplasmanSkor, String dakika, String macOncesiTahmin) {
        this.iddaaEventId = iddaaEventId;
        this.broadageId = broadageId;
        this.lig = lig;
        this.tarih = tarih;
        this.saat = saat;
        this.evSahibi = evSahibi;
        this.deplasman = deplasman;
        this.oranlar = new ArrayList<Oran>(oranlar);
        this.durum = durum == null || durum.trim().isEmpty() ? "Planlandi" : durum;
        this.evSahibiSkor = evSahibiSkor;
        this.deplasmanSkor = deplasmanSkor;
        this.dakika = dakika == null ? "" : dakika.trim();
        this.macOncesiTahmin = macOncesiTahmin == null ? "" : macOncesiTahmin.trim();
    }

    public long getIddaaEventId() {
        return iddaaEventId;
    }

    public long getBroadageId() {
        return broadageId;
    }

    public String getLig() {
        return lig;
    }

    public LocalDate getTarih() {
        return tarih;
    }

    public LocalTime getSaat() {
        return saat;
    }

    public String getEvSahibi() {
        return evSahibi;
    }

    public String getDeplasman() {
        return deplasman;
    }

    public List<Oran> getOranlar() {
        return Collections.unmodifiableList(oranlar);
    }

    public String getDurum() {
        return durum;
    }

    public Integer getEvSahibiSkor() {
        return evSahibiSkor;
    }

    public Integer getDeplasmanSkor() {
        return deplasmanSkor;
    }

    public String getDakika() {
        return dakika;
    }

    public String getMacOncesiTahmin() {
        return macOncesiTahmin;
    }

    public String getKarsilasmaAdi() {
        return evSahibi + " - " + deplasman;
    }

    public boolean devamEdiyorMu() {
        String kucukDurum = durum.toLowerCase();
        return kucukDurum.contains("half")
                || kucukDurum.contains("canli")
                || kucukDurum.contains("live")
                || kucukDurum.contains("devam")
                || kucukDurum.contains("yari")
                || kucukDurum.contains("1h")
                || kucukDurum.contains("2h")
                || kucukDurum.contains("ht");
    }

    public String getSkorMetni() {
        if (evSahibiSkor == null || deplasmanSkor == null) {
            return "Skor bekleniyor";
        }
        return evSahibiSkor + " - " + deplasmanSkor;
    }

    public String getDurumMetni() {
        if (dakika.isEmpty()) {
            return durum;
        }
        return durum + " | " + dakika;
    }
}
