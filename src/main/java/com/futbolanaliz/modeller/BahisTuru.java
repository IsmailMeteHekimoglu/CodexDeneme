package com.futbolanaliz.modeller;

public enum BahisTuru {
    MAC_SONUCU_1("Maç Sonucu 1"),
    MAC_SONUCU_X("Maç Sonucu X"),
    MAC_SONUCU_2("Maç Sonucu 2"),
    CIFTE_SANS_1X("Çifte Şans 1X"),
    CIFTE_SANS_X2("Çifte Şans X2"),
    ALT_2_5("2.5 Alt"),
    UST_2_5("2.5 Üst"),
    KARSILIKLI_GOL_VAR("Karşılıklı Gol Var"),
    KARSILIKLI_GOL_YOK("Karşılıklı Gol Yok");

    private final String gorunenAd;

    BahisTuru(String gorunenAd) {
        this.gorunenAd = gorunenAd;
    }

    public String getGorunenAd() {
        return gorunenAd;
    }
}
