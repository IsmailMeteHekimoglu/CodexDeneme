package com.futbolanaliz.modeller;

public class KadroDurumuAnalizi {
    private final Mac mac;
    private final int evSahibiRiskPuani;
    private final int deplasmanRiskPuani;
    private final String durumOzeti;

    public KadroDurumuAnalizi(Mac mac, int evSahibiRiskPuani, int deplasmanRiskPuani, String durumOzeti) {
        this.mac = mac;
        this.evSahibiRiskPuani = evSahibiRiskPuani;
        this.deplasmanRiskPuani = deplasmanRiskPuani;
        this.durumOzeti = durumOzeti;
    }

    public Mac getMac() {
        return mac;
    }

    public int getEvSahibiRiskPuani() {
        return evSahibiRiskPuani;
    }

    public int getDeplasmanRiskPuani() {
        return deplasmanRiskPuani;
    }

    public String getDurumOzeti() {
        return durumOzeti;
    }
}
