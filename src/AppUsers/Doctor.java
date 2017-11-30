package AppUsers;

/**
 * <h2>Created by Marius Baltramaitis on 24/05/2017.</h2>
 * <p>Doctor class</p>
 */
public class Doctor extends Person {

    private String license,title;


    /**
     * Default constructor
     */
    public Doctor(){}

    /**
     *
     * @param title doctor title
     * @param firstName fist name
     * @param lastName last name
     * @param sex sex (gender)
     * @param additionalInfo additional information
     * @param license license (A or B)
     */
    public Doctor(String title,String firstName,String lastName,String sex, String additionalInfo, String license)
    {
        super(firstName,lastName,sex);
        this.title = title;
        this.license = license;
        super.setAdditionalInfo(additionalInfo);
    }

    /**
     *
     * @param login login
     * @param password password
     */
    public Doctor(String login, String password)
    {
        super.setLogin(login);
        super.setPassword(password);
    }

    /**
     *
     * @return "Doctor"
     */
    public String getTableName()
    {
        return "Doctor";
    }

    /**
     * Getter for license
     * @return license
     */
    public String getLicense() {
        return license;
    }

    /**
     * Setter for license
     * @param license license
     */
    public void setLicense(String license) {
        this.license = license;
    }


    /**
     * Getter for title
     * @return doctor title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for title
     * @param title doctor title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return String with first name and last name
     */
    public String toString() {
        return super.getFirstName() + ", " + super.getLastName();
    }
}
