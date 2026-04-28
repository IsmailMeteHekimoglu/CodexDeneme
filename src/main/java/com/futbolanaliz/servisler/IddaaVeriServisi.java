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
    private static final String IDDAA_LIGLER_URL = "https://sportsbookv2.iddaa.com/sportsbook/competitions?st=1";
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
            if (oranlar.isEmpty()) {
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
                    oranlar
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
            String oyunDegeri = metin(market.get("sov"));

            for (Object secenekObject : liste(market.get("o"))) {
                Map<?, ?> secenek = harita(secenekObject);
                String secenekAdi = metin(secenek.get("n"));
                BigDecimal oranDegeri = decimal(secenek.get("odd"));
                BahisTuru bahisTuru = bahisTurunuBul(marketKodu, oyunDegeri, secenekAdi);

                if (bahisTuru != null && oranDegeri.compareTo(BigDecimal.ZERO) > 0) {
                    oranlar.add(new Oran(bahisTuru, oranDegeri));
                }
            }
        }

        return oranlar;
    }

    private BahisTuru bahisTurunuBul(int marketKodu, String oyunDegeri, String secenekAdi) {
        if (marketKodu == 1) {
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
}
