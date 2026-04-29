package com.futbolanaliz.servisler;

import com.futbolanaliz.modeller.Mac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IddaaIstatistikServisi {
    private static final String ISTATISTIK_OZET_URL = "https://statisticsv2.iddaa.com/statistics/eventsummary/1/";
    private static final ZoneId TURKIYE_SAATI = ZoneId.of("Europe/Istanbul");

    private final JsonServisi jsonServisi = new JsonServisi();
    private final Map<Long, String> cache = new HashMap<Long, String>();

    public String istatistikMetniGetir(Mac mac) {
        if (mac == null || mac.getIddaaEventId() <= 0L) {
            return "";
        }
        if (cache.containsKey(mac.getIddaaEventId())) {
            return cache.get(mac.getIddaaEventId());
        }

        String metin;
        try {
            Object kok = jsonServisi.jsonOku(httpGet(ISTATISTIK_OZET_URL + mac.getIddaaEventId()));
            metin = istatistikMetniOlustur(harita(harita(kok).get("data")));
        } catch (RuntimeException e) {
            metin = "";
        }
        cache.put(mac.getIddaaEventId(), metin);
        return metin;
    }

    private String istatistikMetniOlustur(Map<?, ?> data) {
        if (data.isEmpty()) {
            return "";
        }

        StringBuilder metin = new StringBuilder();
        Map<?, ?> event = harita(data.get("eventInformationModel"));
        Map<?, ?> last = harita(data.get("lastEventModel"));
        Map<?, ?> recent = harita(data.get("recentEventModel"));
        Map<?, ?> refereePage = harita(data.get("soccerRefereePageModel"));

        macBilgisiEkle(metin, event);
        sonMaclarEkle(metin, "Ev sahibi son maclar", liste(last.get("homeOverAll")), takimId(event, "homeTeam"), 5);
        sonMaclarEkle(metin, "Deplasman son maclar", liste(last.get("awayOverAll")), takimId(event, "awayTeam"), 5);
        sonMaclarEkle(metin, "Iki takim arasindaki maclar", liste(recent.get("overall")), takimId(event, "homeTeam"), 5);
        hakemEkle(metin, refereePage);
        veriDurumuEkle(metin, data);

        return metin.toString().trim();
    }

    private void macBilgisiEkle(StringBuilder metin, Map<?, ?> event) {
        if (event.isEmpty()) {
            return;
        }
        Map<?, ?> home = harita(event.get("homeTeam"));
        Map<?, ?> away = harita(event.get("awayTeam"));
        Map<?, ?> tournament = harita(event.get("tournamentInformation"));
        Map<?, ?> round = harita(event.get("roundInformation"));
        Map<?, ?> place = harita(event.get("eventPlaceInformation"));
        ekle(metin, "Mac", metin(home.get("name")) + " - " + metin(away.get("name")));
        ekle(metin, "Organizasyon", metin(tournament.get("name")) + (metin(round.get("name")).isEmpty() ? "" : " / " + metin(round.get("name"))));
        ekle(metin, "Saha", metin(place.get("stadiumName")));
        String hava = metin(place.get("weatherStatus"));
        String sicaklik = metin(place.get("temperatureC"));
        if (!hava.isEmpty() || !sicaklik.isEmpty()) {
            ekle(metin, "Hava", hava + (sicaklik.isEmpty() ? "" : " " + sicaklik + "C"));
        }
    }

    private void sonMaclarEkle(StringBuilder metin, String baslik, List<?> maclar, long hedefTakimId, int limit) {
        metin.append(baslik).append(":\n");
        if (maclar.isEmpty()) {
            metin.append("- Veri yok\n");
            return;
        }

        int adet = Math.min(limit, maclar.size());
        int galibiyet = 0;
        int beraberlik = 0;
        int maglubiyet = 0;
        int attigi = 0;
        int yedigi = 0;
        StringBuilder detaylar = new StringBuilder();

        for (int i = 0; i < adet; i++) {
            Map<?, ?> mac = harita(maclar.get(i));
            Map<?, ?> home = harita(mac.get("homeTeam"));
            Map<?, ?> away = harita(mac.get("awayTeam"));
            int homeScore = skor(mac.get("homeTeamScore"));
            int awayScore = skor(mac.get("awayTeamScore"));
            boolean hedefEvde = hedefTakimId > 0 && hedefTakimId == uzun(home.get("id"));
            int hedefGol = hedefEvde ? homeScore : awayScore;
            int rakipGol = hedefEvde ? awayScore : homeScore;
            if (hedefGol > rakipGol) {
                galibiyet++;
            } else if (hedefGol == rakipGol) {
                beraberlik++;
            } else {
                maglubiyet++;
            }
            attigi += hedefGol;
            yedigi += rakipGol;
            detaylar.append("- ")
                    .append(tarih(mac.get("eventDate")))
                    .append(" | ")
                    .append(metin(home.get("name")))
                    .append(" ")
                    .append(homeScore)
                    .append("-")
                    .append(awayScore)
                    .append(" ")
                    .append(metin(away.get("name")));
            String lig = metin(harita(mac.get("tournamentInformation")).get("name"));
            if (!lig.isEmpty()) {
                detaylar.append(" | ").append(lig);
            }
            detaylar.append("\n");
        }

        metin.append("- Ozet: ")
                .append(adet)
                .append(" mac ")
                .append(galibiyet)
                .append("G/")
                .append(beraberlik)
                .append("B/")
                .append(maglubiyet)
                .append("M, gol ")
                .append(attigi)
                .append("-")
                .append(yedigi)
                .append("\n")
                .append(detaylar);
    }

    private void hakemEkle(StringBuilder metin, Map<?, ?> refereePage) {
        if (refereePage.isEmpty()) {
            return;
        }
        Map<?, ?> referee = harita(refereePage.get("referee"));
        String ad = metin(referee.get("name"));
        if (!ad.isEmpty()) {
            ekle(metin, "Hakem", ad);
        }
        List<?> refereeMatches = liste(refereePage.get("refereeMatches"));
        if (!refereeMatches.isEmpty()) {
            metin.append("Hakem mac/kart gecmisi:\n");
            sonMaclarEkle(metin, "Hakem maclari", refereeMatches, 0L, 5);
        }
    }

    private void veriDurumuEkle(StringBuilder metin, Map<?, ?> data) {
        metin.append("Iddaa veri durumu:\n");
        metin.append("- Son maclar: ").append(liste(harita(data.get("lastEventModel")).get("homeOverAll")).isEmpty()
                && liste(harita(data.get("lastEventModel")).get("awayOverAll")).isEmpty() ? "Yok" : "Var").append("\n");
        metin.append("- Iki takim arasindaki maclar: ").append(liste(harita(data.get("recentEventModel")).get("overall")).isEmpty() ? "Yok" : "Var").append("\n");
        metin.append("- Puan durumu/kadro/oyuncu/korner/kart detaylari: eventsummary cevabinda bu mac icin ayri alan olarak donmedi\n");
    }

    private long takimId(Map<?, ?> event, String alan) {
        return uzun(harita(event.get(alan)).get("id"));
    }

    private int skor(Object skorObjesi) {
        Map<?, ?> skor = harita(skorObjesi);
        int regular = sayi(skor.get("regular"));
        if (regular != 0 || skor.containsKey("regular")) {
            return regular;
        }
        return sayi(skor.get("current"));
    }

    private void ekle(StringBuilder metin, String baslik, String deger) {
        if (deger == null || deger.trim().isEmpty()) {
            return;
        }
        metin.append(baslik).append(": ").append(deger.trim()).append("\n");
    }

    private String tarih(Object epochMillis) {
        try {
            long millis;
            if (epochMillis instanceof Number) {
                millis = ((Number) epochMillis).longValue();
            } else {
                String metin = String.valueOf(epochMillis).trim();
                millis = metin.contains(".") || metin.toUpperCase().contains("E")
                        ? (long) Double.parseDouble(metin)
                        : Long.parseLong(metin);
            }
            return Instant.ofEpochMilli(millis).atZone(TURKIYE_SAATI).toLocalDate().toString();
        } catch (RuntimeException e) {
            return "";
        }
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
                throw new IllegalStateException("iddaa istatistik verisi alinamadi. HTTP " + durumKodu);
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
            throw new IllegalStateException("iddaa istatistik verisine erisilemedi.", e);
        } finally {
            if (baglanti != null) {
                baglanti.disconnect();
            }
        }
    }

    private Map<?, ?> harita(Object deger) {
        return deger instanceof Map ? (Map<?, ?>) deger : new HashMap<Object, Object>();
    }

    private List<?> liste(Object deger) {
        return deger instanceof List ? (List<?>) deger : java.util.Collections.emptyList();
    }

    private String metin(Object deger) {
        return deger == null ? "" : String.valueOf(deger).trim();
    }

    private int sayi(Object deger) {
        if (deger instanceof Number) {
            return ((Number) deger).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(deger));
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private long uzun(Object deger) {
        if (deger instanceof Number) {
            return ((Number) deger).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(deger));
        } catch (RuntimeException e) {
            return 0L;
        }
    }
}
