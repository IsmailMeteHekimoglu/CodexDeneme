package com.futbolanaliz.modeller;

public class TopLig {
    private final int iddaaLigId;
    private final String ulkeKodu;
    private final String ligAdi;
    private final int oncelikSirasi;
    private final String kategori;

    public TopLig(int iddaaLigId, String ulkeKodu, String ligAdi, int oncelikSirasi, String kategori) {
        this.iddaaLigId = iddaaLigId;
        this.ulkeKodu = ulkeKodu;
        this.ligAdi = ligAdi;
        this.oncelikSirasi = oncelikSirasi;
        this.kategori = kategori;
    }

    public int getIddaaLigId() {
        return iddaaLigId;
    }

    public String getUlkeKodu() {
        return ulkeKodu;
    }

    public String getLigAdi() {
        return ligAdi;
    }

    public int getOncelikSirasi() {
        return oncelikSirasi;
    }

    public String getKategori() {
        return kategori;
    }
}
