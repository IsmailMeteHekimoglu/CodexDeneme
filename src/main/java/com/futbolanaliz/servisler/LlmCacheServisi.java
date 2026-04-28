package com.futbolanaliz.servisler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class LlmCacheServisi {
    private static final File CACHE_KLASORU = new File("kaynaklar/llm-cache");

    public String oku(String kategori, String anahtarMetni) {
        File dosya = dosya(kategori, anahtarMetni);
        if (!dosya.exists() || !dosya.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(dosya), StandardCharsets.UTF_8));
            StringBuilder sonuc = new StringBuilder();
            String satir;
            while ((satir = reader.readLine()) != null) {
                sonuc.append(satir).append("\n");
            }
            return sonuc.toString().trim();
        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void yaz(String kategori, String anahtarMetni, String icerik) {
        if (icerik == null || icerik.trim().isEmpty()) {
            return;
        }
        if (!CACHE_KLASORU.exists()) {
            CACHE_KLASORU.mkdirs();
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dosya(kategori, anahtarMetni)), StandardCharsets.UTF_8));
            writer.write(icerik);
        } catch (IOException ignored) {
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private File dosya(String kategori, String anahtarMetni) {
        String guvenliKategori = kategori == null || kategori.trim().isEmpty() ? "genel" : kategori.replaceAll("[^a-zA-Z0-9_-]", "_");
        return new File(CACHE_KLASORU, guvenliKategori + "-" + sha256(anahtarMetni) + ".json");
    }

    private String sha256(String metin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((metin == null ? "" : metin).getBytes(StandardCharsets.UTF_8));
            StringBuilder sonuc = new StringBuilder();
            for (byte b : hash) {
                sonuc.append(String.format("%02x", b));
            }
            return sonuc.toString();
        } catch (Exception e) {
            return String.valueOf(Math.abs((metin == null ? "" : metin).hashCode()));
        }
    }
}
