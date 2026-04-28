package com.futbolanaliz.servisler;

import com.futbolanaliz.modeller.BahisTuru;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.Oran;
import com.futbolanaliz.modeller.TopLig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class IddaaVeriServisi {
    private static final String IDDAA_FUTBOL_PROGRAM_URL = "https://sportsbookv2.iddaa.com/sportsbook/events?st=1&type=0&version=0";
    private static final String IDDAA_CANLI_FUTBOL_URL = "https://sportsbookv2.iddaa.com/sportsbook/events?st=1&type=1&version=0";
    private static final String IDDAA_LIGLER_URL = "https://sportsbookv2.iddaa.com/sportsbook/competitions?st=1";
    private static final String ISTATISTIK_OZET_URL = "https://statisticsv2.iddaa.com/statistics/eventsummary/1/";
    private static final ZoneId TURKIYE_SAATI = ZoneId.of("Europe/Istanbul");
    private static final Locale TURKCE = new Locale("tr", "TR");
    private static final TopLigHedef[] ONCELIKLI_ORGANIZASYON_HEDEFLERI = new TopLigHedef[]{
            new TopLigHedef(1, "GB", "İngiltere Premier Lig", "Top 10 Lig"),
            new TopLigHedef(2, "ES", "İspanya La Liga", "Top 10 Lig"),
            new TopLigHedef(3, "IT", "İtalya Serie A", "Top 10 Lig"),
            new TopLigHedef(4, "DE", "Almanya Bundesliga", "Top 10 Lig"),
            new TopLigHedef(5, "FR", "Fransa Ligue 1", "Top 10 Lig"),
            new TopLigHedef(6, "TR", "Türkiye Süper Lig", "Top 10 Lig"),
            new TopLigHedef(7, "NL", "Hollanda Eredivisie", "Top 10 Lig"),
            new TopLigHedef(8, "PT", "Portekiz Premier Lig", "Top 10 Lig"),
            new TopLigHedef(9, "BE", "Belçika Pro Lig", "Top 10 Lig"),
            new TopLigHedef(10, "US", "ABD Major Lig", "Top 10 Lig"),
            new TopLigHedef(11, "INT", "UEFA Şampiyonlar Ligi", "Avrupa Kupası"),
            new TopLigHedef(12, "INT", "UEFA Avrupa Ligi", "Avrupa Kupası"),
            new TopLigHedef(13, "INT", "UEFA Avrupa Konferans Ligi", "Avrupa Kupası"),
            new TopLigHedef(14, "INT", "UEFA Konferans Ligi", "Avrupa Kupası"),
            new TopLigHedef(15, "GB", "İngiltere FA Cup", "Ülke Kupası"),
            new TopLigHedef(16, "ES", "İspanya Kral Kupası", "Ülke Kupası"),
            new TopLigHedef(17, "IT", "İtalya Kupası", "Ülke Kupası"),
            new TopLigHedef(18, "DE", "Almanya Kupası", "Ülke Kupası"),
            new TopLigHedef(19, "FR", "Fransa Kupası", "Ülke Kupası"),
            new TopLigHedef(20, "TR", "Türkiye Kupası", "Ülke Kupası"),
            new TopLigHedef(21, "NL", "Hollanda Kupası", "Ülke Kupası"),
            new TopLigHedef(22, "PT", "Portekiz Kupası", "Ülke Kupası"),
            new TopLigHedef(23, "BE", "Belçika Kupası", "Ülke Kupası"),
            new TopLigHedef(24, "US", "ABD Açık Kupası", "Ülke Kupası")
    };

    private final JsonServisi jsonServisi = new JsonServisi();

    public List<Mac> bugununFutbolMaclariniGetir(List<TopLig> izinliLigler) {
        String macJson = httpGet(IDDAA_FUTBOL_PROGRAM_URL);
        hamVeriyiKaydet(macJson);

        Map<Integer, String> ligAdlari = ligAdlariniGetir();
        Set<Integer> izinliLigIdleri = ligIdSeti(izinliLigler);
        if (izinliLigIdleri.isEmpty()) {
            return new ArrayList<Mac>();
        }

        Object kok = jsonServisi.jsonOku(macJson);
        Map<?, ?> kokHaritasi = harita(kok);
        Map<?, ?> data = harita(kokHaritasi.get("data"));
        List<?> events = liste(data.get("events"));

        LocalDate bugun = LocalDate.now(TURKIYE_SAATI);
        List<Mac> maclar = new ArrayList<Mac>();

        for (Object eventObject : events) {
            Map<?, ?> event = harita(eventObject);
            LocalDateTime baslamaZamani = baslamaZamani(event.get("d"));

            if (!bugun.equals(baslamaZamani.toLocalDate())) {
                continue;
            }

            int ligId = sayi(event.get("ci"));
            if (!izinliLigIdleri.contains(ligId)) {
                continue;
            }

            List<Oran> oranlar = oranlariOlustur(liste(event.get("m")));
            CanliMacBilgisi macBilgisi = CanliMacBilgisi.planlandi();
            if (oranlar.isEmpty()) {
                macBilgisi = canliMacBilgisiGetir(uzun(event.get("i")));
            }
            if (oranlar.isEmpty() && !macBilgisi.oynanmisVeyaDevamEdiyorMu()) {
                continue;
            }

            String ligAdi = ligAdlari.containsKey(ligId) ? ligAdlari.get(ligId) : "Lig #" + ligId;

            maclar.add(new Mac(
                    uzun(event.get("i")),
                    uzun(event.get("bri")),
                    ligAdi,
                    baslamaZamani.toLocalDate(),
                    baslamaZamani.toLocalTime(),
                    metin(event.get("hn")),
                    metin(event.get("an")),
                    oranlar,
                    macBilgisi.durum,
                    macBilgisi.evSahibiSkor,
                    macBilgisi.deplasmanSkor,
                    macBilgisi.dakika,
                    macOncesiTahmini(oranlar)
            ));
        }

        Collections.sort(maclar, new Comparator<Mac>() {
            @Override
            public int compare(Mac ilk, Mac ikinci) {
                return ilk.getSaat().compareTo(ikinci.getSaat());
            }
        });

        return maclar;
    }

    public List<Mac> devamEdenFutbolMaclariniGetir(List<TopLig> izinliLigler) {
        String macJson = httpGet(IDDAA_CANLI_FUTBOL_URL);
        Map<Integer, String> ligAdlari = ligAdlariniGetir();
        Map<Long, String> macOncesiTahminleri = macOncesiTahminHaritasi();
        Set<Integer> izinliLigIdleri = ligIdSeti(izinliLigler);
        if (izinliLigIdleri.isEmpty()) {
            return new ArrayList<Mac>();
        }

        Object kok = jsonServisi.jsonOku(macJson);
        Map<?, ?> kokHaritasi = harita(kok);
        Map<?, ?> data = harita(kokHaritasi.get("data"));
        List<?> events = liste(data.get("events"));
        List<Mac> maclar = new ArrayList<Mac>();

        for (Object eventObject : events) {
            Map<?, ?> event = harita(eventObject);
            int ligId = sayi(event.get("ci"));
            if (!izinliLigIdleri.contains(ligId)) {
                continue;
            }

            long eventId = uzun(event.get("mpi"));
            if (eventId <= 0L) {
                eventId = uzun(event.get("i"));
            }

            CanliMacBilgisi canliBilgi = canliMacBilgisiGetir(eventId);
            if (canliBahisEventiMi(event)) {
                canliBilgi = canliBahisBilgisi(event, canliBilgi);
            }
            if (!canliBilgi.oynanmisVeyaDevamEdiyorMu()) {
                continue;
            }

            List<Oran> oranlar = oranlariOlustur(liste(event.get("m")));
            String macOncesiTahmin = macOncesiTahmini(oranlar);
            if ((oranlar.isEmpty() || macOncesiTahmin.contains("1X2")) && macOncesiTahminleri.containsKey(eventId)) {
                macOncesiTahmin = macOncesiTahminleri.get(eventId);
            }
            String ligAdi = ligAdlari.containsKey(ligId) ? ligAdlari.get(ligId) : "Lig #" + ligId;
            LocalDateTime baslamaZamani = baslamaZamani(event.get("d"));

            maclar.add(new Mac(
                    eventId,
                    uzun(event.get("bri")),
                    ligAdi,
                    baslamaZamani.toLocalDate(),
                    baslamaZamani.toLocalTime(),
                    metin(event.get("hn")),
                    metin(event.get("an")),
                    oranlar,
                    canliBilgi.durum,
                    canliBilgi.evSahibiSkor,
                    canliBilgi.deplasmanSkor,
                    canliBilgi.dakika,
                    macOncesiTahmin
            ));
        }

        Collections.sort(maclar, new Comparator<Mac>() {
            @Override
            public int compare(Mac ilk, Mac ikinci) {
                return ilk.getSaat().compareTo(ikinci.getSaat());
            }
        });

        return maclar;
    }

    private Map<Long, String> macOncesiTahminHaritasi() {
        Map<Long, String> tahminler = new HashMap<Long, String>();
        try {
            String macJson = httpGet(IDDAA_FUTBOL_PROGRAM_URL);
            Object kok = jsonServisi.jsonOku(macJson);
            Map<?, ?> kokHaritasi = harita(kok);
            Map<?, ?> data = harita(kokHaritasi.get("data"));

            for (Object eventObject : liste(data.get("events"))) {
                Map<?, ?> event = harita(eventObject);
                tahminler.put(uzun(event.get("i")), macOncesiTahmini(oranlariOlustur(liste(event.get("m")))));
            }
        } catch (RuntimeException e) {
            return tahminler;
        }
        return tahminler;
    }

    public List<TopLig> oncelikliOrganizasyonlariKontrolEt() {
        String ligJson = httpGet(IDDAA_LIGLER_URL);
        Object kok = jsonServisi.jsonOku(ligJson);
        Map<?, ?> kokHaritasi = harita(kok);
        List<?> ligler = liste(kokHaritasi.get("data"));
        List<TopLig> bulunanLigler = new ArrayList<TopLig>();

        for (TopLigHedef hedef : ONCELIKLI_ORGANIZASYON_HEDEFLERI) {
            TopLig bulunan = organizasyonHedefiniBul(hedef, ligler);

            if (bulunan != null) {
                bulunanLigler.add(bulunan);
            }
        }

        return bulunanLigler;
    }

    private TopLig organizasyonHedefiniBul(TopLigHedef hedef, List<?> ligler) {
        String hedefAd = normalize(hedef.ligAdi);

        for (Object ligObject : ligler) {
            Map<?, ?> lig = harita(ligObject);
            String ulkeKodu = metin(lig.get("cid"));
            String ligAdi = metin(lig.get("n"));
            String normalizeLigAdi = normalize(ligAdi);

            if (!hedef.ulkeKodu.equalsIgnoreCase(ulkeKodu)) {
                continue;
            }

            if (organizasyonAdiEslesiyor(normalizeLigAdi, hedefAd)) {
                return new TopLig(sayi(lig.get("i")), ulkeKodu, ligAdi, hedef.oncelikSirasi, hedef.kategori);
            }
        }

        return null;
    }

    private boolean organizasyonAdiEslesiyor(String ligAdi, String hedefAd) {
        if (ligAdi.equals(hedefAd) || ligAdi.startsWith(hedefAd + ",")) {
            return true;
        }

        if (hedefAd.contains("uefa avrupa konferans ligi") && ligAdi.equals("uefa konferans ligi")) {
            return true;
        }

        return hedefAd.contains("kupası") && ligAdi.contains(hedefAd.replace(" kupası", "")) && ligAdi.contains("kupası");
    }

    private Map<Integer, String> ligAdlariniGetir() {
        Map<Integer, String> ligAdlari = new HashMap<Integer, String>();
        String ligJson = httpGet(IDDAA_LIGLER_URL);
        Object kok = jsonServisi.jsonOku(ligJson);
        Map<?, ?> kokHaritasi = harita(kok);

        for (Object ligObject : liste(kokHaritasi.get("data"))) {
            Map<?, ?> lig = harita(ligObject);
            ligAdlari.put(sayi(lig.get("i")), metin(lig.get("n")));
        }

        return ligAdlari;
    }

    private Set<Integer> ligIdSeti(List<TopLig> ligler) {
        Set<Integer> ligIdleri = new HashSet<Integer>();

        if (ligler == null) {
            return ligIdleri;
        }

        for (TopLig lig : ligler) {
            ligIdleri.add(lig.getIddaaLigId());
        }

        return ligIdleri;
    }

    private List<Oran> oranlariOlustur(List<?> marketler) {
        List<Oran> oranlar = new ArrayList<Oran>();

        for (Object marketObject : marketler) {
            Map<?, ?> market = harita(marketObject);
            int marketKodu = sayi(market.get("st"));
            String marketAdi = metin(market.get("n"));
            if (marketAdi.isEmpty()) {
                marketAdi = metin(market.get("mn"));
            }
            if (marketAdi.isEmpty()) {
                marketAdi = marketAdi(marketKodu);
            }
            String oyunDegeri = metin(market.get("sov"));

            for (Object secenekObject : liste(market.get("o"))) {
                Map<?, ?> secenek = harita(secenekObject);
                String secenekAdi = metin(secenek.get("n"));
                BigDecimal oranDegeri = decimal(secenek.get("odd"));
                BahisTuru bahisTuru = bahisTurunuBul(marketKodu, oyunDegeri, secenekAdi);

                if (oranDegeri.compareTo(BigDecimal.ZERO) > 0) {
                    oranlar.add(new Oran(bahisTuru, oranDegeri, marketKodu, marketAdi, oyunDegeri, secenekAdi));
                }
            }
        }

        return oranlar;
    }

    private BahisTuru bahisTurunuBul(int marketKodu, String oyunDegeri, String secenekAdi) {
        if (marketKodu == 1 || marketKodu == 4) {
            if ("1".equals(secenekAdi)) {
                return BahisTuru.MAC_SONUCU_1;
            }
            if ("0".equals(secenekAdi) || "X".equalsIgnoreCase(secenekAdi)) {
                return BahisTuru.MAC_SONUCU_X;
            }
            if ("2".equals(secenekAdi)) {
                return BahisTuru.MAC_SONUCU_2;
            }
        }

        if (marketKodu == 60 && "2.5".equals(oyunDegeri)) {
            if ("Alt".equalsIgnoreCase(secenekAdi)) {
                return BahisTuru.ALT_2_5;
            }
            if ("Üst".equalsIgnoreCase(secenekAdi) || "Ust".equalsIgnoreCase(secenekAdi)) {
                return BahisTuru.UST_2_5;
            }
        }

        if (marketKodu == 89) {
            if ("Var".equalsIgnoreCase(secenekAdi)) {
                return BahisTuru.KARSILIKLI_GOL_VAR;
            }
            if ("Yok".equalsIgnoreCase(secenekAdi)) {
                return BahisTuru.KARSILIKLI_GOL_YOK;
            }
        }

        return null;
    }

    private String marketAdi(int marketKodu) {
        if (marketKodu == 1 || marketKodu == 4) {
            return "Mac Sonucu";
        }
        if (marketKodu == 60 || marketKodu == 101 || marketKodu == 14) {
            return "Alt/Ust";
        }
        if (marketKodu == 89 || marketKodu == 131) {
            return "Karsilikli Gol";
        }
        if (marketKodu == 92 || marketKodu == 77) {
            return "Cifte Sans";
        }
        if (marketKodu == 90) {
            return "Ilk Yari/Mac Sonucu";
        }
        if (marketKodu == 36 || marketKodu == 88) {
            return "Ilk Yari Sonucu";
        }
        if (marketKodu == 6) {
            return "En Cok Gol Olacak Yari";
        }
        if (marketKodu == 7) {
            return "Sonuc ve Alt/Ust";
        }
        if (marketKodu == 100 || marketKodu == 23) {
            return "Handikapli Mac Sonucu";
        }
        if (marketKodu == 603) {
            return "Ev Sahibi Gol Alt/Ust";
        }
        if (marketKodu == 604) {
            return "Deplasman Gol Alt/Ust";
        }
        if (marketKodu == 698 || marketKodu == 699 || marketKodu == 700) {
            return "Sonuc ve KG";
        }
        if (marketKodu == 720) {
            return "Kirmizi Kart Olur Mu";
        }
        if (marketKodu == 722) {
            return "Ev Sahibi Ilk Yari Gol";
        }
        if (marketKodu == 723) {
            return "Deplasman Ilk Yari Gol";
        }
        return "Market #" + marketKodu;
    }

    private String macOncesiTahmini(List<Oran> oranlar) {
        Oran ev = null;
        Oran beraberlik = null;
        Oran dep = null;

        for (Oran oran : oranlar) {
            if (oran.getBahisTuru() == BahisTuru.MAC_SONUCU_1) {
                ev = oran;
            } else if (oran.getBahisTuru() == BahisTuru.MAC_SONUCU_X) {
                beraberlik = oran;
            } else if (oran.getBahisTuru() == BahisTuru.MAC_SONUCU_2) {
                dep = oran;
            }
        }

        if (ev == null || beraberlik == null || dep == null) {
            return "MaÃ§ Ã¶ncesi tahmin iÃ§in 1X2 oranÄ± yok";
        }

        Oran secim = ev;
        String tahmin = "Ev sahibi kazanÄ±r";
        if (beraberlik.getDeger().compareTo(secim.getDeger()) < 0) {
            secim = beraberlik;
            tahmin = "Beraberlik";
        }
        if (dep.getDeger().compareTo(secim.getDeger()) < 0) {
            secim = dep;
            tahmin = "Deplasman kazanÄ±r";
        }

        return tahmin + " | oran favorisi: " + secim.formatliDeger();
    }

    private CanliMacBilgisi canliMacBilgisiGetir(long eventId) {
        if (eventId <= 0L) {
            return CanliMacBilgisi.planlandi();
        }

        try {
            String json = httpGet(ISTATISTIK_OZET_URL + eventId);
            Object kok = jsonServisi.jsonOku(json);
            Map<?, ?> kokHaritasi = harita(kok);
            Map<?, ?> data = harita(kokHaritasi.get("data"));
            Map<?, ?> eventInformation = harita(data.get("eventInformationModel"));
            Map<?, ?> statusInformation = harita(eventInformation.get("statusInformation"));
            int durumId = sayi(statusInformation.get("id"));
            String durum = durumMetni(durumId, metin(statusInformation.get("name")));
            Map<?, ?> skorBilgisi = istegeBagliHarita(eventInformation.get("score"));
            Integer evSkor = skorBilgisi == null ? null : takimSkoruBul(skorBilgisi, "ht");
            Integer depSkor = skorBilgisi == null ? null : takimSkoruBul(skorBilgisi, "at");
            if (evSkor == null) {
                evSkor = skorBul(kok, true);
            }
            if (depSkor == null) {
                depSkor = skorBul(kok, false);
            }
            String dakika = skorBilgisi == null ? "" : skorDakikasi(skorBilgisi);
            if (dakika.isEmpty()) {
                dakika = dakikaBul(kok);
            }
            return new CanliMacBilgisi(durumId, durum, evSkor, depSkor, dakika);
        } catch (RuntimeException e) {
            return CanliMacBilgisi.planlandi();
        }
    }

    private boolean canliBahisEventiMi(Map<?, ?> event) {
        return sayiVarsa(event.get("s")) > 0 || sayiVarsa(event.get("bp")) > 0 || Boolean.TRUE.equals(event.get("hr"));
    }

    private Integer takimSkoruBul(Map<?, ?> skorBilgisi, String takimAnahtari) {
        Map<?, ?> takimSkoru = istegeBagliHarita(skorBilgisi.get(takimAnahtari));
        if (takimSkoru == null) {
            return null;
        }

        Integer normalSureSkoru = sayisalDegerBul(takimSkoru, "r");
        if (normalSureSkoru != null) {
            return normalSureSkoru;
        }

        return sayisalDegerBul(takimSkoru, "c");
    }

    private String skorDakikasi(Map<?, ?> skorBilgisi) {
        Integer dakika = sayisalDegerBul(skorBilgisi, "min");
        if (dakika == null) {
            return "";
        }

        return dakika + "'";
    }

    private CanliMacBilgisi canliBahisBilgisi(Map<?, ?> event, CanliMacBilgisi mevcutBilgi) {
        int bahisPeriyodu = sayiVarsa(event.get("bp"));
        String durum = bahisPeriyoduMetni(bahisPeriyodu);
        if (durum.isEmpty()) {
            durum = mevcutBilgi != null && mevcutBilgi.durum != null && !"Planlandi".equals(mevcutBilgi.durum)
                    ? mevcutBilgi.durum
                    : "Canli";
        }

        return new CanliMacBilgisi(
                31,
                durum,
                mevcutBilgi == null ? null : mevcutBilgi.evSahibiSkor,
                mevcutBilgi == null ? null : mevcutBilgi.deplasmanSkor,
                mevcutBilgi == null ? "" : mevcutBilgi.dakika
        );
    }

    private String bahisPeriyoduMetni(int bahisPeriyodu) {
        if (bahisPeriyodu == 1) {
            return "1. yari";
        }
        if (bahisPeriyodu == 2) {
            return "2. yari";
        }
        if (bahisPeriyodu == 3) {
            return "Devre arasi";
        }
        return "";
    }

    private String durumMetni(int durumId, String kaynakDurum) {
        if (durumId == 2) {
            return "1. yari";
        }
        if (durumId == 3) {
            return "2. yari";
        }
        if (durumId == 4) {
            return "Devre arasi";
        }
        if (durumId == 31) {
            return "Canli";
        }
        if (durumId == 5) {
            return "Mac bitti";
        }
        if (durumId == 9) {
            return "Uzatmalarda bitti";
        }
        if (durumId == 11) {
            return "Penaltilarda bitti";
        }
        if (durumId == 6 || durumId == 8 || durumId == 10 || durumId == 32 || durumId == 33) {
            return kaynakDurum == null || kaynakDurum.isEmpty() ? "Uzatma/penalti" : kaynakDurum;
        }
        return kaynakDurum == null || kaynakDurum.isEmpty() ? "Planlandi" : kaynakDurum;
    }

    private Integer skorBul(Object kok, boolean evSahibi) {
        String[] anahtarlar = evSahibi
                ? new String[]{"homeScore", "scoreHome", "homeTeamScore", "homeCurrentScore", "homeGoals"}
                : new String[]{"awayScore", "scoreAway", "awayTeamScore", "awayCurrentScore", "awayGoals"};

        for (String anahtar : anahtarlar) {
            Integer bulunan = sayisalDegerBul(kok, anahtar);
            if (bulunan != null) {
                return bulunan;
            }
        }

        return null;
    }

    private Integer sayisalDegerBul(Object kok, String anahtar) {
        if (kok instanceof Map) {
            Map<?, ?> harita = (Map<?, ?>) kok;
            for (Map.Entry<?, ?> entry : harita.entrySet()) {
                if (anahtar.equalsIgnoreCase(String.valueOf(entry.getKey())) && entry.getValue() instanceof Number) {
                    return ((Number) entry.getValue()).intValue();
                }
                Integer bulunan = sayisalDegerBul(entry.getValue(), anahtar);
                if (bulunan != null) {
                    return bulunan;
                }
            }
        }

        if (kok instanceof List) {
            for (Object deger : (List<?>) kok) {
                Integer bulunan = sayisalDegerBul(deger, anahtar);
                if (bulunan != null) {
                    return bulunan;
                }
            }
        }

        return null;
    }

    private String dakikaBul(Object kok) {
        String[] anahtarlar = new String[]{"minute", "matchMinute", "currentMinute", "periodMinute", "time"};
        for (String anahtar : anahtarlar) {
            String bulunan = metinselDegerBul(kok, anahtar);
            if (!bulunan.isEmpty()) {
                return bulunan;
            }
        }
        return "";
    }

    private String metinselDegerBul(Object kok, String anahtar) {
        if (kok instanceof Map) {
            Map<?, ?> harita = (Map<?, ?>) kok;
            for (Map.Entry<?, ?> entry : harita.entrySet()) {
                if (anahtar.equalsIgnoreCase(String.valueOf(entry.getKey()))) {
                    return metin(entry.getValue());
                }
                String bulunan = metinselDegerBul(entry.getValue(), anahtar);
                if (!bulunan.isEmpty()) {
                    return bulunan;
                }
            }
        }

        if (kok instanceof List) {
            for (Object deger : (List<?>) kok) {
                String bulunan = metinselDegerBul(deger, anahtar);
                if (!bulunan.isEmpty()) {
                    return bulunan;
                }
            }
        }

        return "";
    }

    private String httpGet(String adres) {
        HttpURLConnection baglanti = null;

        try {
            URL url = new URL(adres);
            baglanti = (HttpURLConnection) url.openConnection();
            baglanti.setRequestMethod("GET");
            baglanti.setConnectTimeout(15000);
            baglanti.setReadTimeout(30000);
            baglanti.setRequestProperty("Accept", "application/json");
            baglanti.setRequestProperty("Origin", "https://www.iddaa.com");
            baglanti.setRequestProperty("Referer", "https://www.iddaa.com/program/futbol");
            baglanti.setRequestProperty("User-Agent", "Mozilla/5.0 FutbolAnalizAgent/0.1");

            int durumKodu = baglanti.getResponseCode();
            if (durumKodu < 200 || durumKodu >= 300) {
                throw new IllegalStateException("iddaa.com veri isteği başarısız oldu. HTTP durum kodu: " + durumKodu);
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
            throw new IllegalStateException("iddaa.com verisine erişilemedi.", e);
        } finally {
            if (baglanti != null) {
                baglanti.disconnect();
            }
        }
    }

    private void hamVeriyiKaydet(String json) {
        File hedef = new File("kaynaklar/iddaa-verileri/bugun-futbol-raw.json");
        File klasor = hedef.getParentFile();

        if (klasor != null && !klasor.exists()) {
            klasor.mkdirs();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hedef), StandardCharsets.UTF_8));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("iddaa ham verisi kaydedilemedi.", e);
        }
    }

    private LocalDateTime baslamaZamani(Object epochSaniye) {
        long saniye = ((Number) epochSaniye).longValue();
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(saniye), TURKIYE_SAATI);
    }

    private Map<?, ?> harita(Object deger) {
        if (!(deger instanceof Map)) {
            throw new IllegalArgumentException("Beklenen JSON nesnesi alınamadı.");
        }

        return (Map<?, ?>) deger;
    }

    private Map<?, ?> istegeBagliHarita(Object deger) {
        if (deger instanceof Map) {
            return (Map<?, ?>) deger;
        }

        return null;
    }

    private List<?> liste(Object deger) {
        if (deger == null) {
            return new ArrayList<Object>();
        }

        if (!(deger instanceof List)) {
            throw new IllegalArgumentException("Beklenen JSON listesi alınamadı.");
        }

        return (List<?>) deger;
    }

    private int sayi(Object deger) {
        if (deger instanceof Number) {
            return ((Number) deger).intValue();
        }

        return Integer.parseInt(String.valueOf(deger));
    }

    private int sayiVarsa(Object deger) {
        if (deger == null) {
            return 0;
        }

        if (deger instanceof Number) {
            return ((Number) deger).intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(deger));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long uzun(Object deger) {
        if (deger == null) {
            return 0L;
        }

        if (deger instanceof Number) {
            return ((Number) deger).longValue();
        }

        return Long.parseLong(String.valueOf(deger));
    }

    private String metin(Object deger) {
        return deger == null ? "" : String.valueOf(deger);
    }

    private BigDecimal decimal(Object deger) {
        if (deger == null) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(String.valueOf(deger));
    }

    private String normalize(String deger) {
        return deger == null ? "" : deger.trim().toLowerCase(TURKCE);
    }

    private static class TopLigHedef {
        private final int oncelikSirasi;
        private final String ulkeKodu;
        private final String ligAdi;
        private final String kategori;

        private TopLigHedef(int oncelikSirasi, String ulkeKodu, String ligAdi, String kategori) {
            this.oncelikSirasi = oncelikSirasi;
            this.ulkeKodu = ulkeKodu;
            this.ligAdi = ligAdi;
            this.kategori = kategori;
        }
    }

    private static class CanliMacBilgisi {
        private final int durumId;
        private final String durum;
        private final Integer evSahibiSkor;
        private final Integer deplasmanSkor;
        private final String dakika;

        private CanliMacBilgisi(int durumId, String durum, Integer evSahibiSkor, Integer deplasmanSkor, String dakika) {
            this.durumId = durumId;
            this.durum = durum;
            this.evSahibiSkor = evSahibiSkor;
            this.deplasmanSkor = deplasmanSkor;
            this.dakika = dakika;
        }

        private static CanliMacBilgisi planlandi() {
            return new CanliMacBilgisi(1, "Planlandi", null, null, "");
        }

        private boolean oynanmisVeyaDevamEdiyorMu() {
            return durumId == 2
                    || durumId == 3
                    || durumId == 4
                    || durumId == 5
                    || durumId == 6
                    || durumId == 8
                    || durumId == 9
                    || durumId == 10
                    || durumId == 11
                    || durumId == 31
                    || durumId == 32
                    || durumId == 33;
        }
    }
}
