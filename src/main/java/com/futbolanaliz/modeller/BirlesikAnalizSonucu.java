package com.futbolanaliz.modeller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BirlesikAnalizSonucu {
    private final String profesyonelAnaliz;
    private final List<BahisOnerisi> oneriler;

    public BirlesikAnalizSonucu(String profesyonelAnaliz, List<BahisOnerisi> oneriler) {
        this.profesyonelAnaliz = profesyonelAnaliz == null ? "" : profesyonelAnaliz;
        this.oneriler = oneriler == null ? new ArrayList<BahisOnerisi>() : new ArrayList<BahisOnerisi>(oneriler);
    }

    public String getProfesyonelAnaliz() {
        return profesyonelAnaliz;
    }

    public List<BahisOnerisi> getOneriler() {
        return Collections.unmodifiableList(oneriler);
    }
}
