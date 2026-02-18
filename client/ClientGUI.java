package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientGUI extends JFrame {

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton btnEnvoyer, btnIndice, btnRecommencer, btnQuitter;
    private JLabel scoreLabel;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private int score = 0;
    private String serverIP;

    public ClientGUI(String serverIP) {
        super("Jeu de Devinette");
        this.serverIP = serverIP;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        // ---- Couleurs ----
        Color bgColor = new Color(45, 45, 45);
        Color panelColor = new Color(60, 60, 60);
        Color accentColor = new Color(0, 150, 136);
        Color textColor = Color.WHITE;
        Color chatBg = new Color(30, 30, 30);

        getContentPane().setBackground(bgColor);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---- Header ----
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(accentColor);
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("Jeu de Devinette");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        scoreLabel = new JLabel("Score : 0");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setForeground(Color.YELLOW);
        headerPanel.add(scoreLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ---- Zone de chat ----
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        chatArea.setBackground(chatBg);
        chatArea.setForeground(Color.GREEN);
        chatArea.setCaretColor(Color.GREEN);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(panelColor, 2));
        add(scrollPane, BorderLayout.CENTER);

        // ---- Panneau bas (saisie + boutons) ----
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(bgColor);

        // Champ de saisie
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBackground(panelColor);
        inputField.setForeground(textColor);
        inputField.setCaretColor(textColor);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        // Boutons
        btnEnvoyer = createStyledButton("Envoyer", accentColor);
        btnIndice = createStyledButton("Indice", new Color(255, 152, 0));
        btnRecommencer = createStyledButton("Nouveau mot", new Color(33, 150, 243));
        btnQuitter = createStyledButton("Quitter", new Color(244, 67, 54));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        btnPanel.setBackground(bgColor);
        btnPanel.add(btnEnvoyer);
        btnPanel.add(btnIndice);
        btnPanel.add(btnRecommencer);
        btnPanel.add(btnQuitter);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // ---- Actions ----
        btnEnvoyer.addActionListener(e -> envoyerMessage());
        inputField.addActionListener(e -> envoyerMessage());
        btnIndice.addActionListener(e -> envoyerCommande("indice"));
        btnRecommencer.addActionListener(e -> envoyerCommande("recommencer"));
        btnQuitter.addActionListener(e -> {
            envoyerCommande("quitter");
            // Petit délai pour laisser le temps au message "Au revoir" d'arriver
            Timer timer = new Timer(500, ev -> System.exit(0));
            timer.setRepeats(false);
            timer.start();
        });

        // ---- Connexion au serveur ----
        connecter();
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 35));
        return btn;
    }

    private void connecter() {
        try {
            socket = new Socket(serverIP, 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            // Thread de lecture des messages du serveur
            Thread reader = new Thread(() -> {
                try {
                    String ligne;
                    while ((ligne = in.readLine()) != null) {
                        final String msg = ligne;

                        // Mettre à jour le score si le serveur envoie "SCORE:x"
                        if (msg.startsWith("SCORE:")) {
                            try {
                                score = Integer.parseInt(msg.substring(6));
                                SwingUtilities.invokeLater(() -> scoreLabel.setText("Score : " + score));
                            } catch (NumberFormatException ignored) {}
                            continue;
                        }

                        // Afficher le message dans la zone de chat
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append("  " + msg + "\n");
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        });
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> chatArea.append("[Déconnecté du serveur]\n"));
                }
            });
            reader.setDaemon(true);
            reader.start();

        } catch (IOException e) {
            chatArea.append("[Impossible de se connecter au serveur sur " + serverIP + ":5000]\n");
            chatArea.append("[Vérifiez que le serveur est lancé et que l'IP est correcte.]\n");
            btnEnvoyer.setEnabled(false);
            btnIndice.setEnabled(false);
            btnRecommencer.setEnabled(false);
        }
    }

    private void envoyerMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty() && out != null) {
            chatArea.append("Moi : " + msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            out.println(msg);
            inputField.setText("");
        }
        inputField.requestFocus();
    }

    private void envoyerCommande(String commande) {
        if (out != null) {
            chatArea.append("Moi : " + commande + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            out.println(commande);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // Demander l'IP du serveur
            String ip = JOptionPane.showInputDialog(
                    null,
                    "Entrez l'adresse IP du serveur :\n(ex: 192.168.1.10 ou localhost)",
                    "Connexion au serveur",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (ip == null || ip.trim().isEmpty()) {
                ip = "localhost";
            }
            new ClientGUI(ip.trim()).setVisible(true);
        });
    }
}
