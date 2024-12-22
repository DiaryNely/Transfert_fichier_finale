package affichage;

import client.Client;
import server.ServerIntermediate;
import server.ServerPrimary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import affichage.Adftp;

import methode.Configuration;
import methode.ListFiles;

public class AdftpClient {
    private static final String ServerHost = "192.168.1.113";
    private static final int Serverport = 6063;
    private Client client;
    private ServerIntermediate serverIntermediate;

    // Codes ANSI pour le formatage des couleurs
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";

    // Le constructeur déclare qu'il peut lever une IOException
    public AdftpClient(String host, int port) throws IOException {
        this.client = new Client(host, port);
        Configuration config = Configuration.getInstance();
        List<ServerPrimary> servers = config.getPrimaryServers();
        ServerIntermediate serverIntermediate = config.getServerIntermediate();
        config.setClient(this.client);
        this.serverIntermediate = serverIntermediate;
    }

    public void start() {
        try {
            // Attendre quelques secondes pour s'assurer que le serveur démarre
            Thread.sleep(2000);

            client.connectToServer();

            System.out.println(CYAN + "========================================");
            System.out.println("    Bienvenue dans AdftpClient !");
            System.out.println("========================================" + RESET);
            System.out.println(YELLOW + "Commandes disponibles :" + RESET);
            System.out.println(BLUE +
                    "  - upload <fichier>  : Pour téléverser un fichier");
            System.out.println("  - list              : Pour lister les fichiers dans le serveur");
            System.out.println("  - ls                : Pour lister les fichiers dans le repertoire du client");
            System.out.println("  - download <fichier>: Pour télécharger un fichier");
            System.out.println("  - delete <fichier>  : Pour supprimer un fichier");
            System.out.println("  - quit              : Pour quitter le programme" +
                    RESET);
            System.out.println(CYAN + "========================================" +
                    RESET);

            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
            String command;

            while (true) {
                System.out.print(BLUE + "AdftpClient > " + RESET);
                command = consoleInput.readLine();

                if (command == null || command.equalsIgnoreCase("quit")) {
                    System.out.println(YELLOW + "Fermeture de la connexion..." + RESET);
                    client.quit();
                    System.out.println(GREEN + "Déconnecté du serveur." + RESET);
                    break;
                }

                if (command.startsWith("upload ")) {
                    // Récupérer le chemin du fichier après la commande "upload "
                    String filePath = command.substring(7).trim(); // Retirer "upload " et récupérer le chemin

                    if (filePath.isEmpty()) {
                        System.out.println(
                                RED + "Le chemin du fichier est vide. Veuillez coller ou entrer un chemin." + RESET);
                        continue;
                    }

                    System.out.println(YELLOW + "Téléversement du fichier : " + filePath + RESET);
                    client.uploadFile(filePath);
                    System.out.println(GREEN + "Téléversement terminé !" + RESET);
                } else if (command.equalsIgnoreCase("list")) {
                    System.out.println(YELLOW + "Liste des fichiers disponibles :" + RESET);
                    client.listFiles();
                } else if (command.startsWith("ls")) {
                    System.out.println(YELLOW + "Liste des fichiers disponibles :" + RESET);
                    String directoryPath = "Telechargement/";
                    ListFiles.listFiles(directoryPath);
                } else if (command.startsWith("download ")) {
                    String fileName = command.substring(9).trim();
                    String savePath = "Telechargement/";
                    System.out.println(YELLOW + "Téléchargement du fichier : " + fileName +
                            RESET);
                    client.downloadFile(fileName, savePath);
                    System.out.println(GREEN + "Téléchargement terminé !" + RESET);
                } else if (command.startsWith("delete ")) {
                    String fileName = command.substring(7).trim();
                    System.out.println(YELLOW + "Suppression du fichier : " + fileName + RESET);
                    client.deleteFile(fileName);
                    ListFiles.deleteFile("Telechargement/" + fileName);
                    System.out.println(GREEN + "Fichier supprimé !" + RESET);
                } else {
                    System.out.println(RED +
                            "Commande inconnue. Utilisez : upload <fichier>, list, download <fichier>, quit"
                            + RESET);
                }

                System.out.println(CYAN + "----------------------------------------" +
                        RESET);
            }

        } catch (Exception e) {
            System.err.println(RED + "Erreur : " + e.getMessage() + RESET);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            AdftpClient AdftpClient = new AdftpClient(ServerHost, Serverport);
            AdftpClient.start();

        } catch (IOException e) {
            System.err.println(RED + "Erreur lors de l'initialisation : " + e.getMessage() + RESET);
        }
    }
}
