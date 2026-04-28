package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.TopLig;
import com.futbolanaliz.servisler.IddaaVeriServisi;

import java.util.ArrayList;
import java.util.List;

public class TopLigKontrolAgent implements Agent {
    private final IddaaVeriServisi iddaaVeriServisi = new IddaaVeriServisi();
    private final List<TopLig> kontrolEdilenLigler = new ArrayList<TopLig>();

    @Override
    public String ad() {
        return "Öncelikli Organizasyon Kontrol Agent";
    }

    @Override
    public AgentSonucu calistir() {
        kontrolEdilenLigler.clear();

        try {
            kontrolEdilenLigler.addAll(iddaaVeriServisi.oncelikliOrganizasyonlariKontrolEt());

            return AgentSonucu.basarili(
                    ad(),
                    kontrolEdilenLigler.size() + " öncelikli lig/kupa iddaa.com lig kataloğunda doğrulandı."
            );
        } catch (RuntimeException e) {
            return AgentSonucu.basarisiz(
                    ad(),
                    "Öncelikli organizasyon kontrolü yapılamadı: " + e.getMessage()
            );
        }
    }

    public List<TopLig> getKontrolEdilenLigler() {
        return new ArrayList<TopLig>(kontrolEdilenLigler);
    }
}
