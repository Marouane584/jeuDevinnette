package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true
            );

            out.println("Bienvenue sur le serveur de devinette");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Reçu : " + message);
                out.println("Echo : " + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
