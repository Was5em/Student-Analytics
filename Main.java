package securefile;

import securefile.controller.EncryptorController;
import securefile.model.EncryptedFile;
import securefile.util.FileUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

/**
 * SecureFile Encryptor — Main Swing GUI.
 * AES-256 file encryption with drag & drop support.
 */
public class Main extends JFrame {

    // ── Colors ────────────────────────────────────────────────────────────
    private static final Color BG_DARK     = new Color(15,  20,  30);
    private static final Color BG_PANEL    = new Color(22,  30,  45);
    private static final Color BG_CARD     = new Color(28,  38,  55);
    private static final Color BG_INPUT    = new Color(18,  25,  38);
    private static final Color TEAL        = new Color(20, 184, 166);
    private static final Color TEAL_DARK   = new Color(15, 140, 130);
    private static final Color TEAL_DIM    = new Color(20, 184, 166, 60);
    private static final Color RED         = new Color(239,  68,  68);
    private static final Color RED_DIM     = new Color(239,  68,  68, 40);
    private static final Color GREEN       = new Color( 34, 197,  94);
    private static final Color GREEN_DIM   = new Color( 34, 197,  94, 40);
    private static final Color AMBER       = new Color(251, 191,  36);
    private static final Color TEXT_PRI    = new Color(230, 235, 245);
    private static final Color TEXT_SEC    = new Color(140, 155, 175);
    private static final Color BORDER      = new Color(45,  60,  80);

    // ── State ─────────────────────────────────────────────────────────────
    private final EncryptorController controller = new EncryptorController();
    private File selectedFile = null;

    // ── Components ────────────────────────────────────────────────────────
    private JLabel   dropLabel;
    private JLabel   fileInfoLabel;
    private JLabel   fileIconLabel;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JLabel   confirmLabel;
    private JButton  encryptBtn;
    private JButton  decryptBtn;
    private JPanel   resultPanel;
    private JLabel   resultIcon;
    private JLabel   resultTitle;
    private JLabel   resultMsg;
    private JLabel   resultHash;
    private JPanel   dropZone;

