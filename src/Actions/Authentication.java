package Actions;

import AppUsers.*;
import Classes.ConsoleOutput;
import Classes.GenericPair;
import CustomExceptions.InvalidCredentialsException;
import CustomExceptions.NoAccessException;
import DatabaseConnection.DatabaseConnectionConfiguration;
import NetworkObjects.FirewallCredentials;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import Security.Cryptography;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;


/**
 * <h2>Created by Marius Baltramaitis on 02/04/2017.</h2>
 *
 * This class is responsible for user authentication and authorisation.
 * <br> Internet connection required to maintain connection between application and database
 */
public class Authentication<T extends AbstractApplicationUser> {

    private SimpleObjectProperty<GenericPair<String,Double>> connectionProperty;
    private boolean authenticated;
    private T user;


    /**
     * Constructor with necessary parameters
     * @param userType object any subclass of AbstractApplicationUser to authenticate
     * @see AbstractApplicationUser
     */
    public Authentication(T userType)
    {

        DeviceProperties.detectOS();
        connectionProperty = new SimpleObjectProperty<>(new GenericPair<>("No action",-1.0));
        authenticated = false;
        this.user = userType;
    }

    public SimpleObjectProperty<GenericPair<String,Double>> connectionProperty() { return connectionProperty; }


    /**
     * Method to authenticate user
     * @return true if user is authenticated, false otherwise
     */
    public boolean authenticated() {
        String type = "";

        Supplier<Boolean> authenticationAction = null;


        if (user instanceof Administrator)
        {
            type = "Admin";
            authenticationAction = () ->authenticateAdmin((Administrator) user);
        }

        if (user instanceof Doctor)
        {
            type = "Doctor";
            authenticationAction = () -> authenticateDoctor((Doctor) user);

        }

       if(user instanceof Patient)
       {
           type = "Patient";
           authenticationAction = () -> authenticatePatient((Patient) user);

       }

       if(user instanceof Pharmacy)
       {
           type = "Pharmacy";
           authenticationAction = () -> authenticatePharmacy((Pharmacy) user);
       }

       try {
           sendFirewallRequest(type);
       } catch (IOException e)
       {
           Platform.runLater(() -> connectionProperty.set(new GenericPair<>("Can't encrypt data",-1.0)));
           authenticationAction = null;
       } catch (NoAccessException e)
       {
           Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Access denied!",-1.0)));
           authenticationAction = null;
       } catch (InvalidCredentialsException e)
       {
           Platform.runLater(() -> connectionProperty.set(new GenericPair<>("Invalid credentials",-1.0)));
           authenticationAction = null;
       }

       finally {

           if(authenticationAction != null)
               return authenticationAction.get();
       }

       return false;

    }

    /**
     * Method that is sending firewall request to the server and asking for database login credentials and gsm phone numbers
     * @param userType user type (example doctor etc.. )
     * @throws NoAccessException if user is disabled in the system
     * @throws InvalidCredentialsException if user credentials are not valid
     * @throws IOException if connection couldn't be established
     */
    private void sendFirewallRequest(String userType) throws NoAccessException, InvalidCredentialsException,IOException
    {
        Socket clientSocket = new Socket();
        Cryptography cryptography = new Cryptography();


        FirewallCredentials firewallCredentials = new FirewallCredentials();
        try {
            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Sending access request to server",0.1)));
            firewallCredentials.setType(cryptography.encrypt(userType, DynamicVariables.AES_128_KEY));
            firewallCredentials.setLogin(cryptography.encrypt(user.getLogin(),DynamicVariables.AES_128_KEY));
            firewallCredentials.setPassword(cryptography.encrypt(user.getPassword(),DynamicVariables.AES_128_KEY));
        } catch (Exception e)
        {
            ConsoleOutput.print(getClass().getName(),e);
            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Couldn't encrypt data!",-1.0)));
            throw new IOException("Error on encrypting data! Check Authentication class.");
        }

        clientSocket.connect(new InetSocketAddress(FinalConstants.HOST_DOMAIN_NAME, FinalConstants.FIREWALL_PORT),10000);
        ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
        outToServer.writeObject(firewallCredentials);
        ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());

        try{

            firewallCredentials = (FirewallCredentials)inFromServer.readObject();
        } catch (ClassNotFoundException e) {ConsoleOutput.print(getClass().getName(),e);}

