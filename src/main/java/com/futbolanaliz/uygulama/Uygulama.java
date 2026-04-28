package com.futbolanaliz.uygulama;

import com.futbolanaliz.agentlar.AnaYoneticiAgent;

public class Uygulama {
    public static void main(String[] args) {
        if (args != null && args.length > 0 && "--konsol".equalsIgnoreCase(args[0])) {
            AnaYoneticiAgent anaYoneticiAgent = new AnaYoneticiAgent();
            anaYoneticiAgent.calistir();
            return;
        }

        ArayuzUygulamasi.baslat();
    }
}
