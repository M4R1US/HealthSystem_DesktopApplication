package Registers;

import AppUsers.*;
import Classes.ConsoleOutput;
import Classes.Medicine;
import Classes.Prescription;
import DatabaseConnection.DatabaseConnectionConfiguration;
import Interfaces.ConnectionSensor;
import Interfaces.DatabaseRegisterImplementation;
import Interfaces.DatabaseTransactionFinishedProperty;
import javafx.application.Platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * <h2>Created by Marius Baltramaitis on 27/09/2017.</h2>
 *
 * <p>Prescription register<br>
 *      T - Any non abstract sub class of AbstractApplicationUser
 * </p>
 * @see AbstractApplicationUser
 */
public class PrescriptionRegister<T extends AbstractApplicationUser> implements DatabaseRegisterImplementation {


    private ConnectionSensor connectionSensor;
    public DatabaseTransactionFinishedProperty transactionFinishUpdateFunction;


    /**
     * Method to attach sensor which is used to inform controllers whether transaction succeeded or not
     * @param finishedStatus finishedStatus
     * @see DatabaseTransactionFinishedProperty
     */
    public void attachFinishSensor(DatabaseTransactionFinishedProperty finishedStatus)
    {
        transactionFinishUpdateFunction = finishedStatus;
    }

    /**
     * Method to update status
     * @param status true if finished succeeded, false otherwise
     */
    private void updateTransactionSensor(boolean status) {
        if(transactionFinishUpdateFunction != null)
            transactionFinishUpdateFunction.update(status);
    }

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
     * Method to insert new prescription
     * @param prescription prescription object with data to upload
     * @return true if transaction succeeded, false otherwise
     */
    public boolean insert(Prescription prescription)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database", 0.1));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.2));
            preparedStatement = connection.prepareStatement("INSERT INTO Prescription" +
                    " VALUES (DEFAULT ,?, ?, ?, NULL, 1, ?)");
            preparedStatement.setInt(1, prescription.getMedicine_ID());
            preparedStatement.setInt(2, prescription.getDoctor_ID());
            preparedStatement.setInt(3, prescription.getPatient_ID());
            preparedStatement.setString(4, prescription.getSpecialNotes());

