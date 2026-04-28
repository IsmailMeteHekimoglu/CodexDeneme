package com.futbolanaliz.modeller;

public class LlmYorumAnalizSonucu {
    private final String macSonuTahmini;
    private final int guvenPuani;
    private final String gerekce;
    private final int girisTokenSayisi;
    private final int cikisTokenSayisi;

    public LlmYorumAnalizSonucu(String macSonuTahmini, int guvenPuani, String gerekce, int girisTokenSayisi, int cikisTokenSayisi) {
        this.macSonuTahmini = macSonuTahmini;
        this.guvenPuani = Math.max(0, Math.min(100, guvenPuani));
        this.gerekce = gerekce;
        this.girisTokenSayisi = Math.max(0, girisTokenSayisi);
        this.cikisTokenSayisi = Math.max(0, cikisTokenSayisi);
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

    public int getGirisTokenSayisi() {
        return girisTokenSayisi;
    }

    public int getCikisTokenSayisi() {
        return cikisTokenSayisi;
    }

    public int getToplamTokenSayisi() {
        return girisTokenSayisi + cikisTokenSayisi;
    }
}
