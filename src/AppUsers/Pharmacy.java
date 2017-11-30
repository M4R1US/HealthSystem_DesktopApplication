package AppUsers;

/**
 * <h2>Created by Marius Baltramaitis on 23/06/2017.</h2>
 * <p>Class of pharmacy</p>
 */
public class Pharmacy extends AbstractApplicationUser {

    private String address,name;

    /**
     * Default constructor
     */
    public Pharmacy() {}

    /**
     *
     * @param login login
     * @param password password
     */
    public Pharmacy(String login,String password)
    {
        super.setLogin(login);
        super.setPassword(password);
    }

    /**
     *
     * @return "Pharmacy"
     */
    public String getTableName()
    {
        return "Pharmacy";
    }

    /**
     * Getter for address
     * @return address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Setter for address
     * @param address address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Getter for pharmacy name
     * @return pharmacy name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for pharmacy name
     * @param name pharmacy name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     *
     * @return name
     * @see Pharmacy#getName()
     */
    @Override
    public String toString()
    {
        return getName();
    }
}
