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

    public Mac(long iddaaEventId, long broadageId, String lig, LocalDate tarih, LocalTime saat, String evSahibi, String deplasman, List<Oran> oranlar) {
        this.iddaaEventId = iddaaEventId;
        this.broadageId = broadageId;
        this.lig = lig;
        this.tarih = tarih;
        this.saat = saat;
        this.evSahibi = evSahibi;
        this.deplasman = deplasman;
        this.oranlar = new ArrayList<Oran>(oranlar);
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

    public String getKarsilasmaAdi() {
        return evSahibi + " - " + deplasman;
    }
}
