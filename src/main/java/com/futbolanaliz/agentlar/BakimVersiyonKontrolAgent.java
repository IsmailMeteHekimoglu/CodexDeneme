package com.futbolanaliz.agentlar;

import com.futbolanaliz.servisler.TokenTahminServisi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class BakimVersiyonKontrolAgent implements Agent {
    private static final String OZET_DOSYASI = "tmp/codex-degisiklik-ozeti.md";
    private static final String KAYNAK_KOKU = "src/main/java/com/futbolanaliz";

    private final TokenTahminServisi tokenTahminServisi = new TokenTahminServisi();
    private final File projeKoku;
    private final List<String> islemler = new ArrayList<String>();

    public BakimVersiyonKontrolAgent() {
        this(new File("."));
    }

    public BakimVersiyonKontrolAgent(File projeKoku) {
        this.projeKoku = projeKoku == null ? new File(".") : projeKoku;
    }

    @Override
    public String ad() {
        return "Bakim, Paketleme ve Versiyon Kontrol Agent";
    }

    @Override
    public AgentSonucu calistir() {
        islemler.clear();
        try {
            temizlenebilirDosyalariSil();
            bosDosyalariSil(projeKoku);
            bosPaketKlasorleriniSil(new File(projeKoku, KAYNAK_KOKU));
            bosGenelKlasorleriSil(projeKoku);

            GitDurumu gitDurumu = gitDurumunuOku();
            String ozet = ozetOlustur(gitDurumu);
            File ozetDosyasi = new File(projeKoku, OZET_DOSYASI);

            if (gitDurumu.degisiklikVarMi()) {
                dosyayaYaz(ozetDosyasi, ozet);
                islemler.add("Bekleyen degisiklik ozeti guncellendi: " + OZET_DOSYASI);
            } else {
                if (ozetDosyasi.exists() && !ozetDosyasi.delete()) {
                    dosyayaYaz(ozetDosyasi, "");
                }
                islemler.add("Commit sonrasi bekleyen degisiklik ozeti temizlendi.");
            }

            return AgentSonucu.basarili(ad(), mesajOlustur(), tokenTahminServisi.tahminEt(ozet));
        } catch (RuntimeException e) {
            return AgentSonucu.basarisiz(ad(), e.getMessage());
        }
    }

    private void temizlenebilirDosyalariSil() {
        List<File> sinifDosyalari = new ArrayList<File>();
        classDosyalariniTopla(new File(projeKoku, "src"), sinifDosyalari);
        for (File dosya : sinifDosyalari) {
            if (dosya.delete()) {
                islemler.add("Kaynak agacindaki gereksiz class dosyasi silindi: " + goreliYol(dosya));
            }
        }
    }

    private void bosDosyalariSil(File kok) {
        if (kok == null || !kok.exists() || korunacakYolMu(kok)) {
            return;
        }
        File[] dosyalar = kok.listFiles();
        if (dosyalar == null) {
            return;
        }
        for (File dosya : dosyalar) {
            if (korunacakYolMu(dosya)) {
                continue;
            }
            if (dosya.isDirectory()) {
                bosDosyalariSil(dosya);
            } else if (dosya.length() == 0 && dosya.delete()) {
                islemler.add("Bos dosya silindi: " + goreliYol(dosya));
            }
        }
    }

    private boolean bosGenelKlasorleriSil(File klasor) {
        if (klasor == null || !klasor.exists() || !klasor.isDirectory() || korunacakYolMu(klasor)) {
            return false;
        }

        File[] icerik = klasor.listFiles();
        if (icerik == null) {
            return true;
        }

        for (File dosya : icerik) {
            if (dosya.isDirectory()) {
                bosGenelKlasorleriSil(dosya);
            }
        }

        File[] kalan = klasor.listFiles();
        boolean bos = kalan == null || kalan.length == 0;
        if (bos && !projeKoku.getAbsoluteFile().equals(klasor.getAbsoluteFile()) && klasor.delete()) {
            islemler.add("Bos klasor silindi: " + goreliYol(klasor));
            return true;
        }
        return bos;
    }

    private boolean korunacakYolMu(File dosya) {
        String yol = dosya.getAbsolutePath().replace("\\", "/");
        return yol.contains("/.git")
                || yol.contains("/target")
                || yol.endsWith("/config.properties")
                || yol.contains("/kaynaklar/llm-cache");
    }

    private void classDosyalariniTopla(File kok, List<File> sonuc) {
        if (kok == null || !kok.exists()) {
            return;
        }
        File[] dosyalar = kok.listFiles();
        if (dosyalar == null) {
            return;
        }
        for (File dosya : dosyalar) {
            if (dosya.isDirectory()) {
                classDosyalariniTopla(dosya, sonuc);
            } else if (dosya.getName().endsWith(".class")) {
                sonuc.add(dosya);
            }
        }
    }

    private boolean bosPaketKlasorleriniSil(File klasor) {
        if (klasor == null || !klasor.exists() || !klasor.isDirectory()) {
            return true;
        }

        File[] icerik = klasor.listFiles();
        if (icerik == null) {
            return true;
        }

        boolean altlarBos = true;
        for (File dosya : icerik) {
            if (dosya.isDirectory()) {
                altlarBos = bosPaketKlasorleriniSil(dosya) && altlarBos;
            } else if (".gitkeep".equals(dosya.getName())) {
                continue;
            } else {
                altlarBos = false;
            }
        }

        if (altlarBos && !klasor.getName().equals("futbolanaliz")) {
            gitkeepDosyasiniSil(klasor);
        }

        File[] kalan = klasor.listFiles();
        boolean bos = kalan == null || kalan.length == 0;
        if (bos && !klasor.getName().equals("futbolanaliz") && klasor.delete()) {
            islemler.add("Bos paket klasoru silindi: " + goreliYol(klasor));
            return true;
        }
        return bos;
    }

    private void gitkeepDosyasiniSil(File klasor) {
        File gitkeep = new File(klasor, ".gitkeep");
        if (gitkeep.exists() && gitkeep.isFile() && gitkeep.delete()) {
            islemler.add("Bos paket .gitkeep dosyasi silindi: " + goreliYol(gitkeep));
        }
    }

    private GitDurumu gitDurumunuOku() {
        GitDurumu durum = new GitDurumu();
        durum.branch = komutCiktisi("git", "rev-parse", "--abbrev-ref", "HEAD").trim();
        durum.head = komutCiktisi("git", "rev-parse", "--short", "HEAD").trim();
        durum.version = pomVersiyonunuOku();

        String status = komutCiktisi("git", "status", "--porcelain");
        String[] satirlar = status.split("\\r?\\n");
        for (String satir : satirlar) {
            if (satir.trim().isEmpty()) {
                continue;
            }
            durum.degisiklikler.add(satir);
        }
        return durum;
    }

    private String pomVersiyonunuOku() {
        File pom = new File(projeKoku, "pom.xml");
        if (!pom.exists()) {
            return "bilinmiyor";
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pom), StandardCharsets.UTF_8));
            String satir;
            boolean parentDisiVersionAraniyor = false;
            while ((satir = reader.readLine()) != null) {
                String temiz = satir.trim();
                if (temiz.startsWith("<artifactId>futbol-analiz-agent</artifactId>")) {
                    parentDisiVersionAraniyor = true;
                    continue;
                }
                if (parentDisiVersionAraniyor && temiz.startsWith("<version>")) {
                    reader.close();
                    return temiz.replace("<version>", "").replace("</version>", "").trim();
                }
            }
            reader.close();
        } catch (IOException ignored) {
        }
        return "bilinmiyor";
    }

    private String ozetOlustur(GitDurumu gitDurumu) {
        StringBuilder ozet = new StringBuilder();
        ozet.append("# Codex Degisiklik Ozeti\n\n");
        ozet.append("- Zaman: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        ozet.append("- Branch: ").append(bosIse(gitDurumu.branch, "bilinmiyor")).append("\n");
        ozet.append("- HEAD: ").append(bosIse(gitDurumu.head, "bilinmiyor")).append("\n");
        ozet.append("- Uygulama versiyonu: ").append(gitDurumu.version).append("\n\n");

        ozet.append("## Bakim Islemleri\n\n");
        if (islemler.isEmpty()) {
            ozet.append("- Temizlik veya paketleme islemi gerektiren dosya bulunmadi.\n");
        } else {
            for (String islem : islemler) {
                ozet.append("- ").append(islem).append("\n");
            }
        }

        ozet.append("\n## Paket Durumu\n\n");
        paketDurumunuEkle(ozet);

        ozet.append("\n## Commit Bekleyen Degisiklikler\n\n");
        if (gitDurumu.degisiklikler.isEmpty()) {
            ozet.append("- Bekleyen degisiklik yok. Ozet dosyasi commit sonrasi temizlenir.\n");
        } else {
            for (String degisiklik : gitDurumu.degisiklikler) {
                ozet.append("- `").append(degisiklik).append("`\n");
            }
        }

        ozet.append("\n## Codex Notu\n\n");
        ozet.append("- Bu agent OpenAI/API cagrisi yapmaz; Codex'in yerel token butcesiyle tetiklenir.\n");
        ozet.append("- Her Codex degisikliginden sonra `./codex-bakim.ps1` calistirilmalidir.\n");
        return ozet.toString();
    }

    private void paketDurumunuEkle(StringBuilder ozet) {
        File kok = new File(projeKoku, KAYNAK_KOKU);
        File[] paketler = kok.listFiles();
        if (paketler == null) {
            ozet.append("- Kaynak paket koku bulunamadi.\n");
            return;
        }
        for (File paket : paketler) {
            if (paket.isDirectory()) {
                ozet.append("- `").append(paket.getName()).append("`: ")
                        .append(javaDosyasiSayisi(paket))
                        .append(" Java dosyasi\n");
            }
        }
    }

    private int javaDosyasiSayisi(File klasor) {
        File[] dosyalar = klasor.listFiles();
        if (dosyalar == null) {
            return 0;
        }
        int sayac = 0;
        for (File dosya : dosyalar) {
            if (dosya.isDirectory()) {
                sayac += javaDosyasiSayisi(dosya);
            } else if (dosya.getName().endsWith(".java")) {
                sayac++;
            }
        }
        return sayac;
    }

    private String komutCiktisi(String... komut) {
        try {
            ProcessBuilder builder = new ProcessBuilder(komut);
            builder.directory(projeKoku);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sonuc = new StringBuilder();
            String satir;
            while ((satir = reader.readLine()) != null) {
                sonuc.append(satir).append("\n");
            }
            process.waitFor();
            return sonuc.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Komut calistirilamadi: " + birlestir(komut), e);
        }
    }

    private void dosyayaYaz(File dosya, String icerik) {
        try {
            File parent = dosya.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Klasor olusturulamadi: " + parent.getAbsolutePath());
            }
            FileWriter writer = new FileWriter(dosya, false);
            writer.write(icerik == null ? "" : icerik);
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Ozet dosyasi yazilamadi: " + dosya.getPath(), e);
        }
    }

    private String mesajOlustur() {
        StringBuilder mesaj = new StringBuilder();
        for (String islem : islemler) {
            if (mesaj.length() > 0) {
                mesaj.append(" ");
            }
            mesaj.append(islem);
        }
        return mesaj.length() == 0 ? "Bakim kontrolu tamamlandi." : mesaj.toString();
    }

    private String goreliYol(File dosya) {
        String kok = projeKoku.getAbsoluteFile().toURI().normalize().getPath();
        String yol = dosya.getAbsoluteFile().toURI().normalize().getPath();
        if (yol.startsWith(kok)) {
            return yol.substring(kok.length()).replace("\\", "/");
        }
        return dosya.getPath().replace("\\", "/");
    }

    private String bosIse(String deger, String varsayilan) {
        return deger == null || deger.trim().isEmpty() ? varsayilan : deger.trim();
    }

    private String birlestir(String[] komut) {
        StringBuilder metin = new StringBuilder();
        for (String parca : komut) {
            if (metin.length() > 0) {
                metin.append(" ");
            }
            metin.append(parca);
        }
        return metin.toString();
    }

    private static class GitDurumu {
        private String branch = "";
        private String head = "";
        private String version = "";
        private final List<String> degisiklikler = new ArrayList<String>();

        private boolean degisiklikVarMi() {
            return !degisiklikler.isEmpty();
        }
    }
}
