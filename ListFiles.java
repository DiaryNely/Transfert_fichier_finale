package methode;

import java.io.File;

public class ListFiles {

    public static void listFiles(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            System.out.println("Le répertoire n'existe pas.");
            return;
        }

        if (!directory.isDirectory()) {
            System.out.println("Le chemin fourni n'est pas un répertoire.");
            return;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("Aucun fichier trouvé dans le répertoire.");
            return;
        }

        for (File file : files) {
            System.out.println(file.getName());
        }
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Le fichier n'existe pas.");
            return;
        }

        if (file.isDirectory()) {
            System.out.println("Le chemin fourni n'est pas un fichier.");
            return;
        }

        if (file.delete()) {
            System.out.println("Fichier supprimé avec succès : " + file.getName());
        } else {
            System.out.println("Échec de la suppression du fichier : " + file.getName());
        }
    }
}
