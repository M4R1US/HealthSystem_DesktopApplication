package DatabaseConnection;


import Classes.ConsoleOutput;
import SavedVariables.DynamicVariables;

import java.sql.DriverManager;
import java.sql.Connection;

/**
 * <h2>Created by Marius Baltramaitis on 09-Feb-17.</h2>
 * <p>Driver for database connection.</p>
 */
public class DatabaseConnectionConfiguration {

    public static final String DB_HOST = "health-system.eu";
    private static final String DB_NAME = "HealthSystem";


    /**
     * Reference to database connection
     * @return database connection object if connection is established, null if otherwise
     */
    public static Connection getConnection() {

        Connection connection = null;

        String path = "jdbc:mysql://"+DB_HOST+"/"+DB_NAME+"?autoReconnect=false&useSSL=false";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(path, DynamicVariables.databaseLogin,DynamicVariables.databasePassword);
        } catch (Exception e) {
            ConsoleOutput.print("Couldn't connect to database!");
            ConsoleOutput.print(Connection.class.getName(),e);
            e.printStackTrace();
        }

        return connection;
    }
}
