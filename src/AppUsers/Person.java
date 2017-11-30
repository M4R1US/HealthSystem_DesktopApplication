package AppUsers;

/**
 * <h2>Created by Marius Baltramaitis on 23/06/2017.</h2>
 * <p>Abstract class of all persons</p>
 */
public abstract class Person extends AbstractApplicationUser {

    private String firstName,lastName,sex;

    /**
     * Default constructor
     */
    public Person() {}

    /**
     *
     * @param firstName first name
     * @param lastName last name
     * @param sex sex (gender)
     */
    public Person(String firstName, String lastName, String sex) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
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
    public Person(String login, String encryptedPassword, String laseUsedDevice, String salt, String ID, String password, String primaryPassword, String additionalInfo, int available, String firstName, String lastName, String sex) {
        super(login, encryptedPassword, laseUsedDevice, salt, ID, password, primaryPassword, additionalInfo, available);
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
    }

    /**
     * Getter of first name
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Setter of first name
     * @param firstName first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Getter of last name
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Setter of last name
     * @param lastName last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Getter of sex (gender)
     * @return sex (gender)
     */
    public String getSex() {
        return sex;
    }

    /**
     * setter of sex (gender)
     * @param sex sex (gender)
     */
    public void setSex(String sex) {
        this.sex = sex;
    }
}
