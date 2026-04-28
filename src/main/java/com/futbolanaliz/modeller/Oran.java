package com.futbolanaliz.modeller;

import java.math.BigDecimal;

public class Oran {
    private final BahisTuru bahisTuru;
    private final BigDecimal deger;
    private final int marketKodu;
    private final String marketAdi;
    private final String oyunDegeri;
    private final String secimAdi;

    public Oran(BahisTuru bahisTuru, BigDecimal deger) {
        this(bahisTuru, deger, 0, "", "", bahisTuru == null ? "" : bahisTuru.getGorunenAd());
    }

    public Oran(BahisTuru bahisTuru, BigDecimal deger, int marketKodu, String marketAdi, String oyunDegeri, String secimAdi) {
        this.bahisTuru = bahisTuru == null ? BahisTuru.DIGER : bahisTuru;
        this.deger = deger;
        this.marketKodu = marketKodu;
        this.marketAdi = marketAdi == null ? "" : marketAdi.trim();
        this.oyunDegeri = oyunDegeri == null ? "" : oyunDegeri.trim();
        this.secimAdi = secimAdi == null ? "" : secimAdi.trim();
    }

    public BahisTuru getBahisTuru() {
        return bahisTuru;
    }

    public BigDecimal getDeger() {
        return deger;
    }

    public int getMarketKodu() {
        return marketKodu;
    }

    public String getMarketAdi() {
        return marketAdi;
    }

    public String getOyunDegeri() {
        return oyunDegeri;
    }

    public String getSecimAdi() {
        return secimAdi;
    }

    public String getGorunenAd() {
        if (bahisTuru != BahisTuru.DIGER) {
            return bahisTuru.getGorunenAd();
        }

        StringBuilder ad = new StringBuilder();
        if (!marketAdi.isEmpty()) {
            ad.append(marketAdi);
        } else if (marketKodu > 0) {
            ad.append("Market #").append(marketKodu);
        } else {
            ad.append(bahisTuru.getGorunenAd());
        }

        if (!oyunDegeri.isEmpty()) {
            ad.append(" ").append(oyunDegeri);
        }
        if (!secimAdi.isEmpty()) {
            ad.append(" | ").append(secimAdi);
        }
        return ad.toString();
    }

    public String formatliDeger() {
        return deger.toPlainString();
    }
}
