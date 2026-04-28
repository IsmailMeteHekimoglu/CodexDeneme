package com.futbolanaliz.modeller;

public class TakimGucuAnalizi {
    private final Mac mac;
    private final int evSahibiGucPuani;
    private final int deplasmanGucPuani;
    private final String oneCikanTaraf;
    private final String gerekce;

    public TakimGucuAnalizi(Mac mac, int evSahibiGucPuani, int deplasmanGucPuani, String oneCikanTaraf, String gerekce) {
        this.mac = mac;
        this.evSahibiGucPuani = evSahibiGucPuani;
        this.deplasmanGucPuani = deplasmanGucPuani;
        this.oneCikanTaraf = oneCikanTaraf;
        this.gerekce = gerekce;
    }

    public Mac getMac() {
        return mac;
    }

    public int getEvSahibiGucPuani() {
        return evSahibiGucPuani;
    }

    public int getDeplasmanGucPuani() {
        return deplasmanGucPuani;
    }

    public String getOneCikanTaraf() {
        return oneCikanTaraf;
    }

    public String getGerekce() {
        return gerekce;
    }
}
