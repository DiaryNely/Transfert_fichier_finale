package assemblage;
import java.sql.*;
import fragment.*;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:fragments.db";

    // Méthode pour créer une table dans la base de données
    public static void createTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS fragments ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "fileName TEXT NOT NULL, "
                    + "fragmentIndex INTEGER NOT NULL, "
                    + "data BLOB NOT NULL)";
            Statement stmt = conn.createStatement();
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error creating database table: " + e.getMessage());
        }
    }

    // Méthode pour insérer un fragment dans la base de données
    public static void insertFragment(FileFragment fragment) {
        String insertSQL = "INSERT INTO fragments (fileName, fragmentIndex, data) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, fragment.getFileName());
            pstmt.setInt(2, fragment.getFragmentIndex());
            pstmt.setBytes(3, fragment.getData());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting fragment into database: " + e.getMessage());
        }
    }
}
