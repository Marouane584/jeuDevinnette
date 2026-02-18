package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientHandler implements Runnable {

    private Socket socket;

    // Dictionnaire : mot -> liste d'indices (du plus vague au plus précis)
    private static final LinkedHashMap<String, List<String>> DICTIONNAIRE = new LinkedHashMap<>();

    static {
        DICTIONNAIRE.put("chien", Arrays.asList(
                "C'est un animal domestique.",
                "Il a 4 pattes.",
                "Il aboie.",
                "C'est le meilleur ami de l'homme."
        ));
        DICTIONNAIRE.put("voiture", Arrays.asList(
                "C'est un moyen de transport.",
                "Elle a 4 roues.",
                "Elle fonctionne avec un moteur.",
                "On la conduit sur la route."
        ));
        DICTIONNAIRE.put("ordinateur", Arrays.asList(
                "C'est une machine.",
                "Il sert à traiter de l'information.",
                "Il a un clavier et un écran.",
                "On peut programmer dessus."
        ));
        DICTIONNAIRE.put("pomme", Arrays.asList(
                "C'est un aliment.",
                "C'est un fruit.",
                "Elle peut être rouge, verte ou jaune.",
                "Elle pousse sur un pommier."
        ));
        DICTIONNAIRE.put("livre", Arrays.asList(
                "C'est un objet du quotidien.",
                "Il contient des pages.",
                "On y trouve du texte.",
                "On le lit pour apprendre ou se divertir."
        ));
        DICTIONNAIRE.put("soleil", Arrays.asList(
                "On le voit tous les jours.",
                "Il est dans le ciel.",
                "Il donne de la lumière.",
                "C'est une étoile."
        ));
        DICTIONNAIRE.put("guitare", Arrays.asList(
                "C'est un objet culturel.",
                "Elle produit du son.",
                "Elle a des cordes.",
                "C'est un instrument de musique."
        ));
        DICTIONNAIRE.put("montagne", Arrays.asList(
                "C'est un élément naturel.",
                "On peut y faire de la randonnée.",
                "Elle est très haute.",
                "L'Everest en est la plus célèbre."
        ));
    }

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String clientAddr = socket.getRemoteSocketAddress().toString();
        Random random = new Random();
        List<String> mots = new ArrayList<>(DICTIONNAIRE.keySet());

        try (Socket s = this.socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            int score = 0;
            String motSecret = mots.get(random.nextInt(mots.size()));
            List<String> indices = DICTIONNAIRE.get(motSecret);
            int indiceIndex = 0;
            int tentatives = 0;

            System.out.println("[Serveur] Client " + clientAddr + " connecté. Mot secret : " + motSecret);

            out.println("Bienvenue au Jeu de Devinette !");
            out.println("Devinez le mot caché. Tapez 'indice' pour un indice, 'recommencer' pour un nouveau mot, 'quitter' pour partir.");
            out.println("SCORE:" + score);
            out.println("Indice 1 : " + indices.get(0));
            indiceIndex = 1;

            String message;
            while ((message = in.readLine()) != null) {
                message = message.trim();
                System.out.println("[" + clientAddr + "] Reçu : " + message);

                if (message.equalsIgnoreCase("quitter")) {
                    out.println("Au revoir ! Score final : " + score);
                    System.out.println("[Serveur] Client " + clientAddr + " déconnecté. Score : " + score);
                    break;
                }

                if (message.equalsIgnoreCase("indice")) {
                    if (indiceIndex < indices.size()) {
                        out.println("Indice " + (indiceIndex + 1) + " : " + indices.get(indiceIndex));
                        indiceIndex++;
                    } else {
                        out.println("Plus d'indices disponibles ! Le mot a " + motSecret.length() + " lettres et commence par '" + motSecret.charAt(0) + "'.");
                    }
                    continue;
                }

                if (message.equalsIgnoreCase("recommencer")) {
                    motSecret = mots.get(random.nextInt(mots.size()));
                    indices = DICTIONNAIRE.get(motSecret);
                    indiceIndex = 1;
                    tentatives = 0;
                    System.out.println("[Serveur] Nouveau mot pour " + clientAddr + " : " + motSecret);
                    out.println("Nouveau mot ! Bonne chance.");
                    out.println("Indice 1 : " + indices.get(0));
                    continue;
                }

                // Proposition du joueur
                tentatives++;
                if (message.equalsIgnoreCase(motSecret)) {
                    score++;
                    out.println("BRAVO ! Le mot était '" + motSecret + "' ! Trouvé en " + tentatives + " tentative(s).");
                    out.println("SCORE:" + score);
                    System.out.println("[Serveur] " + clientAddr + " a trouvé '" + motSecret + "' en " + tentatives + " tentative(s). Score : " + score);

                    // Nouveau mot automatiquement
                    motSecret = mots.get(random.nextInt(mots.size()));
                    indices = DICTIONNAIRE.get(motSecret);
                    indiceIndex = 1;
                    tentatives = 0;
                    System.out.println("[Serveur] Nouveau mot pour " + clientAddr + " : " + motSecret);
                    out.println("Nouveau mot ! C'est reparti.");
                    out.println("Indice 1 : " + indices.get(0));
                } else {
                    out.println("Incorrect ! Essayez encore ou tapez 'indice'.");
                }
            }

        } catch (IOException e) {
            System.out.println("[Serveur] Client " + clientAddr + " déconnecté (erreur).");
        }
    }
}
