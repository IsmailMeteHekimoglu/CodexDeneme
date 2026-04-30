package com.futbolanaliz.modeller;

import java.math.BigDecimal;

public class BahisOnerisi {
    private final Mac mac;
    private final BahisTuru bahisTuru;
    private final String secim;
    private final BigDecimal oranDegeri;
    private final int riskPuani;
    private final int guvenPuani;
    private final String riskSeviyesi;
    private final String gerekce;

    public BahisOnerisi(Mac mac, BahisTuru bahisTuru, String secim, BigDecimal oranDegeri, int riskPuani, int guvenPuani, String gerekce) {
        this(mac, bahisTuru, secim, oranDegeri, riskPuani, guvenPuani, riskSeviyesiVarsayilan(riskPuani), gerekce);
    }

    public BahisOnerisi(Mac mac, BahisTuru bahisTuru, String secim, BigDecimal oranDegeri, int riskPuani, int guvenPuani, String riskSeviyesi, String gerekce) {
        this.mac = mac;
        this.bahisTuru = bahisTuru;
        this.secim = secim;
        this.oranDegeri = oranDegeri;
        this.riskPuani = riskPuani;
        this.guvenPuani = guvenPuani;
        this.riskSeviyesi = riskSeviyesi == null || riskSeviyesi.trim().isEmpty() ? riskSeviyesiVarsayilan(riskPuani) : riskSeviyesi.trim().toUpperCase();
        this.gerekce = gerekce;
    }

    public Mac getMac() {
        return mac;
    }

    public BahisTuru getBahisTuru() {
        return bahisTuru;
    }

    public String getSecim() {
        return secim;
    }

    public BigDecimal getOranDegeri() {
        return oranDegeri;
    }

    public String formatliOranDegeri() {
        return oranDegeri == null ? "-" : oranDegeri.toPlainString();
    }

    public int getRiskPuani() {
        return riskPuani;
    }

    public int getGuvenPuani() {
        return guvenPuani;
    }

    public String getRiskSeviyesi() {
        return riskSeviyesi;
    }

    public String getGerekce() {
        return gerekce;
    }

    private static String riskSeviyesiVarsayilan(int riskPuani) {
        if (riskPuani <= 35) {
            return "DUSUK";
        }
        if (riskPuani <= 60) {
            return "ORTA";
        }
        return "COK_RISKLI";
    }
}