            ConsoleOutput.print("IDS:  medicine id " + prescription.getMedicine_ID() + " doctor id " + prescription.getDoctor_ID() + " patient id " + prescription.getPatient_ID() + " pharmacy id " + prescription.getPharmacy_ID());
            preparedStatement.executeUpdate();
            Platform.runLater(() -> updateProcess("SQL statement executed successfully.Medicine is registered", 1.0));
            ConsoleOutput.print(getClass().getName(), " SQL statement executed successfully.Medicine is registered");
            updateTransactionSensor(true);
            return true;


        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> updateProcess("Something went wrong. Contact Marius",-1.0));
            updateTransactionSensor(false);
            return false;
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
                updateTransactionSensor(false);
                return false;
            }

        }
    }

    /**
     * Method to mark prescription as sold
     * @param prescriptionID prescription ID
     * @param pharmacyID pharmacy ID
     */
    public void markAsSold(String prescriptionID,String pharmacyID)
    {
        int pID = Integer.parseInt(prescriptionID);
        int phID = Integer.parseInt(pharmacyID);

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database", 0.1));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.2));
            preparedStatement = connection.prepareStatement("Update Prescription set Pharmacy_ID = ? where ID = ?");
            preparedStatement.setInt(1, phID);
            preparedStatement.setInt(2, pID);
            preparedStatement.executeUpdate();
            Platform.runLater(() -> updateProcess("SQL statement executed successfully.Medicine is registered", 1.0));
            ConsoleOutput.print(getClass().getName(), " SQL statement executed successfully.Medicine is registered");
            updateTransactionSensor(true);


        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> updateProcess("Something went wrong. Contact Marius",-1.0));
            updateTransactionSensor(false);
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
                updateTransactionSensor(false);
                ConsoleOutput.print("Couldn't close sql connection or statements");
                Platform.runLater(() ->updateProcess("Error on closing connection.",-1.0));
            }

        }



    }

    /**
     * Method to find all unsold prescriptions of patient
     * @param patientID patient ID
     * @return Array list of found data
     */
    public ArrayList<Prescription> findAvailableAndNotSold(String patientID)
    {
        ArrayList<Prescription> prescriptionArrayList = new ArrayList<>();
        int ID = Integer.parseInt(patientID);

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        Platform.runLater(() -> updateProcess("Preparing database connection",0.1));
        ResultSet resultSet = null;

        try {

            Platform.runLater(() -> updateProcess("Connecting to database",0.3));
            connection = DatabaseConnectionConfiguration.getConnection();
            preparedStatement= connection.prepareStatement("select Prescription.ID, Prescription.available,Prescription.specialNotes, Doctor.ID, Doctor.firstName, Doctor.lastName, Doctor.title,Doctor.sex, Medicine.*,Patient.ID,Patient.firstName, Patient.lastName,Patient.sex, Pharmacy.ID, Pharmacy.name, Pharmacy.address from Prescription Inner Join Doctor ON Prescription.Doctor_ID = Doctor.ID Inner Join Medicine ON Prescription.Medicine_ID = Medicine.ID Inner Join Patient ON Prescription.Patient_ID = Patient.ID LEFT JOIN Pharmacy ON Prescription.Pharmacy_ID = Pharmacy.ID WHERE Patient_ID = ? AND Prescription.available = 1 AND Pharmacy_ID IS NULL LIMIT 200");
            preparedStatement.setInt(1,ID);
            resultSet = preparedStatement.executeQuery();
            Platform.runLater(() -> updateProcess("Collecting data",0.6));

            while (resultSet.next())
            {
                Doctor doctor = new Doctor();
                Medicine medicine = new Medicine();
                Patient patient = new Patient();
                Prescription prescription = new Prescription();
                Pharmacy pharmacy = null;
                prescription.setID(""+resultSet.getInt(1));
                prescription.setAvailable(resultSet.getInt(2));
                prescription.setSpecialNotes(resultSet.getString(3));
                doctor.setID(""+ resultSet.getInt(4));
                doctor.setFirstName(resultSet.getString(5));
                doctor.setLastName(resultSet.getString(6));
                doctor.setTitle(resultSet.getString(7));
                doctor.setSex(resultSet.getString(8));
                medicine.setID(""+resultSet.getInt(9));
                medicine.setName(resultSet.getString(10));
                medicine.setDescription(resultSet.getString(11));
                medicine.setUsage(resultSet.getString(12));
                medicine.setLicense(resultSet.getString(13));
                medicine.setSideEffect(resultSet.getString(14));
                medicine.setType(resultSet.getString(15));
                medicine.setAvailable(resultSet.getInt(16));
                patient.setID(""+resultSet.getInt(17));
                patient.setFirstName(resultSet.getString(18));
                patient.setLastName(resultSet.getString(19));
                patient.setSex(resultSet.getString(20));

                if(resultSet.getString(22) != null)
                {
                    pharmacy = new Pharmacy();
                    pharmacy.setID(""+resultSet.getInt(21));
                    pharmacy.setName(resultSet.getString(22));
                    pharmacy.setAddress(resultSet.getString(23));

                }

                prescription.setMedicine(medicine);
                prescription.setDoctor(doctor);
                prescription.setPatient(patient);
                prescription.setPharmacy(pharmacy);

                prescriptionArrayList.add(prescription);

                updateTransactionSensor(true);

            }

            if(prescriptionArrayList.isEmpty())
                Platform.runLater(() -> updateProcess("No data found with given details",1.0));


        }catch (Exception e)
        {
            ConsoleOutput.print(getClass().getName(),e);
            Platform.runLater(() -> updateProcess("Error connecting to server",1.0));
            updateTransactionSensor(false);


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
        return prescriptionArrayList;
    }


    /**
     * Method to update availability of prescription
     * @param prescriptionID prescription ID
     * @param available 1 to enable, 0 to disable
     */
    public void updateAvailability(int prescriptionID, int available)
    {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            Platform.runLater(() -> updateProcess("Connecting to health-system.eu database", 0.1));
            ConsoleOutput.print(getClass().getName(), "Connecting to health-system.eu database");
            connection = DatabaseConnectionConfiguration.getConnection();
            Platform.runLater(() -> updateProcess("Connection established. Preparing to upload data", 0.2));
            preparedStatement = connection.prepareStatement("Update Prescription set available = ? where ID = ?");
            preparedStatement.setInt(1, available);
            preparedStatement.setInt(2, prescriptionID);
            preparedStatement.executeUpdate();
            Platform.runLater(() -> updateProcess("SQL statement executed successfully.Medicine is registered", 1.0));
            ConsoleOutput.print(getClass().getName(), " SQL statement executed successfully.Medicine is registered");
            updateTransactionSensor(true);


        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> updateProcess("Something went wrong. Contact Marius",-1.0));
            updateTransactionSensor(false);
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
                updateTransactionSensor(false);
                ConsoleOutput.print("Couldn't close sql connection or statements");
                Platform.runLater(() ->updateProcess("Error on closing connection.",-1.0));
            }

        }

    }

    /**
     * Generating proper prepared statement based of arguments
     * @param t any non abstract sub class of ApplicationUser
     * @param connection conenction object
     * @param onlyAvailable true if only available, false for all
     * @return prepared sql statement
     * @throws SQLException if connection could not be established
     * @see AbstractApplicationUser
     */
    private PreparedStatement findPrescriptionPreparedSqlStatement(T t, Connection connection,boolean onlyAvailable) throws SQLException
    {
        PreparedStatement preparedStatement = null;
        int ID  = Integer.parseInt(t.getID());

       switch (t.getTableName())
       {
           case "Doctor":
               if(onlyAvailable)
                   preparedStatement = connection.prepareStatement("select Prescription.ID, Prescription.available,Prescription.specialNotes, Doctor.ID, Doctor.firstName, Doctor.lastName, Doctor.title, Doctor.sex, Medicine.*,Patient.ID,Patient.firstName, Patient.lastName, Patient.sex, Pharmacy.ID, Pharmacy.name, Pharmacy.address from Prescription Inner Join Doctor ON Prescription.Doctor_ID = Doctor.ID Inner Join Medicine ON Prescription.Medicine_ID = Medicine.ID Inner Join Patient ON Prescription.Patient_ID = Patient.ID LEFT JOIN Pharmacy ON Prescription.Pharmacy_ID = Pharmacy.ID WHERE Doctor_ID = ? AND Prescription.available = 1 LIMIT 200");
               else
               preparedStatement = connection.prepareStatement("select Prescription.ID, Prescription.available,Prescription.specialNotes, Doctor.ID, Doctor.firstName, Doctor.lastName, Doctor.title, Doctor.sex, Medicine.*,Patient.ID,Patient.firstName, Patient.lastName, Patient.sex, Pharmacy.ID, Pharmacy.name, Pharmacy.address from Prescription Inner Join Doctor ON Prescription.Doctor_ID = Doctor.ID Inner Join Medicine ON Prescription.Medicine_ID = Medicine.ID Inner Join Patient ON Prescription.Patient_ID = Patient.ID LEFT JOIN Pharmacy ON Prescription.Pharmacy_ID = Pharmacy.ID WHERE Doctor_ID = ?  LIMIT 200");

               break;


           case "Patient":
               if(onlyAvailable)
                   preparedStatement= connection.prepareStatement("select Prescription.ID, Prescription.available,Prescription.specialNotes, Doctor.ID, Doctor.firstName, Doctor.lastName, Doctor.title,Doctor.sex, Medicine.*,Patient.ID,Patient.firstName, Patient.lastName,Patient.sex, Pharmacy.ID, Pharmacy.name, Pharmacy.address from Prescription Inner Join Doctor ON Prescription.Doctor_ID = Doctor.ID Inner Join Medicine ON Prescription.Medicine_ID = Medicine.ID Inner Join Patient ON Prescription.Patient_ID = Patient.ID LEFT JOIN Pharmacy ON Prescription.Pharmacy_ID = Pharmacy.ID WHERE Patient_ID = ? AND Prescription.available = 1 LIMIT 200");
               else
                   preparedStatement =connection.prepareStatement("select Prescription.ID, Prescription.available,Prescription.specialNotes, Doctor.ID, Doctor.firstName, Doctor.lastName, Doctor.title,Doctor.sex, Medicine.*,Patient.ID,Patient.firstName, Patient.lastName,Patient.sex, Pharmacy.ID, Pharmacy.name, Pharmacy.address from Prescription Inner Join Doctor ON Prescription.Doctor_ID = Doctor.ID Inner Join Medicine ON Prescription.Medicine_ID = Medicine.ID Inner Join Patient ON Prescription.Patient_ID = Patient.ID LEFT JOIN Pharmacy ON Prescription.Pharmacy_ID = Pharmacy.ID WHERE Patient_ID = ? LIMIT 200");

               break;

           case "Pharmacy" :
               if(onlyAvailable)
                   preparedStatement= connection.prepareStatement("select Prescription.ID, Prescription.available,Prescription.specialNotes, Doctor.ID, Doctor.firstName, Doctor.lastName, Doctor.title,Doctor.sex, Medicine.*,Patient.ID,Patient.firstName, Patient.lastName,Patient.sex, Pharmacy.ID, Pharmacy.name, Pharmacy.address from Prescription Inner Join Doctor ON Prescription.Doctor_ID = Doctor.ID Inner Join Medicine ON Prescription.Medicine_ID = Medicine.ID Inner Join Patient ON Prescription.Patient_ID = Patient.ID LEFT JOIN Pharmacy ON Prescription.Pharmacy_ID = Pharmacy.ID WHERE Pharmacy_ID = ? AND Prescription.available = 1 LIMIT 200");
               else
                   preparedStatement =connection.prepareStatement("select Prescription.ID, Prescription.available,Prescription.specialNotes, Doctor.ID, Doctor.firstName, Doctor.lastName, Doctor.title,Doctor.sex, Medicine.*,Patient.ID,Patient.firstName, Patient.lastName,Patient.sex, Pharmacy.ID, Pharmacy.name, Pharmacy.address from Prescription Inner Join Doctor ON Prescription.Doctor_ID = Doctor.ID Inner Join Medicine ON Prescription.Medicine_ID = Medicine.ID Inner Join Patient ON Prescription.Patient_ID = Patient.ID LEFT JOIN Pharmacy ON Prescription.Pharmacy_ID = Pharmacy.ID WHERE Pharmacy_ID = ? LIMIT 200");

               break;
       }

       preparedStatement.setInt(1,ID);
       return preparedStatement;
    }

    /**
     * @param t any non abstract sub class of ApplicationUser
     * @param onlyAvailable true if only available, false otherwise
     * @return arrayList with found data
     * @see AbstractApplicationUser
     */
    public ArrayList<Prescription> findBy(T t, boolean onlyAvailable)
    {
        if(t instanceof Administrator)
            throw new UnsupportedOperationException("Administrators can't have prescriptions in this version on application");

        ArrayList<Prescription> prescriptionArrayList = new ArrayList<>();

        Platform.runLater(() -> updateProcess("Preparing database connection",0.1));
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            Platform.runLater(() -> updateProcess("Connecting to database",0.3));
            connection = DatabaseConnectionConfiguration.getConnection();
            preparedStatement =  findPrescriptionPreparedSqlStatement(t,connection,onlyAvailable);
            resultSet = preparedStatement.executeQuery();
            Platform.runLater(() -> updateProcess("Collecting data",0.6));

            while (resultSet.next())
            {
                Doctor doctor = new Doctor();
                Medicine medicine = new Medicine();
                Patient patient = new Patient();
                Prescription prescription = new Prescription();
                Pharmacy pharmacy = null;
                prescription.setID(""+resultSet.getInt(1));
                prescription.setAvailable(resultSet.getInt(2));
                prescription.setSpecialNotes(resultSet.getString(3));
                doctor.setID(""+ resultSet.getInt(4));
                doctor.setFirstName(resultSet.getString(5));
                doctor.setLastName(resultSet.getString(6));
                doctor.setTitle(resultSet.getString(7));
                doctor.setSex(resultSet.getString(8));
                medicine.setID(""+resultSet.getInt(9));
                medicine.setName(resultSet.getString(10));
                medicine.setDescription(resultSet.getString(11));
                medicine.setUsage(resultSet.getString(12));
                medicine.setLicense(resultSet.getString(13));
                medicine.setSideEffect(resultSet.getString(14));
                medicine.setType(resultSet.getString(15));
                medicine.setAvailable(resultSet.getInt(16));
                patient.setID(""+resultSet.getInt(17));
                patient.setFirstName(resultSet.getString(18));
                patient.setLastName(resultSet.getString(19));
                patient.setSex(resultSet.getString(20));

                if(resultSet.getString(22) != null)
                {
                    pharmacy = new Pharmacy();
                    pharmacy.setID(""+resultSet.getInt(21));
                    pharmacy.setName(resultSet.getString(22));
                    pharmacy.setAddress(resultSet.getString(23));

                }

                prescription.setMedicine(medicine);
                prescription.setDoctor(doctor);
                prescription.setPatient(patient);
                prescription.setPharmacy(pharmacy);

                prescriptionArrayList.add(prescription);

                updateTransactionSensor(true);

            }

            if(prescriptionArrayList.isEmpty())
                Platform.runLater(() -> updateProcess("No data found with given details",1.0));


        }catch (Exception e)
        {
            ConsoleOutput.print(getClass().getName(),e);
            Platform.runLater(() -> updateProcess("Error connecting to server",1.0));
            updateTransactionSensor(false);


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
        return prescriptionArrayList;
    }
}
