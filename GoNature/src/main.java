import java.sql.SQLException;

public class main {

    public static void main(String[] args) {

        OrderConnection orderConnection = new OrderConnection();

        try {
            // connect to DB
            orderConnection.connect();

            // print all orders
            orderConnection.printOrders();

            System.out.println("\n--- UPDATE ---");

            // choose what to update (date / visitors)
            orderConnection.updateOrderByChoice();

            System.out.println("\n--- UPDATED TABLE ---");

            // print again to see changes
            orderConnection.printOrders();

            // close connection
            orderConnection.close();

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
}