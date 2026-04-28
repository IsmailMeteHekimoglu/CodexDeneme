package com.futbolanaliz.servisler;

import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.MacYorumu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MacYorumServisi {
    private static final String ISTATISTIK_OZET_URL = "https://statisticsv2.iddaa.com/statistics/eventsummary/1/";
    private static final String MAC_YORUM_URL = "https://statisticsv2.iddaa.com/broadage/getMatchCommentary?sportType=1&broadageId=";

    private final JsonServisi jsonServisi = new JsonServisi();

    public List<MacYorumu> yorumlariGetir(Mac mac) {
        List<MacYorumu> yorumlar = new ArrayList<MacYorumu>();
        long broadageId = istatistikBroadageIdBul(mac);

        if (broadageId <= 0L) {
            broadageId = mac.getBroadageId();
        }

        if (broadageId <= 0L) {
            return yorumlar;
        }

        try {
            String json = httpGet(MAC_YORUM_URL + broadageId);
            Object kok = jsonServisi.jsonOku(json);
            Set<String> metinler = new LinkedHashSet<String>();
            metinleriTopla(kok, metinler);

            for (String metin : metinler) {
                if (metin.length() >= 35 && maclaIlgiliGorunuyor(mac, metin)) {
                    yorumlar.add(new MacYorumu("iddaa.com istatistik yorumlari", "Broadage", sadeMetin(metin), null, BigDecimal.ZERO));
                }
            }
        } catch (RuntimeException e) {
            return yorumlar;
        }

        return yorumlar;
    }

    private long istatistikBroadageIdBul(Mac mac) {
        if (mac.getIddaaEventId() <= 0L) {
            return 0L;
        }

        try {
            String json = httpGet(ISTATISTIK_OZET_URL + mac.getIddaaEventId());
            Object kok = jsonServisi.jsonOku(json);
            Object broadageId = degeriBul(kok, "broadageId");

            if (broadageId instanceof Number) {
                return ((Number) broadageId).longValue();
            }

            if (broadageId != null) {
                return Long.parseLong(String.valueOf(broadageId));
            }
        } catch (RuntimeException e) {
            return 0L;
        }

        return 0L;
    }

    private boolean maclaIlgiliGorunuyor(Mac mac, String metin) {
        String kucukMetin = metin.toLowerCase();
        return kucukMetin.contains(mac.getEvSahibi().toLowerCase())
                || kucukMetin.contains(mac.getDeplasman().toLowerCase())
                || kucukMetin.contains("maç")
                || kucukMetin.contains("gol")
                || kucukMetin.contains("galibiyet")
                || kucukMetin.contains("beraberlik");
    }

    private Object degeriBul(Object kok, String anahtar) {
        if (kok instanceof Map) {
            Map<?, ?> harita = (Map<?, ?>) kok;
            if (harita.containsKey(anahtar)) {
                return harita.get(anahtar);
            }

            for (Object deger : harita.values()) {
                Object bulunan = degeriBul(deger, anahtar);
                if (bulunan != null) {
                    return bulunan;
                }
            }
        }

        if (kok instanceof List) {
            for (Object deger : (List<?>) kok) {
                Object bulunan = degeriBul(deger, anahtar);
                if (bulunan != null) {
                    return bulunan;
                }
            }
        }

        return null;
    }

    private void metinleriTopla(Object kok, Set<String> metinler) {
        if (kok instanceof Map) {
            Map<?, ?> harita = (Map<?, ?>) kok;

            for (Map.Entry<?, ?> entry : harita.entrySet()) {
                String anahtar = String.valueOf(entry.getKey()).toLowerCase();
                Object deger = entry.getValue();

                if (deger instanceof String && yorumAnahtariMi(anahtar)) {
                    metinler.add(sadeMetin((String) deger));
                }

                metinleriTopla(deger, metinler);
            }
            return;
        }

        if (kok instanceof List) {
            for (Object deger : (List<?>) kok) {
                metinleriTopla(deger, metinler);
            }
        }
    }

    private boolean yorumAnahtariMi(String anahtar) {
        return anahtar.contains("comment")
                || anahtar.contains("yorum")
                || anahtar.contains("text")
                || anahtar.contains("content")
                || anahtar.contains("description");
    }

    private String sadeMetin(String metin) {
        return metin == null ? "" : metin.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String httpGet(String adres) {
        HttpURLConnection baglanti = null;

        try {
            URL url = new URL(adres);
            baglanti = (HttpURLConnection) url.openConnection();
            baglanti.setRequestMethod("GET");
            baglanti.setConnectTimeout(10000);
            baglanti.setReadTimeout(20000);
            baglanti.setRequestProperty("Accept", "application/json");
            baglanti.setRequestProperty("Origin", "https://www.iddaa.com");
            baglanti.setRequestProperty("Referer", "https://www.iddaa.com/");
            baglanti.setRequestProperty("User-Agent", "Mozilla/5.0 FutbolAnalizAgent/0.1");

            int durumKodu = baglanti.getResponseCode();
            if (durumKodu < 200 || durumKodu >= 300) {
                throw new IllegalStateException("Yorum verisi alinamadi. HTTP durum kodu: " + durumKodu);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(baglanti.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sonuc = new StringBuilder();
            String satir;

            while ((satir = reader.readLine()) != null) {
                sonuc.append(satir);
            }

            reader.close();
            return sonuc.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Yorum verisine erisilemedi.", e);
        } finally {
            if (baglanti != null) {
                baglanti.disconnect();
            }
        }
    }
}
