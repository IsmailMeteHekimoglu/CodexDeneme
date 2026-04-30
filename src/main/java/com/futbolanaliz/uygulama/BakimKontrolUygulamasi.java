package com.futbolanaliz.uygulama;

import com.futbolanaliz.agentlar.AgentSonucu;
import com.futbolanaliz.agentlar.BakimVersiyonKontrolAgent;

import java.io.File;

public class BakimKontrolUygulamasi {
    public static void main(String[] args) {
        BakimVersiyonKontrolAgent agent = new BakimVersiyonKontrolAgent(new File("."));
        AgentSonucu sonuc = agent.calistir();
        System.out.println(sonuc.getAgentAdi());
        System.out.println(sonuc.isBasarili() ? "BASARILI" : "BASARISIZ");
        System.out.println(sonuc.getMesaj());
        System.out.println("Tahmini Codex token: " + sonuc.getTahminiTokenSayisi());

        if (!sonuc.isBasarili()) {
            System.exit(1);
        }
    }
}
