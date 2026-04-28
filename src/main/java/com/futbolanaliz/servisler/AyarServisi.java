package com.futbolanaliz.servisler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AyarServisi {
    private static final String AYAR_DOSYASI = "config.properties";
    private static final String VARSAYILAN_MODEL = "gpt-4.1-mini";

    public LlmAyarlari ayarlariOku() {
        Properties properties = dosyadanOku();
        String apiKey = ilkDolu(properties.getProperty("openai.apiKey"), System.getenv("OPENAI_API_KEY"));
        String model = ilkDolu(properties.getProperty("openai.model"), System.getenv("FUTBOL_ANALIZ_LLM_MODEL"), VARSAYILAN_MODEL);
        boolean llmAktif = booleanOku(properties.getProperty("llm.enabled"), !"0".equals(System.getenv("FUTBOL_ANALIZ_LLM")));
        boolean cacheAktif = booleanOku(properties.getProperty("llm.cache.enabled"), true);
        return new LlmAyarlari(apiKey, model, llmAktif, cacheAktif);
    }

    public void ayarlariKaydet(LlmAyarlari ayarlar) {
        Properties properties = dosyadanOku();
        properties.setProperty("openai.apiKey", ayarlar.getApiKey() == null ? "" : ayarlar.getApiKey());
        properties.setProperty("openai.model", ayarlar.getModel() == null ? VARSAYILAN_MODEL : ayarlar.getModel());
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

    private Properties dosyadanOku() {
        Properties properties = new Properties();
        File dosya = new File(AYAR_DOSYASI);
        if (!dosya.exists() || !dosya.isFile()) {
            return properties;
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
        return properties;
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
        private final String apiKey;
        private final String model;
        private final boolean llmAktif;
        private final boolean cacheAktif;

        public LlmAyarlari(String apiKey, String model, boolean llmAktif, boolean cacheAktif) {
            this.apiKey = apiKey;
            this.model = model == null || model.trim().isEmpty() ? VARSAYILAN_MODEL : model.trim();
            this.llmAktif = llmAktif;
            this.cacheAktif = cacheAktif;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getModel() {
            return model;
        }

        public boolean isLlmAktif() {
            return llmAktif;
        }

        public boolean isCacheAktif() {
            return cacheAktif;
        }

        public boolean apiKeyVarMi() {
            return apiKey != null && !apiKey.trim().isEmpty();
        }
    }
}
