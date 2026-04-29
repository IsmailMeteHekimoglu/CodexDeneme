package com.futbolanaliz.uygulama;

import com.futbolanaliz.agentlar.BahisOneriAgent;
import com.futbolanaliz.agentlar.KadroDurumuAgent;
import com.futbolanaliz.agentlar.MacVeOranToplayiciAgent;
import com.futbolanaliz.agentlar.MacYorumAnalizAgent;
import com.futbolanaliz.agentlar.OranRiskAnalizAgent;
import com.futbolanaliz.agentlar.TakimGucuAnalizAgent;
import com.futbolanaliz.agentlar.TopLigKontrolAgent;
import com.futbolanaliz.modeller.BahisOnerisi;
import com.futbolanaliz.modeller.BahisTuru;
import com.futbolanaliz.modeller.KadroDurumuAnalizi;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.MacSonuAnalizi;
import com.futbolanaliz.modeller.MacYorumu;
import com.futbolanaliz.modeller.Oran;
import com.futbolanaliz.modeller.OranRiskAnalizi;
import com.futbolanaliz.modeller.TakimGucuAnalizi;
import com.futbolanaliz.modeller.TopLig;
import com.futbolanaliz.servisler.AyarServisi;
import com.futbolanaliz.servisler.MacYorumServisi;
import com.futbolanaliz.servisler.TokenTahminServisi;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArayuzUygulamasi extends JFrame {
    private static final DateTimeFormatter TARIH_FORMATI = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter SAAT_FORMATI = DateTimeFormatter.ofPattern("HH:mm");
    private static final Color YESIL = new Color(0, 134, 65);
    private static final Color KOYU_YESIL = new Color(8, 73, 52);
    private static final Color MAVI = new Color(31, 118, 255);
    private static final Color SARI = new Color(255, 185, 65);
    private static final Color KIRMIZI = new Color(238, 73, 73);
    private static final Color ZEMIN = new Color(243, 248, 246);
    private static final Locale TURKCE = new Locale("tr", "TR");

    private final DefaultListModel<Mac> macListeModeli = new DefaultListModel<Mac>();
    private final JList<Mac> macListesi = new JList<Mac>(macListeModeli);
    private final JPanel oranPaneli = new JPanel();
    private final JLabel baslikEtiketi = new JLabel("Bugünün maçları yükleniyor...");
    private final JLabel durumEtiketi = new JLabel("Veriler hazırlanıyor");
    private final JLabel seciliMacEtiketi = new JLabel("Maç seç");
    private final JLabel ligSaatEtiketi = new JLabel("Soldaki listeden bir maç seç.");
    private final JButton yenileButonu = new RenkliButon("Verileri Yenile", Color.WHITE, YESIL);
    private final JButton ayarlarButonu = new RenkliButon("Ayarlar", Color.WHITE, MAVI);
    private final JButton analizButonu = new RenkliButon("Analiz Et", YESIL, Color.WHITE);
    private final JTextPane sonucAlani = new JTextPane();
    private final AnimasyonluBaslikPanel baslikPaneli = new AnimasyonluBaslikPanel();
    private final RiskCemberi riskCemberi = new RiskCemberi();
    private final AyarServisi ayarServisi = new AyarServisi();
    private final TokenTahminServisi tokenTahminServisi = new TokenTahminServisi();
    private final MacYorumServisi macYorumServisi = new MacYorumServisi();

    private List<TopLig> kontrolEdilenLigler = new ArrayList<TopLig>();
    private List<Mac> maclar = new ArrayList<Mac>();
    private Mac seciliMac;
    private int sonCalismaTahminiTokenSayisi;

    public ArayuzUygulamasi() {
        super("Futbol Analiz Agent");
        pencereyiHazirla();
        arayuzuKur();
        olaylariBagla();
        maclariYukle();
    }

    public static void baslat() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }

                new ArayuzUygulamasi().setVisible(true);
            }
        });
    }

    private void pencereyiHazirla() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(new Dimension(1180, 760));
        setLocationRelativeTo(null);
        setIcon();
    }

    private void arayuzuKur() {
        JPanel kok = new JPanel(new BorderLayout(18, 18));
        kok.setBackground(ZEMIN);
        kok.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setContentPane(kok);

        baslikPaneli.setLayout(new BorderLayout(14, 6));
        baslikPaneli.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        JLabel uygulamaAdi = new JLabel("Futbol Analiz Agent");
        uygulamaAdi.setForeground(Color.WHITE);
        uygulamaAdi.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslikEtiketi.setForeground(new Color(225, 255, 238));
        baslikEtiketi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JPanel baslikMetinleri = seffafPanel(new BorderLayout());
        baslikMetinleri.add(uygulamaAdi, BorderLayout.NORTH);
        baslikMetinleri.add(baslikEtiketi, BorderLayout.SOUTH);
        JPanel baslikButonlari = seffafPanel(new GridLayout(1, 2, 10, 0));
        baslikButonlari.add(stilliButon(ayarlarButonu));
        baslikButonlari.add(stilliButon(yenileButonu));
        baslikPaneli.add(baslikMetinleri, BorderLayout.WEST);
        baslikPaneli.add(baslikButonlari, BorderLayout.EAST);
        kok.add(baslikPaneli, BorderLayout.NORTH);

        macListesi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        macListesi.setCellRenderer(new MacKartRenderer());
        macListesi.setFixedCellHeight(86);
        JScrollPane listeScroll = new JScrollPane(macListesi);
        listeScroll.setBorder(null);

        JPanel listePaneli = kartPanel(new BorderLayout(0, 12));
        JLabel listeBaslik = baslik("Maç Listesi");
        durumEtiketi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        durumEtiketi.setForeground(new Color(88, 105, 100));
        JPanel listeUst = new JPanel(new BorderLayout());
        listeUst.setOpaque(false);
        listeUst.add(listeBaslik, BorderLayout.WEST);
        listeUst.add(durumEtiketi, BorderLayout.EAST);
        listePaneli.add(listeUst, BorderLayout.NORTH);
        listePaneli.add(listeScroll, BorderLayout.CENTER);

        JPanel detayPaneli = kartPanel(new BorderLayout(0, 14));
        detayPaneli.add(detayUstPaneli(), BorderLayout.NORTH);
        JSplitPane oranAnalizBolumu = new JSplitPane(JSplitPane.VERTICAL_SPLIT, oranlarPaneli(), analizBolumu());
        oranAnalizBolumu.setResizeWeight(0.36);
        oranAnalizBolumu.setDividerSize(8);
        oranAnalizBolumu.setBorder(null);
        detayPaneli.add(oranAnalizBolumu, BorderLayout.CENTER);

        JSplitPane govde = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listePaneli, detayPaneli);
        govde.setResizeWeight(0.38);
        govde.setDividerSize(8);
        govde.setBorder(null);
        kok.add(govde, BorderLayout.CENTER);

        Timer timer = new Timer(45, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                baslikPaneli.animasyonuIlerlet();
            }
        });
        timer.start();
    }

    private JPanel detayUstPaneli() {
        JPanel panel = seffafPanel(new BorderLayout(10, 8));
        JLabel detayBaslik = baslik("Seçili Maç Detayı");
        seciliMacEtiketi.setFont(new Font("Segoe UI", Font.BOLD, 24));
        seciliMacEtiketi.setForeground(new Color(28, 42, 38));
        ligSaatEtiketi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ligSaatEtiketi.setForeground(new Color(88, 105, 100));
        JPanel metinler = seffafPanel(new GridLayout(0, 1, 0, 4));
        metinler.add(detayBaslik);
        metinler.add(seciliMacEtiketi);
        metinler.add(ligSaatEtiketi);
        panel.add(metinler, BorderLayout.CENTER);
        panel.add(riskCemberi, BorderLayout.EAST);
        return panel;
    }

    private JPanel oranlarPaneli() {
        JPanel panel = seffafPanel(new BorderLayout(0, 10));
        panel.add(baslik("Oranlar"), BorderLayout.NORTH);
        oranPaneli.setLayout(new BorderLayout(0, 10));
        oranPaneli.setOpaque(false);
        JScrollPane oranScroll = new JScrollPane(oranPaneli);
        oranScroll.setBorder(BorderFactory.createEmptyBorder());
        oranScroll.setOpaque(false);
        oranScroll.getViewport().setOpaque(false);
        oranScroll.setMinimumSize(new Dimension(360, 120));
        oranScroll.setPreferredSize(new Dimension(420, 170));
        panel.add(oranScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel analizBolumu() {
        JPanel panel = seffafPanel(new BorderLayout(0, 12));
        panel.add(stilliButon(analizButonu), BorderLayout.NORTH);
        sonucAlani.setEditable(false);
        sonucAlani.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sonucAlani.setForeground(new Color(31, 46, 42));
        sonucAlani.setBackground(new Color(250, 252, 251));
        sonucAlani.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JScrollPane sonucScroll = new JScrollPane(sonucAlani);
        sonucScroll.setBorder(BorderFactory.createLineBorder(new Color(224, 234, 230)));
        sonucScroll.setMinimumSize(new Dimension(360, 180));
        sonucScroll.setPreferredSize(new Dimension(420, 360));
        panel.add(sonucScroll, BorderLayout.CENTER);
        return panel;
    }

    private void olaylariBagla() {
        yenileButonu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maclariYukle();
            }
        });

        ayarlarButonu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ayarlariGoster();
            }
        });

        macListesi.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seciliMac = macListesi.getSelectedValue();
                seciliMaciGoster();
            }
        });

        analizButonu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seciliMaciAnalizEt();
            }
        });
    }

    private void ayarlariGoster() {
        AyarServisi.LlmAyarlari ayarlar = ayarServisi.ayarlariOku();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPasswordField apiKeyAlani = new JPasswordField(ayarlar.getApiKey(), 28);
        JCheckBox apiKeyGosterKutusu = new JCheckBox("API keyleri gorunur ve kopyalanabilir yap", ayarServisi.apiKeyleriGoster());
        apiKeyAlanlariniAyarla(apiKeyGosterKutusu.isSelected(), apiKeyAlani);
        apiKeyGosterKutusu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                apiKeyAlanlariniAyarla(apiKeyGosterKutusu.isSelected(), apiKeyAlani);
            }
        });
        JComboBox<String> modelAlani = new JComboBox<String>(ayarServisi.kullanilabilirModeller().toArray(new String[0]));
        modelAlani.setEditable(false);
        modelAlani.setSelectedItem(ayarlar.getModel());
        JCheckBox llmAktifKutusu = new JCheckBox("LLM analizini kullan", ayarlar.isLlmAktif());
        JCheckBox cacheAktifKutusu = new JCheckBox("Ayni analizleri cache ile tekrar kullan", ayarlar.isCacheAktif());

        ayarSatiri(panel, gbc, 0, "OpenAI API Key", apiKeyAlani);
        ayarSatiri(panel, gbc, 1, "Model", modelAlani);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(apiKeyGosterKutusu, gbc);
        gbc.gridy = 3;
        panel.add(llmAktifKutusu, gbc);
        gbc.gridy = 4;
        panel.add(cacheAktifKutusu, gbc);

        int sonuc = JOptionPane.showConfirmDialog(this, panel, "LLM Ayarlari", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (sonuc != JOptionPane.OK_OPTION) {
            return;
        }

        ayarServisi.ayarlariKaydet(new AyarServisi.LlmAyarlari(
                new String(apiKeyAlani.getPassword()).trim(),
                String.valueOf(modelAlani.getSelectedItem()),
                llmAktifKutusu.isSelected(),
                cacheAktifKutusu.isSelected()
        ));
        ayarServisi.apiKeyGorunumunuKaydet(apiKeyGosterKutusu.isSelected());
        baslikEtiketi.setText(llmDurumMetni());
    }

    private void apiKeyAlanlariniAyarla(boolean goster, JPasswordField... alanlar) {
        char echo = goster ? (char) 0 : '\u2022';
        for (JPasswordField alan : alanlar) {
            alan.setEchoChar(echo);
            alan.putClientProperty("JPasswordField.cutCopyAllowed", Boolean.valueOf(goster));
        }
    }

    private void ayarSatiri(JPanel panel, GridBagConstraints gbc, int satir, String etiket, Component alan) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = satir;
        gbc.weightx = 0.0;
        panel.add(new JLabel(etiket), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(alan, gbc);
    }

    private String llmDurumMetni() {
        AyarServisi.LlmAyarlari ayarlar = ayarServisi.ayarlariOku();
        if (!ayarlar.isLlmAktif()) {
            return "LLM kapali; kural tabanli analiz kullaniliyor.";
        }
        if (!ayarlar.apiKeyVarMi()) {
            return "LLM icin API key bekleniyor; kural tabanli analiz kullaniliyor.";
        }
        return "LLM aktif: " + ayarlar.getModel() + (ayarlar.isCacheAktif() ? " | cache acik" : " | cache kapali");
    }

    private void maclariYukle() {
        final int baslangicTokenTahmini = tokenTahminServisi.tahminEt("arayuz calisma baslangici", LocalDate.now().format(TARIH_FORMATI));
        sonCalismaTahminiTokenSayisi = baslangicTokenTahmini;
        durumEtiketi.setText("Yükleniyor...");
        baslikEtiketi.setText(LocalDate.now().format(TARIH_FORMATI) + " için iddaa.com verileri alınıyor");
        yenileButonu.setEnabled(false);
        analizButonu.setEnabled(false);
        sonucAlani.setText("");
        macListeModeli.clear();
        oranPaneli.removeAll();
        riskCemberi.setDeger(0, new Color(185, 198, 194));

        new SwingWorker<List<Mac>, Void>() {
            @Override
            protected List<Mac> doInBackground() {
                TopLigKontrolAgent topLigKontrolAgent = new TopLigKontrolAgent();
                sonCalismaTahminiTokenSayisi += topLigKontrolAgent.calistir().getTahminiTokenSayisi();
                kontrolEdilenLigler = topLigKontrolAgent.getKontrolEdilenLigler();

                MacVeOranToplayiciAgent macAgent = new MacVeOranToplayiciAgent(kontrolEdilenLigler);
                sonCalismaTahminiTokenSayisi += macAgent.calistir().getTahminiTokenSayisi();
                return macAgent.getToplananMaclar();
            }

            @Override
            protected void done() {
                try {
                    maclar = get();
                    for (Mac mac : maclar) {
                        macListeModeli.addElement(mac);
                    }
                    durumEtiketi.setText(maclar.size() + " maç bulundu");
                    baslikEtiketi.setText("Bugünün maçları: " + LocalDate.now().format(TARIH_FORMATI));
                    if (!maclar.isEmpty()) {
                        macListesi.setSelectedIndex(0);
                    } else {
                        seciliMacEtiketi.setText("Maç bulunamadı");
                        ligSaatEtiketi.setText("Öncelikli lig/kupa filtresine uyan maç yok.");
                    }
                } catch (Exception ex) {
                    durumEtiketi.setText("Veri alınamadı");
                    sonucAlani.setText("Veri yüklenirken hata oluştu:\n" + ex.getMessage());
                } finally {
                    yenileButonu.setEnabled(true);
                    analizButonu.setEnabled(seciliMac != null);
                }
            }
        }.execute();
    }

    private void seciliMaciGoster() {
        oranPaneli.removeAll();
        sonucAlani.setText("");
        riskCemberi.setDeger(0, new Color(185, 198, 194));

        if (seciliMac == null) {
            seciliMacEtiketi.setText("Maç seç");
            ligSaatEtiketi.setText("Soldaki listeden bir maç seç.");
            analizButonu.setEnabled(false);
            return;
        }

        seciliMacEtiketi.setText(seciliMac.getKarsilasmaAdi());
        ligSaatEtiketi.setText(seciliMac.getLig() + " | " + seciliMac.getSaat().format(SAAT_FORMATI)
                + " | " + seciliMac.getDurumMetni()
                + " | Skor: " + seciliMac.getSkorMetni());
        analizButonu.setEnabled(true);

        JPanel macSonuOranSatiri = seffafPanel(new GridLayout(1, 3, 10, 0));
        int macSonuOranSayisi = 0;
        for (Oran oran : seciliMac.getOranlar()) {
            if (macSonuOraniMi(oran.getBahisTuru())) {
                macSonuOranSatiri.add(oranKarti(oran));
                macSonuOranSayisi++;
            }
        }
        if (macSonuOranSayisi == 0) {
            macSonuOranSatiri.add(new JLabel("1/X/2 orani bulunamadi."));
        }
        while (macSonuOranSayisi > 0 && macSonuOranSayisi < 3) {
            macSonuOranSatiri.add(new JLabel(""));
            macSonuOranSayisi++;
        }
        oranPaneli.add(macSonuOranSatiri, BorderLayout.CENTER);

        JButton detayButonu = stilliButon(new RenkliButon("Detay Oranlar", Color.WHITE, MAVI));
        detayButonu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                detayOranlariGoster();
            }
        });
        oranPaneli.add(detayButonu, BorderLayout.SOUTH);

        oranPaneli.revalidate();
        oranPaneli.repaint();
    }

    private void seciliMaciAnalizEt() {
        if (seciliMac == null) {
            return;
        }

        analizButonu.setEnabled(false);
        sonucAlani.setText("Analiz oncesi veri kontrolu yapiliyor...\nLLM henuz calistirilmadi.");
        riskCemberi.setDeger(15, SARI);
        final Mac kontrolEdilecekMac = seciliMac;

        new SwingWorker<AnalizOnKontrol, Void>() {
            @Override
            protected AnalizOnKontrol doInBackground() {
                List<MacYorumu> yorumlar = macYorumServisi.yorumlariGetir(kontrolEdilecekMac);
                return new AnalizOnKontrol(kontrolEdilecekMac, yorumlar);
            }

            @Override
            protected void done() {
                try {
                    AnalizOnKontrol kontrol = get();
                    int secim = JOptionPane.showOptionDialog(
                            ArayuzUygulamasi.this,
                            analizOnKontrolPaneli(kontrol),
                            "Analiz Verileri Onayi",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            new Object[]{"Onayla", "Reddet"},
                            "Onayla"
                    );
                    if (secim == JOptionPane.YES_OPTION) {
                        analiziBaslat(kontrol);
                    } else {
                        sonucAlani.setText("Analiz baslatilmadi. LLM istegi yapilmadi.");
                        riskCemberi.setDeger(0, new Color(185, 198, 194));
                        analizButonu.setEnabled(true);
                    }
                } catch (Exception ex) {
                    sonucAlani.setText("Veri kontrolu sirasinda hata olustu:\n" + ex.getMessage());
                    riskCemberi.setDeger(0, KIRMIZI);
                    analizButonu.setEnabled(true);
                }
            }
        }.execute();
    }

    private void analiziBaslat(AnalizOnKontrol kontrol) {
        sonucAlani.setText("Analiz calisiyor...\nYorumlar, takim gucu, kadro riski ve oran dengesi inceleniyor.");
        riskCemberi.setDeger(35, SARI);
        final List<Mac> tekMac = new ArrayList<Mac>();
        tekMac.add(kontrol.mac);

        new SwingWorker<AnalizEkranSonucu, Void>() {
            @Override
            protected AnalizEkranSonucu doInBackground() {
                int tahminiTokenSayisi = 0;
                MacYorumAnalizAgent yorumAgent = new MacYorumAnalizAgent(tekMac, kontrol.yorumlar);
                tahminiTokenSayisi += yorumAgent.calistir().getTahminiTokenSayisi();
                List<MacSonuAnalizi> yorumAnalizleri = yorumAgent.getAnalizler();

                TakimGucuAnalizAgent takimAgent = new TakimGucuAnalizAgent(tekMac);
                tahminiTokenSayisi += takimAgent.calistir().getTahminiTokenSayisi();
                List<TakimGucuAnalizi> takimAnalizleri = takimAgent.getAnalizler();

                KadroDurumuAgent kadroAgent = new KadroDurumuAgent(tekMac, yorumAnalizleri);
                tahminiTokenSayisi += kadroAgent.calistir().getTahminiTokenSayisi();
                List<KadroDurumuAnalizi> kadroAnalizleri = kadroAgent.getAnalizler();

                OranRiskAnalizAgent riskAgent = new OranRiskAnalizAgent(tekMac, yorumAnalizleri, takimAnalizleri, kadroAnalizleri);
                tahminiTokenSayisi += riskAgent.calistir().getTahminiTokenSayisi();
                List<OranRiskAnalizi> riskAnalizleri = riskAgent.getAnalizler();

                BahisOneriAgent oneriAgent = new BahisOneriAgent(riskAnalizleri);
                tahminiTokenSayisi += oneriAgent.calistir().getTahminiTokenSayisi();
                tahminiTokenSayisi += tokenTahminServisi.tahminEt(tekMac)
                        + tokenTahminServisi.tahminEt(yorumAnalizleri)
                        + tokenTahminServisi.tahminEt(takimAnalizleri)
                        + tokenTahminServisi.tahminEt(kadroAnalizleri)
                        + tokenTahminServisi.tahminEt(riskAnalizleri)
                        + tokenTahminServisi.tahminEt(oneriAgent.getOneriler());
                return new AnalizEkranSonucu(yorumAnalizleri, takimAnalizleri, kadroAnalizleri, riskAnalizleri, oneriAgent.getOneriler(), tahminiTokenSayisi);
            }

            @Override
            protected void done() {
                try {
                    AnalizEkranSonucu sonuc = get();
                    analizSonucunuGoster(sonuc);
                } catch (Exception ex) {
                    sonucAlani.setText("Analiz sırasında hata oluştu:\n" + ex.getMessage());
                    riskCemberi.setDeger(0, KIRMIZI);
                } finally {
                    analizButonu.setEnabled(true);
                }
            }
        }.execute();
    }

    private JPanel analizOnKontrolPaneli(AnalizOnKontrol kontrol) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(780, 540));

        JPanel ust = new JPanel(new BorderLayout(12, 4));
        ust.setOpaque(true);
        ust.setBackground(new Color(232, 245, 239));
        ust.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(183, 215, 201), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel baslik = new JLabel(kontrol.mac.getKarsilasmaAdi());
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 18));
        baslik.setForeground(new Color(18, 74, 54));
        JLabel altBilgi = new JLabel(kontrol.mac.getLig() + " | " + kontrol.mac.getDurumMetni() + " | " + kontrol.mac.getSkorMetni());
        altBilgi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        altBilgi.setForeground(new Color(62, 83, 76));
        ust.add(baslik, BorderLayout.NORTH);
        ust.add(altBilgi, BorderLayout.SOUTH);

        JTabbedPane sekmeler = new JTabbedPane();
        sekmeler.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sekmeler.addTab("Mac / Oran", metinSekmesi(onKontrolMacMetni(kontrol)));
        sekmeler.addTab("iddaa Istatistik", iddaaIstatistikSekmesi(kontrol.yorumlar));
        sekmeler.addTab("Yorumlar", metinSekmesi(iddaaYorumPopupMetni(kontrol.yorumlar)));
        sekmeler.addTab("LLM", metinSekmesi(llmOnKontrolMetni()));

        panel.add(ust, BorderLayout.NORTH);
        panel.add(sekmeler, BorderLayout.CENTER);
        return panel;
    }

    private String analizOnKontrolMetni(AnalizOnKontrol kontrol) {
        StringBuilder metin = new StringBuilder();
        metin.append("Mac: ").append(kontrol.mac.getKarsilasmaAdi()).append("\n");
        metin.append("Lig: ").append(kontrol.mac.getLig()).append("\n\n");
        metin.append("1) iddaa mac ve oran verisi\n");
        metin.append("Durum: ").append(kontrol.mac.getDurumMetni()).append(" | Skor: ").append(kontrol.mac.getSkorMetni()).append("\n");
        metin.append(oranFavorisiMetni(kontrol.mac)).append("\n\n");
        if (!kontrol.mac.getMacOncesiTahmin().isEmpty()) {
            metin.append("Mac oncesi oran tahmini: ").append(kontrol.mac.getMacOncesiTahmin()).append("\n");
        }
        metin.append("Detay oran sayisi: ").append(detayOranSayisi(kontrol.mac)).append("\n\n");
        metin.append("2) iddaa istatistik verisi\n");
        metin.append(iddaaIstatistikPopupMetni(kontrol.yorumlar)).append("\n\n");
        metin.append("3) iddaa/Broadage yorum verisi\n");
        metin.append(iddaaYorumSayisi(kontrol.yorumlar)).append(" yorum/haber metni bulundu.\n\n");
        metin.append("4) LLM kullanimi\n");
        AyarServisi.LlmAyarlari ayarlar = ayarServisi.ayarlariOku();
        if (!ayarlar.isLlmAktif()) {
            metin.append("LLM kapali. Onaylasaniz da kural tabanli analiz calisir.\n");
        } else if (!ayarlar.apiKeyVarMi()) {
            metin.append("OpenAI API key yok. Onaylasaniz da LLM istegi yapilamaz.\n");
        } else {
            metin.append("OpenAI modeli: ").append(ayarlar.getModel()).append(". Onaylanirsa LLM istegi yapilir.\n");
        }
        return metin.toString();
    }

    private JScrollPane metinSekmesi(String icerik) {
        JTextPane metin = new JTextPane();
        metin.setEditable(false);
        metin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        metin.setForeground(new Color(31, 46, 42));
        metin.setBackground(new Color(252, 254, 253));
        metin.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        metin.setText(icerik == null ? "" : icerik);
        metin.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(metin);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(213, 228, 221), 1));
        return scroll;
    }

    private JScrollPane iddaaIstatistikSekmesi(List<MacYorumu> yorumlar) {
        JPanel govde = new JPanel();
        govde.setLayout(new BoxLayout(govde, BoxLayout.Y_AXIS));
        govde.setBackground(new Color(252, 254, 253));
        govde.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String metin = iddaaIstatistikPopupMetni(yorumlar);
        if ("Veri bulunamadi.".equals(metin)) {
            govde.add(istatistikKarti("Veri durumu", "iddaa.com istatistik verisi bulunamadi."));
        } else {
            istatistikKartlariniEkle(govde, metin);
        }

        JScrollPane scroll = new JScrollPane(govde);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(213, 228, 221), 1));
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private void istatistikKartlariniEkle(JPanel govde, String metin) {
        List<String> genel = new ArrayList<String>();
        String baslik = "Mac bilgisi";
        List<String> satirlar = new ArrayList<String>();

        String[] bolunmus = metin.split("\\r?\\n");
        for (String hamSatir : bolunmus) {
            String satir = hamSatir == null ? "" : hamSatir.trim();
            if (satir.isEmpty()) {
                continue;
            }

            if (satir.endsWith(":")) {
                if (!satirlar.isEmpty() || !genel.isEmpty()) {
                    govde.add(istatistikKarti(baslik, kartMetni(genel, satirlar)));
                    govde.add(Box.createVerticalStrut(10));
                    genel.clear();
                    satirlar.clear();
                }
                baslik = satir.substring(0, satir.length() - 1);
            } else if (satir.startsWith("- ")) {
                satirlar.add(satir.substring(2));
            } else {
                genel.add(satir);
            }
        }

        if (!satirlar.isEmpty() || !genel.isEmpty()) {
            govde.add(istatistikKarti(baslik, kartMetni(genel, satirlar)));
        }
    }

    private String kartMetni(List<String> genel, List<String> satirlar) {
        StringBuilder sonuc = new StringBuilder();
        for (String satir : genel) {
            if (sonuc.length() > 0) {
                sonuc.append("\n");
            }
            sonuc.append(satir);
        }
        for (String satir : satirlar) {
            if (sonuc.length() > 0) {
                sonuc.append("\n");
            }
            sonuc.append("- ").append(satir);
        }
        return sonuc.toString();
    }

    private JPanel istatistikKarti(String baslik, String icerik) {
        JPanel kart = new JPanel(new BorderLayout(0, 8));
        kart.setOpaque(true);
        kart.setBackground(new Color(248, 252, 250));
        kart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(198, 224, 212), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        kart.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel baslikEtiketi = new JLabel(baslik);
        baslikEtiketi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        baslikEtiketi.setForeground(new Color(14, 97, 67));
        kart.add(baslikEtiketi, BorderLayout.NORTH);

        JPanel satirPaneli = new JPanel();
        satirPaneli.setOpaque(false);
        satirPaneli.setLayout(new BoxLayout(satirPaneli, BoxLayout.Y_AXIS));
        String[] satirlar = (icerik == null ? "" : icerik).split("\\r?\\n");
        for (String hamSatir : satirlar) {
            String satir = hamSatir.trim();
            if (satir.isEmpty()) {
                continue;
            }
            satirPaneli.add(istatistikSatirBileseni(satir));
            satirPaneli.add(Box.createVerticalStrut(4));
        }
        kart.add(satirPaneli, BorderLayout.CENTER);
        return kart;
    }

    private Component istatistikSatirBileseni(String satir) {
        boolean ozet = satir.startsWith("- Ozet:");
        boolean macSatiri = satir.startsWith("- ") && satir.contains(" - ") && satir.matches(".*\\d+-\\d+.*");
        if (macSatiri && !ozet) {
            return istatistikMacSatiri(satir.substring(2));
        }
        return istatistikMetinSatiri(satir, ozet);
    }

    private JLabel istatistikMetinSatiri(String satir, boolean ozet) {
        boolean veriDurumu = satir.toLowerCase(TURKCE).contains("veri") || satir.toLowerCase(TURKCE).contains("bulunamadi");
        Color renk = ozet ? new Color(18, 74, 54) : (veriDurumu ? new Color(122, 82, 22) : new Color(37, 52, 47));
        String yazi = satir.startsWith("- ") ? "• " + satir.substring(2) : satir;
        String html = "<html><div style='width:680px;font-family:Segoe UI;font-size:12px;color:"
                + renkHex(renk)
                + ";'>"
                + (ozet ? "<b>" : "")
                + htmlKacis(yazi)
                + (ozet ? "</b>" : "")
                + "</div></html>";
        JLabel label = new JLabel(html);
        label.setOpaque(ozet);
        if (ozet) {
            label.setBackground(new Color(232, 245, 239));
            label.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
        }
        return label;
    }

    private JPanel istatistikMacSatiri(String satir) {
        JPanel panel = new JPanel(new BorderLayout(10, 4));
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 235, 229), 1),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        String[] parcalar = satir.split(" \\| ");
        String macBilgisi = parcalar.length > 0 ? parcalar[0].trim() : satir;
        String lig = parcalar.length > 1 ? parcalar[1].trim() : "";
        int ilkBosluk = macBilgisi.indexOf(' ');
        String tarih = ilkBosluk > 0 ? macBilgisi.substring(0, ilkBosluk).trim() : "";
        String mac = ilkBosluk > 0 ? macBilgisi.substring(ilkBosluk + 1).trim() : macBilgisi;

        JLabel tarihEtiketi = new JLabel(tarih.isEmpty() ? "Tarih yok" : tarih);
        tarihEtiketi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tarihEtiketi.setForeground(new Color(14, 97, 67));
        tarihEtiketi.setPreferredSize(new Dimension(92, 22));

        JLabel macEtiketi = new JLabel("<html><b>" + htmlKacis(mac) + "</b>"
                + (lig.isEmpty() ? "" : "<br><span style='color:#66736e'>" + htmlKacis(lig) + "</span>")
                + "</html>");
        macEtiketi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        macEtiketi.setForeground(new Color(31, 46, 42));

        panel.add(tarihEtiketi, BorderLayout.WEST);
        panel.add(macEtiketi, BorderLayout.CENTER);
        return panel;
    }

    private String renkHex(Color renk) {
        return String.format("#%02x%02x%02x", renk.getRed(), renk.getGreen(), renk.getBlue());
    }

    private String htmlKacis(String metin) {
        if (metin == null) {
            return "";
        }
        return metin.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String onKontrolMacMetni(AnalizOnKontrol kontrol) {
        StringBuilder metin = new StringBuilder();
        metin.append("Mac\n");
        metin.append("- Karsilasma: ").append(kontrol.mac.getKarsilasmaAdi()).append("\n");
        metin.append("- Lig: ").append(kontrol.mac.getLig()).append("\n");
        metin.append("- Saat: ").append(kontrol.mac.getSaat()).append("\n");
        metin.append("- Durum: ").append(kontrol.mac.getDurumMetni()).append("\n");
        metin.append("- Skor: ").append(kontrol.mac.getSkorMetni()).append("\n\n");
        metin.append("Oran favorisi\n");
        metin.append("- ").append(oranFavorisiMetni(kontrol.mac)).append("\n");
        if (!kontrol.mac.getMacOncesiTahmin().isEmpty()) {
            metin.append("- Mac oncesi oran tahmini: ").append(kontrol.mac.getMacOncesiTahmin()).append("\n");
        }
        metin.append("- Detay oran sayisi: ").append(detayOranSayisi(kontrol.mac)).append("\n");
        return metin.toString();
    }

    private String iddaaYorumPopupMetni(List<MacYorumu> yorumlar) {
        int sayi = iddaaYorumSayisi(yorumlar);
        if (sayi == 0) {
            return "Broadage/iddaa yorum verisi bulunamadi.";
        }

        StringBuilder metin = new StringBuilder();
        metin.append(sayi).append(" yorum/haber metni bulundu.\n\n");
        int sira = 1;
        for (MacYorumu yorum : yorumlar) {
            if ("iddaa.com istatistikleri".equalsIgnoreCase(yorum.getKaynak())) {
                continue;
            }
            metin.append(sira++).append(". ").append(yorum.getKaynak()).append(" / ").append(yorum.getYazar()).append("\n");
            metin.append(kisaltPopupMetni(yorum.getYorumMetni(), 900)).append("\n\n");
        }
        return metin.toString().trim();
    }

    private String llmOnKontrolMetni() {
        AyarServisi.LlmAyarlari ayarlar = ayarServisi.ayarlariOku();
        StringBuilder metin = new StringBuilder();
        metin.append("Analiz onayi\n");
        metin.append("- Onayla secilirse agent analizi baslar.\n");
        metin.append("- Reddet secilirse LLM istegi yapilmaz.\n\n");
        metin.append("LLM durumu\n");
        if (!ayarlar.isLlmAktif()) {
            metin.append("- LLM kapali. Onaylasaniz da kural tabanli analiz calisir.\n");
        } else if (!ayarlar.apiKeyVarMi()) {
            metin.append("- OpenAI API key yok. Onaylasaniz da LLM istegi yapilamaz.\n");
        } else {
            metin.append("- OpenAI modeli: ").append(ayarlar.getModel()).append("\n");
            metin.append("- Onaylanirsa LLM istegi yapilir.\n");
        }
        return metin.toString();
    }

    private void analizSonucunuGoster(AnalizEkranSonucu sonuc) {
        BahisOnerisi oneri = sonuc.oneriler.isEmpty() ? null : sonuc.oneriler.get(0);
        OranRiskAnalizi riskAnalizi = sonuc.riskAnalizleri.isEmpty() ? null : sonuc.riskAnalizleri.get(0);
        MacSonuAnalizi macSonu = sonuc.yorumAnalizleri.isEmpty() ? null : sonuc.yorumAnalizleri.get(0);

        sonucAlani.setText("");
        riskCemberi.setDeger(0, new Color(185, 198, 194));

        bolumEkle("ORAN FAVORISI", oranFavorisiMetni(macSonu));
        bolumEkle("IDDAA ISTATISTIKLERI", iddaaIstatistikMetni(macSonu));

        if (macSonu != null) {
            bolumEkle("AGENT YORUMU", macSonu.getGerekce());
        } else {
            bolumEkle("AGENT YORUMU", "Mac yorumu uretilemedi.");
        }

        if (oneri != null) {
            bolumEkle("ANALIZ SONUCU", oneri.getSecim() + " | Oran: " + oneri.formatliOranDegeri() + "\n" + oneri.getGerekce());
        } else if (riskAnalizi != null) {
            bolumEkle("ANALIZ SONUCU", "Final bahis onerisi uretilemedi. En yakin secenek: " + riskAnalizi.getSecim() + " | Oran: " + riskAnalizi.formatliOranDegeri());
        } else {
            bolumEkle("ANALIZ SONUCU", "Analiz sonucu uretilemedi.");
        }
    }

    private void bolumEkle(String baslik, String aciklama) {
        StyledDocument belge = sonucAlani.getStyledDocument();
        try {
            belge.insertString(belge.getLength(), baslik + "\n", baslikStili());
            belge.insertString(belge.getLength(), (aciklama == null ? "" : aciklama) + "\n\n", aciklamaStili());
        } catch (BadLocationException ignored) {
        }
    }

    private String oranFavorisiMetni(MacSonuAnalizi macSonu) {
        Mac mac = macSonu != null ? macSonu.getMac() : seciliMac;
        return oranFavorisiMetni(mac);
    }

    private String oranFavorisiMetni(Mac mac) {
        if (mac == null) {
            return "Mac secimi bulunamadi.";
        }

        Oran favori = null;
        for (Oran oran : mac.getOranlar()) {
            if (!macSonuOraniMi(oran.getBahisTuru())) {
                continue;
            }
            if (favori == null || oran.getDeger().compareTo(favori.getDeger()) < 0) {
                favori = oran;
            }
        }

        if (favori == null) {
            return "Mac sonucu 1/X/2 orani bulunamadi.";
        }

        return macSonuEtiketi(favori, mac) + " | " + favori.getGorunenAd() + " | Oran: " + favori.formatliDeger();
    }

    private int detayOranSayisi(Mac mac) {
        if (mac == null) {
            return 0;
        }
        int sayac = 0;
        for (Oran oran : mac.getOranlar()) {
            if (!macSonuOraniMi(oran.getBahisTuru())) {
                sayac++;
            }
        }
        return sayac;
    }

    private boolean macSonuOraniMi(BahisTuru bahisTuru) {
        return bahisTuru == BahisTuru.MAC_SONUCU_1
                || bahisTuru == BahisTuru.MAC_SONUCU_X
                || bahisTuru == BahisTuru.MAC_SONUCU_2;
    }

    private String macSonuEtiketi(Oran oran, Mac mac) {
        if (oran.getBahisTuru() == BahisTuru.MAC_SONUCU_1) {
            return "Favori: " + mac.getEvSahibi();
        }
        if (oran.getBahisTuru() == BahisTuru.MAC_SONUCU_2) {
            return "Favori: " + mac.getDeplasman();
        }
        return "Favori: Beraberlik";
    }

    private String iddaaIstatistikMetni(MacSonuAnalizi macSonu) {
        if (macSonu == null) {
            return "iddaa.com istatistik verisi bulunamadi.";
        }
        for (com.futbolanaliz.modeller.MacYorumu yorum : macSonu.getYorumlar()) {
            if ("iddaa.com istatistikleri".equalsIgnoreCase(yorum.getKaynak())) {
                return yorum.getYorumMetni();
            }
        }
        return "iddaa.com istatistik verisi bulunamadi.";
    }

    private String iddaaIstatistikPopupMetni(List<MacYorumu> yorumlar) {
        if (yorumlar == null || yorumlar.isEmpty()) {
            return "Veri bulunamadi.";
        }
        for (MacYorumu yorum : yorumlar) {
            if ("iddaa.com istatistikleri".equalsIgnoreCase(yorum.getKaynak())) {
                return satirliVeriMetni(yorum.getYorumMetni());
            }
        }
        return "Veri bulunamadi.";
    }

    private int iddaaYorumSayisi(List<MacYorumu> yorumlar) {
        if (yorumlar == null || yorumlar.isEmpty()) {
            return 0;
        }
        int sayac = 0;
        for (MacYorumu yorum : yorumlar) {
            if (!"iddaa.com istatistikleri".equalsIgnoreCase(yorum.getKaynak())) {
                sayac++;
            }
        }
        return sayac;
    }

    private String satirliVeriMetni(String kaynakMetin) {
        String metin = kaynakMetin == null ? "" : kaynakMetin.trim();
        if (metin.isEmpty()) {
            return "Veri bulunamadi.";
        }
        metin = metin.replace(" | ", "\n- ");
        metin = metin.replace("; ", "\n  - ");
        return metin;
    }

    private String kisaltPopupMetni(String metin, int limit) {
        if (metin == null) {
            return "";
        }
        String temiz = metin.trim();
        if (temiz.length() <= limit) {
            return temiz;
        }
        return temiz.substring(0, Math.max(0, limit - 3)) + "...";
    }

    private SimpleAttributeSet baslikStili() {
        SimpleAttributeSet stil = new SimpleAttributeSet();
        StyleConstants.setBold(stil, true);
        StyleConstants.setFontFamily(stil, "Segoe UI");
        StyleConstants.setFontSize(stil, 14);
        StyleConstants.setForeground(stil, new Color(18, 54, 43));
        return stil;
    }

    private SimpleAttributeSet aciklamaStili() {
        SimpleAttributeSet stil = new SimpleAttributeSet();
        StyleConstants.setBold(stil, false);
        StyleConstants.setFontFamily(stil, "Segoe UI");
        StyleConstants.setFontSize(stil, 14);
        StyleConstants.setForeground(stil, new Color(31, 46, 42));
        return stil;
    }

    private boolean llmKullanildiMi(BahisOnerisi oneri, MacSonuAnalizi macSonu) {
        return metinLlmMi(oneri == null ? null : oneri.getGerekce())
                || metinLlmMi(macSonu == null ? null : macSonu.getGerekce());
    }

    private boolean metinLlmMi(String metin) {
        return metin != null && metin.toLowerCase(TURKCE).contains("llm destekli");
    }

    private void detayOranlariGoster() {
        if (seciliMac == null) {
            return;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        boolean detayVar = false;
        for (Oran oran : seciliMac.getOranlar()) {
            if (!macSonuOraniMi(oran.getBahisTuru())) {
                panel.add(oranKarti(oran));
                panel.add(Box.createVerticalStrut(8));
                detayVar = true;
            }
        }

        if (!detayVar) {
            panel.add(new JLabel("1/X/2 disinda oran bulunamadi."));
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(560, 420));
        JOptionPane.showMessageDialog(this, scroll, "Detay Oranlar", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel oranKarti(Oran oran) {
        JPanel kart = new JPanel(new BorderLayout(12, 0));
        kart.setOpaque(true);
        kart.setBackground(new Color(248, 252, 250));
        kart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 226, 216), 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        kart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        kart.setPreferredSize(new Dimension(130, 54));

        JLabel ad = new JLabel(oran.getGorunenAd());
        ad.setOpaque(true);
        ad.setBackground(new Color(248, 252, 250));
        ad.setForeground(new Color(23, 44, 38));
        ad.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel deger = new JLabel(oran.formatliDeger());
        deger.setOpaque(true);
        deger.setBackground(YESIL);
        deger.setForeground(Color.WHITE);
        deger.setHorizontalAlignment(SwingConstants.CENTER);
        deger.setFont(new Font("Segoe UI", Font.BOLD, 18));
        deger.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        deger.setPreferredSize(new Dimension(82, 32));

        kart.add(ad, BorderLayout.CENTER);
        kart.add(deger, BorderLayout.EAST);
        return kart;
    }

    private JButton stilliButon(JButton buton) {
        buton.setFocusPainted(false);
        buton.setContentAreaFilled(false);
        buton.setOpaque(false);
        buton.setBorderPainted(false);
        buton.setBorder(BorderFactory.createEmptyBorder(13, 20, 13, 20));
        buton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        return buton;
    }

    private JLabel baslik(String metin) {
        JLabel label = new JLabel(metin);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(new Color(30, 48, 43));
        return label;
    }

    private JPanel kartPanel(BorderLayout layout) {
        JPanel panel = new YuvarlakPanel(layout, Color.WHITE, new Color(224, 235, 231), 24);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        return panel;
    }

    private JPanel seffafPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private Color riskRengi(int risk) {
        if (risk <= 35) {
            return YESIL;
        }
        if (risk <= 60) {
            return SARI;
        }
        return KIRMIZI;
    }

    private void setIcon() {
        try {
            File ikon = ikonDosyasiniBul();
            if (ikon.exists()) {
                Image image = ImageIO.read(ikon);
                setIconImage(image);
                List<Image> ikonlar = new ArrayList<Image>();
                ikonlar.add(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                ikonlar.add(image.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
                ikonlar.add(image.getScaledInstance(48, 48, Image.SCALE_SMOOTH));
                ikonlar.add(image.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                ikonlar.add(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
                ikonlar.add(image);
                setIconImages(ikonlar);
            }
        } catch (Exception ignored) {
        }
    }

    private File ikonDosyasiniBul() {
        String goreliYol = "varliklar/ikonlar/futbol-analiz-agent.png";
        List<File> kokler = new ArrayList<File>();
        kokler.add(new File(System.getProperty("user.dir")));

        try {
            File kodKonumu = new File(ArayuzUygulamasi.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File aday = kodKonumu.isFile() ? kodKonumu.getParentFile() : kodKonumu;
            for (int i = 0; i < 5 && aday != null; i++) {
                kokler.add(aday);
                aday = aday.getParentFile();
            }
        } catch (Exception ignored) {
        }

        for (File kok : kokler) {
            File ikon = new File(kok, goreliYol);
            if (ikon.exists()) {
                return ikon;
            }
        }

        return new File(goreliYol);
    }

    private static class AnalizEkranSonucu {
        private final List<MacSonuAnalizi> yorumAnalizleri;
        private final List<TakimGucuAnalizi> takimAnalizleri;
        private final List<KadroDurumuAnalizi> kadroAnalizleri;
        private final List<OranRiskAnalizi> riskAnalizleri;
        private final List<BahisOnerisi> oneriler;
        private final int tahminiTokenSayisi;

        private AnalizEkranSonucu(List<MacSonuAnalizi> yorumAnalizleri, List<TakimGucuAnalizi> takimAnalizleri, List<KadroDurumuAnalizi> kadroAnalizleri, List<OranRiskAnalizi> riskAnalizleri, List<BahisOnerisi> oneriler, int tahminiTokenSayisi) {
            this.yorumAnalizleri = yorumAnalizleri;
            this.takimAnalizleri = takimAnalizleri;
            this.kadroAnalizleri = kadroAnalizleri;
            this.riskAnalizleri = riskAnalizleri;
            this.oneriler = oneriler;
            this.tahminiTokenSayisi = tahminiTokenSayisi;
        }
    }

    private static class AnalizOnKontrol {
        private final Mac mac;
        private final List<MacYorumu> yorumlar;

        private AnalizOnKontrol(Mac mac, List<MacYorumu> yorumlar) {
            this.mac = mac;
            this.yorumlar = yorumlar == null ? new ArrayList<MacYorumu>() : new ArrayList<MacYorumu>(yorumlar);
        }
    }

    private static class MacKartRenderer extends JPanel implements ListCellRenderer<Mac> {
        private final JLabel saat = new JLabel();
        private final JLabel takimlar = new JLabel();
        private final JLabel lig = new JLabel();
        private final JLabel skor = new JLabel();

        private MacKartRenderer() {
            super(new BorderLayout(10, 4));
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            saat.setHorizontalAlignment(SwingConstants.CENTER);
            saat.setFont(new Font("Segoe UI", Font.BOLD, 14));
            takimlar.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lig.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            skor.setFont(new Font("Segoe UI", Font.BOLD, 12));
            JPanel metinler = new JPanel(new GridLayout(0, 1, 0, 3));
            metinler.setOpaque(false);
            metinler.add(takimlar);
            metinler.add(lig);
            metinler.add(skor);
            add(saat, BorderLayout.WEST);
            add(metinler, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Mac> list, Mac mac, int index, boolean selected, boolean cellHasFocus) {
            saat.setText(mac.getSaat().format(SAAT_FORMATI));
            takimlar.setText(mac.getKarsilasmaAdi());
            lig.setText(mac.getLig() + " | " + mac.getDurumMetni());
            skor.setText((mac.devamEdiyorMu() ? "CANLI " : "") + "Skor: " + mac.getSkorMetni());
            setBackground(selected ? new Color(218, 245, 231) : Color.WHITE);
            saat.setForeground(selected ? YESIL : new Color(42, 61, 55));
            takimlar.setForeground(new Color(28, 42, 38));
            lig.setForeground(new Color(91, 109, 103));
            skor.setForeground(mac.devamEdiyorMu() ? KIRMIZI : new Color(42, 61, 55));
            return this;
        }
    }

    private static class YuvarlakPanel extends JPanel {
        private final Color arkaPlan;
        private final Color kenar;
        private final int yaricap;

        private YuvarlakPanel(java.awt.LayoutManager layout, Color arkaPlan, Color kenar, int yaricap) {
            super(layout);
            this.arkaPlan = arkaPlan;
            this.kenar = kenar;
            this.yaricap = yaricap;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(arkaPlan);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, yaricap, yaricap);
            g2.setColor(kenar);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, yaricap, yaricap);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RenkliButon extends JButton {
        private final Color arkaPlan;
        private final Color yaziRengi;

        private RenkliButon(String metin, Color arkaPlan, Color yaziRengi) {
            super(metin);
            this.arkaPlan = arkaPlan;
            this.yaziRengi = yaziRengi;
            setForeground(yaziRengi);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setHorizontalAlignment(SwingConstants.CENTER);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color renk = isEnabled() ? arkaPlan : new Color(190, 202, 198);
            if (getModel().isPressed()) {
                renk = renk.darker();
            }
            g2.setColor(renk);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setColor(new Color(255, 255, 255, 80));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setForeground(yaziRengi);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
        }
    }

    private static class AnimasyonluBaslikPanel extends JPanel {
        private int adim = 0;

        private AnimasyonluBaslikPanel() {
            setOpaque(false);
        }

        private void animasyonuIlerlet() {
            adim = (adim + 2) % 360;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gradient = new GradientPaint(0, 0, KOYU_YESIL, getWidth(), getHeight(), MAVI);
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
            int x = (int) ((Math.sin(Math.toRadians(adim)) + 1) * getWidth() / 2);
            g2.setColor(new Color(255, 255, 255, 38));
            g2.fillOval(x - 130, -70, 260, 260);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RiskCemberi extends JPanel {
        private int deger = 0;
        private Color renk = YESIL;

        private RiskCemberi() {
            setPreferredSize(new Dimension(110, 110));
            setOpaque(false);
        }

        private void setDeger(int deger, Color renk) {
            this.deger = Math.max(0, Math.min(100, deger));
            this.renk = renk;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int boyut = Math.min(getWidth(), getHeight()) - 18;
            int x = (getWidth() - boyut) / 2;
            int y = (getHeight() - boyut) / 2;
            g2.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(229, 237, 234));
            g2.drawArc(x, y, boyut, boyut, 90, -360);
            g2.setColor(renk);
            g2.drawArc(x, y, boyut, boyut, 90, -(int) (360 * (deger / 100.0)));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g2.setColor(new Color(30, 48, 43));
            String metin = String.valueOf(deger);
            int genislik = g2.getFontMetrics().stringWidth(metin);
            g2.drawString(metin, (getWidth() - genislik) / 2, getHeight() / 2 + 7);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String alt = "Risk";
            int altGenislik = g2.getFontMetrics().stringWidth(alt);
            g2.drawString(alt, (getWidth() - altGenislik) / 2, getHeight() / 2 + 24);
            g2.dispose();
        }
    }
}
