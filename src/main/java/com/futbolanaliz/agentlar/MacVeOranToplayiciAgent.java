package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.TopLig;
import com.futbolanaliz.servisler.IddaaVeriServisi;

import java.util.ArrayList;
import java.util.List;

public class MacVeOranToplayiciAgent implements Agent {
    private final IddaaVeriServisi iddaaVeriServisi = new IddaaVeriServisi();
    private final List<TopLig> izinliLigler;
    private final List<Mac> toplananMaclar = new ArrayList<Mac>();

    public MacVeOranToplayiciAgent(List<TopLig> izinliLigler) {
        this.izinliLigler = izinliLigler == null ? new ArrayList<TopLig>() : new ArrayList<TopLig>(izinliLigler);
    }

    @Override
    public String ad() {
        return "Maç ve Oran Toplayıcı Agent";
    }

    @Override
    public AgentSonucu calistir() {
        toplananMaclar.clear();
        try {
            toplananMaclar.addAll(iddaaVeriServisi.bugununFutbolMaclariniGetir(izinliLigler));

            return AgentSonucu.basarili(
                    ad(),
                    "iddaa.com üzerinden öncelikli lig/kupa filtresiyle " + toplananMaclar.size() + " maç ve oran bilgisi toplandı."
            );
        } catch (RuntimeException e) {
            return AgentSonucu.basarisiz(
                    ad(),
                    "iddaa.com verisi alınamadı: " + e.getMessage()
            );
        }
    }

    public List<Mac> getToplananMaclar() {
        return new ArrayList<Mac>(toplananMaclar);
    }
}
