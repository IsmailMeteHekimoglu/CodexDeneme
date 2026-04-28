package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.TopLig;
import com.futbolanaliz.servisler.IddaaVeriServisi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
            for (Mac canliMac : iddaaVeriServisi.devamEdenFutbolMaclariniGetir(izinliLigler)) {
                canliMaciEkleVeyaGuncelle(canliMac);
            }
            maclariSirala();

            return AgentSonucu.basarili(
                    ad(),
                    "iddaa.com üzerinden öncelikli lig/kupa filtresiyle " + toplananMaclar.size() + " maç, skor ve oran bilgisi toplandı."
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

    private void canliMaciEkleVeyaGuncelle(Mac aday) {
        for (int i = 0; i < toplananMaclar.size(); i++) {
            Mac mac = toplananMaclar.get(i);
            if (ayniMacMi(mac, aday)) {
                toplananMaclar.set(i, aday);
                return;
            }
        }
        toplananMaclar.add(aday);
    }

    private boolean ayniMacMi(Mac ilk, Mac ikinci) {
        if (ilk.getIddaaEventId() > 0L && ilk.getIddaaEventId() == ikinci.getIddaaEventId()) {
            return true;
        }
        return ilk.getKarsilasmaAdi().equalsIgnoreCase(ikinci.getKarsilasmaAdi()) && ilk.getTarih().equals(ikinci.getTarih());
    }

    private void maclariSirala() {
        Collections.sort(toplananMaclar, new Comparator<Mac>() {
            @Override
            public int compare(Mac ilk, Mac ikinci) {
                int tarihKarsilastirma = ilk.getTarih().compareTo(ikinci.getTarih());
                if (tarihKarsilastirma != 0) {
                    return tarihKarsilastirma;
                }
                return ilk.getSaat().compareTo(ikinci.getSaat());
            }
        });
    }
}
