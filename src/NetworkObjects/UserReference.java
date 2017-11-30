package NetworkObjects;

import java.io.Serializable;

/**
 * <h2>Created by Marius Baltramaitis on 13/04/2017.</h2>
 *
 * <p>Object with client information</p>
 */
public class UserReference implements Serializable {

    private String name;
    private String type;
    private String login;
    // 0 for disconnecting, 1 for connecting
    private byte action;


    /**
     *  Constructor taking necessary parameters
     * @param login login of client
     * @param name name of user
     * @param type user type
     * @param action action [0 for disconnecting and 1 for connecting]
     */
    public UserReference(String login,String name,String type,byte action)
    {
        this.login = login;
        this.name = name;
        this.type = type;
        this.action = action;
    }

    /**
     * Getter for name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for type
     * @return user type
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for user type
     * @param type user type
     */
    public void setType(String type) {
        this.type = type;
    }


    /**
     * Getter for login
     * @return login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Setter for login
     * @param login login
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Setter for action
     * @param action 1 for connecting, 0 for disconnecting
     */
    public void setAction(byte action) {
        this.action = action;
    }

    /**
     * Getter for action
     * @return 1 for connecting, 0 for disconnecting
     */
    public byte getAction() {
        return action;
    }

    /**
     * Comparing objects
     * @param o object to compare
     * @return true if o equal this class
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof UserReference))
            return false;

        return ((UserReference) o).getLogin().equals(login) && ((UserReference) o).getType().equals(type) && ((UserReference) o).getName().equals(name);
    }
}
