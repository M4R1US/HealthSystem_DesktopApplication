package AppUsers;

/**
 * <h2>Created by Marius Baltramaitis on 23/06/2017.</h2>
 *
 * <p>Patient class</p>
 */
public class Patient extends Person {

    /**
     * Default Constructor
     */
    public Patient(){}

    /**
     *
     * @param firstName first name
     * @param lastName last name
     * @param sex sex (gender)
     */
    public Patient(String firstName, String lastName, String sex) {
        super(firstName, lastName, sex);
    }

    /**
     *
     * @param login login
     * @param encryptedPassword encrypted password
     * @param laseUsedDevice last used device
     * @param salt salt used to encrypt password
     * @param ID ID
     * @param password plain text password
     * @param primaryPassword plain text primary password
     * @param additionalInfo addicional information
     * @param available 1 if available 0 if disabled
     * @param firstName first name
     * @param lastName last name
     * @param sex sex (gender)
     */
    public Patient(String login, String encryptedPassword, String laseUsedDevice, String salt, String ID, String password, String primaryPassword, String additionalInfo, int available, String firstName, String lastName, String sex) {
        super(login, encryptedPassword, laseUsedDevice, salt, ID, password, primaryPassword, additionalInfo, available, firstName, lastName, sex);
    }

    /**
     *
     * @param login login
     * @param password password
     */
    public Patient(String login, String password) {
        super.setLogin(login);
        super.setPassword(password);

    }

    /**
     *
     * @return "Patient"
     */
    public String getTableName()
    {
        return "Patient";
    }

    /**
     *
     * @return String with first name and last name
     */
    @Override
    public String toString()
    {
        return getFirstName() + " " + getLastName();
    }


}
