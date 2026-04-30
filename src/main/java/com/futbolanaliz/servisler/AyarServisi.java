package com.futbolanaliz.servisler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AyarServisi {
    private static final String AYAR_DOSYASI = "config.properties";
    private static final List<String> OKUNACAK_AYAR_DOSYALARI = Arrays.asList("application.properties", ".env", AYAR_DOSYASI);
    private static final String VARSAYILAN_SAGLAYICI = "openai";
    private static final String VARSAYILAN_MODEL = "gpt-5-mini";
    private static final String VARSAYILAN_GEMINI_MODEL = "gemini-1.5-flash";
    private static final List<String> KULLANILABILIR_MODELLER = Arrays.asList(
            "gpt-5.2",
            "gpt-5.2-pro",
            "gpt-5-mini",
            "gpt-5-nano",
            "gpt-4.1",
            "gpt-4.1-mini",
            "gpt-4.1-nano"
    );
    private static final List<String> KULLANILABILIR_GEMINI_MODELLER = Arrays.asList(
            "gemini-2.0-flash",
            "gemini-1.5-pro",
            "gemini-1.5-flash",
            "gemini-1.5-flash-8b"
    );
    private static final List<String> KULLANILABILIR_SAGLAYICILAR = Arrays.asList("openai", "gemini");

    public LlmAyarlari ayarlariOku() {
        Properties properties = dosyadanOku();
        String saglayici = ilkDolu(properties.getProperty("llm.provider"), System.getenv("FUTBOL_ANALIZ_LLM_PROVIDER"), VARSAYILAN_SAGLAYICI);
        String openaiApiKey = ilkDolu(properties.getProperty("openai.apiKey"), System.getenv("OPENAI_API_KEY"));
        String geminiApiKey = ilkDolu(properties.getProperty("gemini.apiKey"), System.getenv("GEMINI_API_KEY"), System.getenv("GOOGLE_API_KEY"));
        String openaiModel = ilkDolu(properties.getProperty("openai.model"), System.getenv("FUTBOL_ANALIZ_LLM_MODEL"), VARSAYILAN_MODEL);
        String geminiModel = ilkDolu(properties.getProperty("gemini.model"), System.getenv("FUTBOL_ANALIZ_GEMINI_MODEL"), VARSAYILAN_GEMINI_MODEL);
        boolean llmAktif = booleanOku(properties.getProperty("llm.enabled"), !"0".equals(System.getenv("FUTBOL_ANALIZ_LLM")));
        boolean cacheAktif = booleanOku(properties.getProperty("llm.cache.enabled"), true);
        return new LlmAyarlari(saglayici, openaiApiKey, openaiModel, geminiApiKey, geminiModel, llmAktif, cacheAktif);
    }

    public boolean apiKeyleriGoster() {
        Properties properties = dosyadanOku();
        return booleanOku(properties.getProperty("apiKeys.visible"), false);
    }

    public void ayarlariKaydet(LlmAyarlari ayarlar) {
        Properties properties = dosyadanOku();
        properties.setProperty("llm.provider", ayarlar.getSaglayici());
        properties.setProperty("openai.apiKey", ayarlar.getOpenaiApiKey() == null ? "" : ayarlar.getOpenaiApiKey());
        properties.setProperty("openai.model", ayarlar.getOpenaiModel() == null ? VARSAYILAN_MODEL : ayarlar.getOpenaiModel());
        properties.setProperty("gemini.apiKey", ayarlar.getGeminiApiKey() == null ? "" : ayarlar.getGeminiApiKey());
        properties.setProperty("gemini.model", ayarlar.getGeminiModel() == null ? VARSAYILAN_GEMINI_MODEL : ayarlar.getGeminiModel());
        properties.setProperty("llm.enabled", String.valueOf(ayarlar.isLlmAktif()));
        properties.setProperty("llm.cache.enabled", String.valueOf(ayarlar.isCacheAktif()));

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(AYAR_DOSYASI);
            properties.store(outputStream, "Futbol Analiz Agent ayarlari");
        } catch (IOException e) {
            throw new IllegalStateException("Ayarlar kaydedilemedi.", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void apiKeyGorunumunuKaydet(boolean goster) {
        Properties properties = dosyadanOku();
        properties.setProperty("apiKeys.visible", String.valueOf(goster));
        ayarDosyasiniYaz(properties, "API key gorunum ayarlari kaydedilemedi.");
    }

    private void ayarDosyasiniYaz(Properties properties, String hataMesaji) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(AYAR_DOSYASI);
            properties.store(outputStream, "Futbol Analiz Agent ayarlari");
        } catch (IOException e) {
            throw new IllegalStateException(hataMesaji, e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public List<String> kullanilabilirModeller() {
        return KULLANILABILIR_MODELLER;
    }

    public List<String> kullanilabilirGeminiModeller() {
        return KULLANILABILIR_GEMINI_MODELLER;
    }

    public List<String> kullanilabilirSaglayicilar() {
        return KULLANILABILIR_SAGLAYICILAR;
    }

    private Properties dosyadanOku() {
        Properties properties = new Properties();
        for (String dosyaAdi : OKUNACAK_AYAR_DOSYALARI) {
            dosyayiOku(properties, dosyaAdi);
        }
        return properties;
    }

    private void dosyayiOku(Properties properties, String dosyaAdi) {
        File dosya = new File(dosyaAdi);
        if (!dosya.exists() || !dosya.isFile()) {
            return;
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(dosya);
            properties.load(inputStream);
        } catch (IOException ignored) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private String ilkDolu(String... degerler) {
        if (degerler == null) {
            return "";
        }
        for (String deger : degerler) {
            if (deger != null && !deger.trim().isEmpty()) {
                return deger.trim();
            }
        }
        return "";
    }

    private boolean booleanOku(String deger, boolean varsayilan) {
        if (deger == null || deger.trim().isEmpty()) {
            return varsayilan;
        }
        return "true".equalsIgnoreCase(deger.trim()) || "1".equals(deger.trim()) || "evet".equalsIgnoreCase(deger.trim());
    }

    public static class LlmAyarlari {
        private final String saglayici;
        private final String openaiApiKey;
        private final String openaiModel;
        private final String geminiApiKey;
        private final String geminiModel;
        private final boolean llmAktif;
        private final boolean cacheAktif;

        public LlmAyarlari(String apiKey, String model, boolean llmAktif, boolean cacheAktif) {
            this(VARSAYILAN_SAGLAYICI, apiKey, model, "", VARSAYILAN_GEMINI_MODEL, llmAktif, cacheAktif);
        }

        public LlmAyarlari(String saglayici, String openaiApiKey, String openaiModel, String geminiApiKey, String geminiModel, boolean llmAktif, boolean cacheAktif) {
            this.saglayici = saglayiciSec(saglayici);
            this.openaiApiKey = openaiApiKey;
            this.openaiModel = openaiModelSec(openaiModel);
            this.geminiApiKey = geminiApiKey;
            this.geminiModel = geminiModelSec(geminiModel);
            this.llmAktif = llmAktif;
            this.cacheAktif = cacheAktif;
        }

        private String saglayiciSec(String saglayici) {
            if (saglayici == null || saglayici.trim().isEmpty()) {
                return VARSAYILAN_SAGLAYICI;
            }
            String temiz = saglayici.trim().toLowerCase();
            return KULLANILABILIR_SAGLAYICILAR.contains(temiz) ? temiz : VARSAYILAN_SAGLAYICI;
        }

        private String openaiModelSec(String model) {
            if (model == null || model.trim().isEmpty()) {
                return VARSAYILAN_MODEL;
            }
            String temizModel = model.trim();
            return KULLANILABILIR_MODELLER.contains(temizModel) ? temizModel : VARSAYILAN_MODEL;
        }

        private String geminiModelSec(String model) {
            if (model == null || model.trim().isEmpty()) {
                return VARSAYILAN_GEMINI_MODEL;
            }
            String temizModel = model.trim();
            return KULLANILABILIR_GEMINI_MODELLER.contains(temizModel) ? temizModel : VARSAYILAN_GEMINI_MODEL;
        }

        public String getSaglayici() {
            return saglayici;
        }

        public String getApiKey() {
            return geminiMi() ? geminiApiKey : openaiApiKey;
        }

        public String getModel() {
            return geminiMi() ? geminiModel : openaiModel;
        }

        public String getOpenaiApiKey() {
            return openaiApiKey;
        }

        public String getOpenaiModel() {
            return openaiModel;
        }

        public String getGeminiApiKey() {
            return geminiApiKey;
        }

        public String getGeminiModel() {
            return geminiModel;
        }

        public boolean isLlmAktif() {
            return llmAktif;
        }

        public boolean isCacheAktif() {
            return cacheAktif;
        }

        public boolean apiKeyVarMi() {
            return getApiKey() != null && !getApiKey().trim().isEmpty();
        }

        public boolean openaiMi() {
            return "openai".equals(saglayici);
        }

        public boolean geminiMi() {
            return "gemini".equals(saglayici);
        }
    }
}
