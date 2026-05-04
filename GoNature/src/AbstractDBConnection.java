import java.sql.*;

public abstract class AbstractDBConnection {

    protected Connection conn;

    // Connection details - SAME for all subclasses
    private static final String URL =
        "jdbc:mysql://localhost:3306/gonature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "Aa123456";

    // Connect once - no need for abstract methods
    public void connect() throws SQLException {
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Only thing that changes between subclasses
    protected abstract String getTableName();

    public void updateField(String columnName, String newValue, String keyColumn, String keyValue) throws SQLException {
        String sql = "UPDATE `" + getTableName() + "` SET " + columnName + " = ? WHERE " + keyColumn + " = ?";

        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setString(1, newValue);
        pstmt.setString(2, keyValue);

        int rows = pstmt.executeUpdate();

        if (rows > 0) {
            System.out.println("Update completed successfully!");
        } else {
            System.out.println("Update failed: record not found.");
        }

        pstmt.close();
    }

    public ResultSet selectAll() throws SQLException {
        String sql = "SELECT * FROM `" + getTableName() + "`";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        return pstmt.executeQuery();
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}