package Registers;

import Classes.ConsoleOutput;
import Classes.Medicine;
import DatabaseConnection.DatabaseConnectionConfiguration;
import Interfaces.ConnectionSensor;
import Interfaces.DatabaseRegisterImplementation;
import javafx.application.Platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * <h2>Created by Marius Baltramaitis on 10/08/2017.</h2>
 *
 * <p>Medicine register</p>
 */
public class MedicineRegister implements DatabaseRegisterImplementation {

    private ConnectionSensor connectionSensor;

    /**
     *  {@inheritDoc}
     */

    public void addListener(ConnectionSensor connectionSensor) {this.connectionSensor = connectionSensor;}

    /**
     *  {@inheritDoc}
     */

    public void updateProcess(String connectionInformation, Double progressValue) {
        if(connectionSensor != null)
            connectionSensor.apply(connectionInformation,progressValue);
    }

    /**
     * Method to find medicine by given parameters
     * @param medicineName name of medicine
     * @param license license of medicine
     * @param all true if all, false if only available
     * @return ArrayList with matching data from database
     */
    public ArrayList<Medicine> find(String medicineName, String license, boolean all)
    {
        String available = (all) ? "%" : "1";
        medicineName = "%"+medicineName+"%";

        if(license == null)
            license = "%";

        Platform.runLater(() -> updateProcess("Preparing database connection",0.1));
        ArrayList<Medicine> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            Platform.runLater(() -> updateProcess("Connecting to database",0.3));
            connection = DatabaseConnectionConfiguration.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM Medicine WHERE name LIKE ? and license LIKE ? and available LIKE ? LIMIT 200");
            preparedStatement.setString(1,medicineName);
            preparedStatement.setString(2,license);
            preparedStatement.setString(3,available);
            resultSet = preparedStatement.executeQuery();
            Platform.runLater(() -> updateProcess("Collecting data",0.6));

            while (resultSet.next())
            {
                Medicine medicine = new Medicine();
                medicine.setID(""+resultSet.getInt("ID"));
                medicine.setName(resultSet.getString("name"));
                medicine.setDescription(resultSet.getString("description"));
                medicine.setLicense(resultSet.getString("license"));
                medicine.setUsage(resultSet.getString("usage"));
                medicine.setSideEffect(resultSet.getString("sideEffect"));
                medicine.setType(resultSet.getString("type"));
                medicine.setAvailable(resultSet.getInt("available"));
                list.add(medicine);
            }

            if(list.isEmpty())
                Platform.runLater(() -> updateProcess("No data found with given details",1.0));


        }catch (Exception e)
        {
            ConsoleOutput.print(getClass().getName(),e);
            Platform.runLater(() -> updateProcess("Error connecting to server",1.0));

        } finally {

            if(resultSet != null)
            {
                try { resultSet.close(); } catch (SQLException e) { ConsoleOutput.print(getClass().getName(),e); }
            }

            if(preparedStatement != null)
            {
                try { preparedStatement.close(); }catch (SQLException e)  {ConsoleOutput.print(getClass().getName(),e);}
            }


            if(connection != null)
            {
                try{ connection.close(); } catch (SQLException e) { ConsoleOutput.print(getClass().getName(),e); }
            }
        }

        Platform.runLater(() -> updateProcess("Transaction finished",1.0));
        return list;
    }


    /**
     * Method to update medicine availability
     * @param id medicine ID
     * @param availability 1 to enable, 0 to disable
     */
    public void availability(String id,int availability)
    {
        int ID  = Integer.parseInt(id);
        if(availability > 1 || availability < 0)
            throw new IllegalArgumentException("Availability is either 0 or 1!");

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database", 0.1));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.2));
            preparedStatement = connection.prepareStatement("UPDATE Medicine Set available = ? WHERE ID = ?");
            preparedStatement.setInt(1, availability);
            preparedStatement.setInt(2, ID);
            preparedStatement.executeUpdate();
            Platform.runLater(() -> updateProcess("SQL statement executed successfully.Medicine is registered", 1.0));
            ConsoleOutput.print(getClass().getName(), " SQL statement executed successfully.Medicine is registered");


        } catch (Exception e) {

            e.printStackTrace();
            Platform.runLater(() -> updateProcess("Something went wrong. Contact Marius",-1.0));

        } finally {

            try {
                if(preparedStatement != null)
                {
                    preparedStatement.close();
                    ConsoleOutput.print("Closing prepared insert Statement");
                }

                if(connection != null)
                {
                    connection.close();
                    ConsoleOutput.print("Closing database Connection");
                }


            } catch (SQLException e)
            {
                ConsoleOutput.print("Couldn't close sql connection or statements");
                Platform.runLater(() ->updateProcess("Error on closing connection.",-1.0));

            }

        }
    }


    /**
     * Registration of new medicine
     * @param medicine medicine object to insert
     * @see Medicine
     */
    public void insert(Medicine medicine)
    {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database", 0.1));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.2));
            preparedStatement = connection.prepareStatement("INSERT INTO Medicine" +
                    " VALUES (DEFAULT ,?, ?, ?, ?, ?, ?,DEFAULT)");
            preparedStatement.setString(1, medicine.getName());
            preparedStatement.setString(2, medicine.getDescription());
            preparedStatement.setString(3, medicine.getUsage());
            preparedStatement.setString(4, medicine.getLicense());
            preparedStatement.setString(5, medicine.getSideEffect());
            preparedStatement.setString(6, medicine.getType());
            preparedStatement.executeUpdate();
            Platform.runLater(() -> updateProcess("SQL statement executed successfully.Medicine is registered", 1.0));
            ConsoleOutput.print(getClass().getName(), " SQL statement executed successfully.Medicine is registered");



        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> updateProcess("Something went wrong. Contact Marius",-1.0));

        } finally {

            try {
                if(preparedStatement != null)
                {
                    preparedStatement.close();
                    ConsoleOutput.print("Closing prepared insert Statement");
                }

                if(connection != null)
                {
                    connection.close();
                    ConsoleOutput.print("Closing database Connection");
                }


            } catch (SQLException e)
            {
                ConsoleOutput.print("Couldn't close sql connection or statements");
                Platform.runLater(() ->updateProcess("Error on closing connection.",-1.0));

            }

        }

    }

}