    public Main() {
        super("SecureFile Encryptor — CARETECH");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 680);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);

        setVisible(true);
    }

    // ── Header ────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(18, 24, 18, 24)
        ));

        JLabel logo = new JLabel("🔐  SecureFile Encryptor");
        logo.setFont(new Font("Monospaced", Font.BOLD, 16));
        logo.setForeground(TEAL);

        JLabel sub = new JLabel("AES-256 · PBKDF2 · SHA-256");
        sub.setFont(new Font("Monospaced", Font.PLAIN, 11));
        sub.setForeground(TEXT_SEC);

        JPanel left = new JPanel();
        left.setBackground(BG_PANEL);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(logo);
        left.add(Box.createRigidArea(new Dimension(0, 3)));
        left.add(sub);

        JLabel badge = new JLabel("v1.0");
        badge.setFont(new Font("Monospaced", Font.PLAIN, 11));
        badge.setForeground(TEXT_SEC);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));

        header.add(left,  BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    // ── Body ──────────────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setBackground(BG_DARK);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        body.add(buildDropZone());
        body.add(Box.createRigidArea(new Dimension(0, 16)));
        body.add(buildPasswordSection());
        body.add(Box.createRigidArea(new Dimension(0, 16)));
        body.add(buildButtons());
        body.add(Box.createRigidArea(new Dimension(0, 16)));
        body.add(buildResultPanel());

        return body;
    }

    // ── Drop Zone ─────────────────────────────────────────────────────────

    private JPanel buildDropZone() {
        dropZone = new JPanel(new BorderLayout());
        dropZone.setBackground(BG_CARD);
        dropZone.setPreferredSize(new Dimension(0, 130));
        dropZone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        dropZone.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(BORDER, 4, 4),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        dropZone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel inner = new JPanel();
        inner.setBackground(BG_CARD);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setAlignmentX(Component.CENTER_ALIGNMENT);

        fileIconLabel = new JLabel("📂");
        fileIconLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));
        fileIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        dropLabel = new JLabel("Drop a file here or click to browse");
        dropLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dropLabel.setForeground(TEXT_SEC);
        dropLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        fileInfoLabel = new JLabel(" ");
        fileInfoLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        fileInfoLabel.setForeground(TEAL);
        fileInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(fileIconLabel);
        inner.add(Box.createRigidArea(new Dimension(0, 6)));
        inner.add(dropLabel);
        inner.add(Box.createRigidArea(new Dimension(0, 4)));
        inner.add(fileInfoLabel);

        dropZone.add(inner, BorderLayout.CENTER);

        // Click to browse
        dropZone.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { browseFile(); }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                dropZone.setBackground(new Color(32, 45, 65));
                inner.setBackground(new Color(32, 45, 65));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                dropZone.setBackground(BG_CARD);
                inner.setBackground(BG_CARD);
            }
        });

        // Drag & drop
        new DropTarget(dropZone, new DropTargetAdapter() {
            public void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> files = (List<File>) e.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) setSelectedFile(files.get(0));
                } catch (Exception ex) { ex.printStackTrace(); }
            }
            public void dragEnter(DropTargetDragEvent e) {
                dropZone.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createDashedBorder(TEAL, 4, 4),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)
                ));
            }
            public void dragExit(DropTargetEvent e) {
                dropZone.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createDashedBorder(BORDER, 4, 4),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)
                ));
            }
        });

        return dropZone;
    }

    // ── Password Section ──────────────────────────────────────────────────

    private JPanel buildPasswordSection() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JLabel passTitle = new JLabel("🔑  Password");
        passTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        passTitle.setForeground(TEXT_PRI);
        passTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(passTitle);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        passwordField = styledPasswordField("Enter encryption password...");
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passwordField);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        confirmLabel = new JLabel("Confirm password (for encryption):");
        confirmLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        confirmLabel.setForeground(TEXT_SEC);
        confirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(confirmLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));

        confirmField = styledPasswordField("Confirm password...");
        confirmField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(confirmField);

        return card;
    }

    // ── Buttons ───────────────────────────────────────────────────────────

    private JPanel buildButtons() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(BG_DARK);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        encryptBtn = actionButton("🔒  Encrypt File", TEAL, TEAL_DARK);
        decryptBtn = actionButton("🔓  Decrypt File", new Color(99, 102, 241), new Color(79, 82, 200));

        encryptBtn.addActionListener(e -> handleEncrypt());
        decryptBtn.addActionListener(e -> handleDecrypt());

        row.add(encryptBtn);
        row.add(decryptBtn);
        return row;
    }

    // ── Result Panel ──────────────────────────────────────────────────────

    private JPanel buildResultPanel() {
        resultPanel = new JPanel();
        resultPanel.setBackground(BG_CARD);
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        resultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        resultPanel.setVisible(false);

        resultIcon = new JLabel();
        resultIcon.setFont(new Font("SansSerif", Font.PLAIN, 22));
        resultIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultTitle = new JLabel();
        resultTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        resultTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultMsg = new JLabel();
        resultMsg.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultMsg.setForeground(TEXT_SEC);
        resultMsg.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultHash = new JLabel();
        resultHash.setFont(new Font("Monospaced", Font.PLAIN, 10));
        resultHash.setForeground(TEXT_SEC);
        resultHash.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultPanel.add(resultIcon);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        resultPanel.add(resultTitle);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        resultPanel.add(resultMsg);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        resultPanel.add(resultHash);

        return resultPanel;
    }

    // ── Footer ────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(BG_PANEL);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        JLabel info = new JLabel("CARETECH · AES-256 · Files never leave your device");
        info.setFont(new Font("Monospaced", Font.PLAIN, 10));
        info.setForeground(TEXT_SEC);
        footer.add(info);
        return footer;
    }

    // ── Logic ─────────────────────────────────────────────────────────────

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a file to encrypt or decrypt");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setSelectedFile(chooser.getSelectedFile());
        }
    }

    private void setSelectedFile(File file) {
        selectedFile = file;
        boolean isEnc = file.getName().endsWith(".enc");
        fileIconLabel.setText(isEnc ? "🔐" : "📄");
        dropLabel.setText(FileUtil.shortName(file, 40));
        dropLabel.setForeground(TEXT_PRI);
        fileInfoLabel.setText(FileUtil.readableSize(file.length())
                + "  ·  " + FileUtil.extension(file));
        resultPanel.setVisible(false);
    }

    private void handleEncrypt() {
        if (!validateInputs(true)) return;

        char[] pass    = passwordField.getPassword();
        encryptBtn.setEnabled(false);
        encryptBtn.setText("Encrypting...");

        SwingWorker<EncryptedFile, Void> worker = new SwingWorker<>() {
            protected EncryptedFile doInBackground() {
                return controller.encrypt(selectedFile, pass);
            }
            protected void done() {
                try {
                    showResult(get());
                } catch (Exception ex) {
                    showError("Unexpected error: " + ex.getMessage());
                } finally {
                    encryptBtn.setEnabled(true);
                    encryptBtn.setText("🔒  Encrypt File");
                }
            }
        };
        worker.execute();
    }

    private void handleDecrypt() {
        if (!validateInputs(false)) return;

        char[] pass    = passwordField.getPassword();
        decryptBtn.setEnabled(false);
        decryptBtn.setText("Decrypting...");

        SwingWorker<EncryptedFile, Void> worker = new SwingWorker<>() {
            protected EncryptedFile doInBackground() {
                return controller.decrypt(selectedFile, pass);
            }
            protected void done() {
                try {
                    showResult(get());
                } catch (Exception ex) {
                    showError("Unexpected error: " + ex.getMessage());
                } finally {
                    decryptBtn.setEnabled(true);
                    decryptBtn.setText("🔓  Decrypt File");
                }
            }
        };
        worker.execute();
    }

    private boolean validateInputs(boolean isEncrypt) {
        if (selectedFile == null) {
            showError("Please select a file first.");
            return false;
        }
        char[] pass = passwordField.getPassword();
        if (pass.length == 0) {
            showError("Password cannot be empty.");
            return false;
        }
        if (isEncrypt) {
            char[] confirm = confirmField.getPassword();
            if (!java.util.Arrays.equals(pass, confirm)) {
                showError("Passwords do not match.");
                return false;
            }
        }
        return true;
    }

    private void showResult(EncryptedFile result) {
        resultPanel.setVisible(true);
        if (result.isSuccess()) {
            resultPanel.setBackground(new Color(28, 45, 35));
            resultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(34, 197, 94, 80), 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
            ));
            resultIcon.setText("✅");
            resultTitle.setText(result.getOperation() == EncryptedFile.Operation.ENCRYPT
                    ? "Encrypted successfully" : "Decrypted successfully");
            resultTitle.setForeground(GREEN);
            resultMsg.setText("Saved: " + (result.getOutputFile() != null
                    ? result.getOutputFile().getName() : "—"));
            resultHash.setText("SHA-256: " + (result.getSha256Hash() != null
                    ? result.getSha256Hash().substring(0, 32) + "..." : "—"));
        } else {
            resultPanel.setBackground(new Color(45, 20, 22));
            resultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(239, 68, 68, 80), 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
            ));
            resultIcon.setText("❌");
            resultTitle.setText("Operation failed");
            resultTitle.setForeground(RED);
            resultMsg.setText(result.getMessage());
            resultHash.setText(" ");
        }
        revalidate();
        repaint();
    }

    private void showError(String msg) {
        resultPanel.setVisible(true);
        resultPanel.setBackground(new Color(45, 20, 22));
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(239, 68, 68, 80), 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        resultIcon.setText("⚠️");
        resultTitle.setText("Error");
        resultTitle.setForeground(AMBER);
        resultMsg.setText(msg);
        resultHash.setText(" ");
        revalidate();
        repaint();
    }

    // ── UI Helpers ────────────────────────────────────────────────────────

    private JPasswordField styledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Monospaced", Font.PLAIN, 13));
        field.setForeground(TEXT_PRI);
        field.setBackground(BG_INPUT);
        field.setCaretColor(TEAL);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return field;
    }

    private JButton actionButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    // ── Entry Point ───────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(Main::new);
    }
}
