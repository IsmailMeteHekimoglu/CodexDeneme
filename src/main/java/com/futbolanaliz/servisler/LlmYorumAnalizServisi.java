package com.futbolanaliz.servisler;

import com.futbolanaliz.modeller.LlmYorumAnalizSonucu;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.MacYorumu;
import com.futbolanaliz.modeller.Oran;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class LlmYorumAnalizServisi {
    private static final String OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final String VARSAYILAN_MODEL = "gpt-4.1-mini";
    private static final String YORUM_PROMPT_DOSYASI = "promptlar/mac-yorum-analizi.prompt.md";
    private static final int EN_FAZLA_YORUM = 5;
    private static final int YORUM_KARAKTER_LIMITI = 700;

    private final JsonServisi jsonServisi = new JsonServisi();
    private final TokenTahminServisi tokenTahminServisi = new TokenTahminServisi();

    public boolean kullanilabilirMi() {
        String apiKey = apiAnahtari();
        return apiKey != null && !apiKey.trim().isEmpty() && !"0".equals(System.getenv("FUTBOL_ANALIZ_LLM"));
    }

    public LlmYorumAnalizSonucu macYorumlariniAnalizEt(Mac mac, List<MacYorumu> yorumlar) {
        if (!kullanilabilirMi() || mac == null || yorumlar == null || yorumlar.isEmpty()) {
            return null;
        }

        String sistemMesaji = sistemMesajiOlustur();
        String kullaniciMesaji = kullaniciMesajiOlustur(mac, yorumlar);
        String cevap = httpPost(istekGovdesiOlustur(sistemMesaji, kullaniciMesaji));
        String ciktiMetni = ciktiMetniniBul(cevap);

        if (ciktiMetni == null || ciktiMetni.trim().isEmpty()) {
            return null;
        }

        Map<?, ?> sonucHaritasi = jsonHaritasi(ciktiMetni);
        String tahmin = temizTahmin(metin(sonucHaritasi.get("tahmin")));
        int guven = sayi(sonucHaritasi.get("guven"), 50);
        String gerekce = metin(sonucHaritasi.get("gerekce"));

        if (gerekce.isEmpty()) {
            gerekce = "LLM yorum metinlerini ve oran ozetini birlikte degerlendirdi.";
        }

        return new LlmYorumAnalizSonucu(
                tahmin,
                guven,
                "LLM destekli analiz: " + gerekce,
                tokenTahminServisi.tahminEt(sistemMesaji, kullaniciMesaji),
                tokenTahminServisi.tahminEt(ciktiMetni)
        );
    }

    private String kullaniciMesajiOlustur(Mac mac, List<MacYorumu> yorumlar) {
        StringBuilder mesaj = new StringBuilder();
        mesaj.append("Mac: ").append(mac.getEvSahibi()).append(" - ").append(mac.getDeplasman()).append("\n");
        mesaj.append("Lig: ").append(mac.getLig()).append("\n");
        mesaj.append("Saat: ").append(mac.getSaat()).append("\n");
        mesaj.append("Oranlar:\n");
        for (Oran oran : mac.getOranlar()) {
            mesaj.append("- ").append(oran.getBahisTuru().getGorunenAd()).append(": ").append(oran.formatliDeger()).append("\n");
        }

        mesaj.append("Yorumlar:\n");
        int adet = Math.min(EN_FAZLA_YORUM, yorumlar.size());
        for (int i = 0; i < adet; i++) {
            MacYorumu yorum = yorumlar.get(i);
            mesaj.append(i + 1).append(". ").append(kisalt(yorum.getYorumMetni(), YORUM_KARAKTER_LIMITI)).append("\n");
        }

        return mesaj.toString();
    }

    private String sistemMesajiOlustur() {
        String prompt = promptDosyasiniOku();
        if (prompt.isEmpty()) {
            prompt = "Sen futbol mac yorumlarini analiz eden dikkatli bir asistansin. "
                    + "Sadece verilen veriyle karar ver. Kesin bahis vaadi verme.";
        }

        return prompt
                + "\n\nUygulama uyumlulugu icin cevabi yalniz JSON olarak dondur. "
                + "En az su alanlar bulunmali: "
                + "{\"tahmin\":\"1|X|2|Belirsiz\",\"guven\":0-100,\"gerekce\":\"kisa gerekce\"}. "
                + "Prompt dosyasindaki daha genis semayi kullanirsan da bu uc alani mutlaka ekle.";
    }

    private String promptDosyasiniOku() {
        File dosya = new File(YORUM_PROMPT_DOSYASI);
        if (!dosya.exists() || !dosya.isFile()) {
            return "";
        }

        try {
            return streamOku(new FileInputStream(dosya));
        } catch (IOException e) {
            return "";
        }
    }

    private String istekGovdesiOlustur(String sistemMesaji, String kullaniciMesaji) {
        return "{"
                + "\"model\":\"" + jsonEscape(model()) + "\","
                + "\"max_output_tokens\":350,"
                + "\"input\":["
                + "{\"role\":\"system\",\"content\":[{\"type\":\"input_text\",\"text\":\"" + jsonEscape(sistemMesaji) + "\"}]},"
                + "{\"role\":\"user\",\"content\":[{\"type\":\"input_text\",\"text\":\"" + jsonEscape(kullaniciMesaji) + "\"}]}"
                + "]"
                + "}";
    }

    private String httpPost(String govde) {
        HttpURLConnection baglanti = null;

        try {
            URL url = new URL(OPENAI_RESPONSES_URL);
            baglanti = (HttpURLConnection) url.openConnection();
            baglanti.setRequestMethod("POST");
            baglanti.setConnectTimeout(15000);
            baglanti.setReadTimeout(45000);
            baglanti.setDoOutput(true);
            baglanti.setRequestProperty("Authorization", "Bearer " + apiAnahtari());
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

    private Map<?, ?> jsonHaritasi(String json) {
        Object kok = jsonServisi.jsonOku(jsonMetniniTemizle(json));
        if (!(kok instanceof Map)) {
            throw new IllegalArgumentException("LLM JSON cevabi nesne formatinda degil.");
        }
        return (Map<?, ?>) kok;
    }

    private String apiAnahtari() {
        return System.getenv("OPENAI_API_KEY");
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

    private String model() {
        String model = System.getenv("FUTBOL_ANALIZ_LLM_MODEL");
        return model == null || model.trim().isEmpty() ? VARSAYILAN_MODEL : model.trim();
    }

    private String temizTahmin(String tahmin) {
        if ("1".equals(tahmin) || "X".equalsIgnoreCase(tahmin) || "2".equals(tahmin)) {
            return tahmin.toUpperCase();
        }
        return "Belirsiz";
    }

    private String kisalt(String metin, int enFazlaKarakter) {
        if (metin == null || metin.length() <= enFazlaKarakter) {
            return metin;
        }
        return metin.substring(0, enFazlaKarakter - 3) + "...";
    }

    private String streamOku(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder sonuc = new StringBuilder();
        String satir;
        while ((satir = reader.readLine()) != null) {
            sonuc.append(satir);
        }
        reader.close();
        return sonuc.toString();
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
}
