package Registers;

import Actions.SmsOutPut;
import AppUsers.*;
import Classes.ConsoleOutput;
import Interfaces.ConnectionSensor;
import Interfaces.DatabaseRegisterImplementation;
import javafx.application.Platform;

/**
 * <h2>Created by Marius Baltramaitis on 23/06/2017.</h2>
 * <p>
 *     Abstract class for database registers<br>
 *
 *     T - any non abstract subclass of AbstractApplicationUsers
 *
 * </p>
 * @see AbstractApplicationUser
 */
public abstract class AbstractRegister<T extends AbstractApplicationUser> implements DatabaseRegisterImplementation {

    protected ConnectionSensor connectionSensor;

    private String login;

    /**
     *  {@inheritDoc}
     */

    public void addListener(ConnectionSensor sensor) {this.connectionSensor = sensor;}

    /** Method to generate login name
     * @param t any non abstract subclass of AbstractApplicationUser
     * @param ID ID of user
     * @return String with user login name
     * @see AbstractApplicationUser
     */
    protected String generateLogin(T t, int ID)
    {
        if(t instanceof Pharmacy)
            login = ((Pharmacy) t).getName() + ID;

        if(t instanceof Person)
        {
            Person p = (Person) t;
            String namePart = (p.getFirstName().length() < 3) ? p.getFirstName() : p.getFirstName().substring(0, 3);
            String lastNamePart = (p.getLastName().length() < 3) ? p.getLastName() : p.getLastName().substring(0, 3);
            login = (namePart + lastNamePart + ID).toLowerCase();
        }

        StringBuilder stringBuilder = new StringBuilder(login);

        for(int i = 0; i < login.toCharArray().length; i++)
        {
            if(login.charAt(i) == ' ')
                stringBuilder.setCharAt(i,'_');
        }

        login = stringBuilder.toString();

        return login;

    }

    /**
     * Method to update object information inside database
     * @param t object to update
     * @return true if transaction succeeded, false otherwise
     */
    public abstract boolean update(T t);

    /**
     * Method to deliver login details
     * @param phoneNumber destination number
     * @param password password
     */
    protected void deliverLoginDetails(String phoneNumber,String password)
    {
        String loginText = "Login : " + login + " " + "Password : " + password;
        if(phoneNumber != null)
        {
            sendSms(phoneNumber,loginText);
            return;
        }

        ConsoleOutput.print("DONE! login details: " + "Login: " + login + " password " + password);
        Platform.runLater(() -> updateProcess("DONE! login details:",0.6));
        Platform.runLater(() -> updateProcess("Login: " + login,0.7));
        Platform.runLater(() -> updateProcess("Primary password: " + password,0.8));
        Platform.runLater(() -> updateProcess("Transaction finished successfully",1.0));

    }

    /**
     * Method to deliver sms message
     * @param phoneNumber destination number
     * @param loginText message content
     */
    private void sendSms(String phoneNumber,String loginText)
    {
        SmsOutPut smsOutPut = new SmsOutPut(loginText,phoneNumber);
        smsOutPut.sendShortMessage();


        Platform.runLater(() -> updateProcess("Delivered authentication details to " + phoneNumber,1.0));
    }

}
