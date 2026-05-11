package server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.*;


public class OrderConnection extends AbstractDBConnection {
	private final String 
					ORDER_NUMBER = "order_number",
					ORDER_DATE = "order_date",
					VISITOR_NUMBER = "number_of_visitors",
					CONF_CODE = "confirmation_code",
					USER_ID = "subscriber_id",
					PLACEMENT_DATE = "date_of_placing_order";
	
	public OrderConnection() {
		super();
		try {
			this.connect();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getTableName() {
		return ConstantsDBTableNames.ORDER;
	}
	
	/*public static void main(String[] args) 
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
	}*/
	
	public void updateOrder(UpdateMessage um) throws SQLException {
		List<Object> newValues = new ArrayList<>(), keyValues = new ArrayList<>();
		List<String> columnNames = new ArrayList<>(), keyColumns = new ArrayList<>();
		
		if (um.getUpdateDate() != null) {
			columnNames.add(ORDER_DATE);
	        newValues.add(java.sql.Date.valueOf(um.getUpdateDate()));
	    }

	    if (um.getNumberOfVisitors() != 0) {
	    	columnNames.add(VISITOR_NUMBER);
	        newValues.add(um.getNumberOfVisitors());
	    }
	    
	    keyColumns.add(ORDER_NUMBER);
        keyValues.add(um.getOrderId());
	    
		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);
	}
	
	/*public void updateOrder(UpdateMessage um) throws SQLException {
	    StringBuilder sql = new StringBuilder("UPDATE `" + getTableName() + "` SET ");
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
	}*/

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
		String s = selectByFields(new String[] {"*"}, new String[] {USER_ID});
		if(s == null)
			return null;
		
		PreparedStatement pstmt = conn.prepareStatement(s);
        
		pstmt.setInt(1, Integer.parseInt((String)m.getData()));
        
		// Execute update and get number of affected rows
	    ResultSet rs = pstmt.executeQuery();
	    
	    List<OrderRow> l = new ArrayList<>();
	    int i = 1;
	    while (rs.next()) {
	        l.add(new OrderRow(
	        		i++,
	        		rs.getInt(ORDER_NUMBER), 
	        		rs.getDate(ORDER_DATE).toLocalDate(), 
	        		rs.getInt(VISITOR_NUMBER),
	        		rs.getInt(CONF_CODE),
	        		rs.getInt(USER_ID),
	        		rs.getDate(PLACEMENT_DATE).toLocalDate()
	        		));
	    }
	    
	    rs.close();
	    pstmt.close();
	    
	    
	    return l;
	}
	
	/*public List<OrderRow> OLDgetUserOrders(Message m) throws SQLException {   
	    // SQL query to update number of visitors by order number
	    String sql = "SELECT * FROM `" + getTableName() + "` WHERE subscriber_id=?";
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
	    
	    rs.close();
	    pstmt.close();
	    
	    
	    return l;
	}*/
	
	/*// Print all orders from the database
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
	}*/
	
	/*public static void main(String[] args) {
		OrderConnection oc = new OrderConnection();
		UpdateMessage um = new UpdateMessage(LocalDate.now(), -5, 999);
		try{oc.updateOrder(um);}
		catch(Exception e){System.out.println(e.getMessage());};
	}*/
}



