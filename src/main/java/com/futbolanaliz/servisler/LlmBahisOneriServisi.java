package com.futbolanaliz.servisler;

import com.futbolanaliz.modeller.BahisOnerisi;
import com.futbolanaliz.modeller.BahisTuru;
import com.futbolanaliz.modeller.OranRiskAnalizi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LlmBahisOneriServisi {
    private static final String OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final String ONERI_PROMPT_DOSYASI = "promptlar/bahis-oneri.prompt.md";
    private static final int EN_FAZLA_ADAY = 25;

    private final JsonServisi jsonServisi = new JsonServisi();
    private final AyarServisi ayarServisi = new AyarServisi();
    private final LlmCacheServisi cacheServisi = new LlmCacheServisi();

    public boolean kullanilabilirMi() {
        AyarServisi.LlmAyarlari ayarlar = ayarServisi.ayarlariOku();
        return ayarlar.isLlmAktif() && ayarlar.apiKeyVarMi();
    }

    public BahisOnerisi oneriUret(List<OranRiskAnalizi> riskAnalizleri) {
        List<BahisOnerisi> oneriler = onerilerUret(riskAnalizleri);
        return oneriler.isEmpty() ? null : oneriler.get(0);
    }

    public List<BahisOnerisi> onerilerUret(List<OranRiskAnalizi> riskAnalizleri) {
        List<BahisOnerisi> oneriler = new ArrayList<BahisOnerisi>();
        if (!kullanilabilirMi() || riskAnalizleri == null || riskAnalizleri.isEmpty()) {
            return oneriler;
        }

        String sistemMesaji = sistemMesajiOlustur();
        String kullaniciMesaji = kullaniciMesajiOlustur(riskAnalizleri);
        AyarServisi.LlmAyarlari ayarlar = ayarServisi.ayarlariOku();
        String cacheAnahtari = ayarlar.getModel() + "\n" + sistemMesaji + "\n" + kullaniciMesaji;
        String ciktiMetni = ayarlar.isCacheAktif() ? cacheServisi.oku("oneri", cacheAnahtari) : null;
        boolean cacheKullanildi = ciktiMetni != null && !ciktiMetni.trim().isEmpty();
        if (!cacheKullanildi) {
            String cevap = httpPost(istekGovdesiOlustur(sistemMesaji, kullaniciMesaji, ayarlar.getModel()), ayarlar.getApiKey());
            ciktiMetni = ciktiMetniniBul(cevap);
            if (ayarlar.isCacheAktif()) {
                cacheServisi.yaz("oneri", cacheAnahtari, ciktiMetni);
            }
        }
        if (ciktiMetni == null || ciktiMetni.trim().isEmpty()) {
            return oneriler;
        }

        Map<?, ?> sonuc = jsonHaritasi(ciktiMetni);
        boolean oneriVar = !"false".equalsIgnoreCase(metin(sonuc.get("onerilebilir")));
        if (!oneriVar) {
            return oneriler;
        }

        Object oneriListesi = sonuc.get("oneriler");
        if (oneriListesi instanceof List) {
            for (Object oneriObjesi : (List<?>) oneriListesi) {
                Map<?, ?> oneriHaritasi = oneriObjesi instanceof Map ? (Map<?, ?>) oneriObjesi : null;
                BahisOnerisi oneri = oneriHaritasi == null ? null : oneriOlustur(riskAnalizleri, oneriHaritasi, cacheKullanildi);
                if (oneri != null) {
                    oneriler.add(oneri);
                }
                if (oneriler.size() == 3) {
                    break;
                }
            }
        }

        if (oneriler.isEmpty()) {
            BahisOnerisi tekOneri = oneriOlustur(riskAnalizleri, sonuc, cacheKullanildi);
            if (tekOneri != null) {
                oneriler.add(tekOneri);
            }
        }

        return oneriler;
    }

    private BahisOnerisi oneriOlustur(List<OranRiskAnalizi> riskAnalizleri, Map<?, ?> sonuc, boolean cacheKullanildi) {
        OranRiskAnalizi eslesenAnaliz = analizBul(riskAnalizleri, sonuc);
        if (eslesenAnaliz == null) {
            eslesenAnaliz = enYakinRiskProfilindekiAnaliz(riskAnalizleri, metin(sonuc.get("riskSeviyesi")));
        }
        if (eslesenAnaliz == null) {
            eslesenAnaliz = enDusukRiskliAnaliz(riskAnalizleri);
        }
        if (eslesenAnaliz == null) {
            return null;
        }
        String gerekce = metin(sonuc.get("gerekce"));
        if (gerekce.isEmpty()) {
            gerekce = eslesenAnaliz.getGerekce();
        }
        String riskSeviyesi = metin(sonuc.get("riskSeviyesi"));
        if (!riskSeviyesi.isEmpty()) {
            gerekce = riskSeviyesi + " risk profili: " + gerekce;
        }

        int riskPuani = sayi(sonuc.get("riskPuani"), eslesenAnaliz.getRiskPuani());
        int guvenPuani = sayi(sonuc.get("guvenPuani"), eslesenAnaliz.getGuvenPuani());

        return new BahisOnerisi(
                eslesenAnaliz.getMac(),
                eslesenAnaliz.getBahisTuru(),
                eslesenAnaliz.getSecim(),
                eslesenAnaliz.getOranDegeri(),
                sinirla(riskPuani),
                sinirla(guvenPuani),
                "LLM destekli final oneri" + (cacheKullanildi ? " (cache)" : "") + ": " + gerekce
        );
    }

    private String sistemMesajiOlustur() {
        String prompt = promptDosyasiniOku();
        if (prompt.isEmpty()) {
            prompt = "Sen analiz sonuclarini birlestiren temkinli bahis onerisi agentisin. Garanti veya kesin ifade kullanma.";
        }

        return prompt
                + "\n\nUygulama uyumlulugu icin cevabi yalniz JSON olarak dondur. "
                + "Dusuk, orta ve cok riskli olmak uzere tam 3 tahmin uret. "
                + "En az su alanlar bulunmali: "
                + "{\"onerilebilir\":true,\"oneriler\":[{\"macId\":0,\"bahisTuru\":\"MAC_SONUCU_1\",\"secim\":\"Mac Sonucu 1\","
                + "\"riskSeviyesi\":\"DUSUK\",\"riskPuani\":30,\"guvenPuani\":70,\"gerekce\":\"kisa gerekce\"}]}. "
                + "Tanimsiz marketlerde bahisTuru DIGER olabilir; bu durumda secim alanini aday listesinde geldigi gibi kullan. "
                + "Oneri yapilmayacaksa {\"onerilebilir\":false,\"gerekce\":\"neden\"} dondur.";
    }

    private String kullaniciMesajiOlustur(List<OranRiskAnalizi> riskAnalizleri) {
        StringBuilder mesaj = new StringBuilder();
        mesaj.append("Aday analizler:\n");
        int adet = Math.min(EN_FAZLA_ADAY, riskAnalizleri.size());
        for (int i = 0; i < adet; i++) {
            OranRiskAnalizi analiz = riskAnalizleri.get(i);
            mesaj.append(i + 1).append(". ")
                    .append("macId=").append(analiz.getMac().getIddaaEventId())
                    .append(" | ").append(analiz.getMac().getKarsilasmaAdi())
                    .append(" | lig=").append(analiz.getMac().getLig())
                    .append(" | evSahibi=").append(analiz.getMac().getEvSahibi())
                    .append(" | deplasman=").append(analiz.getMac().getDeplasman())
                    .append(" | bahisTuru=").append(analiz.getBahisTuru().name())
                    .append(" | secim=").append(analiz.getSecim())
                    .append(" | oran=").append(analiz.formatliOranDegeri())
                    .append(" | risk=").append(analiz.getRiskPuani())
                    .append(" | guven=").append(analiz.getGuvenPuani())
                    .append(" | gerekce=").append(analiz.getGerekce())
                    .append("\n");
        }
        return mesaj.toString();
    }

    private OranRiskAnalizi analizBul(List<OranRiskAnalizi> analizler, Map<?, ?> sonuc) {
        long macId = uzun(sonuc.get("macId"), -1L);
        BahisTuru bahisTuru = bahisTuru(metin(sonuc.get("bahisTuru")));
        String secim = metin(sonuc.get("secim"));

        for (OranRiskAnalizi analiz : analizler) {
            boolean macUyumlu = macId <= 0 || analiz.getMac().getIddaaEventId() == macId;
            boolean turUyumlu = bahisTuru == null || analiz.getBahisTuru() == bahisTuru;
            boolean secimUyumlu = secim.isEmpty() || analiz.getSecim().equalsIgnoreCase(secim);
            if (macUyumlu && turUyumlu && secimUyumlu) {
                return analiz;
            }
        }

        return null;
    }

    private OranRiskAnalizi enDusukRiskliAnaliz(List<OranRiskAnalizi> analizler) {
        OranRiskAnalizi enIyi = null;
        for (OranRiskAnalizi analiz : analizler) {
            if (enIyi == null || analiz.getRiskPuani() < enIyi.getRiskPuani()) {
                enIyi = analiz;
            }
        }
        return enIyi;
    }

    private OranRiskAnalizi enYakinRiskProfilindekiAnaliz(List<OranRiskAnalizi> analizler, String riskSeviyesi) {
        if (riskSeviyesi == null || riskSeviyesi.trim().isEmpty()) {
            return null;
        }
        String temiz = riskSeviyesi.trim().toUpperCase();
        int alt;
        int ust;
        if ("DUSUK".equals(temiz)) {
            alt = 0;
            ust = 35;
        } else if ("ORTA".equals(temiz)) {
            alt = 36;
            ust = 60;
        } else {
            alt = 61;
            ust = 100;
        }

        OranRiskAnalizi enIyi = null;
        for (OranRiskAnalizi analiz : analizler) {
            if (analiz.getRiskPuani() < alt || analiz.getRiskPuani() > ust) {
                continue;
            }
            if (enIyi == null || analiz.getGuvenPuani() > enIyi.getGuvenPuani()) {
                enIyi = analiz;
            }
        }
        return enIyi;
    }

    private String istekGovdesiOlustur(String sistemMesaji, String kullaniciMesaji, String model) {
        return "{"
                + "\"model\":\"" + jsonEscape(model) + "\","
                + "\"max_output_tokens\":700,"
                + "\"input\":["
                + "{\"role\":\"system\",\"content\":[{\"type\":\"input_text\",\"text\":\"" + jsonEscape(sistemMesaji) + "\"}]},"
                + "{\"role\":\"user\",\"content\":[{\"type\":\"input_text\",\"text\":\"" + jsonEscape(kullaniciMesaji) + "\"}]}"
                + "]"
                + "}";
    }

    private String httpPost(String govde, String apiKey) {
        HttpURLConnection baglanti = null;
        try {
            URL url = new URL(OPENAI_RESPONSES_URL);
            baglanti = (HttpURLConnection) url.openConnection();
            baglanti.setRequestMethod("POST");
            baglanti.setConnectTimeout(15000);
            baglanti.setReadTimeout(45000);
            baglanti.setDoOutput(true);
            baglanti.setRequestProperty("Authorization", "Bearer " + apiKey);
            baglanti.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            baglanti.setRequestProperty("Accept", "application/json");

            OutputStream outputStream = baglanti.getOutputStream();
            outputStream.write(govde.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            int durumKodu = baglanti.getResponseCode();
            InputStream stream = durumKodu >= 200 && durumKodu < 300 ? baglanti.getInputStream() : baglanti.getErrorStream();
            String cevap = streamOku(stream);
            if (durumKodu < 200 || durumKodu >= 300) {
                throw new IllegalStateException("OpenAI istegi basarisiz oldu. HTTP durum kodu: " + durumKodu + " " + cevap);
            }
            return cevap;
        } catch (IOException e) {
            throw new IllegalStateException("OpenAI API istegi yapilamadi.", e);
        } finally {
            if (baglanti != null) {
                baglanti.disconnect();
            }
        }
    }

    private String ciktiMetniniBul(String cevapJson) {
        Object kok = jsonServisi.jsonOku(cevapJson);
        Object bulunan = ciktiMetniDegeriniBul(kok);
        return bulunan == null ? null : String.valueOf(bulunan);
    }

    private Object ciktiMetniDegeriniBul(Object kok) {
        if (kok instanceof Map) {
            Map<?, ?> harita = (Map<?, ?>) kok;
            if (harita.containsKey("output_text")) {
                return harita.get("output_text");
            }
            if ("output_text".equals(harita.get("type")) && harita.containsKey("text")) {
                return harita.get("text");
            }
            for (Object deger : harita.values()) {
                Object bulunan = ciktiMetniDegeriniBul(deger);
                if (bulunan != null) {
                    return bulunan;
                }
            }
        }

        if (kok instanceof List) {
            for (Object deger : (List<?>) kok) {
                Object bulunan = ciktiMetniDegeriniBul(deger);
                if (bulunan != null) {
                    return bulunan;
                }
            }
        }

        return null;
    }

    private Map<?, ?> jsonHaritasi(String json) {
        Object kok = jsonServisi.jsonOku(jsonMetniniTemizle(json));
        if (!(kok instanceof Map)) {
            throw new IllegalArgumentException("LLM JSON cevabi nesne formatinda degil.");
        }
        return (Map<?, ?>) kok;
    }

    private String promptDosyasiniOku() {
        File dosya = new File(ONERI_PROMPT_DOSYASI);
        if (!dosya.exists() || !dosya.isFile()) {
            return "";
        }
        try {
            return streamOku(new FileInputStream(dosya));
        } catch (IOException e) {
            return "";
        }
    }

    private String jsonMetniniTemizle(String json) {
        String temiz = json == null ? "" : json.trim();
        if (temiz.startsWith("```")) {
            temiz = temiz.replaceFirst("^```[a-zA-Z]*", "").trim();
            if (temiz.endsWith("```")) {
                temiz = temiz.substring(0, temiz.length() - 3).trim();
            }
        }
        return temiz;
    }

    private String streamOku(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder sonuc = new StringBuilder();
        String satir;
        while ((satir = reader.readLine()) != null) {
            sonuc.append(satir).append("\n");
        }
        reader.close();
        return sonuc.toString();
    }

    private BahisTuru bahisTuru(String deger) {
        if (deger == null || deger.trim().isEmpty()) {
            return null;
        }
        try {
            return BahisTuru.valueOf(deger.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String jsonEscape(String metin) {
        if (metin == null) {
            return "";
        }
        StringBuilder sonuc = new StringBuilder();
        for (int i = 0; i < metin.length(); i++) {
            char karakter = metin.charAt(i);
            switch (karakter) {
                case '"':
                    sonuc.append("\\\"");
                    break;
                case '\\':
                    sonuc.append("\\\\");
                    break;
                case '\b':
                    sonuc.append("\\b");
                    break;
                case '\f':
                    sonuc.append("\\f");
                    break;
                case '\n':
                    sonuc.append("\\n");
                    break;
                case '\r':
                    sonuc.append("\\r");
                    break;
                case '\t':
                    sonuc.append("\\t");
                    break;
                default:
                    if (karakter < 32) {
                        sonuc.append(String.format("\\u%04x", (int) karakter));
                    } else {
                        sonuc.append(karakter);
                    }
            }
        }
        return sonuc.toString();
    }

    private String metin(Object deger) {
        return deger == null ? "" : String.valueOf(deger).trim();
    }

    private int sayi(Object deger, int varsayilan) {
        if (deger instanceof Number) {
            return ((Number) deger).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(deger));
        } catch (Exception e) {
            return varsayilan;
        }
    }

    private long uzun(Object deger, long varsayilan) {
        if (deger instanceof Number) {
            return ((Number) deger).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(deger));
        } catch (Exception e) {
            return varsayilan;
        }
    }

    private int sinirla(int puan) {
        return Math.max(0, Math.min(100, puan));
    }
}
