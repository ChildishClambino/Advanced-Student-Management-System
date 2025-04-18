// Singleton class for database connection
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            // Load the H2 database driver
            Class.forName("org.h2.Driver");
            // Create an in-memory H2 database
            this.connection = DriverManager.getConnection("jdbc:h2:mem:studentdb", "sa", "");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}