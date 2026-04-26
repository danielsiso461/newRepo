
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;


public class OrderConnection {

	public static void main(String[] args) 
	{
	    try 
	    {
	        // Establish connection to MySQL database
	        Connection conn = DriverManager.getConnection(
	            "jdbc:mysql://localhost:3306/gonature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false",
	            "root",
	            "Aa123456"
	        );

	        
	        // Print all orders from the database
	        printOrder(conn);

	        System.out.println("\n\n");
	        
	        // Update order date based on user input
	        updateOrder_date(conn);

	        // Update number of visitors based on user input
	        updateNumber_of_visitors(conn);


	        
	    } catch (SQLException ex) 
	    {
	        // Handle SQL errors and print details
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("VendorError: " + ex.getErrorCode());
	    }
	}
	

	public static void updateOrder_date(Connection conn) throws SQLException {
	    Scanner input = new Scanner(System.in);
	    
	    // SQL query to update order date by order number
	    String sql = "UPDATE `order` SET order_date = ? WHERE order_number = ?";
	    PreparedStatement pstmt = conn.prepareStatement(sql);

	    System.out.print("Enter your order number: ");
	    String order_number = input.nextLine();

	    System.out.print("Enter new order date: ");
	    String order_date = input.nextLine();

	    // Set parameters for prepared statement
	    pstmt.setString(1, order_date);
	    pstmt.setString(2, order_number);
	    
	    // Execute update and get number of affected rows
	    int rows = pstmt.executeUpdate();

	    // Check if update was successful
	    if (rows > 0) {
	        System.out.println("Order date updated successfully!");
	    } else {
	        System.out.println("Update failed: order number not found.");
	    }

	    pstmt.close();
	}
    
	public static void updateNumber_of_visitors(Connection conn) throws SQLException {
	    Scanner input = new Scanner(System.in);
	    
	    // SQL query to update number of visitors by order number
	    String sql = "UPDATE `order` SET number_of_visitors = ? WHERE order_number = ?";
	    PreparedStatement pstmt = conn.prepareStatement(sql);

	    System.out.print("Enter your order number: ");
	    String order_number = input.nextLine();

	    System.out.print("Enter the new number of visitors: ");
	    String number_of_visitors = input.nextLine();

	    // Set parameters for prepared statement
	    pstmt.setString(1, number_of_visitors);
	    pstmt.setString(2, order_number);
	    
	    // Execute update and get number of affected rows
	    int rows = pstmt.executeUpdate();

	    // Check if update was successful
	    if (rows > 0) {
	        System.out.println("Number of visitors updated successfully!");
	    } else {
	        System.out.println("Update failed: order number not found.");
	    }

	    pstmt.close();
	}
	
	
	// Print all orders from the database
	public static void printOrder(Connection conn) throws SQLException {
	    
	    // SQL query to retrieve all rows from 'order' table
	    String sql = "SELECT * FROM `order`";

	    PreparedStatement pstmt = conn.prepareStatement(sql);
	    
	    // Execute query and store result
	    ResultSet rs = pstmt.executeQuery();

	    // Iterate over each row in the result set
	    while (rs.next()) {
	        
	        // Print each column value in a readable format
	        System.out.println(
	                "order number: " + rs.getString("order_number") + "  " +
	                "order date: " + rs.getString("order_date") + "  " +
	                "number of visitors: " + rs.getString("number_of_visitors") + "  " +
	                "subscriber id: " + rs.getString("subscriber_id") + "  " +
	                "date of placing order: " + rs.getString("date_of_placing_order")
	        );
	    }

	    // Close resources
	    rs.close();     
	    pstmt.close();  
	}


}



