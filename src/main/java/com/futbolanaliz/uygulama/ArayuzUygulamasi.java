package com.futbolanaliz.uygulama;

import com.futbolanaliz.agentlar.BahisOneriAgent;
import com.futbolanaliz.agentlar.KadroDurumuAgent;
import com.futbolanaliz.agentlar.MacVeOranToplayiciAgent;
import com.futbolanaliz.agentlar.MacYorumAnalizAgent;
import com.futbolanaliz.agentlar.OranRiskAnalizAgent;
import com.futbolanaliz.agentlar.TakimGucuAnalizAgent;
import com.futbolanaliz.agentlar.TopLigKontrolAgent;
import com.futbolanaliz.modeller.BahisOnerisi;
import com.futbolanaliz.modeller.KadroDurumuAnalizi;
import com.futbolanaliz.modeller.Mac;
import com.futbolanaliz.modeller.MacSonuAnalizi;
import com.futbolanaliz.modeller.Oran;
import com.futbolanaliz.modeller.OranRiskAnalizi;
import com.futbolanaliz.modeller.TakimGucuAnalizi;
import com.futbolanaliz.modeller.TopLig;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
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
    private final JButton analizButonu = new RenkliButon("Analiz Et", YESIL, Color.WHITE);
    private final JTextArea sonucAlani = new JTextArea();
    private final AnimasyonluBaslikPanel baslikPaneli = new AnimasyonluBaslikPanel();
    private final RiskCemberi riskCemberi = new RiskCemberi();

    private List<TopLig> kontrolEdilenLigler = new ArrayList<TopLig>();
    private List<Mac> maclar = new ArrayList<Mac>();
    private Mac seciliMac;

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
        baslikPaneli.add(baslikMetinleri, BorderLayout.WEST);
        baslikPaneli.add(stilliButon(yenileButonu), BorderLayout.EAST);
        kok.add(baslikPaneli, BorderLayout.NORTH);

        macListesi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        macListesi.setCellRenderer(new MacKartRenderer());
        macListesi.setFixedCellHeight(82);
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
        detayPaneli.add(oranlarPaneli(), BorderLayout.CENTER);
        detayPaneli.add(analizBolumu(), BorderLayout.SOUTH);

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
        oranPaneli.setLayout(new BoxLayout(oranPaneli, BoxLayout.Y_AXIS));
        oranPaneli.setOpaque(false);
        JScrollPane oranScroll = new JScrollPane(oranPaneli);
        oranScroll.setBorder(BorderFactory.createEmptyBorder());
        oranScroll.setOpaque(false);
        oranScroll.getViewport().setOpaque(false);
        oranScroll.setPreferredSize(new Dimension(420, 250));
        panel.add(oranScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel analizBolumu() {
        JPanel panel = seffafPanel(new BorderLayout(0, 12));
        panel.add(stilliButon(analizButonu), BorderLayout.NORTH);
        sonucAlani.setEditable(false);
        sonucAlani.setLineWrap(true);
        sonucAlani.setWrapStyleWord(true);
        sonucAlani.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sonucAlani.setForeground(new Color(31, 46, 42));
        sonucAlani.setBackground(new Color(250, 252, 251));
        sonucAlani.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JScrollPane sonucScroll = new JScrollPane(sonucAlani);
        sonucScroll.setBorder(BorderFactory.createLineBorder(new Color(224, 234, 230)));
        sonucScroll.setPreferredSize(new Dimension(420, 160));
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

    private void maclariYukle() {
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
                topLigKontrolAgent.calistir();
                kontrolEdilenLigler = topLigKontrolAgent.getKontrolEdilenLigler();

                MacVeOranToplayiciAgent macAgent = new MacVeOranToplayiciAgent(kontrolEdilenLigler);
                macAgent.calistir();
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
        ligSaatEtiketi.setText(seciliMac.getLig() + " | " + seciliMac.getSaat().format(SAAT_FORMATI));
        analizButonu.setEnabled(true);

        for (Oran oran : seciliMac.getOranlar()) {
            oranPaneli.add(oranKarti(oran));
            oranPaneli.add(Box.createVerticalStrut(8));
        }

        oranPaneli.revalidate();
        oranPaneli.repaint();
    }

    private void seciliMaciAnalizEt() {
        if (seciliMac == null) {
            return;
        }

        analizButonu.setEnabled(false);
        sonucAlani.setText("Analiz çalışıyor...\nYorumlar, takım gücü, kadro riski ve oran dengesi inceleniyor.");
        riskCemberi.setDeger(35, SARI);
        final List<Mac> tekMac = new ArrayList<Mac>();
        tekMac.add(seciliMac);

        new SwingWorker<AnalizEkranSonucu, Void>() {
            @Override
            protected AnalizEkranSonucu doInBackground() {
                MacYorumAnalizAgent yorumAgent = new MacYorumAnalizAgent(tekMac);
                yorumAgent.calistir();
                List<MacSonuAnalizi> yorumAnalizleri = yorumAgent.getAnalizler();

                TakimGucuAnalizAgent takimAgent = new TakimGucuAnalizAgent(tekMac);
                takimAgent.calistir();
                List<TakimGucuAnalizi> takimAnalizleri = takimAgent.getAnalizler();

                KadroDurumuAgent kadroAgent = new KadroDurumuAgent(tekMac, yorumAnalizleri);
                kadroAgent.calistir();
                List<KadroDurumuAnalizi> kadroAnalizleri = kadroAgent.getAnalizler();

                OranRiskAnalizAgent riskAgent = new OranRiskAnalizAgent(tekMac, yorumAnalizleri, takimAnalizleri, kadroAnalizleri);
                riskAgent.calistir();
                List<OranRiskAnalizi> riskAnalizleri = riskAgent.getAnalizler();

                BahisOneriAgent oneriAgent = new BahisOneriAgent(riskAnalizleri);
                oneriAgent.calistir();
                return new AnalizEkranSonucu(yorumAnalizleri, takimAnalizleri, kadroAnalizleri, riskAnalizleri, oneriAgent.getOneriler());
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

    private void analizSonucunuGoster(AnalizEkranSonucu sonuc) {
        StringBuilder metin = new StringBuilder();
        BahisOnerisi oneri = sonuc.oneriler.isEmpty() ? null : sonuc.oneriler.get(0);
        OranRiskAnalizi riskAnalizi = sonuc.riskAnalizleri.isEmpty() ? null : sonuc.riskAnalizleri.get(0);
        MacSonuAnalizi macSonu = sonuc.yorumAnalizleri.isEmpty() ? null : sonuc.yorumAnalizleri.get(0);
        TakimGucuAnalizi takim = sonuc.takimAnalizleri.isEmpty() ? null : sonuc.takimAnalizleri.get(0);
        KadroDurumuAnalizi kadro = sonuc.kadroAnalizleri.isEmpty() ? null : sonuc.kadroAnalizleri.get(0);

        if (oneri != null) {
            metin.append("ÖNERİ\n");
            metin.append(oneri.getSecim()).append(" | Oran: ").append(oneri.formatliOranDegeri()).append("\n\n");
            metin.append("Risk/Güven: ").append(oneri.getRiskPuani()).append("/").append(oneri.getGuvenPuani()).append("\n");
            metin.append(oneri.getGerekce()).append("\n\n");
            riskCemberi.setDeger(oneri.getRiskPuani(), riskRengi(oneri.getRiskPuani()));
        } else if (riskAnalizi != null) {
            metin.append("Düşük/orta risk eşiğini geçen öneri yok.\n\n");
            metin.append("En yakın seçenek: ").append(riskAnalizi.getSecim()).append(" | Oran: ").append(riskAnalizi.formatliOranDegeri()).append("\n");
            metin.append("Risk/Güven: ").append(riskAnalizi.getRiskPuani()).append("/").append(riskAnalizi.getGuvenPuani()).append("\n\n");
            riskCemberi.setDeger(riskAnalizi.getRiskPuani(), riskRengi(riskAnalizi.getRiskPuani()));
        }

        if (macSonu != null) {
            metin.append("Maç sonu tahmini: ").append(macSonu.getMacSonuTahmini()).append(" | Güven: ").append(macSonu.getGuvenPuani()).append("/100\n");
        }

        if (takim != null) {
            metin.append("Takım gücü: Ev ").append(takim.getEvSahibiGucPuani()).append(" / Dep ").append(takim.getDeplasmanGucPuani()).append(" | Öne çıkan: ").append(takim.getOneCikanTaraf()).append("\n");
        }

        if (kadro != null) {
            metin.append("Kadro riski: Ev ").append(kadro.getEvSahibiRiskPuani()).append(" / Dep ").append(kadro.getDeplasmanRiskPuani()).append("\n");
        }

        sonucAlani.setText(metin.toString());
    }

    private JPanel oranKarti(Oran oran) {
        JPanel kart = new JPanel(new BorderLayout(12, 0));
        kart.setOpaque(true);
        kart.setBackground(new Color(248, 252, 250));
        kart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 226, 216), 2),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        kart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        kart.setPreferredSize(new Dimension(420, 66));

        JLabel ad = new JLabel(oran.getBahisTuru().getGorunenAd());
        ad.setOpaque(true);
        ad.setBackground(new Color(248, 252, 250));
        ad.setForeground(new Color(23, 44, 38));
        ad.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel deger = new JLabel(oran.formatliDeger());
        deger.setOpaque(true);
        deger.setBackground(YESIL);
        deger.setForeground(Color.WHITE);
        deger.setHorizontalAlignment(SwingConstants.CENTER);
        deger.setFont(new Font("Segoe UI", Font.BOLD, 24));
        deger.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        deger.setPreferredSize(new Dimension(110, 44));

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
            File ikon = new File("varliklar/ikonlar/futbol-analiz-agent.png");
            if (ikon.exists()) {
                Image image = ImageIO.read(ikon);
                setIconImage(image);
            }
        } catch (Exception ignored) {
        }
    }

    private static class AnalizEkranSonucu {
        private final List<MacSonuAnalizi> yorumAnalizleri;
        private final List<TakimGucuAnalizi> takimAnalizleri;
        private final List<KadroDurumuAnalizi> kadroAnalizleri;
        private final List<OranRiskAnalizi> riskAnalizleri;
        private final List<BahisOnerisi> oneriler;

        private AnalizEkranSonucu(List<MacSonuAnalizi> yorumAnalizleri, List<TakimGucuAnalizi> takimAnalizleri, List<KadroDurumuAnalizi> kadroAnalizleri, List<OranRiskAnalizi> riskAnalizleri, List<BahisOnerisi> oneriler) {
            this.yorumAnalizleri = yorumAnalizleri;
            this.takimAnalizleri = takimAnalizleri;
            this.kadroAnalizleri = kadroAnalizleri;
            this.riskAnalizleri = riskAnalizleri;
            this.oneriler = oneriler;
        }
    }

    private static class MacKartRenderer extends JPanel implements ListCellRenderer<Mac> {
        private final JLabel saat = new JLabel();
        private final JLabel takimlar = new JLabel();
        private final JLabel lig = new JLabel();

        private MacKartRenderer() {
            super(new BorderLayout(10, 4));
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            saat.setHorizontalAlignment(SwingConstants.CENTER);
            saat.setFont(new Font("Segoe UI", Font.BOLD, 14));
            takimlar.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lig.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            JPanel metinler = new JPanel(new GridLayout(0, 1, 0, 3));
            metinler.setOpaque(false);
            metinler.add(takimlar);
            metinler.add(lig);
            add(saat, BorderLayout.WEST);
            add(metinler, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Mac> list, Mac mac, int index, boolean selected, boolean cellHasFocus) {
            saat.setText(mac.getSaat().format(SAAT_FORMATI));
            takimlar.setText(mac.getKarsilasmaAdi());
            lig.setText(mac.getLig());
            setBackground(selected ? new Color(218, 245, 231) : Color.WHITE);
            saat.setForeground(selected ? YESIL : new Color(42, 61, 55));
            takimlar.setForeground(new Color(28, 42, 38));
            lig.setForeground(new Color(91, 109, 103));
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
