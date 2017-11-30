package Registers;

import AppUsers.Pharmacy;
import Classes.ConsoleOutput;
import DatabaseConnection.DatabaseConnectionConfiguration;
import Security.BCrypt;
import Security.RandomPasswordGenerator;
import javafx.application.Platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * <h2>Created by Marius Baltramaitis on 05/08/2017.</h2>
 *
 * <p>Pharmacy register</p>
 */
public class PharmacyRegister extends AbstractRegister<Pharmacy> {


    private String login;


    /**
     *  {@inheritDoc}
     */

    @Override
    public void updateProcess(String connectionInformation, Double progressValue) {
        if(super.connectionSensor != null)
            super.connectionSensor.apply(connectionInformation,progressValue);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public boolean update(Pharmacy pharmacy) {


        int ID = Integer.parseInt(pharmacy.getID());
        Connection connection = null;
        PreparedStatement preparedStatement = null;



        try {
            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database",0.3));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.4));
            preparedStatement = connection.prepareStatement("UPDATE  Pharmacy set name = ?, address = ?, additionalInfo = ?, password = ?, available  = ? WHERE ID = ?");
            preparedStatement.setString(1, pharmacy.getName());
            preparedStatement.setString(2, pharmacy.getAddress());
            preparedStatement.setString(3, pharmacy.getAdditionalInfo());
            preparedStatement.setString(4, pharmacy.getEncryptedPassword());
            preparedStatement.setInt(5, pharmacy.getAvailable());
            preparedStatement.setInt(6, ID);
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

    /**
     * Method to find pharmacies inside database
     * @param name pharmacy name
     * @return array list with found data
     */
    public ArrayList<Pharmacy> find(String name)
    {
        name = "%"+name+"%";
        Platform.runLater(() -> updateProcess("Preparing database connection",0.1));
        ArrayList<Pharmacy> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            Platform.runLater(() -> updateProcess("Connecting to database",0.3));
            connection = DatabaseConnectionConfiguration.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM Pharmacy WHERE NAME Like ? LIMIT 200");
            preparedStatement.setString(1,name);
            resultSet = preparedStatement.executeQuery();
            Platform.runLater(() -> updateProcess("Collecting data",0.6));

            while (resultSet.next()){
                Pharmacy pharmacy = new Pharmacy();
                pharmacy.setID(""+resultSet.getInt("ID"));
                pharmacy.setLogin(resultSet.getString("login"));
                pharmacy.setName(resultSet.getString("name"));
                pharmacy.setAddress(resultSet.getString("address"));
                pharmacy.setAdditionalInfo(resultSet.getString("additionalInfo"));
                pharmacy.setSalt(resultSet.getString("salt"));
                pharmacy.setEncryptedPassword(resultSet.getString("password"));
                pharmacy.setAvailable(resultSet.getInt("available"));
                pharmacy.setLastUsedDevice(resultSet.getString("lastUsedDevice"));
                pharmacy.setPrimaryPassword(resultSet.getString("primaryPassword"));
                list.add(pharmacy);
            }

            if(list.isEmpty())
                Platform.runLater(() -> updateProcess("No data found with given details",1.0));


        }catch (Exception e)
        {
            ConsoleOutput.print(getClass().getName(),e);
            Platform.runLater(() -> updateProcess("Error connecting to server",-1.0));

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
     * Method to register new pharmacy
     * @param pharmacy pharmacy object with data inside
     * @param phoneNumber destination number
     */
    public void insert(Pharmacy pharmacy, String phoneNumber)
    {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement idStatement = null;
        PreparedStatement loginStatement = null;
        ResultSet resultSet = null;
        int ID = 0;

        Platform.runLater(() ->updateProcess("Generating random password",0.1));
        String password = RandomPasswordGenerator.generatePassword();
        Platform.runLater(() ->updateProcess("Generating salt and encrypting password using BCrypt",0.2));
        String salt = BCrypt.gensalt();
        String encryptedPassword = BCrypt.hashpw(password,salt);

        pharmacy.setPrimaryPassword(encryptedPassword);
        pharmacy.setPassword(encryptedPassword);
        pharmacy.setSalt(salt);
        try {
            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database", 0.3));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.4));
            preparedStatement = connection.prepareStatement("INSERT INTO Pharmacy (name,address,additionalInfo,salt,password,primaryPassword)" +
                    "VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, pharmacy.getName());
            preparedStatement.setString(2, pharmacy.getAddress());
            preparedStatement.setString(3, pharmacy.getAdditionalInfo());
            preparedStatement.setString(4, pharmacy.getSalt());
            preparedStatement.setString(5, pharmacy.getPassword());
            preparedStatement.setString(6, pharmacy.getPassword());
            preparedStatement.executeUpdate();
            Platform.runLater(() -> updateProcess("SQL statement executed successfully.Updating login details now", 0.5));
            ConsoleOutput.print(getClass().getName(), " SQL statement executed successfully.Updating login details now");
            idStatement = connection.prepareStatement("SELECT LAST_INSERT_ID();");
            resultSet = idStatement.executeQuery();


            while (resultSet.next()) {
                ID = resultSet.getInt(1);

            }


            login = generateLogin(pharmacy,ID);


            resultSet = null;

            loginStatement = connection.prepareStatement("UPDATE Pharmacy SET login = ? WHERE ID = ?;");
            loginStatement.setString(1,login);
            loginStatement.setInt(2,ID);
            loginStatement.executeUpdate();

            super.deliverLoginDetails(phoneNumber,password);


        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> updateProcess("Something went wrong. Contact Marius",1.0));

        } finally {

            try {
                if(loginStatement != null)
                {
                    loginStatement.close();
                    ConsoleOutput.print("Closing login statement");
                }
                if(idStatement != null)
                {
                    idStatement.close();
                    ConsoleOutput.print("Closing id statement");
                }
                if(resultSet != null)
                {
                    resultSet.close();
                    ConsoleOutput.print("Closing resultSet");
                }
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
                Platform.runLater(() ->updateProcess("Error on closing connection.",1.0));

            }

        }

    }
}
