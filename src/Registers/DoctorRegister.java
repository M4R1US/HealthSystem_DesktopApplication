package Registers;

import AppUsers.Doctor;
import Classes.ConsoleOutput;
import DatabaseConnection.DatabaseConnectionConfiguration;
import Security.BCrypt;
import Security.RandomPasswordGenerator;
import javafx.application.Platform;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by Marius Baltramaitis on 24/05/2017.
 */
public class DoctorRegister extends AbstractRegister<Doctor> {


    private String login = null;

    /**
     *
     * @param doctor Doctor object with data
     * @param phoneNumber Phone number to deliver login details
     */
    public void insert(Doctor doctor, String phoneNumber)
    {

            Connection connection = null;
            PreparedStatement preparedStatement = null;
            PreparedStatement idStatement = null;
            PreparedStatement loginStatement = null;
            ResultSet resultSet = null;
            int ID = 0;

            Platform.runLater(() -> updateProcess("Generating random password",0.1));
            String password = RandomPasswordGenerator.generatePassword();
            Platform.runLater(() -> updateProcess("Generating salt and encrypting password using BCrypt",0.2));
            String salt = BCrypt.gensalt();
            String encryptedPassword = BCrypt.hashpw(password,salt);

            doctor.setPrimaryPassword(encryptedPassword);
            doctor.setPassword(encryptedPassword);
            doctor.setSalt(salt);
            try {
                Platform.runLater(() -> updateProcess("Connecting to health-system.eu database",0.3));
                ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
                connection = DatabaseConnectionConfiguration.getConnection();
                Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.4));
                preparedStatement = connection.prepareStatement("INSERT INTO Doctor (title,firstName,lastName,sex,license,additionalInfo,salt,password,primaryPassword)" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                preparedStatement.setString(1, doctor.getTitle());
                preparedStatement.setString(2, doctor.getFirstName());
                preparedStatement.setString(3, doctor.getLastName());
                preparedStatement.setString(4, doctor.getSex());
                preparedStatement.setString(5, doctor.getLicense());
                preparedStatement.setString(6, doctor.getAdditionalInfo());
                preparedStatement.setString(7, doctor.getSalt());
                preparedStatement.setString(8, doctor.getPassword());
                preparedStatement.setString(9, doctor.getPrimaryPassword());
                preparedStatement.executeUpdate();
                Platform.runLater(() -> updateProcess("SQL statement executed successfully.Updating login details now", 0.5));
                ConsoleOutput.print(getClass().getName(), " SQL statement executed successfully.Updating login details now");
                idStatement = connection.prepareStatement("SELECT LAST_INSERT_ID();");
                resultSet = idStatement.executeQuery();


                while (resultSet.next()) {
                    ID = resultSet.getInt(1);

                }


                login = super.generateLogin(doctor,ID);

                resultSet = null;

                loginStatement = connection.prepareStatement("UPDATE Doctor SET login = ? WHERE ID = ?;");
                loginStatement.setString(1,login);
                loginStatement.setInt(2,ID);
                loginStatement.executeUpdate();
                super.deliverLoginDetails(phoneNumber,password);


            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->updateProcess("Something went wrong. Contact Marius",1.0));

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
                    Platform.runLater(() -> updateProcess("Error on closing connection.",1.0));

                }

            }

    }


    /**
     * @param fName first name
     * @param lName last name
     * @return ArrayList with all doctors matching given parameters
     */
    public ArrayList<Doctor> find(String fName, String lName)
    {
        String firstName = (fName == null || fName.equals("")) ? "%" : "%"+fName+"%";
        String lastName = (lName == null || lName.equals("")) ? "%" : "%"+lName+"%";

            Platform.runLater(() -> updateProcess("Preparing database connection",0.1));
            ArrayList<Doctor> list = new ArrayList<>();
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;

            try {

                Platform.runLater(() -> updateProcess("Connecting to database",0.3));
                connection = DatabaseConnectionConfiguration.getConnection();
                preparedStatement = connection.prepareStatement("SELECT * FROM Doctor WHERE firstName Like ? AND lastName LIKE ? LIMIT 200");
                preparedStatement.setString(1,firstName);
                preparedStatement.setString(2,lastName);
                resultSet = preparedStatement.executeQuery();
                Platform.runLater(() -> updateProcess("Collecting data",0.6));

                while (resultSet.next()){
                    Doctor doctor = new Doctor();
                    doctor.setID(""+resultSet.getInt("ID"));
                    doctor.setLogin(resultSet.getString("login"));
                    doctor.setTitle(resultSet.getString("title"));
                    doctor.setFirstName(resultSet.getString("firstName"));
                    doctor.setLastName(resultSet.getString("lastName"));
                    doctor.setSex(resultSet.getString("sex"));
                    doctor.setLicense(resultSet.getString("license"));
                    doctor.setAdditionalInfo(resultSet.getString("additionalInfo"));
                    doctor.setSalt(resultSet.getString("salt"));
                    doctor.setEncryptedPassword(resultSet.getString("password"));
                    doctor.setAvailable(resultSet.getInt("available"));
                    doctor.setLastUsedDevice(resultSet.getString("lastUsedDevice"));
                    doctor.setPrimaryPassword(resultSet.getString("primaryPassword"));
                    list.add(doctor);
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
    public boolean update(Doctor doctor) {

        int ID = Integer.parseInt(doctor.getID());
        Connection connection = null;
        PreparedStatement preparedStatement = null;



        try {
            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database",0.3));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.4));
            preparedStatement = connection.prepareStatement("UPDATE  Doctor set title = ?, firstName = ?, lastName = ?, sex = ?, license  = ?, additionalInfo = ?, password = ?, available = ? WHERE ID = ?");
            preparedStatement.setString(1, doctor.getTitle());
            preparedStatement.setString(2, doctor.getFirstName());
            preparedStatement.setString(3, doctor.getLastName());
            preparedStatement.setString(4, doctor.getSex());
            preparedStatement.setString(5, doctor.getLicense());
            preparedStatement.setString(6, doctor.getAdditionalInfo());
            preparedStatement.setString(7, doctor.getEncryptedPassword());
            preparedStatement.setInt(8, doctor.getAvailable());
            preparedStatement.setInt(9, ID);
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
