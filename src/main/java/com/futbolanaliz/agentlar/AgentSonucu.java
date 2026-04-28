package com.futbolanaliz.agentlar;

import java.time.LocalDateTime;

public class AgentSonucu {
    private final String agentAdi;
    private final boolean basarili;
    private final String mesaj;
    private final LocalDateTime olusturmaZamani;

    public AgentSonucu(String agentAdi, boolean basarili, String mesaj, LocalDateTime olusturmaZamani) {
        this.agentAdi = agentAdi;
        this.basarili = basarili;
        this.mesaj = mesaj;
        this.olusturmaZamani = olusturmaZamani;
    }

    public static AgentSonucu basarili(String agentAdi, String mesaj) {
        return new AgentSonucu(agentAdi, true, mesaj, LocalDateTime.now());
    }

    public static AgentSonucu basarisiz(String agentAdi, String mesaj) {
        return new AgentSonucu(agentAdi, false, mesaj, LocalDateTime.now());
    }

    public String getAgentAdi() {
        return agentAdi;
    }

    public boolean isBasarili() {
        return basarili;
    }

    public String getMesaj() {
        return mesaj;
    }

    public LocalDateTime getOlusturmaZamani() {
        return olusturmaZamani;
    }
}
