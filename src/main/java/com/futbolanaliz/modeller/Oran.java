package com.futbolanaliz.modeller;

import java.math.BigDecimal;

public class Oran {
    private final BahisTuru bahisTuru;
    private final BigDecimal deger;

    public Oran(BahisTuru bahisTuru, BigDecimal deger) {
        this.bahisTuru = bahisTuru;
        this.deger = deger;
    }

    public BahisTuru getBahisTuru() {
        return bahisTuru;
    }

    public BigDecimal getDeger() {
        return deger;
    }

    public String formatliDeger() {
        return deger.toPlainString();
    }
}
