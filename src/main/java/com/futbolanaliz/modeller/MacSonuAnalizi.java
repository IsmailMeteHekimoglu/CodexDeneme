package com.futbolanaliz.modeller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MacSonuAnalizi {
    private final Mac mac;
    private final String macSonuTahmini;
    private final int guvenPuani;
    private final String gerekce;
    private final List<MacYorumu> yorumlar;

    public MacSonuAnalizi(Mac mac, String macSonuTahmini, int guvenPuani, String gerekce, List<MacYorumu> yorumlar) {
        this.mac = mac;
        this.macSonuTahmini = macSonuTahmini;
        this.guvenPuani = guvenPuani;
        this.gerekce = gerekce;
        this.yorumlar = new ArrayList<MacYorumu>(yorumlar);
    }

    public Mac getMac() {
        return mac;
    }

    public String getMacSonuTahmini() {
        return macSonuTahmini;
    }

    public int getGuvenPuani() {
        return guvenPuani;
    }

    public String getGerekce() {
        return gerekce;
    }

    public List<MacYorumu> getYorumlar() {
        return Collections.unmodifiableList(yorumlar);
    }
}
