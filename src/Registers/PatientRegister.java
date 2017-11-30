package Registers;

import AppUsers.Patient;
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
 * <h2>Created by Marius Baltramaitis on 23/06/2017.</h2>
 *
 * <p>Patient register</p>
 */
public class PatientRegister extends AbstractRegister<Patient> {

    private String login = null;

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
    public boolean update(Patient patient) {

        int ID = Integer.parseInt(patient.getID());
        Connection connection = null;
        PreparedStatement preparedStatement = null;



        try {
            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database",0.3));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.4));
            preparedStatement = connection.prepareStatement("UPDATE  Patient set firstName = ?, lastName = ?, sex = ?, additionalInfo = ?, password = ?, available  = ? WHERE ID = ?");
            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getLastName());
            preparedStatement.setString(3, patient.getSex());
            preparedStatement.setString(4, patient.getAdditionalInfo());
            preparedStatement.setString(5, patient.getEncryptedPassword());
            preparedStatement.setInt(6, patient.getAvailable());
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

    /**
     * Method to find patient information in database
     * @param fName first name
     * @param lName last name
     * @param onlyAvailable true if to pick only available, false otherwise
     * @return array list with found data
     */
    public ArrayList<Patient> find(String fName, String lName, boolean onlyAvailable)
    {
        String firstName = (fName == null || fName.equals("")) ? "%" : "%"+fName+"%";
        String lastName = (lName == null || lName.equals("")) ? "%" : "%"+lName+"%";


        Platform.runLater(() -> updateProcess("Preparing database connection",0.1));
        ArrayList<Patient> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            connection = DatabaseConnectionConfiguration.getConnection();
            preparedStatement = (onlyAvailable) ? connection.prepareStatement("Select * from Patient where firstName Like ? AND lastName like ? AND available = 1 LIMIT 200") : connection.prepareStatement("SELECT * FROM Patient WHERE firstName Like ? AND lastName LIKE ? LIMIT 200");
            Platform.runLater(() -> updateProcess("Connecting to database",0.3));
            preparedStatement.setString(1,firstName);
            preparedStatement.setString(2,lastName);
            resultSet = preparedStatement.executeQuery();
            Platform.runLater(() -> updateProcess("Collecting data",0.6));

            while (resultSet.next()){
                Patient patient = new Patient();
                patient.setID(""+resultSet.getInt("ID"));
                patient.setLogin(resultSet.getString("login"));
                patient.setFirstName(resultSet.getString("firstName"));
                patient.setLastName(resultSet.getString("lastName"));
                patient.setSex(resultSet.getString("sex"));
                patient.setAdditionalInfo(resultSet.getString("additionalInfo"));
                patient.setSalt(resultSet.getString("salt"));
                patient.setEncryptedPassword(resultSet.getString("password"));
                patient.setAvailable(resultSet.getInt("available"));
                patient.setLastUsedDevice(resultSet.getString("lastUsedDevice"));
                patient.setPrimaryPassword(resultSet.getString("primaryPassword"));
                list.add(patient);
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
     * Method to register new patient
     * @param patient patient object with data to insert
     * @param phoneNumber destination phone number
     */
    public void insert(Patient patient, String phoneNumber)
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

        patient.setPrimaryPassword(encryptedPassword);
        patient.setPassword(encryptedPassword);
        patient.setSalt(salt);
        try {
            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database", 0.3));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.4));
            preparedStatement = connection.prepareStatement("INSERT INTO Patient (firstName,lastName,sex,additionalInfo,salt,password,primaryPassword)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getLastName());
            preparedStatement.setString(3, patient.getSex());
            preparedStatement.setString(4, patient.getAdditionalInfo());
            preparedStatement.setString(5, patient.getSalt());
            preparedStatement.setString(6, patient.getPassword());
            preparedStatement.setString(7, patient.getPrimaryPassword());
            preparedStatement.executeUpdate();
            Platform.runLater(() -> updateProcess("SQL statement executed successfully.Updating login details now", 0.5));
            ConsoleOutput.print(getClass().getName(), " SQL statement executed successfully.Updating login details now");
            idStatement = connection.prepareStatement("SELECT LAST_INSERT_ID();");
            resultSet = idStatement.executeQuery();


            while (resultSet.next()) {
                ID = resultSet.getInt(1);

            }


            login = generateLogin(patient,ID);


            resultSet = null;

            loginStatement = connection.prepareStatement("UPDATE Patient SET login = ? WHERE ID = ?;");
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