        finally {

            try
            {

                if(firewallCredentials.getDatabaseLoginName().equalsIgnoreCase("0") || firewallCredentials.getDatabaseLoginPassword().equalsIgnoreCase("0"))
                {
                    Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Invalid credentials",-1.0)));
                    throw new InvalidCredentialsException("No matching data with credentials");

                }
                if(firewallCredentials.getDatabaseLoginName().equalsIgnoreCase("-1") || firewallCredentials.getDatabaseLoginPassword().equalsIgnoreCase("-1"))
                {
                    Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Account disabled!",-1.0)));
                    throw new NoAccessException("Account disabled!");


                }

                //setting parameters here
                DynamicVariables.databaseLogin = cryptography.decrypt(firewallCredentials.getDatabaseLoginName(),DynamicVariables.AES_128_KEY);
                DynamicVariables.databasePassword = cryptography.decrypt(firewallCredentials.getDatabaseLoginPassword(),DynamicVariables.AES_128_KEY);
                DynamicVariables.ACCOUNT_SID = cryptography.decrypt(firewallCredentials.getAccountSID(),DynamicVariables.AES_128_KEY);
                DynamicVariables.AUTH_TOKEN = cryptography.decrypt(firewallCredentials.getAuthToken(),DynamicVariables.AES_128_KEY);
                DynamicVariables.NORWEGIAN_NUMBER = cryptography.decrypt(firewallCredentials.getNorwegianNumber(),DynamicVariables.AES_128_KEY);
                DynamicVariables.LITHUANIAN_NUMBER = cryptography.decrypt(firewallCredentials.getLithuanianNumber(),DynamicVariables.AES_128_KEY);



            } catch (Exception e) {ConsoleOutput.print(getClass().getName(),e);}

        }

    }


    /**
     * Authentication method for administrators
     * @param administrator object with login details
     * @return <p>
     *     true if administrator login details are valid
     *     <br>false if administrator login details are not valid or user has no access(Banned in database)
     * </p>
     */
    private boolean authenticateAdmin(Administrator administrator)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement updateDeviceStatement = null;
        ResultSet resultSet = null;


        try {

            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Connecting to database",0.2)));
            connection = DatabaseConnectionConfiguration.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM Administrator WHERE login = ?");
            preparedStatement.setString(1, administrator.getLogin());
            preparedStatement.executeQuery();
            resultSet = preparedStatement.getResultSet();

                if(!resultSet.isBeforeFirst())
                {
                    Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Invalid credentials",-1.0)));
                    return false;
                }

                while (resultSet.next())
                {
                    administrator.setID(""+resultSet.getInt("ID"));
                    administrator.setLogin(resultSet.getString("login"));
                    administrator.setFirstName(resultSet.getString("firstName"));
                    administrator.setLastName(resultSet.getString("lastName"));
                    administrator.setSex(resultSet.getString("sex"));
                    administrator.setAdditionalInfo(resultSet.getString("additionalInfo"));
                    administrator.setSalt(resultSet.getString("salt"));
                    administrator.setEncryptedPassword(resultSet.getString("password"));
                    administrator.setAvailable(resultSet.getInt("available"));
                    administrator.setLastUsedDevice(resultSet.getString("lastUsedDevice"));

                }

                resultSet.close();


            updateDeviceStatement = connection.prepareStatement("UPDATE Administrator SET lastUsedDevice = ? WHERE login = ?");
            updateDeviceStatement.setString(1,DeviceProperties.DEVICE_NAME);
            updateDeviceStatement.setString(2,administrator.getLogin());
            updateDeviceStatement.executeUpdate();
            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Authenticated",1.0)));
            authenticated = true;


            } catch (Exception e) {

                ConsoleOutput.print(getClass().getName(),e);
                ConsoleOutput.print(getClass().getName(),"Unexpected exception.. Report this to developers");
                Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Error connecting to database",-1.0)));
                return false;

            } finally {

                if(updateDeviceStatement != null)
                {
                    try { updateDeviceStatement.close(); } catch (SQLException e)
                    {
                        ConsoleOutput.print(getClass().getName(),"Error on closing updateDeviceStatement. Report this to Marius");
                    }

                }
                if(resultSet != null)
                {
                    try { resultSet.close(); } catch (SQLException e1)
                    {
                        ConsoleOutput.print(getClass().getName(),"Error on closing result set. Report this to Marius");
                    }

                }
                if(preparedStatement != null)
                {
                    try {preparedStatement.close();} catch (SQLException e1)
                    {
                        ConsoleOutput.print(getClass().getName(),"Error on closing preparedStatement. Report this to Marius");
                    }

                }

                if(connection != null)
                {
                    try { connection.close(); } catch (SQLException e1)
                    {
                        ConsoleOutput.print(getClass().getName(),"Error on closing connection. Report this to Marius");
                    }
                }


            }

        if(authenticated)
        {
            Platform.runLater(() -> WindowLauncher.launchAdminDashboard(administrator));
            return true;
        }

        return false;

    }


    /**
     * Authentication method for doctors
     * @param doctor object with login details
     * @return <p>
     *     true if doctor login details are valid
     *     <br>false if doctor login details are not valid or user has no access(Banned in database)
     * </p>
     */
    public boolean authenticateDoctor(Doctor doctor)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement updateDeviceStatement = null;
        ResultSet resultSet = null;


        try {

            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Connecting to database",0.3)));
            connection = DatabaseConnectionConfiguration.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM Doctor WHERE login = ?");
            preparedStatement.setString(1, doctor.getLogin());
            preparedStatement.executeQuery();
            resultSet = preparedStatement.getResultSet();

            if(!resultSet.isBeforeFirst())
            {
                Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Invalid credentials",-1.0)));
                return false;
            }

            while (resultSet.next())
            {
                doctor.setID(""+resultSet.getInt("ID"));
                doctor.setLogin(resultSet.getString("login"));
                doctor.setFirstName(resultSet.getString("firstName"));
                doctor.setLastName(resultSet.getString("lastName"));
                doctor.setSex(resultSet.getString("sex"));
                doctor.setAdditionalInfo(resultSet.getString("additionalInfo"));
                doctor.setSalt(resultSet.getString("salt"));
                doctor.setEncryptedPassword(resultSet.getString("password"));
                doctor.setAvailable(resultSet.getInt("available"));
                doctor.setLastUsedDevice(resultSet.getString("lastUsedDevice"));
                doctor.setPrimaryPassword(resultSet.getString("primaryPassword"));
                doctor.setLicense(resultSet.getString("license"));
                doctor.setTitle(resultSet.getString("title"));

            }

            resultSet.close();


            updateDeviceStatement = connection.prepareStatement("UPDATE Doctor SET lastUsedDevice = ? WHERE login = ?");
            updateDeviceStatement.setString(1,DeviceProperties.DEVICE_NAME);
            updateDeviceStatement.setString(2,doctor.getLogin());
            updateDeviceStatement.executeUpdate();
            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Authenticated",1.0)));
            authenticated = true;

        } catch (Exception e) {

            ConsoleOutput.print(getClass().getName(),e);
            ConsoleOutput.print(getClass().getName(),"Unexpected exception.. Report this to developers");
            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Error connecting to database",-1.0)));
            return false;

        } finally {

            if(updateDeviceStatement != null)
            {
                try { updateDeviceStatement.close(); } catch (SQLException e)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing updateDeviceStatement. Report this to Marius");
                }

            }
            if(resultSet != null)
            {
                try { resultSet.close(); } catch (SQLException e1)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing result set. Report this to Marius");
                }

            }
            if(preparedStatement != null)
            {
                try {preparedStatement.close();} catch (SQLException e1)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing preparedStatement. Report this to Marius");
                }

            }

            if(connection != null)
            {
                try { connection.close(); } catch (SQLException e1)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing connection. Report this to Marius");
                }
            }


        }

        if(authenticated)
        {
            Platform.runLater(() -> WindowLauncher.launchDoctorDashboard(doctor));
            return true;
        }

        return false;
    }


    /**
     * Authentication method for patients
     * @param patient object with login details
     * @return <p>
     *     true if patient login details are valid
     *     <br>false if patient login details are not valid or user has no access(Banned in database)
     * </p>
     */
    public boolean authenticatePatient(Patient patient)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement updateDeviceStatement = null;
        ResultSet resultSet = null;


        try {

            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Connecting to database",0.3)));
            connection = DatabaseConnectionConfiguration.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM Patient WHERE login = ?");
            preparedStatement.setString(1, patient.getLogin());
            preparedStatement.executeQuery();
            resultSet = preparedStatement.getResultSet();

            if(!resultSet.isBeforeFirst())
            {
                Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Invalid credentials",-1.0)));
                return false;
            }

            while (resultSet.next())
            {
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

            }

            resultSet.close();


            updateDeviceStatement = connection.prepareStatement("UPDATE Patient SET lastUsedDevice = ? WHERE login = ?");
            updateDeviceStatement.setString(1,DeviceProperties.DEVICE_NAME);
            updateDeviceStatement.setString(2,patient.getLogin());
            updateDeviceStatement.executeUpdate();
            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Authenticated",1.0)));
            authenticated = true;


        } catch (Exception e) {

            ConsoleOutput.print(getClass().getName(),e);
            ConsoleOutput.print(getClass().getName(),"Unexpected exception.. Report this to developers");
            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Error connecting to database",-1.0)));
            return false;

        } finally {

            if(updateDeviceStatement != null)
            {
                try { updateDeviceStatement.close(); } catch (SQLException e)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing updateDeviceStatement. Report this to Marius");
                }

            }
            if(resultSet != null)
            {
                try { resultSet.close(); } catch (SQLException e1)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing result set. Report this to Marius");
                }

            }
            if(preparedStatement != null)
            {
                try {preparedStatement.close();} catch (SQLException e1)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing preparedStatement. Report this to Marius");
                }

            }

            if(connection != null)
            {
                try { connection.close(); } catch (SQLException e1)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing connection. Report this to Marius");
                }
            }


        }

        if(authenticated)
        {
            Platform.runLater(() -> WindowLauncher.launchPatientDashboard(patient));
            return true;
        }

        return false;
    }


    /**
     * Authentication method for pharmacies
     * @param pharmacy object with login details
     * @return <p>
     *     true if pharmacy login details are valid
     *     <br>false if pharmacy login details are not valid or user has no access(Banned in database)
     * </p>
     */
    public boolean authenticatePharmacy(Pharmacy pharmacy)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement updateDeviceStatement = null;
        ResultSet resultSet = null;


        try {

            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Connecting to database",0.3)));
            connection = DatabaseConnectionConfiguration.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM Pharmacy WHERE login = ?");
            preparedStatement.setString(1, pharmacy.getLogin());
            preparedStatement.executeQuery();
            resultSet = preparedStatement.getResultSet();

            if(!resultSet.isBeforeFirst())
            {
                Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Invalid credentials",-1.0)));
                return false;
            }

            while (resultSet.next())
            {
                pharmacy.setID(""+resultSet.getInt("ID"));
                pharmacy.setLogin(resultSet.getString("login"));
                pharmacy.setName(resultSet.getString("Name"));
                pharmacy.setAdditionalInfo(resultSet.getString("additionalInfo"));
                pharmacy.setSalt(resultSet.getString("salt"));
                pharmacy.setEncryptedPassword(resultSet.getString("password"));
                pharmacy.setAvailable(resultSet.getInt("available"));
                pharmacy.setLastUsedDevice(resultSet.getString("lastUsedDevice"));
                pharmacy.setPrimaryPassword(resultSet.getString("primaryPassword"));
                pharmacy.setAddress(resultSet.getString("address"));

            }

            resultSet.close();

            updateDeviceStatement = connection.prepareStatement("UPDATE Pharmacy SET lastUsedDevice = ? WHERE login = ?");
            updateDeviceStatement.setString(1,DeviceProperties.DEVICE_NAME);
            updateDeviceStatement.setString(2,pharmacy.getLogin());
            updateDeviceStatement.executeUpdate();
            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Authenticated",1.0)));
            authenticated = true;


        } catch (Exception e) {

            ConsoleOutput.print(getClass().getName(),e);
            ConsoleOutput.print(getClass().getName(),"Unexpected exception.. Report this to developers");
            Platform.runLater(() ->connectionProperty.set(new GenericPair<>("Error connecting to database.",-1.0)));
            return false;

        } finally {

            if(updateDeviceStatement != null)
            {
                try { updateDeviceStatement.close(); } catch (SQLException e)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing updateDeviceStatement. Report this to Marius");
                }

            }
            if(resultSet != null)
            {
                try { resultSet.close(); } catch (SQLException e1)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing result set. Report this to Marius");
                }

            }
            if(preparedStatement != null)
            {
                try {preparedStatement.close();} catch (SQLException e1)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing preparedStatement. Report this to Marius");
                }

            }

            if(connection != null)
            {
                try { connection.close(); } catch (SQLException e1)
                {
                    ConsoleOutput.print(getClass().getName(),"Error on closing connection. Report this to Marius");
                }
            }


        }

        if(authenticated)
        {
            Platform.runLater(() -> WindowLauncher.launchPharmacyDashboard(pharmacy));
            return true;
        }

        return false;
    }

}
