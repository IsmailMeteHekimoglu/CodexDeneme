package com.futbolanaliz.modeller;

public enum BahisTuru {
    MAC_SONUCU_1("Mac Sonucu 1"),
    MAC_SONUCU_X("Mac Sonucu X"),
    MAC_SONUCU_2("Mac Sonucu 2"),
    CIFTE_SANS_1X("Cifte Sans 1X"),
    CIFTE_SANS_X2("Cifte Sans X2"),
    ALT_2_5("2.5 Alt"),
    UST_2_5("2.5 Ust"),
    KARSILIKLI_GOL_VAR("Karsilikli Gol Var"),
    KARSILIKLI_GOL_YOK("Karsilikli Gol Yok"),
    DIGER("Diger Bahis");

    private final String gorunenAd;

    BahisTuru(String gorunenAd) {
        this.gorunenAd = gorunenAd;
    }

    public String getGorunenAd() {
        return gorunenAd;
    }
}
