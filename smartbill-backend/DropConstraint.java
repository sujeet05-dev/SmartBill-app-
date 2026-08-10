import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropConstraint {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/smartbill";
        String user = "postgres";
        String password = "postgres";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("ALTER TABLE invoice_items ALTER COLUMN product_id DROP NOT NULL");
            System.out.println("Constraint dropped successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
