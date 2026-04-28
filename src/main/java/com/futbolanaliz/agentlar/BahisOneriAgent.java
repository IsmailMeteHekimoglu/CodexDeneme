package com.futbolanaliz.agentlar;

import com.futbolanaliz.modeller.BahisOnerisi;
import com.futbolanaliz.modeller.OranRiskAnalizi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BahisOneriAgent implements Agent {
    private final List<OranRiskAnalizi> riskAnalizleri;
    private final List<BahisOnerisi> oneriler = new ArrayList<BahisOnerisi>();

    public BahisOneriAgent(List<OranRiskAnalizi> riskAnalizleri) {
        this.riskAnalizleri = riskAnalizleri == null ? new ArrayList<OranRiskAnalizi>() : new ArrayList<OranRiskAnalizi>(riskAnalizleri);
    }

    @Override
    public String ad() {
        return "Bahis Öneri Agent";
    }

    @Override
    public AgentSonucu calistir() {
        oneriler.clear();
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

        for (OranRiskAnalizi analiz : siraliAnalizler) {
            if (analiz.getRiskPuani() <= 55) {
                oneriler.add(new BahisOnerisi(
                        analiz.getMac(),
                        analiz.getBahisTuru(),
                        analiz.getSecim(),
                        analiz.getOranDegeri(),
                        analiz.getRiskPuani(),
                        analiz.getGuvenPuani(),
                        analiz.getGerekce()
                ));
            }
        }

        return AgentSonucu.basarili(ad(), oneriler.size() + " düşük/orta riskli bahis önerisi üretildi.");
    }

    public List<BahisOnerisi> getOneriler() {
        return new ArrayList<BahisOnerisi>(oneriler);
    }
}
