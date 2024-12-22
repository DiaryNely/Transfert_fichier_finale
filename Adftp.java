package affichage;

import client.Client;
import server.ServerIntermediate;
import server.ServerPrimary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import methode.Configuration;

public class Adftp {
    private static final String host = "localhost";
    private static final int port = 6063;
    private ServerIntermediate serverIntermediate;
    private Client client;

    // Codes ANSI pour le formatage des couleurs
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";

    // Le constructeur déclare qu'il peut lever une IOException
    public Adftp(String host, int port, List<ServerPrimary> primaryServers) throws IOException {
        this.serverIntermediate = new ServerIntermediate(port, primaryServers);
        Configuration config = Configuration.getInstance();
        this.client = config.getClient();
    }

    public void start() {
        try {
            Thread serverThread = new Thread(() -> {
                try {
                    serverIntermediate.startServer();
                } catch (IOException e) {
                    System.err.println(
                            RED + "Erreur lors du démarrage du serveur intermédiaire : " + e.getMessage() + RESET);
                }
            });
            serverThread.start();
        } catch (Exception e) {
            System.err.println(RED + "Erreur : " + e.getMessage() + RESET);
        }

    }

    public void stop() {
        try {
            serverIntermediate.stopServer();
        } catch (IOException e) {
            System.err.println(RED + "Erreur : " + e.getMessage() + RESET);
        }
    }

    public static void main(String[] args) {
        try {
            // Charger le fichier .conf
            Properties properties = new Properties();
            FileInputStream inputStream = new FileInputStream("conf/server_config.conf");
            properties.load(inputStream);

            // Lire la liste des serveurs à démarrer
            String choix = properties.getProperty("server_to_start");

            // Initialiser les serveurs principaux
            ServerPrimary server1 = new ServerPrimary("Sous-Server1", "Storage/Server1", false);
            ServerPrimary server2 = new ServerPrimary("Sous-Server2", "Storage/Server2", false);
            ServerPrimary server3 = new ServerPrimary("Sous-Server3", "Storage/Server3", false);
            ServerPrimary server4 = new ServerPrimary("Sous-Server4", "Storage/Server4", false);

            List<ServerPrimary> primaryServers = new ArrayList<>();
            int serverCount = 0;

            String[] serveursChoisis = choix.split(",");
            for (String serveur : serveursChoisis) {
                switch (serveur.trim()) {
                    case "Sous-Server1":
                        primaryServers.add(server1);
                        serverCount++;
                        break;
                    case "Sous-Server2":
                        primaryServers.add(server2);
                        serverCount++;
                        break;
                    case "Sous-Server3":
                        primaryServers.add(server3);
                        serverCount++;
                        break;
                    case "Sous-Server4":
                        primaryServers.add(server4);
                        serverCount++;
                        break;
                    default:
                        System.out.println("Serveur non reconnu : " + serveur);
                        break;
                }
            }

            // Afficher le nombre de serveurs initialisés
            System.out.println("Nombre de Sous-Servers initialisés : " + serverCount);
            Configuration config = Configuration.getInstance();
            config.setPrimaryServers(primaryServers);
            config.setServerIntermediate(new ServerIntermediate(port, primaryServers));

            if (!primaryServers.isEmpty()) {
                Adftp adftp = new Adftp(host, port, primaryServers);
                adftp.start();

                BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
                String command;

                System.out.println(CYAN + "======================================================================");
                System.out.println(" Bienvenue dans AdftpServer sur l'ip : '" + host + "' et le port : " + port);
                System.out.println("======================================================================" + RESET);

                System.out.println(YELLOW + "Commande(s) disponible(s) :" + RESET);
                System.out.println(BLUE + " - quit              : Pour quitter le programme" + RESET);
                System.out.println(BLUE + " - list              : Pour lister les fichiers" + RESET);
                System.out.println(
                        CYAN + "======================================================================" + RESET);
                while (true) {

                    System.out.print(BLUE + "AdftpServer > " + RESET);

                    command = consoleInput.readLine();

                    if (command == null || command.equalsIgnoreCase("quit")) {
                        System.out.println(YELLOW + "Fermeture de la connexion..." + RESET);
                        adftp.stop();
                        System.out.println(GREEN + "Déconnecté du serveur." + RESET);
                        break;
                    } else if (command.equalsIgnoreCase("list")) {
                        System.out.println(YELLOW + "Liste des fichiers disponibles :" + RESET);
                        adftp.serverIntermediate.listFiles();
                    } else {
                        System.out.println(RED + "Commande inconnue. Utilisez : list, quit" + RESET);
                    }
                }
            } else {
                System.out.println("Aucun serveur valide n'a été choisi.");
            }

        } catch (IOException e) {
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
        }

    }
}
