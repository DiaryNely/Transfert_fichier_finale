package server;

import java.io.*;
import java.util.*;
import fragment.*;

public class ServerPrimary {
    private String serverName;

    public String getName() {
        return serverName;
    }

    @Override
    public String toString() {
        return serverName;
    }

    private String storageFilePath;
    private List<FileFragment> fragments;
    private boolean status; // Attribut pour indiquer si le serveur peut recevoir des fragments

    // Constructeur
    public ServerPrimary(String serverName, String storageFilePath, boolean status) {
        this.serverName = serverName;
        this.storageFilePath = storageFilePath;
        this.status = status;
        this.fragments = new ArrayList<>();
        loadFragmentsFromStorage();
    }

    public String getServerName() {
        return this.serverName;
    }

    public boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    // Méthode pour stocker un fragment
    public void storeFragment(FileFragment fragment) {
        try {
            File directory = new File(storageFilePath);
            if (!directory.exists()) {
                directory.mkdirs(); // Créer le répertoire s'il n'existe pas
            }

            File fragmentFile = new File(directory, fragment.getFileName() + "_part" + fragment.getFragmentIndex());
            try (FileOutputStream fos = new FileOutputStream(fragmentFile)) {
                fos.write(fragment.getData());
            }

            // System.out.println("Fragment stored successfully: " +
            // fragmentFile.getPath());
        } catch (IOException e) {
            System.err.println("Error storing fragment: " + e.getMessage());
        }
    }

    // Méthode pour récupérer un fragment depuis le dossier de stockage
    public FileFragment retrieveFragment(String fileName, int fragmentIndex) {
        File fragmentFile = new File(storageFilePath, fileName + "_part" + fragmentIndex);
        if (fragmentFile.exists()) {
            try (FileInputStream fis = new FileInputStream(fragmentFile)) {
                byte[] data = fis.readAllBytes();
                System.out.println("Fragment retrieved successfully: " + fragmentFile.getName());
                return new FileFragment(fileName, fragmentIndex, data);
            } catch (IOException e) {
                System.err.println("Error retrieving fragment: " + e.getMessage());
            }
        } else {
            System.out.println("Fragment not found: fileName=" + fileName + ", fragmentIndex=" + fragmentIndex);
        }
        return null;
    }

    // suppression de fragments
    public void deleteFragment(String fileName, int fragmentIndex) {
        File fragmentFile = new File(storageFilePath, "temp_" + fileName + "_part" + fragmentIndex);
        // System.out.println(storageFilePath);
        if (fragmentFile.exists()) {
            if (fragmentFile.delete()) {
                System.out.println("Fragment " + fragmentIndex + " deleted successfully: " +
                        fragmentFile.getName());
            } else {
                System.err.println("Failed to delete fragment: " + fragmentFile.getName());
            }
        } else {
            System.out.println("Fragment not found: fileName=" + fileName + ", fragmentIndex=" + fragmentIndex);
        }
    }

    // Méthode pour récupérer tous les fragments d'un fichier spécifique depuis le
    // dossier de stockage
    /*
     * public List<FileFragment> getFragmentsByFileName(String fileName) {
     * List<FileFragment> result = new ArrayList<>();
     * File directory = new File(storageFilePath);
     * 
     * if (!directory.exists() || !directory.isDirectory()) {
     * System.out.println("Storage directory does not exist: " + storageFilePath);
     * return result;
     * }
     * 
     * File[] files = directory.listFiles((dir, name) -> name.startsWith("temp_" +
     * fileName + "_part"));
     * if (files != null) {
     * for (File file : files) {
     * try (FileInputStream fis = new FileInputStream(file)) {
     * byte[] data = fis.readAllBytes();
     * int fragmentIndex = Integer.parseInt(file.getName().split("_part")[1]);
     * result.add(new FileFragment(fileName, fragmentIndex, data));
     * } catch (IOException | NumberFormatException e) {
     * System.err.println("Error loading fragment: " + file.getName() + " - " +
     * e.getMessage());
     * }
     * }
     * }
     * 
     * System.out.println("Retrieved " + result.size() + " fragments for file: " +
     * fileName);
     * return result;
     * }
     */

    public List<FileFragment> getFragmentsByFileName(String fileName) {
        // Utilisation d'un Set pour éviter les doublons
        Set<FileFragment> resultSet = new HashSet<>();
        File directory = new File(storageFilePath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Storage directory does not exist: " + storageFilePath);
            return new ArrayList<>();
        }

        File[] files = directory.listFiles((dir, name) -> name.startsWith("temp_" + fileName + "_part"));
        if (files != null) {
            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] data = fis.readAllBytes();
                    int fragmentIndex = Integer.parseInt(file.getName().split("_part")[1]);

                    // Créer un fragment et l'ajouter au Set (les doublons seront automatiquement
                    // ignorés)
                    resultSet.add(new FileFragment(fileName, fragmentIndex, data));
                } catch (IOException | NumberFormatException e) {
                    System.err.println("Error loading fragment: " + file.getName() + " - " + e.getMessage());
                }
            }
        }

        // Convertir le Set en List et trier les fragments par index
        List<FileFragment> resultList = new ArrayList<>(resultSet);
        resultList.sort((f1, f2) -> Integer.compare(f1.getFragmentIndex(), f2.getFragmentIndex()));

        System.out.println("Retrieved " + resultList.size() + " fragments for file: " + fileName);
        return resultList;
    }

    // Charger les fragments depuis le dossier de stockage
    private void loadFragmentsFromStorage() {
        File directory = new File(storageFilePath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("No existing storage directory. Starting with an empty fragment list.");
            return;
        }

        File[] files = directory.listFiles((dir, name) -> name.contains("_part"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName().split("_part")[0];
                int fragmentIndex = Integer.parseInt(file.getName().split("_part")[1]);
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] data = fis.readAllBytes();
                    fragments.add(new FileFragment(fileName, fragmentIndex, data));
                } catch (IOException e) {
                    System.err.println("Error loading fragment: " + e.getMessage());
                }
            }
        }
        // System.out.println("Loaded " + fragments.size() + " fragments from
        // storage.");
    }

    // Méthode pour afficher les fragments stockés (debug)
    public void listFragments() {
        System.out.println("Fragments stored in " + serverName + ":");
        for (FileFragment fragment : fragments) {
            System.out.println("File: " + fragment.getFileName() + ", Index: " + fragment.getFragmentIndex());
        }
    }
}
