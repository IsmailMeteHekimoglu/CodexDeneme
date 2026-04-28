package com.futbolanaliz.agentlar;

import java.time.LocalDateTime;

public class AgentSonucu {
    private final String agentAdi;
    private final boolean basarili;
    private final String mesaj;
    private final LocalDateTime olusturmaZamani;
    private final int tahminiTokenSayisi;

    public AgentSonucu(String agentAdi, boolean basarili, String mesaj, LocalDateTime olusturmaZamani) {
        this(agentAdi, basarili, mesaj, olusturmaZamani, tahminEt(agentAdi) + tahminEt(mesaj));
    }

    public AgentSonucu(String agentAdi, boolean basarili, String mesaj, LocalDateTime olusturmaZamani, int tahminiTokenSayisi) {
        this.agentAdi = agentAdi;
        this.basarili = basarili;
        this.mesaj = mesaj;
        this.olusturmaZamani = olusturmaZamani;
        this.tahminiTokenSayisi = Math.max(0, tahminiTokenSayisi);
    }

    public static AgentSonucu basarili(String agentAdi, String mesaj) {
        return new AgentSonucu(agentAdi, true, mesaj, LocalDateTime.now());
    }

    public static AgentSonucu basarili(String agentAdi, String mesaj, int tahminiTokenSayisi) {
        return new AgentSonucu(agentAdi, true, mesaj, LocalDateTime.now(), tahminiTokenSayisi);
    }

    public static AgentSonucu basarisiz(String agentAdi, String mesaj) {
        return new AgentSonucu(agentAdi, false, mesaj, LocalDateTime.now());
    }

    public static AgentSonucu basarisiz(String agentAdi, String mesaj, int tahminiTokenSayisi) {
        return new AgentSonucu(agentAdi, false, mesaj, LocalDateTime.now(), tahminiTokenSayisi);
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

    public int getTahminiTokenSayisi() {
        return tahminiTokenSayisi;
    }

    private static int tahminEt(String metin) {
        if (metin == null || metin.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(metin.length() / 4.0);
    }
}
