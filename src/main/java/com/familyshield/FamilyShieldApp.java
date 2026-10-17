package com.familyshield;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.net.IDN;
import java.net.URI;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * FamilyShield - Java Swing + MySQL JDBC scam-awareness mini project.
 * Java 8+ compatible. Database driver is loaded at runtime through JDBC.
 */
public class FamilyShieldApp extends JFrame {
    private static final Color NAVY = new Color(12, 22, 43);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color CYAN = new Color(14, 165, 233);
    private static final Color BG = new Color(246, 248, 252);
    private static final Color WHITE = Color.WHITE;
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color AMBER = new Color(217, 119, 6);
    private static final Color RED = new Color(220, 38, 38);
    private static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font BOLD = new Font("SansSerif", Font.BOLD, 14);
    private static final Font H1 = new Font("SansSerif", Font.BOLD, 25);
    private static final Font H2 = new Font("SansSerif", Font.BOLD, 18);

    private final Database db = new Database();
    private final java.util.Map<String, JButton> navButtons = new LinkedHashMap<>();
    private String activePage = "dashboard";
    private JLabel connectionLabel;
    private JLabel totalLabel, highLabel, mediumLabel, safeLabel;
    private JTextArea messageArea;
    private JLabel messageStats;
    private JComboBox<String> categoryCombo;
    private JTextField sourceField;
    private JPanel analysisResultPanel;
    private Analysis latestAnalysis;
    private JTable reportsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> riskFilter;
    private JLabel reportStatus;
    private JTextField linkField;
    private JPanel linkResultPanel;
    private JLabel screenshotPreview;
    private JTextArea screenshotTextArea;
    private JLabel screenshotStatus;
    private File selectedScreenshot;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FamilyShieldApp() {
        super("FamilyShield 2.0 | Digital Scam Prevention");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1180, 720);
        setLocationByPlatform(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        add(buildSidebar(), BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG);
        right.add(buildTopbar(), BorderLayout.NORTH);
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG);
        contentPanel.add(buildDashboard(), "dashboard");
        contentPanel.add(buildAnalyzer(), "analyzer");
        contentPanel.add(buildLinkInspector(), "links");
        contentPanel.add(buildScreenshotScanner(), "screenshots");
        contentPanel.add(buildReports(), "reports");
        contentPanel.add(buildSafetyGuide(), "guide");
        contentPanel.add(buildSettings(), "settings");
        right.add(contentPanel, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);
        refreshDashboard();
        refreshReports();
        updateNavStyles();
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        SwingUtilities.invokeLater(() -> testConnection(false));
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(230, 0));
        side.setBackground(new Color(239, 244, 251));
        side.setBorder(new EmptyBorder(22, 15, 18, 15));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("✦ FamilyShield");
        logo.setForeground(new Color(15, 23, 42));
        logo.setFont(new Font("SansSerif", Font.BOLD, 23));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(logo);
        JLabel subtitle = new JLabel("DIGITAL SAFETY WORKSPACE  •  2.0");
        subtitle.setForeground(new Color(71, 85, 105));
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        subtitle.setBorder(new EmptyBorder(8, 2, 34, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(subtitle);

        addNavButton(side, "▦   Dashboard", "dashboard");
        addNavButton(side, "⌕   Analyze Message", "analyzer");
        addNavButton(side, "↗   Link Inspector", "links");
        addNavButton(side, "▧   Screenshot Scan", "screenshots");
        addNavButton(side, "☷   Report Library", "reports");
        addNavButton(side, "✓   Safety Guide", "guide");
        addNavButton(side, "⚙   Settings", "settings");
        side.add(Box.createVerticalGlue());

        JLabel reminder = new JLabel("<html><div style='width:170px'>Stay alert.<br>Never share OTPs, PINs or passwords.</div></html>");
        reminder.setForeground(new Color(30, 41, 59));
        reminder.setFont(new Font("SansSerif", Font.PLAIN, 13));
        reminder.setBorder(new CompoundBorder(new LineBorder(new Color(203, 213, 225)), new EmptyBorder(12, 10, 12, 10)));
        reminder.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(reminder);
        JLabel build = new JLabel("FAMILYSHIELD  /  DESKTOP");
        build.setForeground(new Color(71, 85, 105));
        build.setFont(new Font("SansSerif", Font.BOLD, 9));
        build.setBorder(new EmptyBorder(14, 2, 0, 0));
        build.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(build);
        return side;
    }

    private void addNavButton(JPanel side, String text, String card) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        b.setPreferredSize(new Dimension(200, 48));
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(new Color(30, 41, 59));
        b.setBackground(new Color(239, 244, 251));
        b.setBorder(new CompoundBorder(new EmptyBorder(2, 0, 2, 0), new EmptyBorder(11, 13, 11, 8)));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> showPage(card));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!card.equals(activePage)) b.setBackground(new Color(219, 234, 254)); }
            public void mouseExited(MouseEvent e) { updateNavStyles(); }
        });
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        navButtons.put(card, b);
        side.add(b);
        side.add(Box.createVerticalStrut(7));
    }

    private void showPage(String card) {
        activePage = card;
        cardLayout.show(contentPanel, card);
        updateNavStyles();
        if ("dashboard".equals(card)) refreshDashboard();
        if ("reports".equals(card)) refreshReports();
    }

    private void updateNavStyles() {
        for (java.util.Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean selected = entry.getKey().equals(activePage);
            JButton b = entry.getValue();
            b.setBackground(selected ? new Color(219, 234, 254) : new Color(239, 244, 251));
            b.setForeground(new Color(15, 23, 42));
            b.setBorder(new CompoundBorder(new MatteBorder(0, selected ? 4 : 4, 0, 0,
                selected ? new Color(37, 99, 235) : new Color(239, 244, 251)), new EmptyBorder(11, 9, 11, 8)));
        }
    }

    private JPanel buildTopbar() {
        JPanel top = new JPanel(new BorderLayout(18, 0));
        top.setBackground(WHITE);
        top.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)), new EmptyBorder(14, 26, 14, 26)));
        JPanel titleBox = new JPanel(); titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Your digital safety, at a glance");
        title.setFont(new Font("SansSerif", Font.BOLD, 17)); title.setForeground(new Color(15, 23, 42));
        JLabel subtitle = new JLabel("Analyze suspicious messages • learn the warning signs • keep a record");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11)); subtitle.setForeground(new Color(100, 116, 139));
        titleBox.add(title); titleBox.add(Box.createVerticalStrut(3)); titleBox.add(subtitle);
        top.add(titleBox, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 2)); right.setOpaque(false);
        connectionLabel = new JLabel("● Checking database...");
        connectionLabel.setFont(new Font("SansSerif", Font.BOLD, 11)); connectionLabel.setForeground(AMBER);
        JButton quickAnalyze = primaryButton("＋  New analysis");
        quickAnalyze.addActionListener(e -> showPage("analyzer"));
        right.add(connectionLabel); right.add(quickAnalyze);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildDashboard() {
        JPanel page = pagePanel();
        page.add(pageTitle("Dashboard", "Your overview of analyzed messages and community reports"), BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setBackground(BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JPanel stats = new JPanel(new GridLayout(1, 4, 15, 15));
        stats.setOpaque(false);
        totalLabel = new JLabel("0");
        highLabel = new JLabel("0");
        mediumLabel = new JLabel("0");
        safeLabel = new JLabel("0");
        stats.add(statCard("TOTAL REPORTS", totalLabel, BLUE, "All saved analyses"));
        stats.add(statCard("HIGH RISK", highLabel, RED, "Strong scam signals"));
        stats.add(statCard("MEDIUM RISK", mediumLabel, AMBER, "Check carefully"));
        stats.add(statCard("LOW RISK", safeLabel, GREEN, "Fewer signals found"));
        body.add(stats);
        body.add(Box.createVerticalStrut(20));

        JPanel welcome = new GradientHeroPanel();
        welcome.setLayout(new BorderLayout(20, 12));
        welcome.setBorder(new EmptyBorder(24, 26, 24, 26));
        JPanel heroText = new JPanel(); heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("YOUR PERSONAL SCAM-AWARENESS ASSISTANT");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 10)); eyebrow.setForeground(new Color(191, 219, 254));
        JLabel heroTitle = new JLabel("Pause. Check. Protect.");
        heroTitle.setFont(new Font("SansSerif", Font.BOLD, 29)); heroTitle.setForeground(Color.WHITE);
        JTextArea intro = new JTextArea("Spot common red flags in messages before you click, pay or share private information.");
        intro.setFont(new Font("SansSerif", Font.PLAIN, 14)); intro.setForeground(new Color(226, 237, 255));
        intro.setLineWrap(true); intro.setWrapStyleWord(true); intro.setEditable(false); intro.setOpaque(false); intro.setRows(2);
        heroText.add(eyebrow); heroText.add(Box.createVerticalStrut(9)); heroText.add(heroTitle);
        heroText.add(Box.createVerticalStrut(8)); heroText.add(intro);
        welcome.add(heroText, BorderLayout.CENTER);
        JPanel heroActions = new JPanel(); heroActions.setOpaque(false); heroActions.setLayout(new BoxLayout(heroActions, BoxLayout.Y_AXIS));
        JButton analyze = new JButton("＋  Analyze a message  →");
        analyze.setFont(new Font("SansSerif", Font.BOLD, 13)); analyze.setForeground(new Color(30, 64, 175));
        analyze.setBackground(Color.WHITE); analyze.setOpaque(true); analyze.setFocusPainted(false);
        analyze.setBorder(new EmptyBorder(13, 17, 13, 17)); analyze.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        analyze.addActionListener(e -> showPage("analyzer"));
        JLabel privacy = new JLabel("Works locally • You stay in control"); privacy.setFont(new Font("SansSerif", Font.PLAIN, 10)); privacy.setForeground(new Color(219, 234, 254));
        heroActions.add(Box.createVerticalGlue()); heroActions.add(analyze); heroActions.add(Box.createVerticalStrut(10)); heroActions.add(privacy); heroActions.add(Box.createVerticalGlue());
        welcome.add(heroActions, BorderLayout.EAST);
        body.add(welcome);
        body.add(Box.createVerticalStrut(18));

        JPanel tips = cardPanel();
        tips.setLayout(new BorderLayout(10, 10));
        tips.add(sectionHeading("Three rules to remember"), BorderLayout.NORTH);
        JPanel tipGrid = new JPanel(new GridLayout(1, 3, 12, 12));
        tipGrid.setOpaque(false);
        tipGrid.add(tipCard("01", "Pause before clicking", "Urgent messages and shortened links deserve extra checking.", new Color(239, 246, 255), BLUE));
        tipGrid.add(tipCard("02", "Keep secrets private", "Banks and legitimate support teams never need your OTP or UPI PIN.", new Color(254, 242, 242), RED));
        tipGrid.add(tipCard("03", "Verify independently", "Open the official app or type the official website address yourself.", new Color(240, 253, 244), GREEN));
        tips.add(tipGrid, BorderLayout.CENTER);
        body.add(tips);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private JPanel statCard(String title, JLabel value, Color color, String note) {
        JPanel p = cardPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(new Color(100, 116, 139));
        value.setFont(new Font("SansSerif", Font.BOLD, 32));
        value.setForeground(color);
        JLabel n = new JLabel(note);
        n.setFont(new Font("SansSerif", Font.PLAIN, 12));
        n.setForeground(new Color(100, 116, 139));
        p.add(t); p.add(Box.createVerticalStrut(8)); p.add(value); p.add(Box.createVerticalStrut(5)); p.add(n);
        return p;
    }

    private JPanel tipCard(String num, String title, String body, Color color, Color accent) {
        JPanel p = new JPanel();
        p.setBackground(color);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(226, 232, 240)), new EmptyBorder(14, 14, 14, 14)));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel n = new JLabel(num); n.setFont(new Font("SansSerif", Font.BOLD, 12)); n.setForeground(accent);
        JLabel t = new JLabel(title); t.setFont(BOLD); t.setForeground(NAVY);
        JTextArea d = new JTextArea(body); d.setFont(new Font("SansSerif", Font.PLAIN, 12)); d.setForeground(new Color(71, 85, 105));
        d.setLineWrap(true); d.setWrapStyleWord(true); d.setEditable(false); d.setOpaque(false); d.setRows(3);
        p.add(n); p.add(Box.createVerticalStrut(8)); p.add(t); p.add(Box.createVerticalStrut(6)); p.add(d);
        return p;
    }

    private JPanel buildAnalyzer() {
        JPanel page = pagePanel();
        page.add(pageTitle("Analyze Message", "Check an SMS, email excerpt, payment request or suspicious link"), BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setBackground(BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel form = cardPanel();
        form.setLayout(new BorderLayout(12, 12));
        form.add(sectionHeading("Message details"), BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        g.gridx = 0; g.gridy = 0; fields.add(fieldLabel("Source / sender (optional)"), g);
        g.gridx = 1; fields.add(fieldLabel("Message category"), g);
        sourceField = new JTextField();
        styleInput(sourceField);
        categoryCombo = new JComboBox<>(new String[]{"SMS / Text", "Banking / KYC", "UPI / Payment", "Job / Prize", "Shopping / Delivery", "Email", "Other"});
        categoryCombo.setFont(BODY);
        g.gridx = 0; g.gridy = 1; fields.add(sourceField, g);
        g.gridx = 1; fields.add(categoryCombo, g);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; fields.add(fieldLabel("Paste the suspicious message below"), g);
        messageArea = new JTextArea(7, 50);
        messageArea.setFont(new Font("SansSerif", Font.PLAIN, 15));
        messageArea.setLineWrap(true); messageArea.setWrapStyleWord(true);
        messageArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setBorder(new LineBorder(new Color(203, 213, 225)));
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; g.weighty = 1; g.fill = GridBagConstraints.BOTH; fields.add(messageScroll, g);
        messageStats = new JLabel("0 characters  •  0 words"); messageStats.setFont(new Font("SansSerif", Font.PLAIN, 11)); messageStats.setForeground(new Color(100, 116, 139));
        g.gridy = 4; g.weighty = 0; g.fill = GridBagConstraints.HORIZONTAL; fields.add(messageStats, g);
        messageArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateMessageStats(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateMessageStats(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateMessageStats(); }
        });
        form.add(fields, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        JButton analyze = primaryButton("Analyze message");
        JButton example = secondaryButton("Load sample scam");
        JButton clear = secondaryButton("Clear");
        analyze.addActionListener(e -> analyzeMessage());
        example.addActionListener(e -> {
            sourceField.setText("Unknown sender");
            categoryCombo.setSelectedItem("Banking / KYC");
            messageArea.setText("Your bank account will be blocked today. Verify your KYC immediately and share your OTP using this link: http://bank-verify-example.invalid");
        });
        clear.addActionListener(e -> { messageArea.setText(""); sourceField.setText(""); latestAnalysis = null; analysisResultPanel.removeAll(); analysisResultPanel.revalidate(); analysisResultPanel.repaint(); });
        buttons.add(analyze); buttons.add(example); buttons.add(clear);
        form.add(buttons, BorderLayout.SOUTH);
        body.add(form); body.add(Box.createVerticalStrut(16));
        analysisResultPanel = new JPanel();
        analysisResultPanel.setOpaque(false);
        analysisResultPanel.setLayout(new BoxLayout(analysisResultPanel, BoxLayout.Y_AXIS));
        body.add(analysisResultPanel);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    /** Dedicated URL inspection screen. This is heuristic only; it does not visit the URL. */
    private JPanel buildLinkInspector() {
        JPanel page = pagePanel();
        page.add(pageTitle("Link Inspector", "Check a suspicious web address locally before opening it"), BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setBackground(BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel intro = new GradientHeroPanel();
        intro.setLayout(new BorderLayout(18, 10));
        intro.setBorder(new EmptyBorder(20, 24, 20, 24));
        JPanel introText = new JPanel(); introText.setOpaque(false);
        introText.setLayout(new BoxLayout(introText, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("LOCAL URL CHECK • NO WEBSITE VISIT");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 10)); eyebrow.setForeground(new Color(219, 234, 254));
        JLabel headline = new JLabel("Inspect before you connect.");
        headline.setFont(new Font("SansSerif", Font.BOLD, 24)); headline.setForeground(Color.WHITE);
        JTextArea sub = new JTextArea("Look for suspicious domains, URL shorteners, insecure HTTP and deceptive address patterns.");
        sub.setFont(BODY); sub.setForeground(new Color(239, 246, 255)); sub.setOpaque(false); sub.setEditable(false); sub.setLineWrap(true); sub.setWrapStyleWord(true); sub.setRows(2);
        introText.add(eyebrow); introText.add(Box.createVerticalStrut(8)); introText.add(headline); introText.add(Box.createVerticalStrut(7)); introText.add(sub);
        intro.add(introText, BorderLayout.CENTER);
        JLabel shield = new JLabel("◈"); shield.setFont(new Font("SansSerif", Font.BOLD, 58)); shield.setForeground(new Color(219, 234, 254));
        intro.add(shield, BorderLayout.EAST);
        body.add(intro); body.add(Box.createVerticalStrut(16));

        JPanel inputCard = cardPanel(); inputCard.setLayout(new BorderLayout(10, 12));
        inputCard.add(sectionHeading("Paste a link to inspect"), BorderLayout.NORTH);
        JPanel inputRow = new JPanel(new BorderLayout(8, 0)); inputRow.setOpaque(false);
        linkField = new JTextField(); linkField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        linkField.setBorder(new CompoundBorder(new LineBorder(new Color(203, 213, 225)), new EmptyBorder(10, 12, 10, 12)));
        linkField.setToolTipText("Example: https://example.com/login");
        JButton inspect = primaryButton("Inspect link  →"); inspect.addActionListener(e -> inspectLink());
        linkField.addActionListener(e -> inspectLink());
        inputRow.add(linkField, BorderLayout.CENTER); inputRow.add(inspect, BorderLayout.EAST);
        inputCard.add(inputRow, BorderLayout.CENTER);
        JLabel hint = new JLabel("Tip: copy the full address without opening it. FamilyShield will not connect to the site.");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12)); hint.setForeground(new Color(100, 116, 139));
        inputCard.add(hint, BorderLayout.SOUTH);
        body.add(inputCard); body.add(Box.createVerticalStrut(14));

        linkResultPanel = new JPanel(); linkResultPanel.setOpaque(false); linkResultPanel.setLayout(new BoxLayout(linkResultPanel, BoxLayout.Y_AXIS));
        JPanel empty = cardPanel(); empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        JLabel emptyTitle = sectionHeading("Your link report will appear here");
        JLabel emptyDesc = wrapLabel("This tool checks the text of a URL only. It cannot confirm whether a website is safe, and a normal-looking URL can still be malicious.");
        empty.add(emptyTitle); empty.add(Box.createVerticalStrut(8)); empty.add(emptyDesc);
        linkResultPanel.add(empty);
        body.add(linkResultPanel);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private void inspectLink() {
        String raw = linkField == null ? "" : linkField.getText().trim();
        if (raw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Paste a URL first, for example https://example.com/login", "Link required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String candidate = raw.matches("(?i)^[a-z][a-z0-9+.-]*://.*") ? raw : "https://" + raw;
        URI uri;
        try {
            uri = new URI(candidate);
            if (uri.getHost() == null || uri.getHost().trim().isEmpty()) throw new Exception("No valid domain could be read from this address.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "That address could not be parsed. Paste the complete web address, including its domain.", "Invalid URL", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        String displayHostValue;
        try { displayHostValue = IDN.toUnicode(host); } catch (Exception ignored) { displayHostValue = host; }
        final String displayHost = displayHostValue;
        List<String> warnings = new ArrayList<>();
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"https".equals(scheme)) warnings.add("This address does not use HTTPS. Data may not be encrypted in transit.");
        if (host.matches("[0-9.]+") || host.contains(":")) warnings.add("The address uses an IP-style host instead of a familiar domain name.");
        if (host.startsWith("xn--") || host.contains(".xn--")) warnings.add("The domain contains punycode, which can be used to disguise look-alike characters.");
        if (host.matches(".*\\.(xyz|top|click|loan|work|support|gq|tk|zip|mov)$")) warnings.add("The domain ends with an extension commonly seen in suspicious links. This alone does not prove fraud.");
        if (host.matches(".*(bit\\.ly|tinyurl\\.com|t\\.co|shorturl\\.|is\\.gd|cutt\\.ly)$")) warnings.add("This appears to be a URL shortener, so the final destination is hidden.");
        if (host.split("\\.").length >= 5) warnings.add("The address has many subdomain levels. Check which domain is actually registered.");
        if (uri.getUserInfo() != null) warnings.add("The URL includes text before the @ symbol, which can disguise the real destination host.");
        String path = (uri.getRawPath() == null ? "" : uri.getRawPath()).toLowerCase(Locale.ROOT);
        if (path.matches(".*(login|verify|secure|account|kyc|password|wallet|payment|otp).*")) warnings.add("The path uses account or payment-related wording. Verify the sender independently.");
        if (raw.length() > 120) warnings.add("The address is unusually long. Read the domain carefully before trusting it.");
        int linkScore = 0;
        if (!"https".equals(scheme)) linkScore += 12;
        if (host.matches("[0-9.]+") || host.contains(":")) linkScore += 30;
        if (host.startsWith("xn--") || host.contains(".xn--")) linkScore += 35;
        if (host.matches(".*\\.(xyz|top|click|loan|work|support|gq|tk|zip|mov)$")) linkScore += 24;
        if (host.matches(".*(bit\\.ly|tinyurl\\.com|t\\.co|shorturl\\.|is\\.gd|cutt\\.ly|rb\\.gy)$")) linkScore += 24;
        if (host.split("\\.").length >= 5) linkScore += 18;
        if (uri.getUserInfo() != null) linkScore += 45;
        if (path.matches(".*(login|verify|secure|account|kyc|password|wallet|payment|otp|claim|refund|update|unlock).*")) linkScore += 16;
        if (raw.length() > 120) linkScore += 10;
        if (host.matches(".*(paypa[l1]|amaz[o0]n|g[o0]{2}gle|micr[o0]soft|faceb[o0]ok|whatsapp|sbi|hdfc|icici|axisbank).*") && !host.matches(".*(paypal\\.com|amazon\\.com|google\\.com|microsoft\\.com|facebook\\.com|whatsapp\\.com|sbi\\.co\\.in|hdfcbank\\.com|icicibank\\.com|axisbank\\.com).*$")) linkScore += 35;
        if (warnings.size() >= 3) linkScore += 12;
        // Stronger combination checks: phishing often uses a generic domain plus a credential/payment path.
        boolean hostHasLure = host.matches(".*(login|verify|secure|account|update|bank|wallet|kyc|payment|pay|refund|claim|reward|bonus|otp|auth|billing|support|unlock|confirm).*");
        boolean pathHasCredentialAction = path.matches(".*(login|verify|secure|account|kyc|password|wallet|payment|otp|claim|refund|update|unlock|confirm|card|signin|sign-in).*");
        boolean brandMention = host.matches(".*(paypal|paypa[l1]|amazon|amaz[o0]n|google|g[o0]{2}gle|microsoft|micr[o0]soft|facebook|faceb[o0]ok|whatsapp|sbi|hdfc|icici|axisbank|phonepe|paytm|gpay).*");
        boolean trustedBrandDomain = host.matches("(^|.*\\.)(paypal\\.com|amazon\\.com|google\\.com|microsoft\\.com|facebook\\.com|whatsapp\\.com|sbi\\.co\\.in|hdfcbank\\.com|icicibank\\.com|axisbank\\.com|phonepe\\.com|paytm\\.com)$");
        if (hostHasLure) linkScore += 18;
        if (hostHasLure && pathHasCredentialAction) linkScore += 24;
        if (brandMention && !trustedBrandDomain) {
            linkScore += 35;
            warnings.add("The domain mentions a well-known bank or service but does not match its usual official domain. Verify independently.");
        }
        if ((hostHasLure && pathHasCredentialAction && (host.split("\\.").length >= 2)) || (brandMention && !trustedBrandDomain && pathHasCredentialAction)) {
            linkScore = Math.max(linkScore, 65);
            warnings.add("Multiple phishing indicators appear together: a lure-style domain and a sign-in, verification or payment path.");
        }
        final int score = Math.min(100, linkScore);
        final String risk = score >= 50 ? "HIGH" : score >= 20 ? "MEDIUM" : "LOW INDICATORS";
        Color riskColor = score >= 50 ? RED : score >= 20 ? AMBER : GREEN;

        linkResultPanel.removeAll();
        JPanel result = cardPanel(); result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        JPanel heading = new JPanel(new BorderLayout(10, 0)); heading.setOpaque(false);
        JLabel title = new JLabel("Inspection result: " + risk); title.setFont(new Font("SansSerif", Font.BOLD, 20)); title.setForeground(riskColor);
        JLabel scoreLabel = new JLabel(score + " / 100"); scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 23)); scoreLabel.setForeground(riskColor);
        heading.add(title, BorderLayout.WEST); heading.add(scoreLabel, BorderLayout.EAST); result.add(heading);
        result.add(Box.createVerticalStrut(10));
        JProgressBar meter = new JProgressBar(0, 100); meter.setValue(score); meter.setStringPainted(true); meter.setString("Heuristic indicator: " + score + "%"); meter.setForeground(riskColor); meter.setPreferredSize(new Dimension(500, 22));
        result.add(meter); result.add(Box.createVerticalStrut(12));
        result.add(sectionHeading("Address details"));
        result.add(bulletLabel("Domain: " + displayHost, BLUE));
        result.add(bulletLabel("Protocol: " + (scheme.isEmpty() ? "unknown" : scheme.toUpperCase(Locale.ROOT)), BLUE));
        result.add(bulletLabel("Port: " + (uri.getPort() < 0 ? "default" : String.valueOf(uri.getPort())), BLUE));
        result.add(Box.createVerticalStrut(8)); result.add(sectionHeading("Checks to review"));
        if (warnings.isEmpty()) result.add(wrapLabel("No configured URL warning patterns were found. This is not a guarantee that the link is safe. Treat unexpected links as untrusted until verified."));
        else for (String warning : warnings) result.add(bulletLabel(warning, riskColor));
        result.add(Box.createVerticalStrut(8));
        result.add(wrapLabel("Recommended: do not enter passwords, OTPs, card details or UPI PINs from a message link. Open the official app or type the known website address yourself."));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); actions.setOpaque(false);
        JButton analyze = primaryButton("Analyze in message scanner");
        analyze.addActionListener(e -> { sourceField.setText("Link Inspector"); categoryCombo.setSelectedItem("SMS / Text"); messageArea.setText("Suspicious URL: " + raw); showPage("analyzer"); });
        JButton copy = secondaryButton("Copy inspection summary");
        copy.addActionListener(e -> {
            String summary = "FamilyShield Link Inspection\nURL: " + raw + "\nDomain: " + displayHost + "\nIndicator: " + risk + " (" + score + "/100)\n\nChecks:\n- " + (warnings.isEmpty() ? "No configured warning patterns found" : String.join("\n- ", warnings)) + "\n\nThis is a heuristic indicator, not a guarantee of safety.";
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(summary), null);
            JOptionPane.showMessageDialog(this, "Inspection summary copied.", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });
        actions.add(analyze); actions.add(copy); result.add(Box.createVerticalStrut(12)); result.add(actions);
        linkResultPanel.add(result); linkResultPanel.revalidate(); linkResultPanel.repaint();
        Analysis linkAnalysis = new Analysis();
        linkAnalysis.score = score;
        linkAnalysis.risk = score >= 50 ? "HIGH" : score >= 20 ? "MEDIUM" : "LOW";
        linkAnalysis.signs.addAll(warnings);
        linkAnalysis.tips.add("Do not enter passwords, OTPs, card details or UPI PINs from a message link.");
        linkAnalysis.tips.add("Verify the sender independently and use the official app or known website address.");
        autoSaveReport("Link Inspector", "URL Inspection", raw, linkAnalysis);
    }

    private void updateMessageStats() {
        if (messageArea == null || messageStats == null) return;
        String value = messageArea.getText(); String trimmed = value.trim();
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        messageStats.setText(value.length() + " characters  •  " + words + " words");
    }

    private void copyAnalysisSummary() {
        if (latestAnalysis == null) return;
        StringBuilder summary = new StringBuilder("FamilyShield Safety Summary\n")
            .append("Risk: ").append(latestAnalysis.risk).append(" ( ").append(latestAnalysis.score).append("/100 )\n\n")
            .append("Warning signs:\n");
        if (latestAnalysis.signs.isEmpty()) summary.append("No configured warning signs detected.\n");
        else for (String sign : latestAnalysis.signs) summary.append("• ").append(sign).append("\n");
        summary.append("\nRecommended next steps:\n");
        for (String tip : latestAnalysis.tips) summary.append("• ").append(tip).append("\n");
        summary.append("\nEducational rule-based indicator; not a verified fraud diagnosis.");
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(summary.toString()), null);
        JOptionPane.showMessageDialog(this, "Safety summary copied to clipboard.", "Copied", JOptionPane.INFORMATION_MESSAGE);
    }

    private void analyzeMessage() {
        String message = messageArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please paste a message before analyzing.", "Message required", JOptionPane.WARNING_MESSAGE);
            messageArea.requestFocusInWindow();
            return;
        }
        latestAnalysis = ScamAnalyzer.analyze(message);
        analysisResultPanel.removeAll();
        analysisResultPanel.add(renderAnalysis(latestAnalysis));
        analysisResultPanel.add(Box.createVerticalStrut(10));
        autoSaveReport(sourceField.getText().trim(), (String) categoryCombo.getSelectedItem(), message, latestAnalysis);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton save = primaryButton("Open Report Library");
        JButton export = secondaryButton("Export result as text");
        JButton copy = secondaryButton("Copy safety summary");
        save.addActionListener(e -> showPage("reports"));
        export.addActionListener(e -> exportAnalysis());
        copy.addActionListener(e -> copyAnalysisSummary());
        actions.add(save); actions.add(export); actions.add(copy);
        analysisResultPanel.add(actions);
        analysisResultPanel.revalidate();
        analysisResultPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            Container p = analysisResultPanel.getParent();
            if (p instanceof JComponent) ((JComponent) p).scrollRectToVisible(analysisResultPanel.getBounds());
        });
    }

    private JPanel renderAnalysis(Analysis a) {
        JPanel p = cardPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        Color riskColor = a.risk.equals("HIGH") ? RED : a.risk.equals("MEDIUM") ? AMBER : GREEN;
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Risk assessment: " + a.risk + " RISK");
        title.setFont(new Font("SansSerif", Font.BOLD, 20)); title.setForeground(riskColor);
        JLabel score = new JLabel(a.score + " / 100");
        score.setFont(new Font("SansSerif", Font.BOLD, 26)); score.setForeground(riskColor);
        header.add(title, BorderLayout.WEST); header.add(score, BorderLayout.EAST);
        p.add(header);
        p.add(Box.createVerticalStrut(10));
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(a.score); bar.setStringPainted(true); bar.setString("Risk indicator: " + a.score + "%");
        bar.setForeground(riskColor); bar.setBackground(new Color(226, 232, 240)); bar.setPreferredSize(new Dimension(500, 24));
        p.add(bar); p.add(Box.createVerticalStrut(12));
        p.add(sectionHeading("Warning signs detected"));
        if (a.signs.isEmpty()) p.add(wrapLabel("No configured warning signs were detected. This does not mean the message is safe."));
        else for (String sign : a.signs) p.add(bulletLabel(sign, riskColor));
        p.add(Box.createVerticalStrut(10));
        p.add(sectionHeading("Recommended next steps"));
        for (String tip : a.tips) p.add(bulletLabel(tip, BLUE));
        JLabel disclaimer = new JLabel("<html><div style='width:700px'><b>Note:</b> This score is a rule-based educational indicator, not a verified fraud diagnosis.</div></html>");
        disclaimer.setFont(new Font("SansSerif", Font.ITALIC, 12)); disclaimer.setForeground(new Color(100, 116, 139));
        disclaimer.setBorder(new EmptyBorder(12, 0, 0, 0)); p.add(disclaimer);
        return p;
    }

    /** Screenshot scanning uses the locally installed Tesseract OCR command when available. */
    private JPanel buildScreenshotScanner() {
        JPanel page = pagePanel();
        page.add(pageTitle("Screenshot Scan", "Choose a screenshot of a suspicious SMS, email, payment request or web page"), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(14, 14));
        body.setOpaque(false);
        JPanel left = cardPanel();
        left.setLayout(new BorderLayout(8, 8));
        left.add(sectionHeading("Screenshot preview"), BorderLayout.NORTH);
        screenshotPreview = new JLabel("Choose an image to preview", SwingConstants.CENTER);
        screenshotPreview.setForeground(new Color(71, 85, 105));
        screenshotPreview.setBackground(new Color(241, 245, 249));
        screenshotPreview.setOpaque(true);
        screenshotPreview.setBorder(new LineBorder(new Color(203, 213, 225)));
        left.add(screenshotPreview, BorderLayout.CENTER);
        JButton choose = primaryButton("Choose screenshot…");
        choose.addActionListener(e -> chooseScreenshot());
        left.add(choose, BorderLayout.SOUTH);

        JPanel right = cardPanel();
        right.setLayout(new BorderLayout(8, 8));
        right.add(sectionHeading("Recognized text & risk analysis"), BorderLayout.NORTH);
        screenshotTextArea = new JTextArea(12, 40);
        screenshotTextArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        screenshotTextArea.setLineWrap(true); screenshotTextArea.setWrapStyleWord(true);
        screenshotTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        right.add(new JScrollPane(screenshotTextArea), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(8, 8)); bottom.setOpaque(false);
        screenshotStatus = new JLabel("Select an image. OCR requires Tesseract installed and available on PATH.");
        screenshotStatus.setFont(new Font("SansSerif", Font.PLAIN, 11)); screenshotStatus.setForeground(new Color(71, 85, 105));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); actions.setOpaque(false);
        JButton extract = secondaryButton("Extract text from image");
        extract.addActionListener(e -> extractScreenshotText());
        JButton analyze = primaryButton("Analyze screenshot text");
        analyze.addActionListener(e -> analyzeScreenshotText());
        actions.add(extract); actions.add(analyze);
        bottom.add(screenshotStatus, BorderLayout.NORTH); bottom.add(actions, BorderLayout.SOUTH);
        right.add(bottom, BorderLayout.SOUTH);
        body.add(left, BorderLayout.WEST); body.add(right, BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(300, 380));
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private void chooseScreenshot() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a scam-message screenshot");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files (PNG, JPG, JPEG, BMP)", "png", "jpg", "jpeg", "bmp"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        selectedScreenshot = chooser.getSelectedFile();
        try {
            ImageIcon icon = new ImageIcon(selectedScreenshot.getAbsolutePath());
            int w = Math.max(180, screenshotPreview.getWidth() - 20);
            int h = Math.max(180, screenshotPreview.getHeight() - 20);
            Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            screenshotPreview.setIcon(new ImageIcon(scaled)); screenshotPreview.setText("");
            screenshotStatus.setText("Selected: " + selectedScreenshot.getName() + " — click Extract text from image.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not preview this image: " + ex.getMessage(), "Image error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void extractScreenshotText() {
        if (selectedScreenshot == null) {
            JOptionPane.showMessageDialog(this, "Choose a screenshot image first.", "Image required", JOptionPane.WARNING_MESSAGE); return;
        }
        screenshotStatus.setText("Reading screenshot with local OCR…");
        final File imageFile = selectedScreenshot;
        new SwingWorker<String, Void>() {
            protected String doInBackground() throws Exception {
                ProcessBuilder pb = new ProcessBuilder("tesseract", imageFile.getAbsolutePath(), "stdout", "-l", "eng");
                pb.redirectErrorStream(true);
                Process process;
                try { process = pb.start(); }
                catch (IOException ex) { throw new IOException("Tesseract OCR was not found. Install Tesseract OCR and add it to PATH, or type/paste the screenshot text into the text box to analyze it.", ex); }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (InputStream in = process.getInputStream()) { byte[] buf = new byte[4096]; int n; while ((n = in.read(buf)) != -1) out.write(buf, 0, n); }
                int exit = process.waitFor();
                String result = new String(out.toByteArray(), StandardCharsets.UTF_8).trim();
                if (exit != 0 || result.isEmpty()) throw new IOException("OCR could not read text from this image. Try a clearer screenshot or paste the visible text manually.");
                return result;
            }
            protected void done() {
                try { screenshotTextArea.setText(get()); screenshotStatus.setText("Text extracted. Review it, then click Analyze screenshot text."); }
                catch (Exception ex) { Throwable cause = ex.getCause() == null ? ex : ex.getCause(); screenshotStatus.setText(cause.getMessage()); JOptionPane.showMessageDialog(FamilyShieldApp.this, cause.getMessage(), "Screenshot OCR", JOptionPane.INFORMATION_MESSAGE); }
            }
        }.execute();
    }

    private void analyzeScreenshotText() {
        String text = screenshotTextArea.getText().trim();
        if (text.isEmpty()) { JOptionPane.showMessageDialog(this, "Extract text or type/paste the screenshot text first.", "Text required", JOptionPane.WARNING_MESSAGE); return; }
        Analysis a = ScamAnalyzer.analyze(text);
        // Screenshot-specific second pass: OCR often drops punctuation or line breaks,
        // so classify combinations of strong scam signals independently of the base score.
        String ocr = text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        boolean safetyEducation = ocr.matches("(?s).*(never|do not|don't|dont|avoid|warning|beware).{0,70}(click|open|share|send|enter|provide).{0,70}(link|otp|password|pin|cvv|code).*");
        boolean hasUrl = ocr.matches("(?s).*(https?://|www\\.|bit\\.ly|tinyurl|cutt\\.ly|t\\.co|\\.xyz|\\.top|\\.click).*" );
        boolean bankOrAccount = ocr.matches("(?s).*(bank|account|card|wallet|upi|kyc|sbi|hdfc|icici|paytm|phonepe).*");
        boolean threat = ocr.matches("(?s).*(blocked|block|suspend|suspended|deactivat|frozen|expire|expired|close your account|permanently|arrest|legal action).*");
        boolean urgency = ocr.matches("(?s).*(urgent|immediately|today|within [0-9]+ hours?|verify now|act now|final warning|restore your account|click the link).*");
        boolean secretRequest = ocr.matches("(?s).*(otp|one[- ]time password|password|upi pin|atm pin|cvv|verification code|bank details).*")
                && ocr.matches("(?s).*(enter|share|send|provide|confirm|verify|submit|update|reply|click).*");
        int screenshotSignals = (hasUrl ? 1 : 0) + (bankOrAccount ? 1 : 0) + (threat ? 1 : 0) + (urgency ? 1 : 0) + (secretRequest ? 1 : 0);
        if (!safetyEducation && ((hasUrl && bankOrAccount && threat && urgency) || (hasUrl && secretRequest && (bankOrAccount || urgency)) || screenshotSignals >= 4)) {
            a.score = Math.max(a.score, 85);
            a.risk = "HIGH";
            if (!a.signs.contains("Screenshot-specific detection: suspicious link combined with account threat and urgency")) a.signs.add("Screenshot-specific detection: suspicious link combined with account threat and urgency");
            if (!a.signs.contains("OCR text includes multiple independent phishing indicators")) a.signs.add("OCR text includes multiple independent phishing indicators");
            a.tips.clear();
            a.tips.add("STOP: Do not click the link, reply, transfer money or share any code.");
            a.tips.add("Verify using the official bank/company app or a website address you type yourself.");
            a.tips.add("Block and report the sender. If money was lost in India, contact your bank and call 1930.");
        }
        autoSaveReport(selectedScreenshot == null ? "Screenshot Scan" : selectedScreenshot.getName(), "Screenshot Scan", text, a);
        StringBuilder result = new StringBuilder();
        result.append("SCREENSHOT TEXT RISK RESULT\n");
        result.append("Risk level: ").append(a.risk).append("\nRisk score: ").append(a.score).append(" / 100\n\n");
        result.append("WARNING SIGNS\n");
        if (a.signs.isEmpty()) result.append("• No configured warning pattern was detected. This does not prove the message is safe.\n");
        else for (String sign : a.signs) result.append("• ").append(sign).append("\n");
        result.append("\nRECOMMENDATIONS\n");
        for (String tip : a.tips) result.append("• ").append(tip).append("\n");
        JTextArea output = new JTextArea(result.toString(), 16, 54); output.setEditable(false); output.setLineWrap(true); output.setWrapStyleWord(true); output.setFont(new Font("SansSerif", Font.PLAIN, 13)); output.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(output), "Screenshot risk analysis — " + a.risk, JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel buildReports() {
        JPanel page = pagePanel();
        page.add(pageTitle("Report Library", "Search and manage saved scam-awareness reports"), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(10, 12)); body.setBackground(BG);
        JPanel tools = cardPanel();
        tools.setLayout(new BorderLayout(10, 10));
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); searchRow.setOpaque(false);
        searchField = new JTextField(24); styleInput(searchField); searchField.setToolTipText("Search message, sender, category or risk");
        riskFilter = new JComboBox<>(new String[]{"All risks", "HIGH", "MEDIUM", "LOW"});
        riskFilter.setFont(BODY);
        JButton search = primaryButton("Search");
        JButton reset = secondaryButton("Reset");
        JButton export = secondaryButton("Export CSV");
        JButton deleteAll = dangerButton("Delete all history");
        search.addActionListener(e -> refreshReports());
        searchField.addActionListener(e -> refreshReports());
        riskFilter.addActionListener(e -> refreshReports());
        reset.addActionListener(e -> { searchField.setText(""); riskFilter.setSelectedIndex(0); refreshReports(); });
        export.addActionListener(e -> exportReports());
        deleteAll.addActionListener(e -> deleteAllReports());
        searchRow.add(new JLabel("Search:")); searchRow.add(searchField); searchRow.add(riskFilter); searchRow.add(search); searchRow.add(reset); searchRow.add(export); searchRow.add(deleteAll);
        tools.add(searchRow, BorderLayout.CENTER);
        body.add(tools, BorderLayout.NORTH);

        String[] cols = {"ID", "Risk", "Score", "Category", "Sender", "Message preview", "Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        reportsTable = new JTable(tableModel);
        reportsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        reportsTable.setRowHeight(32);
        reportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        reportsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        reportsTable.getTableHeader().setBackground(new Color(226, 236, 250));
        reportsTable.getTableHeader().setForeground(NAVY);
        reportsTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        reportsTable.getColumnModel().getColumn(1).setPreferredWidth(75);
        reportsTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        reportsTable.getColumnModel().getColumn(3).setPreferredWidth(125);
        reportsTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        reportsTable.getColumnModel().getColumn(5).setPreferredWidth(360);
        JScrollPane tableScroll = new JScrollPane(reportsTable);
        tableScroll.setBorder(new LineBorder(new Color(221, 230, 242)));
        body.add(tableScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        reportStatus = new JLabel("Connect to MySQL to load reports.");
        reportStatus.setFont(BODY); reportStatus.setForeground(new Color(71, 85, 105));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false);
        JButton view = secondaryButton("View full report");
        JButton delete = dangerButton("Delete selected");
        view.addActionListener(e -> viewSelectedReport());
        delete.addActionListener(e -> deleteSelectedReport());
        actions.add(view); actions.add(delete);
        bottom.add(reportStatus, BorderLayout.WEST); bottom.add(actions, BorderLayout.EAST);
        body.add(bottom, BorderLayout.SOUTH);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildSafetyGuide() {
        JPanel page = pagePanel();
        page.add(pageTitle("Safety Guide", "Simple habits that reduce your risk of digital fraud"), BorderLayout.NORTH);
        JPanel list = new JPanel();
        list.setBackground(BG);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(guideItem("1. Never share OTPs or UPI PINs", "Banks, payment apps and customer-support staff should never ask you to disclose an OTP, UPI PIN, password or screen-sharing code."));
        list.add(guideItem("2. Do not rush because of threats", "Scammers create urgency: account blocked, KYC expired, parcel held, prize expiring or police action. Stop and verify through official channels."));
        list.add(guideItem("3. Check the link carefully", "Look for misspellings, extra words, strange domains and shortened links. Do not enter credentials after opening a link from an unsolicited message."));
        list.add(guideItem("4. Never install remote-access apps on request", "A stranger who asks you to install a screen-sharing or remote-control app may be trying to control your device or payments."));
        list.add(guideItem("5. Verify job offers and prizes", "Be suspicious of guaranteed earnings, interview-free jobs, prize claims you did not enter, and requests for an upfront processing fee."));
        list.add(guideItem("6. If you already paid, act quickly", "Contact your bank or payment provider using its official app or published number. In India, call 1930 promptly for cyber financial fraud and report at cybercrime.gov.in."));
        list.add(guideItem("7. Keep evidence", "Save the message, sender ID, transaction reference, timestamps and screenshots. Do not forward suspicious links to others."));
        page.add(list, BorderLayout.CENTER);
        return page;
    }

    private JPanel guideItem(String title, String desc) {
        JPanel p = cardPanel();
        p.setLayout(new BorderLayout(12, 4));
        JLabel t = new JLabel(title); t.setFont(BOLD); t.setForeground(NAVY);
        JTextArea d = new JTextArea(desc); d.setFont(BODY); d.setForeground(new Color(71, 85, 105));
        d.setLineWrap(true); d.setWrapStyleWord(true); d.setEditable(false); d.setOpaque(false); d.setRows(2);
        p.add(t, BorderLayout.NORTH); p.add(d, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        return p;
    }

    private JPanel buildSettings() {
        JPanel page = pagePanel();
        page.add(pageTitle("Settings", "Configure and test your MySQL JDBC connection"), BorderLayout.NORTH);
        JPanel body = new JPanel(); body.setBackground(BG); body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JPanel conn = cardPanel(); conn.setLayout(new BoxLayout(conn, BoxLayout.Y_AXIS));
        conn.add(sectionHeading("Database connection"));
        JTextArea instructions = new JTextArea("Connection settings are read from config/db.properties. You can also override them with environment variables: FAMILYSHIELD_DB_URL, FAMILYSHIELD_DB_USER and FAMILYSHIELD_DB_PASSWORD. Restart the application after changing settings.");
        instructions.setLineWrap(true); instructions.setWrapStyleWord(true); instructions.setEditable(false); instructions.setOpaque(false); instructions.setFont(BODY); instructions.setRows(3);
        conn.add(Box.createVerticalStrut(8)); conn.add(instructions); conn.add(Box.createVerticalStrut(12));
        JTextArea current = new JTextArea(db.getConfigSummary());
        current.setEditable(false); current.setFont(new Font("Monospaced", Font.PLAIN, 13)); current.setBackground(new Color(246, 249, 255)); current.setBorder(new EmptyBorder(10, 10, 10, 10)); current.setRows(4);
        conn.add(current); conn.add(Box.createVerticalStrut(12));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); row.setOpaque(false);
        JButton test = primaryButton("Test database connection");
        JButton init = secondaryButton("Create / verify tables");
        JButton refresh = secondaryButton("Refresh dashboard");
        test.addActionListener(e -> testConnection(true));
        init.addActionListener(e -> {
            try { db.initializeSchema(); JOptionPane.showMessageDialog(this, "Tables are ready.", "Database", JOptionPane.INFORMATION_MESSAGE); refreshDashboard(); refreshReports(); }
            catch (Exception ex) { showDbError(ex); }
        });
        refresh.addActionListener(e -> { refreshDashboard(); refreshReports(); });
        row.add(test); row.add(init); row.add(refresh); conn.add(row);
        body.add(conn);
        JPanel notes = cardPanel(); notes.setLayout(new BoxLayout(notes, BoxLayout.Y_AXIS));
        notes.add(sectionHeading("Project notes"));
        notes.add(bulletLabel("Backend: MySQL using JDBC prepared statements.", BLUE));
        notes.add(bulletLabel("The message scoring engine runs locally in Java; no message is sent to an external API.", BLUE));
        notes.add(bulletLabel("Use a dedicated database user with permissions only on the familyshield database.", BLUE));
        notes.add(bulletLabel("A database connection is required for automatic history, viewing, deleting and counting saved reports.", BLUE));
        body.add(Box.createVerticalStrut(15)); body.add(notes);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private JPanel pagePanel() {
        JPanel p = new JPanel(new BorderLayout(18, 18));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 16, 14, 16));
        return p;
    }
    private JPanel pageTitle(String title, String subtitle) {
        JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel h = new JLabel(title); h.setFont(H1); h.setForeground(NAVY);
        JLabel s = new JLabel(subtitle); s.setFont(BODY); s.setForeground(new Color(100, 116, 139));
        p.add(h); p.add(Box.createVerticalStrut(5)); p.add(s); p.add(Box.createVerticalStrut(18));
        return p;
    }
    private JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(228, 233, 242), 1, true), new EmptyBorder(18, 19, 18, 19)));
        return p;
    }
    private JLabel sectionHeading(String text) {
        JLabel l = new JLabel(text); l.setFont(H2); l.setForeground(NAVY); return l;
    }
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(BOLD); l.setForeground(new Color(51, 65, 85)); return l;
    }
    private JLabel wrapLabel(String text) {
        JLabel l = new JLabel("<html><div style='width:700px'>" + htmlEscape(text) + "</div></html>");
        l.setFont(BODY); l.setForeground(new Color(71, 85, 105)); l.setBorder(new EmptyBorder(7, 0, 7, 0)); return l;
    }
    private JLabel bulletLabel(String text, Color color) {
        JLabel l = new JLabel("<html><div style='width:760px'>• " + htmlEscape(text) + "</div></html>");
        l.setFont(BODY); l.setForeground(new Color(55, 65, 81)); l.setBorder(new EmptyBorder(4, 0, 4, 0)); return l;
    }
    private void styleInput(JTextField field) {
        field.setFont(BODY);
        field.setPreferredSize(new Dimension(250, 36));
        field.setBorder(new CompoundBorder(new LineBorder(new Color(203, 213, 225)), new EmptyBorder(5, 8, 5, 8)));
    }
    private JButton primaryButton(String text) { return styledButton(text, new Color(219, 234, 254), NAVY); }
    private JButton secondaryButton(String text) { return styledButton(text, new Color(235, 242, 252), NAVY); }
    private JButton dangerButton(String text) { return styledButton(text, new Color(254, 226, 226), RED); }
    private JButton styledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(NAVY); b.setBackground(bg); b.setOpaque(true);
        b.setContentAreaFilled(true); b.setEnabled(true);
        b.setBorder(new CompoundBorder(new LineBorder(new Color(180, 196, 218)), new EmptyBorder(9, 13, 9, 13)));
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Persist each completed analysis so the Report Library is a history, not just a manual-save list. */
    private void autoSaveReport(String sender, String category, String message, Analysis analysis) {
        if (message == null || message.trim().isEmpty() || analysis == null) return;
        try {
            long id = db.insertReport(sender, category, message, analysis);
            if (reportStatus != null) reportStatus.setText("Latest work auto-saved • Report #" + id);
            if (connectionLabel != null) { connectionLabel.setText("● MySQL connected • Saved #" + id); connectionLabel.setForeground(GREEN); }
            refreshDashboard();
            refreshReports();
        } catch (Exception ex) {
            if (connectionLabel != null) { connectionLabel.setText("● History not saved — check MySQL"); connectionLabel.setForeground(RED); }
            if (reportStatus != null) reportStatus.setText("Could not auto-save: check MySQL settings and connection");
            JOptionPane.showMessageDialog(this,
                "The analysis was completed, but it could not be saved to Report Library.\n\n" + ex.getMessage() +
                "\n\nCheck that MySQL is running and config/db.properties is correct.",
                "History not saved", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveReport() {
        if (latestAnalysis == null) return;
        try {
            String sender = sourceField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            long id = db.insertReport(sender, category, messageArea.getText().trim(), latestAnalysis);
            JOptionPane.showMessageDialog(this, "Report saved successfully. Report ID: " + id, "Saved", JOptionPane.INFORMATION_MESSAGE);
            refreshDashboard(); refreshReports();
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void viewSelectedReport() {
        int row = reportsTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a report first."); return; }
        long id = ((Number) tableModel.getValueAt(reportsTable.convertRowIndexToModel(row), 0)).longValue();
        try {
            Report r = db.getReport(id);
            if (r == null) { JOptionPane.showMessageDialog(this, "That report no longer exists."); return; }
            JTextArea text = new JTextArea(18, 70);
            text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true); text.setFont(BODY);
            text.setText("REPORT ID: " + r.id + "\nRISK: " + r.risk + " (" + r.score + "/100)\nCATEGORY: " + r.category +
                "\nSENDER: " + r.sender + "\nCREATED: " + r.createdAt + "\n\nMESSAGE:\n" + r.message +
                "\n\nWARNING SIGNS:\n" + r.signs + "\n\nRECOMMENDATIONS:\n" + r.tips);
            text.setCaretPosition(0);
            JOptionPane.showMessageDialog(this, new JScrollPane(text), "Full report #" + id, JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { showDbError(ex); }
    }

    private void deleteSelectedReport() {
        int row = reportsTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a report first."); return; }
        long id = ((Number) tableModel.getValueAt(reportsTable.convertRowIndexToModel(row), 0)).longValue();
        int confirm = JOptionPane.showConfirmDialog(this, "Delete report #" + id + "? This cannot be undone.", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try { db.deleteReport(id); refreshReports(); refreshDashboard(); }
        catch (Exception ex) { showDbError(ex); }
    }

    private void deleteAllReports() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete ALL saved report history from the database? This cannot be undone.",
            "Confirm delete all history", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try { int deleted = db.deleteAllReports(); refreshReports(); refreshDashboard(); reportStatus.setText(deleted + " saved report(s) deleted"); }
        catch (Exception ex) { showDbError(ex); }
    }

    private void refreshDashboard() {
        if (totalLabel == null) return;
        try {
            Map<String, Integer> counts = db.getCounts();
            totalLabel.setText(String.valueOf(counts.get("TOTAL")));
            highLabel.setText(String.valueOf(counts.get("HIGH")));
            mediumLabel.setText(String.valueOf(counts.get("MEDIUM")));
            safeLabel.setText(String.valueOf(counts.get("LOW")));
        } catch (Exception ex) {
            totalLabel.setText("—"); highLabel.setText("—"); mediumLabel.setText("—"); safeLabel.setText("—");
        }
    }

    private void refreshReports() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        try {
            String query = searchField == null ? "" : searchField.getText().trim();
            String risk = riskFilter == null || riskFilter.getSelectedIndex() == 0 ? "" : (String) riskFilter.getSelectedItem();
            List<Report> reports = db.searchReports(query, risk);
            for (Report r : reports) {
                String preview = r.message.replaceAll("\\s+", " ");
                if (preview.length() > 100) preview = preview.substring(0, 97) + "...";
                tableModel.addRow(new Object[]{r.id, r.risk, r.score, r.category, r.sender, preview, r.createdAt});
            }
            reportStatus.setText(reports.size() + " report(s) loaded");
        } catch (Exception ex) {
            reportStatus.setText("Cannot load reports: check MySQL connection in Settings.");
        }
    }

    private void testConnection(boolean showMessage) {
        try {
            db.initializeSchema();
            connectionLabel.setText("● MySQL connected");
            connectionLabel.setForeground(GREEN);
            refreshDashboard();
            if (showMessage) JOptionPane.showMessageDialog(this, "Connected to MySQL. Tables are ready.", "Connection successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            connectionLabel.setText("● MySQL not connected");
            connectionLabel.setForeground(RED);
            if (showMessage) showDbError(ex);
        }
    }

    private void showDbError(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.trim().isEmpty()) msg = ex.getClass().getSimpleName();
        JOptionPane.showMessageDialog(this,
            "The database operation could not be completed.\n\n" + msg +
            "\n\nCheck that MySQL is running, database/schema.sql has been executed, and config/db.properties is correct.",
            "Database error", JOptionPane.ERROR_MESSAGE);
        connectionLabel.setText("● MySQL connection issue");
        connectionLabel.setForeground(RED);
    }

    private void exportAnalysis() {
        if (latestAnalysis == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("familyshield-analysis.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Analysis a = latestAnalysis;
        String text = "FamilyShield Analysis\nDate: " + LocalDateTime.now().format(DATE_FORMAT) + "\nRisk: " + a.risk +
            "\nScore: " + a.score + "/100\n\nMessage:\n" + messageArea.getText() + "\n\nWarning signs:\n- " +
            String.join("\n- ", a.signs) + "\n\nRecommended next steps:\n- " + String.join("\n- ", a.tips) +
            "\n\nEducational indicator only; not a verified fraud diagnosis.\n";
        try { Files.write(chooser.getSelectedFile().toPath(), text.getBytes(StandardCharsets.UTF_8)); JOptionPane.showMessageDialog(this, "Analysis exported."); }
        catch (IOException ex) { JOptionPane.showMessageDialog(this, "Could not write file: " + ex.getMessage(), "Export error", JOptionPane.ERROR_MESSAGE); }
    }

    private void exportReports() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("familyshield-reports.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            List<Report> rows = db.searchReports(searchField.getText().trim(), riskFilter.getSelectedIndex() == 0 ? "" : (String) riskFilter.getSelectedItem());
            StringBuilder csv = new StringBuilder("ID,Risk,Score,Category,Sender,Message,Created At,Warning Signs,Recommendations\n");
            for (Report r : rows) {
                csv.append(r.id).append(',').append(csvCell(r.risk)).append(',').append(r.score).append(',')
                    .append(csvCell(r.category)).append(',').append(csvCell(r.sender)).append(',')
                    .append(csvCell(r.message)).append(',').append(csvCell(r.createdAt)).append(',')
                    .append(csvCell(r.signs)).append(',').append(csvCell(r.tips)).append('\n');
            }
            Files.write(chooser.getSelectedFile().toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, rows.size() + " report(s) exported.");
        } catch (Exception ex) { showDbError(ex); }
    }
    private static String csvCell(String s) { return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\""; }
    private static String htmlEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FamilyShieldApp().setVisible(true));
    }

    private static class GradientHeroPanel extends JPanel {
        GradientHeroPanel() { setOpaque(false); }
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, new Color(29, 78, 216), getWidth(), getHeight(), new Color(14, 165, 233)));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g.setColor(new Color(255, 255, 255, 25));
            g.fillOval(getWidth() - 180, -70, 230, 230);
            g.fillOval(getWidth() - 115, 65, 145, 145);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    static class Analysis {
        int score; String risk; List<String> signs = new ArrayList<>(); List<String> tips = new ArrayList<>();
    }
    static class ScamAnalyzer {
        /** Rule-based scam indicator. Strong credential/payment requests trigger HIGH risk. */
        static Analysis analyze(String message) {
            String m = message == null ? "" : message.toLowerCase(Locale.ROOT);
            Analysis a = new Analysis();
            int score = 0;
            String[][] patterns = {
                {"\\b(otp|one[- ]time password|verification code|security code)\\b.{0,55}(share|send|tell|reply|provide|enter|give)|(?:share|send|tell|reply|provide|enter|give).{0,55}\\b(otp|one[- ]time password|verification code|security code)\\b", "Requests an OTP or verification code", "45"},
                {"\\b(upi pin|atm pin|debit card pin|credit card pin|cvv|cvc|card number|net banking password|bank password|password)\\b.{0,55}(share|send|provide|enter|confirm|verify|reply|tell)|(?:share|send|provide|enter|confirm|verify|reply|tell).{0,55}\\b(upi pin|atm pin|debit card pin|credit card pin|cvv|cvc|password)\\b", "Requests sensitive PIN, CVV or password information", "45"},
                {"\\b(account|bank account|card|wallet)\\b.{0,35}(blocked|suspended|frozen|disabled|closed)|(?:blocked|suspended|frozen|disabled).{0,30}\\b(account|bank|card)\\b|\\b(kyc|know your customer)\\b.{0,30}(expire|expired|update|verify|suspend|block)", "Account-blocking or urgent KYC warning", "24"},
                {"\\b(urgent|immediately|within 24 hours|today only|last warning|act now|final notice|expires today|limited time|right now)\\b", "Creates urgency or pressure to act quickly", "16"},
                {"\\b(prize|won|winner|lottery|claim your reward|free gift|cashback|you have been selected|congratulations)\\b", "Unexpected prize, reward or cashback claim", "18"},
                {"\\b(click|tap|open|visit|follow)\\b.{0,30}\\b(link|here|url|below)\\b|https?://|www\\.", "Contains a link or asks you to visit a web address", "12"},
                {"\\b(processing fee|registration fee|pay first|advance payment|deposit to get|release fee|customs fee|pay to receive)\\b", "Requests an advance fee or payment", "30"},
                {"\\b(work from home|earn.{0,15}(daily|per day)|guaranteed income|no experience required|double your money|guaranteed profit)\\b", "Promises unusually easy or guaranteed earnings", "20"},
                {"\\b(remote access|screen sharing|anydesk|teamviewer|install this app|download this apk)\\b", "Requests remote-access software or an untrusted app installation", "30"},
                {"\\b(police case|arrest warrant|digital arrest|pay fine now|legal action|court case)\\b", "Uses threats of arrest or legal action to pressure you", "24"},
                {"\\b(parcel held|delivery failed|customs fee|address confirmation|package is on hold)\\b", "Unexpected delivery or parcel fee / verification request", "16"},
                {"\\b(bank|payment|refund|wallet|upi|kyc|credit card|debit card|transaction)\\b", "Mentions a financial account or payment context", "8"},
                {"\\b(refund|cashback|reimbursement)\\b.{0,40}\\b(upi pin|otp|password|cvv)\\b", "Combines a refund claim with a request for secret credentials", "35"},
                {"\\b(collect request|approve request|receive money)\\b.{0,50}\\b(upi|pin|scan|qr)\\b", "Payment instruction may trick you into authorizing a payment", "28"},
                {"\\b(bit\\.ly|tinyurl\\.com|t\\.co|cutt\\.ly|is\\.gd|rb\\.gy|shorturl)\\b", "Contains a shortened link that hides the destination", "20"},
                {"[a-z0-9-]+\\.(xyz|top|click|loan|work|support|gq|tk|zip)(/.*|\\b)", "Link may use an unusual or higher-risk domain ending", "18"}
            };
            for (String[] pattern : patterns) {
                if (m.matches("(?s).*?(" + pattern[0] + ").*")) {
                    a.signs.add(pattern[1]);
                    score += Integer.parseInt(pattern[2]);
                }
            }
            boolean negatedAdviceForScore = m.matches("(?s).*(never|do not|don't|dont|avoid|never ever).{0,30}(share|send|tell|provide|give|reveal).{0,30}(otp|one[- ]time password|upi pin|atm pin|cvv|cvc|password|verification code|security code).*");
            if (negatedAdviceForScore) {
                if (a.signs.remove("Requests an OTP or verification code")) score = Math.max(0, score - 45);
                if (a.signs.remove("Requests sensitive PIN, CVV or password information")) score = Math.max(0, score - 45);
            }
            boolean secretTerm = m.matches("(?s).*(otp|one[- ]time password|upi pin|atm pin|cvv|cvc|bank password|net banking password|verification code|security code).*");
            boolean negatedSafetyAdvice = m.matches("(?s).*(never|do not|don\'t|dont|avoid|never ever).{0,30}(share|send|tell|provide|give|reveal).{0,30}(otp|one[- ]time password|upi pin|atm pin|cvv|cvc|password|verification code|security code).*");
            boolean asksSecret = !negatedSafetyAdvice && secretTerm && m.matches("(?s).*(share|send|tell|reply|provide|enter|give|confirm|verify|submit|type|click|provide us|send us).*" ) && m.matches("(?s).*(otp|one[- ]time password|upi pin|atm pin|cvv|cvc|bank password|net banking password|verification code|security code).*");
            boolean asksMoney = m.matches("(?s).*(transfer|send money|pay now|payment|processing fee|deposit|upi|bank account|wallet|refund|cashback).*");
            boolean pressure = m.matches("(?s).*(urgent|immediately|act now|blocked|suspended|expire today|final notice|within 24 hours|arrest|police case).*");
            boolean link = m.matches("(?s).*(https?://|www\\.|click here|tap here|open this link|verify now|login here).*");
            if (asksSecret) {
                score = Math.max(score, 72);
                addOnce(a.signs, "Contains credential-related wording; never share OTPs, PINs, CVVs or passwords");
            }
            if ((asksMoney && pressure && link) || (asksMoney && pressure && asksSecret) || (asksMoney && link && m.matches("(?s).*(pay|transfer|deposit|fee|upi|bank).*(urgent|immediately|blocked|verify|claim).*"))) {
                score = Math.max(score, 68);
                addOnce(a.signs, "Combines financial action with urgency or a link — a strong phishing/scam pattern");
            }
            if (m.matches("(?s).*(digital arrest|pay.*to.*release|police.*(upi|transfer|pay)|remote access.*bank).*") ) {
                score = Math.max(score, 75);
                addOnce(a.signs, "High-risk coercion or remote-access scam pattern");
            }
            // Catch common high-risk scam constructions even when OCR text or wording is imperfect.
            boolean hasLink = m.matches("(?s).*(https?://|www\\.|bit\\.ly|tinyurl|cutt\\.ly|click here|tap here|link below).*");
            boolean accountThreat = m.matches("(?s).*(account|bank|card|upi|wallet|sim|parcel|delivery).{0,45}(blocked|suspended|frozen|deactivated|will be closed|held|failed|expire|expired).*") || m.matches("(?s).*(blocked|suspended|frozen|deactivated).{0,45}(account|bank|card|upi|wallet).*");
            boolean credentialAsk = m.matches("(?s).*(enter|share|send|provide|confirm|verify|submit|update).{0,55}(otp|password|pin|cvv|card number|bank details|upi pin|verification code).*") || m.matches("(?s).*(otp|password|pin|cvv|card number|bank details|upi pin|verification code).{0,55}(enter|share|send|provide|confirm|verify|submit|update).*");
            boolean moneyAndUrgency = m.matches("(?s).*(pay|payment|fee|transfer|upi|bank|refund|prize|reward|wallet|cashback).*") && m.matches("(?s).*(urgent|immediately|today|within \\d+ hours|act now|blocked|suspended|expire|final warning|last chance).*");
            boolean safetyWarningOnly = m.matches("(?s).*(never|do not|don't|dont|avoid).{0,45}(click|open|share|send|enter|provide).{0,45}(link|otp|password|pin|cvv|code).*") && !credentialAsk;
            if (!safetyWarningOnly && hasLink && accountThreat) {
                score = Math.max(score, 70);
                addOnce(a.signs, "High-risk pattern: a link is combined with an account, bank or delivery threat");
            }
            if (!safetyWarningOnly && credentialAsk && (hasLink || accountThreat || moneyAndUrgency)) {
                score = Math.max(score, 78);
                addOnce(a.signs, "High-risk pattern: credential request combined with a link, threat or financial pressure");
            }
            if (!safetyWarningOnly && moneyAndUrgency && hasLink) {
                score = Math.max(score, 72);
                addOnce(a.signs, "High-risk pattern: financial action and urgency combined with a link");
            }
            a.score = Math.min(100, score);
            a.risk = a.score >= 50 ? "HIGH" : a.score >= 20 ? "MEDIUM" : "LOW";
            if (a.risk.equals("HIGH")) {
                a.tips.add("STOP: Do not click the link, reply, transfer money, install an app or share a code.");
                a.tips.add("Verify independently through the official app, a known website address or a published phone number.");
                a.tips.add("Block and report the sender. If money was lost in India, contact your bank immediately and call 1930 or report at cybercrime.gov.in.");
            } else if (a.risk.equals("MEDIUM")) {
                a.tips.add("Pause and independently verify the sender and request before acting.");
                a.tips.add("Do not open unexpected links or provide private account information.");
                a.tips.add("Contact the company through its official app or known website, not through the message.");
            } else {
                a.tips.add("No strong configured warning pattern was found; this does not prove the message is safe.");
                a.tips.add("Check the sender, destination URL and context. Never share OTPs, PINs or passwords.");
                a.tips.add("When in doubt, verify through an official channel instead of using the message link.");
            }
            return a;
        }
        private static void addOnce(List<String> list, String value) { if (!list.contains(value)) list.add(value); }
    }

    static class Report {
        long id; String risk, category, sender, message, signs, tips, createdAt; int score;
    }

    static class Database {
        private String url = "jdbc:mysql://localhost:3306/familyshield?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        private String user = "root";
        private String password = "";
        Database() {
            Properties p = new Properties();
            File f = new File("config/db.properties");
            if (f.exists()) {
                try (InputStream in = new FileInputStream(f)) { p.load(in); } catch (IOException ignored) {}
            }
            url = p.getProperty("db.url", url);
            user = p.getProperty("db.user", user);
            password = p.getProperty("db.password", password);
            url = System.getenv().getOrDefault("FAMILYSHIELD_DB_URL", url);
            user = System.getenv().getOrDefault("FAMILYSHIELD_DB_USER", user);
            password = System.getenv().getOrDefault("FAMILYSHIELD_DB_PASSWORD", password);
        }
        String getConfigSummary() {
            return "JDBC URL : " + url + "\nDB User  : " + user + "\nPassword : " + (password.isEmpty() ? "(empty)" : "********") +
                "\nConfig   : " + new File("config/db.properties").getAbsolutePath();
        }
        Connection connect() throws SQLException {
            try { Class.forName("com.mysql.cj.jdbc.Driver"); }
            catch (ClassNotFoundException e) { throw new SQLException("MySQL Connector/J was not found. Run with Maven (mvn package) or add mysql-connector-j to the classpath.", e); }
            return DriverManager.getConnection(url, user, password);
        }
        void initializeSchema() throws SQLException {
            try (Connection c = connect(); Statement s = c.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS scam_reports (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "risk_level VARCHAR(10) NOT NULL," +
                    "risk_score INT NOT NULL," +
                    "category VARCHAR(80) NOT NULL," +
                    "sender VARCHAR(255) NOT NULL DEFAULT ''," +
                    "message_text TEXT NOT NULL," +
                    "warning_signs TEXT," +
                    "recommendations TEXT," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
        }
        long insertReport(String sender, String category, String message, Analysis a) throws SQLException {
            String sql = "INSERT INTO scam_reports(risk_level,risk_score,category,sender,message_text,warning_signs,recommendations) VALUES(?,?,?,?,?,?,?)";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, a.risk); ps.setInt(2, a.score); ps.setString(3, category == null ? "Other" : category);
                ps.setString(4, sender == null ? "" : sender); ps.setString(5, message);
                ps.setString(6, String.join("\n", a.signs)); ps.setString(7, String.join("\n", a.tips));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getLong(1); }
            }
            throw new SQLException("Report was inserted but the generated ID was not returned.");
        }
        Map<String, Integer> getCounts() throws SQLException {
            Map<String, Integer> m = new HashMap<>();
            m.put("TOTAL", 0); m.put("HIGH", 0); m.put("MEDIUM", 0); m.put("LOW", 0);
            String sql = "SELECT risk_level, COUNT(*) AS n FROM scam_reports GROUP BY risk_level";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String risk = rs.getString("risk_level"); int n = rs.getInt("n");
                    m.put(risk, n); m.put("TOTAL", m.get("TOTAL") + n);
                }
            }
            return m;
        }
        List<Report> searchReports(String query, String risk) throws SQLException {
            List<Report> out = new ArrayList<>();
            StringBuilder sql = new StringBuilder("SELECT id,risk_level,risk_score,category,sender,message_text,warning_signs,recommendations,created_at FROM scam_reports WHERE 1=1");
            if (query != null && !query.trim().isEmpty()) sql.append(" AND (message_text LIKE ? OR sender LIKE ? OR category LIKE ? OR risk_level LIKE ?)");
            if (risk != null && !risk.trim().isEmpty()) sql.append(" AND risk_level=?");
            sql.append(" ORDER BY created_at DESC, id DESC LIMIT 1000");
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
                int i = 1;
                if (query != null && !query.trim().isEmpty()) {
                    String q = "%" + query.trim() + "%";
                    ps.setString(i++, q); ps.setString(i++, q); ps.setString(i++, q); ps.setString(i++, q);
                }
                if (risk != null && !risk.trim().isEmpty()) ps.setString(i, risk);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(fromResult(rs)); }
            }
            return out;
        }
        Report getReport(long id) throws SQLException {
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("SELECT id,risk_level,risk_score,category,sender,message_text,warning_signs,recommendations,created_at FROM scam_reports WHERE id=?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) { return rs.next() ? fromResult(rs) : null; }
            }
        }
        void deleteReport(long id) throws SQLException {
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM scam_reports WHERE id=?")) {
                ps.setLong(1, id);
                if (ps.executeUpdate() == 0) throw new SQLException("No report with ID " + id + " was found.");
            }
        }
        int deleteAllReports() throws SQLException {
            try (Connection c = connect(); Statement s = c.createStatement()) {
                return s.executeUpdate("DELETE FROM scam_reports");
            }
        }
        private Report fromResult(ResultSet rs) throws SQLException {
            Report r = new Report();
            r.id = rs.getLong("id"); r.risk = rs.getString("risk_level"); r.score = rs.getInt("risk_score");
            r.category = rs.getString("category"); r.sender = rs.getString("sender"); r.message = rs.getString("message_text");
            r.signs = rs.getString("warning_signs"); r.tips = rs.getString("recommendations");
            Timestamp ts = rs.getTimestamp("created_at"); r.createdAt = ts == null ? "" : ts.toLocalDateTime().format(DATE_FORMAT);
            return r;
        }
    }
}
