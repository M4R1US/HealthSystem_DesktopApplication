package Registers;

import AppUsers.Administrator;
import Classes.ConsoleOutput;
import DatabaseConnection.DatabaseConnectionConfiguration;
import javafx.application.Platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <h2>Created by Marius Baltramaitis on 13/08/2017.</h2>
 *
 * <p>Administrator register</p>
 */
public class AdministratorRegister extends AbstractRegister<Administrator> {


    /**
     *  {@inheritDoc}
     */

    @Override
    public void updateProcess(String connectionInformation, Double progressValue) {
        if(super.connectionSensor != null)
            connectionSensor.apply(connectionInformation,progressValue);
    }

    /**
     *  {@inheritDoc}
     */

    @Override
    public boolean update(Administrator administrator)
    {

        int ID = Integer.parseInt(administrator.getID());
        Connection connection = null;
        PreparedStatement preparedStatement = null;



        try {
            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database",0.3));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.4));
            preparedStatement = connection.prepareStatement("UPDATE  Administrator set firstName = ?, lastName = ?, sex = ?, additionalInfo = ?, password = ?, available  = ? WHERE ID = ?");
            preparedStatement.setString(1, administrator.getFirstName());
            preparedStatement.setString(2, administrator.getLastName());
            preparedStatement.setString(3, administrator.getSex());
            preparedStatement.setString(4, administrator.getAdditionalInfo());
            preparedStatement.setString(5, administrator.getEncryptedPassword());
            preparedStatement.setInt(6, administrator.getAvailable());
            preparedStatement.setInt(7, ID);
            preparedStatement.executeUpdate();
            Platform.runLater(() -> updateProcess("Update finished successfully!", 1.0));
            ConsoleOutput.print(getClass().getName(), " Update finished successfully!");


        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() ->updateProcess("Something went wrong. Contact Marius",-1.0));

        } finally {

            try {
                if(preparedStatement != null)
                {
                    preparedStatement.close();
                    ConsoleOutput.print("Closing prepared insert Statement");
                    return true;
                }

                if(connection != null)
                {
                    connection.close();
                    ConsoleOutput.print("Closing database Connection");
                    return true;
                }


            } catch (SQLException e)
            {
                ConsoleOutput.print("Couldn't close sql connection or statements");
                Platform.runLater(() -> updateProcess("Error on closing connection.",-1.0));
                return false;
            }

        }

        return true;

    }
}
