package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.BahisOnerisi;
import com.futbolanaliz.modeller.BirlesikAnalizSonucu;
import com.futbolanaliz.modeller.KadroDurumuAnalizi;
import com.futbolanaliz.modeller.MacSonuAnalizi;
import com.futbolanaliz.modeller.OranRiskAnalizi;
import com.futbolanaliz.modeller.TakimGucuAnalizi;
import com.futbolanaliz.servisler.LlmBahisOneriServisi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BahisOneriAgent implements Agent {
    private final LlmBahisOneriServisi llmBahisOneriServisi = new LlmBahisOneriServisi();
    private final List<OranRiskAnalizi> riskAnalizleri;
    private final List<MacSonuAnalizi> yorumAnalizleri;
    private final List<TakimGucuAnalizi> takimAnalizleri;
    private final List<KadroDurumuAnalizi> kadroAnalizleri;
    private final List<BahisOnerisi> oneriler = new ArrayList<BahisOnerisi>();
    private String profesyonelAnaliz = "";

    public BahisOneriAgent(List<OranRiskAnalizi> riskAnalizleri) {
        this.riskAnalizleri = riskAnalizleri == null ? new ArrayList<OranRiskAnalizi>() : new ArrayList<OranRiskAnalizi>(riskAnalizleri);
        this.yorumAnalizleri = new ArrayList<MacSonuAnalizi>();
        this.takimAnalizleri = new ArrayList<TakimGucuAnalizi>();
        this.kadroAnalizleri = new ArrayList<KadroDurumuAnalizi>();
    }

    public BahisOneriAgent(List<OranRiskAnalizi> riskAnalizleri, List<MacSonuAnalizi> yorumAnalizleri, List<TakimGucuAnalizi> takimAnalizleri, List<KadroDurumuAnalizi> kadroAnalizleri) {
        this.riskAnalizleri = riskAnalizleri == null ? new ArrayList<OranRiskAnalizi>() : new ArrayList<OranRiskAnalizi>(riskAnalizleri);
        this.yorumAnalizleri = yorumAnalizleri == null ? new ArrayList<MacSonuAnalizi>() : new ArrayList<MacSonuAnalizi>(yorumAnalizleri);
        this.takimAnalizleri = takimAnalizleri == null ? new ArrayList<TakimGucuAnalizi>() : new ArrayList<TakimGucuAnalizi>(takimAnalizleri);
        this.kadroAnalizleri = kadroAnalizleri == null ? new ArrayList<KadroDurumuAnalizi>() : new ArrayList<KadroDurumuAnalizi>(kadroAnalizleri);
    }

    @Override
    public String ad() {
        return "Bahis Oneri Agent";
    }

    @Override
    public AgentSonucu calistir() {
        oneriler.clear();
        profesyonelAnaliz = "";
        List<OranRiskAnalizi> siraliAnalizler = new ArrayList<OranRiskAnalizi>(riskAnalizleri);
        Collections.sort(siraliAnalizler, new Comparator<OranRiskAnalizi>() {
            @Override
            public int compare(OranRiskAnalizi ilk, OranRiskAnalizi ikinci) {
                int riskKarsilastirma = Integer.compare(ilk.getRiskPuani(), ikinci.getRiskPuani());
                if (riskKarsilastirma != 0) {
                    return riskKarsilastirma;
                }
                return Integer.compare(ikinci.getGuvenPuani(), ilk.getGuvenPuani());
            }
        });

        BirlesikAnalizSonucu llmSonucu = llmAnaliziUret(siraliAnalizler);
        if (llmSonucu != null && !llmSonucu.getOneriler().isEmpty()) {
            profesyonelAnaliz = llmSonucu.getProfesyonelAnaliz();
            oneriler.addAll(llmSonucu.getOneriler());
            return AgentSonucu.basarili(ad(), oneriler.size() + " LLM destekli bahis onerisi uretildi.");
        }

        fallbackProfilOnerisiEkle(siraliAnalizler, "DUSUK", 0, 35);
        fallbackProfilOnerisiEkle(siraliAnalizler, "ORTA", 36, 60);
        fallbackProfilOnerisiEkle(siraliAnalizler, "COK_RISKLI", 61, 100);

        return AgentSonucu.basarili(ad(), oneriler.size() + " risk profilli bahis onerisi uretildi.");
    }

    public List<BahisOnerisi> getOneriler() {
        return new ArrayList<BahisOnerisi>(oneriler);
    }

    public String getProfesyonelAnaliz() {
        return profesyonelAnaliz == null ? "" : profesyonelAnaliz;
    }

    private BirlesikAnalizSonucu llmAnaliziUret(List<OranRiskAnalizi> siraliAnalizler) {
        try {
            return llmBahisOneriServisi.birlesikAnalizUret(siraliAnalizler, yorumAnalizleri, takimAnalizleri, kadroAnalizleri);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void fallbackProfilOnerisiEkle(List<OranRiskAnalizi> analizler, String riskSeviyesi, int altRisk, int ustRisk) {
        OranRiskAnalizi secilen = null;
        for (OranRiskAnalizi analiz : analizler) {
            if (analiz.getRiskPuani() < altRisk || analiz.getRiskPuani() > ustRisk) {
                continue;
            }
            if (analizKullanildiMi(analiz)) {
                continue;
            }
            if (secilen == null || analiz.getGuvenPuani() > secilen.getGuvenPuani()) {
                secilen = analiz;
            }
        }

        if (secilen == null) {
            secilen = profilIcinEnYakinAnaliz(analizler, riskSeviyesi);
        }

        if (secilen == null) {
            return;
        }

        oneriler.add(new BahisOnerisi(
                secilen.getMac(),
                secilen.getBahisTuru(),
                secilen.getSecim(),
                secilen.getOranDegeri(),
                profilRiskPuani(riskSeviyesi, secilen.getRiskPuani()),
                secilen.getGuvenPuani(),
                riskSeviyesi,
                riskSeviyesi + " risk profili:\n"
                        + "Karar ozeti: Bu profil icin en uygun oran/risk adayi secildi.\n"
                        + "Veri degerlendirmesi: " + secilen.getGerekce()
        ));
    }

    private OranRiskAnalizi profilIcinEnYakinAnaliz(List<OranRiskAnalizi> analizler, String riskSeviyesi) {
        OranRiskAnalizi secilen = null;
        for (OranRiskAnalizi analiz : analizler) {
            if (analizKullanildiMi(analiz)) {
                continue;
            }
            if (secilen == null) {
                secilen = analiz;
                continue;
            }
            if ("DUSUK".equals(riskSeviyesi) && analiz.getRiskPuani() < secilen.getRiskPuani()) {
                secilen = analiz;
            } else if ("COK_RISKLI".equals(riskSeviyesi) && analiz.getRiskPuani() > secilen.getRiskPuani()) {
                secilen = analiz;
            } else if ("ORTA".equals(riskSeviyesi)
                    && Math.abs(analiz.getRiskPuani() - 50) < Math.abs(secilen.getRiskPuani() - 50)) {
                secilen = analiz;
            }
        }
        return secilen;
    }

    private boolean analizKullanildiMi(OranRiskAnalizi analiz) {
        for (BahisOnerisi oneri : oneriler) {
            if (oneri.getBahisTuru() == analiz.getBahisTuru()
                    && oneri.getSecim().equalsIgnoreCase(analiz.getSecim())) {
                return true;
            }
        }
        return false;
    }

    private int profilRiskPuani(String riskSeviyesi, int varsayilan) {
        if ("DUSUK".equals(riskSeviyesi)) {
            return Math.min(varsayilan, 35);
        }
        if ("ORTA".equals(riskSeviyesi)) {
            return Math.max(36, Math.min(varsayilan, 60));
        }
        return Math.max(61, varsayilan);
    }
}
