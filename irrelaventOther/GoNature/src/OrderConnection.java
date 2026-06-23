import java.sql.*;
import java.util.Scanner;

public class OrderConnection extends AbstractDBConnection {

    // Return the table name
    @Override
    protected String getTableName() {
        return "order";
    }

    // Print all orders
    public void printOrders() throws SQLException {
        ResultSet rs = selectAll();

        while (rs.next()) {
            System.out.println(
                "order number: " + rs.getString("order_number") + "  " +
                "order date: " + rs.getString("order_date") + "  " +
                "number of visitors: " + rs.getString("number_of_visitors") + "  " +
                "subscriber id: " + rs.getString("subscriber_id") + "  " +
                "date of placing order: " + rs.getString("date_of_placing_order")
            );
        }

        rs.close();
    }

    // Main update selector method
    public void updateOrderByChoice() throws SQLException {
        Scanner input = new Scanner(System.in);

        System.out.print("What do you want to update? (date / visitors): ");
        String choice = input.nextLine();

        if (choice.equalsIgnoreCase("date")) {
            updateOrderDate();
        } 
        else if (choice.equalsIgnoreCase("visitors")) {
            updateNumberOfVisitors();
        } 
        else {
            System.out.println("Invalid option.");
        }
    }

    // Update order_date
    public void updateOrderDate() throws SQLException {
        Scanner input = new Scanner(System.in);

        System.out.print("Enter your order number: ");
        String orderNumber = input.nextLine();

        System.out.print("Enter new order date: ");
        String orderDate = input.nextLine();

        updateField("order_date", orderDate, "order_number", orderNumber);
    }

    // Update number_of_visitors
    public void updateNumberOfVisitors() throws SQLException {
        Scanner input = new Scanner(System.in);

        System.out.print("Enter your order number: ");
        String orderNumber = input.nextLine();

        System.out.print("Enter the new number of visitors: ");
        String numberOfVisitors = input.nextLine();

        updateField("number_of_visitors", numberOfVisitors, "order_number", orderNumber);
    }
}