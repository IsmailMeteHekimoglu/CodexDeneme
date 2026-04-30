package com.futbolanaliz.servisler;

import com.futbolanaliz.modeller.BahisOnerisi;
import com.futbolanaliz.modeller.BahisTuru;
import com.futbolanaliz.modeller.BirlesikAnalizSonucu;
import com.futbolanaliz.modeller.KadroDurumuAnalizi;
import com.futbolanaliz.modeller.MacSonuAnalizi;
import com.futbolanaliz.modeller.OranRiskAnalizi;
import com.futbolanaliz.modeller.TakimGucuAnalizi;

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
    private static final String GEMINI_GENERATE_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
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
        return birlesikAnalizUret(riskAnalizleri).getOneriler();
    }

    public BirlesikAnalizSonucu birlesikAnalizUret(List<OranRiskAnalizi> riskAnalizleri) {
        return birlesikAnalizUret(riskAnalizleri, new ArrayList<MacSonuAnalizi>(), new ArrayList<TakimGucuAnalizi>(), new ArrayList<KadroDurumuAnalizi>());
    }

    public BirlesikAnalizSonucu birlesikAnalizUret(List<OranRiskAnalizi> riskAnalizleri, List<MacSonuAnalizi> yorumAnalizleri, List<TakimGucuAnalizi> takimAnalizleri, List<KadroDurumuAnalizi> kadroAnalizleri) {
        List<BahisOnerisi> oneriler = new ArrayList<BahisOnerisi>();
        if (!kullanilabilirMi() || riskAnalizleri == null || riskAnalizleri.isEmpty()) {
            return new BirlesikAnalizSonucu("", oneriler);
        }

        String sistemMesaji = sistemMesajiOlustur();
        String kullaniciMesaji = kullaniciMesajiOlustur(riskAnalizleri, yorumAnalizleri, takimAnalizleri, kadroAnalizleri);
        AyarServisi.LlmAyarlari ayarlar = ayarServisi.ayarlariOku();
        String cacheAnahtari = ayarlar.getSaglayici() + "\n" + ayarlar.getModel() + "\n" + sistemMesaji + "\n" + kullaniciMesaji;
        String ciktiMetni = ayarlar.isCacheAktif() ? cacheServisi.oku("oneri", cacheAnahtari) : null;
        boolean cacheKullanildi = ciktiMetni != null && !ciktiMetni.trim().isEmpty();
        if (!cacheKullanildi) {
            String cevap = llmIstegiYap(sistemMesaji, kullaniciMesaji, ayarlar);
            ciktiMetni = ciktiMetniniBul(cevap);
            if (ayarlar.isCacheAktif()) {
                cacheServisi.yaz("oneri", cacheAnahtari, ciktiMetni);
            }
        }
        if (ciktiMetni == null || ciktiMetni.trim().isEmpty()) {
            return new BirlesikAnalizSonucu("", oneriler);
        }

        Map<?, ?> sonuc = jsonHaritasi(ciktiMetni);
        boolean oneriVar = !"false".equalsIgnoreCase(metin(sonuc.get("onerilebilir")));
        if (!oneriVar) {
            return new BirlesikAnalizSonucu(profesyonelAnalizMetni(sonuc), oneriler);
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

        return new BirlesikAnalizSonucu(
                profesyonelAnalizMetni(sonuc),
                ucProfilSiraliOneriler(oneriler, riskAnalizleri, cacheKullanildi)
        );
    }

    private String profesyonelAnalizMetni(Map<?, ?> sonuc) {
        String metin = metin(sonuc.get("profesyonelAnaliz"));
        if (!metin.isEmpty()) {
            return metin;
        }
        Object macAnalizi = sonuc.get("macAnalizi");
        if (macAnalizi instanceof Map) {
            Map<?, ?> analiz = (Map<?, ?>) macAnalizi;
            StringBuilder birlesik = new StringBuilder();
            satirEkle(birlesik, "Karar ozeti", metin(analiz.get("kararOzeti")));
            satirEkle(birlesik, "Oran ve piyasa okuması", metin(analiz.get("oranPiyasa")));
            satirEkle(birlesik, "Form ve oyun dengesi", metin(analiz.get("form")));
            satirEkle(birlesik, "Eksik/rotasyon etkisi", metin(analiz.get("oyuncuDurumu")));
            satirEkle(birlesik, "Saha ve motivasyon", metin(analiz.get("sahaMotivasyon")));
            satirEkle(birlesik, "Nihai yorum", metin(analiz.get("sonuc")));
            return birlesik.toString();
        }
        return "";
    }

    private void satirEkle(StringBuilder metin, String baslik, String deger) {
        if (deger == null || deger.trim().isEmpty()) {
            return;
        }
        if (metin.length() > 0) {
            metin.append("\n");
        }
        metin.append(baslik).append(": ").append(deger.trim());
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
        String riskSeviyesi = riskSeviyesiNormalize(metin(sonuc.get("riskSeviyesi")));
        if (!riskSeviyesi.isEmpty()) {
            gerekce = riskSeviyesi + " risk profili:\n" + profesyonelOneriMetni(gerekce, sonuc);
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
                riskSeviyesi,
                "LLM destekli final oneri" + (cacheKullanildi ? " (cache)" : "") + ": " + gerekce
        );
    }

    private List<BahisOnerisi> ucProfilSiraliOneriler(List<BahisOnerisi> mevcutOneriler, List<OranRiskAnalizi> riskAnalizleri, boolean cacheKullanildi) {
        List<BahisOnerisi> sirali = new ArrayList<BahisOnerisi>();
        String[] profiller = {"DUSUK", "ORTA", "COK_RISKLI"};

        for (String profil : profiller) {
            BahisOnerisi mevcut = profildekiIlkOneri(mevcutOneriler, profil);
            if (mevcut != null && !ayniTahminVarMi(sirali, mevcut)) {
                sirali.add(mevcut);
                continue;
            }

            BahisOnerisi tamamlayici = profilOnerisiOlustur(riskAnalizleri, profil, cacheKullanildi, sirali);
            if (tamamlayici != null) {
                sirali.add(tamamlayici);
            }
        }

        return sirali;
    }

    private BahisOnerisi profildekiIlkOneri(List<BahisOnerisi> oneriler, String profil) {
        for (BahisOnerisi oneri : oneriler) {
            if (profilEslesiyor(oneri, profil)) {
                return oneri;
            }
        }
        return null;
    }

    private boolean profilEslesiyor(BahisOnerisi oneri, String profil) {
        return profil.equals(riskSeviyesiNormalize(oneri.getRiskSeviyesi()));
    }

    private boolean ayniTahminVarMi(List<BahisOnerisi> oneriler, BahisOnerisi aday) {
        for (BahisOnerisi oneri : oneriler) {
            if (ayniTahminMi(oneri.getBahisTuru(), oneri.getSecim(), aday.getBahisTuru(), aday.getSecim())) {
                return true;
            }
        }
        return false;
    }

    private BahisOnerisi profilOnerisiOlustur(List<OranRiskAnalizi> riskAnalizleri, String profil, boolean cacheKullanildi, List<BahisOnerisi> mevcutOneriler) {
        OranRiskAnalizi analiz = enYakinRiskProfilindekiAnaliz(riskAnalizleri, profil, mevcutOneriler);
        if (analiz == null) {
            analiz = profilIcinEnYakinAnaliz(riskAnalizleri, profil, mevcutOneriler);
        }
        if (analiz == null) {
            return null;
        }

        return new BahisOnerisi(
                analiz.getMac(),
                analiz.getBahisTuru(),
                analiz.getSecim(),
                analiz.getOranDegeri(),
                sinirla(profilRiskPuani(profil, analiz.getRiskPuani())),
                sinirla(analiz.getGuvenPuani()),
                profil,
                "LLM destekli final oneri" + (cacheKullanildi ? " (cache)" : "") + ": "
                        + profil + " risk profili:\n"
                        + "Karar ozeti: LLM bu profil icin ayri bir tahmin dondurmedigi icin en uygun aday analiz kullanildi.\n"
                        + "Veri degerlendirmesi: " + analiz.getGerekce()
        );
    }

    private OranRiskAnalizi profilIcinEnYakinAnaliz(List<OranRiskAnalizi> analizler, String profil, List<BahisOnerisi> mevcutOneriler) {
        OranRiskAnalizi secilen = null;
        for (OranRiskAnalizi analiz : analizler) {
            if (analizKullanildiMi(mevcutOneriler, analiz)) {
                continue;
            }
            if (secilen == null) {
                secilen = analiz;
                continue;
            }
            if ("DUSUK".equals(profil) && analiz.getRiskPuani() < secilen.getRiskPuani()) {
                secilen = analiz;
            } else if ("COK_RISKLI".equals(profil) && analiz.getRiskPuani() > secilen.getRiskPuani()) {
                secilen = analiz;
            } else if ("ORTA".equals(profil)
                    && Math.abs(analiz.getRiskPuani() - 50) < Math.abs(secilen.getRiskPuani() - 50)) {
                secilen = analiz;
            }
        }
        return secilen;
    }

    private boolean analizKullanildiMi(List<BahisOnerisi> mevcutOneriler, OranRiskAnalizi analiz) {
        for (BahisOnerisi oneri : mevcutOneriler) {
            if (ayniTahminMi(oneri.getBahisTuru(), oneri.getSecim(), analiz.getBahisTuru(), analiz.getSecim())) {
                return true;
            }
        }
        return false;
    }

    private boolean ayniTahminMi(BahisTuru ilkTur, String ilkSecim, BahisTuru ikinciTur, String ikinciSecim) {
        return ilkTur == ikinciTur && metin(ilkSecim).equalsIgnoreCase(metin(ikinciSecim));
    }

    private int profilRiskPuani(String profil, int varsayilan) {
        if ("DUSUK".equals(profil)) {
            return Math.min(varsayilan, 35);
        }
        if ("ORTA".equals(profil)) {
            return Math.max(36, Math.min(varsayilan, 60));
        }
        return Math.max(61, varsayilan);
    }

    private String profesyonelOneriMetni(String gerekce, Map<?, ?> sonuc) {
        StringBuilder metin = new StringBuilder();
        metin.append("Karar ozeti: ").append(gerekce);

        Object kontroller = sonuc.get("kontroller");
        if (kontroller instanceof Map) {
            Map<?, ?> kontrolHaritasi = (Map<?, ?>) kontroller;
            metin.append("\nForm: ").append(metin(kontrolHaritasi.get("form")));
            metin.append("\nOyuncu durumu: ").append(metin(kontrolHaritasi.get("oyuncuDurumu")));
            metin.append("\nIc saha/deplasman: ").append(metin(kontrolHaritasi.get("icSahaDeplasman")));
            metin.append("\nLig/kupa durumu: ").append(metin(kontrolHaritasi.get("ligKupaDurumu")));
        }

        Object uyarilar = sonuc.get("uyarilar");
        if (uyarilar instanceof List && !((List<?>) uyarilar).isEmpty()) {
            metin.append("\nUyarilar: ");
            for (int i = 0; i < ((List<?>) uyarilar).size(); i++) {
                if (i > 0) {
                    metin.append("; ");
                }
                metin.append(metin(((List<?>) uyarilar).get(i)));
            }
        }

        return metin.toString();
    }

    private String sistemMesajiOlustur() {
        String prompt = promptDosyasiniOku();
        if (prompt.isEmpty()) {
            prompt = "Sen analiz sonuclarini birlestiren temkinli bahis onerisi agentisin. Garanti veya kesin ifade kullanma.";
        }

        return prompt
                + "\n\nUygulama uyumlulugu icin cevabi yalniz JSON olarak dondur. "
                + "Dusuk, orta ve cok riskli olmak uzere tam 3 tahmin uret; hicbir profili bos birakma. "
                + "Ayni cevapta arayuzde gosterilecek profesyonelAnaliz alanini da uret. "
                + "oneriler dizisi sirasiyla DUSUK, ORTA, COK_RISKLI olmali. "
                + "riskSeviyesi alanlari birebir ve tekil olarak DUSUK, ORTA, COK_RISKLI degerlerini tasimali; iki oneride ayni riskSeviyesi kullanma. "
                + "En az su alanlar bulunmali: "
                + "{\"onerilebilir\":true,\"profesyonelAnaliz\":\"Karar ozeti: ...\\nOran ve piyasa okuması: ...\","
                + "\"oneriler\":[{\"macId\":0,\"bahisTuru\":\"MAC_SONUCU_1\",\"secim\":\"Mac Sonucu 1\","
                + "\"riskSeviyesi\":\"DUSUK\",\"riskPuani\":30,\"guvenPuani\":70,\"gerekce\":\"kisa gerekce\"}]}. "
                + "Tanimsiz marketlerde bahisTuru DIGER olabilir; bu durumda secim alanini aday listesinde geldigi gibi kullan. "
                + "Oneri yapilmayacaksa {\"onerilebilir\":false,\"gerekce\":\"neden\"} dondur.";
    }

    private String kullaniciMesajiOlustur(List<OranRiskAnalizi> riskAnalizleri, List<MacSonuAnalizi> yorumAnalizleri, List<TakimGucuAnalizi> takimAnalizleri, List<KadroDurumuAnalizi> kadroAnalizleri) {
        StringBuilder mesaj = new StringBuilder();
        mesaj.append("Birlesik analiz baglami:\n");
        if (yorumAnalizleri != null) {
            for (MacSonuAnalizi analiz : yorumAnalizleri) {
                mesaj.append("- On analiz: ")
                        .append(analiz.getMac().getKarsilasmaAdi())
                        .append(" | tahmin=").append(analiz.getMacSonuTahmini())
                        .append(" | guven=").append(analiz.getGuvenPuani())
                        .append(" | gerekce=").append(kisalt(analiz.getGerekce(), 900))
                        .append("\n");
            }
        }
        if (takimAnalizleri != null) {
            for (TakimGucuAnalizi analiz : takimAnalizleri) {
                mesaj.append("- Takim gucu: ")
                        .append(analiz.getMac().getKarsilasmaAdi())
                        .append(" | ev=").append(analiz.getEvSahibiGucPuani())
                        .append(" | dep=").append(analiz.getDeplasmanGucPuani())
                        .append(" | oneCikan=").append(analiz.getOneCikanTaraf())
                        .append(" | gerekce=").append(kisalt(analiz.getGerekce(), 500))
                        .append("\n");
            }
        }
        if (kadroAnalizleri != null) {
            for (KadroDurumuAnalizi analiz : kadroAnalizleri) {
                mesaj.append("- Kadro riski: ")
                        .append(analiz.getMac().getKarsilasmaAdi())
                        .append(" | evRisk=").append(analiz.getEvSahibiRiskPuani())
                        .append(" | depRisk=").append(analiz.getDeplasmanRiskPuani())
                        .append(" | ozet=").append(kisalt(analiz.getDurumOzeti(), 500))
                        .append("\n");
            }
        }
        mesaj.append("\n");
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

    private String kisalt(String metin, int limit) {
        if (metin == null || metin.length() <= limit) {
            return metin == null ? "" : metin;
        }
        return metin.substring(0, Math.max(0, limit - 3)) + "...";
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
        return enYakinRiskProfilindekiAnaliz(analizler, riskSeviyesi, new ArrayList<BahisOnerisi>());
    }

    private OranRiskAnalizi enYakinRiskProfilindekiAnaliz(List<OranRiskAnalizi> analizler, String riskSeviyesi, List<BahisOnerisi> mevcutOneriler) {
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
            if (analizKullanildiMi(mevcutOneriler, analiz)) {
                continue;
            }
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
                + "\"max_output_tokens\":1200,"
                + "\"input\":["
                + "{\"role\":\"system\",\"content\":[{\"type\":\"input_text\",\"text\":\"" + jsonEscape(sistemMesaji) + "\"}]},"
                + "{\"role\":\"user\",\"content\":[{\"type\":\"input_text\",\"text\":\"" + jsonEscape(kullaniciMesaji) + "\"}]}"
                + "]"
                + "}";
    }

    private String geminiIstekGovdesiOlustur(String sistemMesaji, String kullaniciMesaji) {
        return "{"
                + "\"systemInstruction\":{\"parts\":[{\"text\":\"" + jsonEscape(sistemMesaji) + "\"}]},"
                + "\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"" + jsonEscape(kullaniciMesaji) + "\"}]}],"
                + "\"generationConfig\":{\"responseMimeType\":\"application/json\",\"maxOutputTokens\":1200}"
                + "}";
    }

    private String llmIstegiYap(String sistemMesaji, String kullaniciMesaji, AyarServisi.LlmAyarlari ayarlar) {
        if (ayarlar.geminiMi()) {
            return geminiPost(geminiIstekGovdesiOlustur(sistemMesaji, kullaniciMesaji), ayarlar.getGeminiApiKey(), ayarlar.getGeminiModel());
        }
        return openaiPost(istekGovdesiOlustur(sistemMesaji, kullaniciMesaji, ayarlar.getOpenaiModel()), ayarlar.getOpenaiApiKey());
    }

    private String openaiPost(String govde, String apiKey) {
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

    private String geminiPost(String govde, String apiKey, String model) {
        HttpURLConnection baglanti = null;
        try {
            URL url = new URL(String.format(GEMINI_GENERATE_URL_TEMPLATE, urlEncode(model)));
            baglanti = (HttpURLConnection) url.openConnection();
            baglanti.setRequestMethod("POST");
            baglanti.setConnectTimeout(15000);
            baglanti.setReadTimeout(45000);
            baglanti.setDoOutput(true);
            baglanti.setRequestProperty("x-goog-api-key", apiKey);
            baglanti.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            baglanti.setRequestProperty("Accept", "application/json");

            OutputStream outputStream = baglanti.getOutputStream();
            outputStream.write(govde.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            int durumKodu = baglanti.getResponseCode();
            InputStream stream = durumKodu >= 200 && durumKodu < 300 ? baglanti.getInputStream() : baglanti.getErrorStream();
            String cevap = streamOku(stream);
            if (durumKodu < 200 || durumKodu >= 300) {
                throw new IllegalStateException("Gemini istegi basarisiz oldu. HTTP durum kodu: " + durumKodu + " " + cevap);
            }
            return cevap;
        } catch (IOException e) {
            throw new IllegalStateException("Gemini API istegi yapilamadi.", e);
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

    private String urlEncode(String metin) {
        if (metin == null) {
            return "";
        }
        return metin.trim().replace(" ", "%20");
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

    private String riskSeviyesiNormalize(String riskSeviyesi) {
        String temiz = riskSeviyesi == null ? "" : riskSeviyesi.trim().toUpperCase();
        if (temiz.contains("COK") || temiz.contains("YUKSEK")) {
            return "COK_RISKLI";
        }
        if (temiz.contains("ORTA")) {
            return "ORTA";
        }
        if (temiz.contains("DUSUK")) {
            return "DUSUK";
        }
        return temiz;
    }
}
