package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.*;


public class OrderConnection {
	private Connection conn;
	
	public OrderConnection() {
		try 
	    {
	        // Establish connection to MySQL database
	        conn = DriverManager.getConnection(
	            "jdbc:mysql://localhost:3306/gonature?"
	            + "allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false",
	            "root",
	            "Aa123456"
	        );
	    } catch (SQLException ex) 
	    {
	        // Handle SQL errors and print details
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("VendorError: " + ex.getErrorCode());
	    }
	}
	
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
	        //updateOrder_date(conn);

	        // Update number of visitors based on user input
	        //updateNumber_of_visitors(conn);


	        
	    } catch (SQLException ex) 
	    {
	        // Handle SQL errors and print details
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("VendorError: " + ex.getErrorCode());
	    }
	}
	
	public void updateOrder(UpdateMessage um) throws SQLException {
	    StringBuilder sql = new StringBuilder("UPDATE `order` SET ");
	    List<Object> params = new ArrayList<>();

	    if (um.getUpdateDate() != null) {
	        sql.append("order_date = ?, ");
	        params.add(java.sql.Date.valueOf(um.getUpdateDate()));
	    }

	    if (um.getNumberOfVisitors() != 0) {
	        sql.append("number_of_visitors = ?, ");
	        params.add(um.getNumberOfVisitors());
	    }

	    // remove last comma
	    sql.setLength(sql.length() - 2);

	    sql.append(" WHERE order_number = ?");
	    params.add(um.getOrderNumber());

	    PreparedStatement ps = conn.prepareStatement(sql.toString());

	    // set params
	    for (int i = 0; i < params.size(); i++) {
	        ps.setObject(i + 1, params.get(i));
	    }

	    int rows = ps.executeUpdate();
	    
	    // Check if update was successful
	    if (rows > 0) {
	        System.out.println("Order date updated successfully!");
	    } else {
	        System.out.println("Update failed: order number not found.");
	    }

	    ps.close();
	}

	/*public void updateOrder_date(UpdateMessage um) throws SQLException {
	    // SQL query to update order date by order number
	    String sql = "UPDATE `order` SET order_date = ? WHERE order_number = ?";
	    PreparedStatement pstmt = conn.prepareStatement(sql);
	    
	    // Set parameters for prepared statement
	    pstmt.setDate(1, java.sql.Date.valueOf(um.getUpdateDate()));
	    pstmt.setInt(2, um.getOrderNumber());
	    
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
    
	public void updateNumber_of_visitors(UpdateMessage um) throws SQLException {   
	    // SQL query to update number of visitors by order number
	    String sql = "UPDATE `order` SET number_of_visitors = ? WHERE order_number = ?";
	    PreparedStatement pstmt = conn.prepareStatement(sql);
	    
	    // Set parameters for prepared statement
	    pstmt.setInt(1, um.getNumberOfVisitors());
	    pstmt.setInt(2, um.getOrderNumber());

	    // Execute update and get number of affected rows
	    int rows = pstmt.executeUpdate();

	    // Check if update was successful
	    if (rows > 0) {
	        System.out.println("Number of visitors updated successfully!");
	    } else {
	        System.out.println("Update failed: order number not found.");
	    }

	    pstmt.close();
	}*/
	
	public List<OrderRow> getUserOrders(Message m) throws SQLException {   
	    // SQL query to update number of visitors by order number
	    String sql = "SELECT * FROM `order` WHERE subscriber_id=?";
	    PreparedStatement pstmt = conn.prepareStatement(sql);
	    
	    // Set parameters for prepared statement
	    pstmt.setInt(1, Integer.parseInt((String)m.getData()));

	    // Execute update and get number of affected rows
	    ResultSet rs = pstmt.executeQuery();
	    
	    List<OrderRow> l = new ArrayList<>();
	    while (rs.next()) {
	        l.add(new OrderRow(
	        		rs.getInt("order_number"), 
	        		rs.getDate("order_date").toLocalDate(), 
	        		rs.getInt("number_of_visitors"),
	        		rs.getInt("confirmation_code"),
	        		rs.getInt("subscriber_id"),
	        		rs.getDate("date_of_placing_order").toLocalDate()
	        		));
	    }
	    pstmt.close();
	    rs.close();
	    
	    return l;
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



