package server;

import fragment.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerIntermediate {
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public List<ServerPrimary> getAllServer(List<ServerPrimary> dejaExistante) {
        ServerPrimary server1 = new ServerPrimary("Sous-Server1", "Storage/Server1", false);
        ServerPrimary server2 = new ServerPrimary("Sous-Server2", "Storage/Server2", false);
        ServerPrimary server3 = new ServerPrimary("Sous-Server3", "Storage/Server3", false);
        ServerPrimary server4 = new ServerPrimary("Sous-Server4", "Storage/Server4", false);

        List<ServerPrimary> primaryServers = new ArrayList<>();

        // Ajouter uniquement les serveurs qui ne sont pas déjà dans dejaExistante
        if (!dejaExistante.contains(server1)) {
            primaryServers.add(server1);
        }
        if (!dejaExistante.contains(server2)) {
            primaryServers.add(server2);
        }
        if (!dejaExistante.contains(server3)) {
            primaryServers.add(server3);
        }
        if (!dejaExistante.contains(server4)) {
            primaryServers.add(server4);
        }

        return primaryServers;
    }

    private List<ServerPrimary> primaryServers;
    private List<String> files; // Liste des fichiers connus par le serveur intermédiaire
    private ServerSocket serverSocket; // Socket principal du serveur
    private static final String FILE_LIST_PATH = "storage_List/file_list.txt";

    // Constructeur
    public ServerIntermediate(int port, List<ServerPrimary> primaryServers) {
        this.port = port;
        this.primaryServers = primaryServers;
        this.files = new ArrayList<>();
        loadFileList(); // Charger la liste des fichiers lors du démarrage du serveur
    }

    // Liste des fichiers internes de serveur intermédiaire
    public List<String> getNameFile() {
        return this.files;
    }

    // Ajouter un fichier à la liste et sauvegarder la liste
    public void addNameFile(String nameFile) {
        this.files.add(nameFile);
        saveFileList(); // Sauvegarder la liste après ajout
        if (files.size() == 0) {
            System.out.println("tsy misy ao");
        }
    }

    // Sauvegarder la liste des fichiers dans un fichier texte
    private void saveFileList() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_LIST_PATH))) {
            for (String fileName : files) {
                writer.write(fileName);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de la liste des fichiers : " + e.getMessage());
        }
    }

    // Charger la liste des fichiers depuis le fichier texte
    private void loadFileList() {
        File file = new File(FILE_LIST_PATH);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    files.add(line);
                }
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de la liste des fichiers : " + e.getMessage());
            }
        }
    }

    // Démarrer le serveur intermédiaire
    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port);
        // System.out.println("ServerIntermediate running on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    public void stopServer() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            // System.out.println("ServerIntermediate stopped.");
        }
    }

    // Gérer un client
    private void handleClient(Socket clientSocket) {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            boolean running = true;

            while (running) {
                String command = in.readUTF(); // Lecture de la commande du client

                switch (command) {
                    case "UPLOAD":
                        handleUpload(in, out);
                        break;

                    case "DOWNLOAD":
                        handleDownload(in, out);
                        break;

                    case "LIST_FILES":
                        listFiles(out);
                        break;

                    case "DELETE":
                        handleDelete(in, out);
                        break;

                    case "QUIT":
                        running = false;
                        out.writeUTF("QUIT_SUCCESS");
                        break;

                    default:
                        out.writeUTF("INVALID_COMMAND");
                        break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close(); // Fermer le socket après réception de "QUIT"
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    // Méthode handleDelete pour supprimer le fichier et ses fragments
    public void handleDelete(DataInputStream in, DataOutputStream out) throws IOException {
        String fileName = in.readUTF();

        if (!files.contains(fileName)) {
            out.writeUTF("FILE_NOT_FOUND");
            return;
        }

        // Supprimer les fragments associés à ce fichier sur les serveurs primaires
        deleteFragments(fileName);

        // Supprimer le fichier de la liste
        files.remove(fileName);
        saveFileList(); // Sauvegarder la liste mise à jour

        out.writeUTF("DELETE_SUCCESS");
    }

    // Supprimer les fragments associés à un fichier sur les serveurs primaires
    private void deleteFragments(String fileName) {
        List<ServerPrimary> allServers = getAllServer(primaryServers);

        for (ServerPrimary server : allServers) {
            List<FileFragment> fragments = server.getFragmentsByFileName(fileName);
            if (fragments != null) {
                for (FileFragment fragment : fragments) {
                    // System.out.println("Mandalo");
                    server.deleteFragment(fileName, fragment.getFragmentIndex());
                }
            }
        }
    }

    // Gérer l'upload de fichiers
    private void handleUpload(DataInputStream in, DataOutputStream out) throws IOException {
        String fileName = in.readUTF();
        long fileSize = in.readLong();
        File tempFile = new File("temp_" + fileName);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;

            while (totalRead < fileSize && (bytesRead = in.read(buffer)) > 0) {
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }
        }

        List<FileFragment> fragments = splitFile(tempFile, 1024 * 1024);
        distributeFragments(fragments);
        addNameFile(fileName);
        tempFile.delete();
        out.writeUTF("UPLOAD_SUCCESS");
    }

    // Diviser un fichier en fragments
    private List<FileFragment> splitFile(File file, int defaultFragmentSize) throws IOException {
        List<FileFragment> fragments = new ArrayList<>();

        // Compter le nombre de serveurs disponibles
        long availableServers = primaryServers.stream().filter(s -> !s.getStatus()).count();

        if (availableServers == 0) {
            System.err.println("No available servers to distribute fragments.");
            return fragments;
        }

        // Calculer la taille des fragments pour correspondre au nombre de serveurs
        // disponibles
        long fileSize = file.length();
        int fragmentSize = (int) Math.ceil((double) fileSize / availableServers);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[fragmentSize];
            int bytesRead;
            int index = 0;

            while ((bytesRead = fis.read(buffer)) > 0) {
                byte[] fragmentData = new byte[bytesRead];
                System.arraycopy(buffer, 0, fragmentData, 0, bytesRead);
                fragments.add(new FileFragment(file.getName(), index++, fragmentData));
            }
        }

        // System.out.println("File split into " + fragments.size() + " fragments.");
        return fragments;
    }

    // Distribuer les fragments uniquement aux serveurs primaires avec le statut
    // false (0)
    /*
     * private void distributeFragments(List<FileFragment> fragments) {
     * if (fragments.isEmpty()) {
     * System.err.println("No fragments to distribute.");
     * return;
     * }
     * 
     * // System.out.println("Distributing " + fragments.size() + " fragments.");
     * 
     * // Créer une liste des serveurs primaires disponibles avec statut false
     * List<ServerPrimary> availableServers = new ArrayList<>();
     * for (ServerPrimary s : primaryServers) {
     * if (!s.getStatus()) {
     * availableServers.add(s);
     * }
     * }
     * 
     * // Vérifier si on a assez de serveurs disponibles pour les fragments
     * if (availableServers.size() < fragments.size()) {
     * System.err.
     * println("Not enough available servers to distribute all fragments.");
     * return;
     * }
     * 
     * // Distribuer chaque fragment à un serveur différent
     * for (int i = 0; i < fragments.size(); i++) {
     * ServerPrimary server = availableServers.get(i);
     * server.storeFragment(fragments.get(i));
     * 
     * // System.out.println("Distributed fragment " + i + " to " +
     * // server.getServerName());
     * }
     * }
     */

    // Distribuer tous les fragments à tous les serveurs primaires
    private void distributeFragments(List<FileFragment> fragments) {
        if (fragments.isEmpty()) {
            System.err.println("No fragments to distribute.");
            return;
        }

        // Créer une liste de tous les serveurs primaires
        List<ServerPrimary> allServers = getAllServer(primaryServers);

        // Distribuer chaque fragment à tous les serveurs
        for (FileFragment fragment : fragments) {
            for (ServerPrimary server : allServers) {
                server.storeFragment(fragment);
            }
        }
    }

    // Gérer un téléchargement
    private void handleDownload(DataInputStream in, DataOutputStream out) throws IOException {
        String fileName = in.readUTF();

        if (!files.contains(fileName)) {
            out.writeUTF("FILE_NOT_FOUND");
            return;
        }

        out.writeUTF("OK");

        List<FileFragment> fragments = getFragmentsForFile(fileName);
        if (fragments.isEmpty()) {
            out.writeUTF("FRAGMENTS_NOT_FOUND");
            return;
        }

        String assembledFilePath = "assembled_" + fileName;
        File assembledFile = assembleFile(fragments, assembledFilePath);

        long fileSize = assembledFile.length();
        out.writeLong(fileSize);

        try (FileInputStream fis = new FileInputStream(assembledFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
        }

        assembledFile.delete();
    }

    // Assembler un fichier à partir de fragments
    private File assembleFile(List<FileFragment> fragments, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            for (FileFragment fragment : fragments) {
                fos.write(fragment.getData());
            }
        }
        return outputFile;
    }

    // Rechercher les fragments du fichier
    /*
     * private List<FileFragment> getFragmentsForFile(String fileName) {
     * List<FileFragment> fragments = new ArrayList<>();
     * 
     * // Parcourir les serveurs primaires pour collecter les fragments du fichier
     * List<ServerPrimary> allServer = getAllServer(primaryServers);
     * 
     * for (ServerPrimary server : allServer) {
     * List<FileFragment> serverFragments = server.getFragmentsByFileName(fileName);
     * if (serverFragments != null) {
     * fragments.addAll(serverFragments);
     * }
     * }
     * 
     * // Trier les fragments par index pour s'assurer qu'ils sont dans le bon ordre
     * fragments.sort((f1, f2) -> Integer.compare(f1.getFragmentIndex(),
     * f2.getFragmentIndex()));
     * return fragments;
     * }
     */

    private List<FileFragment> getFragmentsForFile(String fileName) {
        // Utilisation d'un Set pour éviter les doublons
        Set<FileFragment> fragmentsSet = new HashSet<>();

        // Parcourir les serveurs primaires pour collecter les fragments du fichier
        List<ServerPrimary> allServer = getAllServer(primaryServers);

        for (ServerPrimary server : allServer) {
            List<FileFragment> serverFragments = server.getFragmentsByFileName(fileName);
            if (serverFragments != null) {
                // Ajouter les fragments au Set (les doublons seront automatiquement ignorés)
                fragmentsSet.addAll(serverFragments);
            }
        }

        // Convertir le Set en List
        List<FileFragment> fragments = new ArrayList<>(fragmentsSet);

        // Trier les fragments par index pour s'assurer qu'ils sont dans le bon ordre
        fragments.sort((f1, f2) -> Integer.compare(f1.getFragmentIndex(), f2.getFragmentIndex()));

        return fragments;
    }

    // Lister les fichiers disponibles sur le serveur intermédiaire
    private void listFiles(DataOutputStream out) throws IOException {
        out.writeInt(files.size());
        for (String fileName : files) {
            out.writeUTF(fileName);
        }
    }

    public void listFiles() {
        if (files.isEmpty()) {
            System.out.println("No files uploaded yet.");
            return;
        }

        // System.out.println("Files available on the server:");
        for (String file : files) {
            System.out.println(file);
        }
    }

    public List<FileFragment> fetchFileFragments(String fileName) {
        return getFragmentsForFile(fileName); // Appel à la méthode privée existante
    }
}
