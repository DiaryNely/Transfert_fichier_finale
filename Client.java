package client;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import fragment.*;
import java.net.*;

public class Client {
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";

    // Constructeur
    public Client(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    // Se connecter au serveur intermédiaire
    public void connectToServer() throws IOException {
        socket = new Socket(serverHost, serverPort);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        // System.out.println("Connected to the Server.... on port " + serverPort);
        System.out.println(YELLOW + "Connected to the Server....");
    }

    public void deleteFile(String fileName) throws IOException {
        out.writeUTF("DELETE"); // Commande de suppression
        out.writeUTF(fileName); // Nom du fichier à supprimer

        String response = in.readUTF();
        if ("FILE_NOT_FOUND".equals(response)) {
            System.out.println("File '" + fileName + "' not found on the server.");
        } else if ("DELETE_SUCCESS".equals(response)) {
            System.out.println("File '" + fileName + "' deleted successfully.");
        } else {
            System.out.println("Unexpected server response: " + response);
        }
    }

    // Envoyer un fichier au serveur intermédiaire
    public void uploadFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("Invalid file. Please check the file path.");
            return;
        }

        // Envoi des métadonnées du fichier
        out.writeUTF("UPLOAD");
        out.writeUTF(file.getName());
        out.writeLong(file.length());

        // Envoi des données du fichier
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            System.out.println("File '" + file.getName() + "' uploaded successfully.");
        }

        String response = in.readUTF();
        System.out.println("Server response: " + response);
    }

    // Télécharger un fichier depuis le serveur intermédiaire
    public void downloadFile(String fileName, String savePath) throws IOException {
        out.writeUTF("DOWNLOAD");
        out.writeUTF(fileName);
        savePath = savePath + fileName;

        String response = in.readUTF();
        if ("FILE_NOT_FOUND".equals(response)) {
            System.out.println("The requested file '" + fileName + "' was not found on the server.");
            return;
        }

        if ("OK".equals(response)) {
            long fileSize = in.readLong();
            try (FileOutputStream fos = new FileOutputStream(savePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalRead = 0;

                while (totalRead < fileSize && (bytesRead = in.read(buffer)) > 0) {
                    fos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
                System.out.println("File '" + fileName + "' downloaded successfully to " + savePath);
            }
        } else {
            System.out.println("Unexpected server response: " + response);
        }
    }

    // Lister les fichiers disponibles sur le serveur
    public void listFiles() throws IOException {
        // Envoi de la commande de listing
        out.writeUTF("LIST_FILES");

        // Réception de la liste des fichiers
        int fileCount = in.readInt();
        if (fileCount == 0) {
            System.out.println("No files available on the server.");
        } else {
            // System.out.println("Files available on the server:");
            for (int i = 0; i < fileCount; i++) {
                System.out.println("- " + in.readUTF());
            }
        }
    }

    public void ls() throws IOException {
        out.writeUTF("LS");

        // Réception de la liste des fichiers
        int fileCount = in.readInt();
        if (fileCount == 0) {
            System.out.println("No files available on the server.");
        } else {
            // System.out.println("Files available on the server:");
            for (int i = 0; i < fileCount; i++) {
                System.out.println("- " + in.readUTF());
            }
        }
    }

    public void quit() throws IOException {
        out.writeUTF("QUIT");
        // System.out.println("Sent quit command to the server.");
        closeConnection();
    }

    // Fermer la connexion avec le serveur intermédiaire
    public void closeConnection() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        // System.out.println("Disconnected from the server.");
    }

    public String receiveResponse() throws IOException {
        return in.readUTF();
    }

}
