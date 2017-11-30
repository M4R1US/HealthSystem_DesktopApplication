package AppUsers;

/**
 * <h2>Created by Marius Baltramaitis on 18/03/17.</h2>
 * <p>Administrator class</p>
 */
public class Administrator extends Person {


    /**
     * Default constructor
     */
    public Administrator() {}

    /**
     * @param login login
     * @param password password
     */
    public Administrator(String login,String password)
    {
        super.setLogin(login);
        super.setPassword(password);
    }

    /**
     * Getter for table name
     * @return "Administrator"
     */
    @Override
    public String getTableName()
    {
        return "Administrator";
    }

    /**
     * toString method
     * @return information of Administrator in one String line
     */
    public String toString()
    {
        return "SALT " + salt + " login " + login + "last used device: " + super.lastUsedDevice + " lastname " + super.getLastName() + System.lineSeparator();
    }
}
