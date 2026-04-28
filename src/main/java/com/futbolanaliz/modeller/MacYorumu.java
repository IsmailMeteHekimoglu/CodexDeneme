package com.futbolanaliz.modeller;

import java.math.BigDecimal;

public class MacYorumu {
    private final String kaynak;
    private final String yazar;
    private final String yorumMetni;
    private final BahisTuru onerilenBahisTuru;
    private final BigDecimal oran;

    public MacYorumu(String kaynak, String yazar, String yorumMetni, BahisTuru onerilenBahisTuru, BigDecimal oran) {
        this.kaynak = kaynak;
        this.yazar = yazar;
        this.yorumMetni = yorumMetni;
        this.onerilenBahisTuru = onerilenBahisTuru;
        this.oran = oran;
    }

    public String getKaynak() {
        return kaynak;
    }

    public String getYazar() {
        return yazar;
    }

    public String getYorumMetni() {
        return yorumMetni;
    }

    public BahisTuru getOnerilenBahisTuru() {
        return onerilenBahisTuru;
    }

    public BigDecimal getOran() {
        return oran;
    }
}
