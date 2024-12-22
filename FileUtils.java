package methode;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import fragment.*;

public class FileUtils {
    // Méthode pour diviser un fichier en fragments de taille spécifiée
    private List<FileFragment> splitFile(File file, int fragmentSize) throws IOException {
        List<FileFragment> fragments = new ArrayList<>();
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
        //System.out.println("File split into " + fragments.size() + " fragments.");
        return fragments;
    }
    
    
    // Méthode pour assembler un fichier à partir de fragments
    public static File assembleFile(List<FileFragment> fragments, String outputPath) {
        File outputFile = new File(outputPath);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            for (FileFragment fragment : fragments) {
                fos.write(fragment.getData());
            }
            System.out.println("File assembled successfully.");
        } catch (IOException e) {
            System.err.println("Error assembling file: " + e.getMessage());
        }
        return outputFile;
    }
}
