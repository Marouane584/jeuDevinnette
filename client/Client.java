package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        String serverIP = "localhost"; // ou IP du serveur
        int port = 5000;

        try {
            Socket socket = new Socket(serverIP, port);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true
            );

            Scanner scanner = new Scanner(System.in);

            System.out.println(in.readLine()); // message du serveur

            while (true) {
                System.out.print("Votre message : ");
                String msg = scanner.nextLine();
                out.println(msg);

                String response = in.readLine();
                System.out.println("Serveur : " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
