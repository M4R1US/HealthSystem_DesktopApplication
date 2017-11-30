package AppUsers;

/**
 * <h2>Created by Marius Baltramaitis on 02/04/2017.</h2>
 *
 * <p>Abstract class with common user data </p>
 */
public abstract class AbstractApplicationUser {

    public String login, password, encryptedPassword, lastUsedDevice, salt, ID, additionalInfo,primaryPassword;
    public int available;

    /**
     * Default constructor
     */
    public AbstractApplicationUser() {}


    /**
     * @param login login
     * @param encryptedPassword encrypted password
     * @param lastUsedDevice device used last time
     * @param salt salt used to encrypt password
     * @param ID users ID
     * @param password password
     * @param primaryPassword encrypted password
     * @param additionalInfo additional information of this user
     * @param available 1 - available 0 - disabled
     */
    public AbstractApplicationUser(String login, String encryptedPassword, String lastUsedDevice, String salt, String ID, String password, String primaryPassword, String additionalInfo, int available) {
        this.login = login;
        this.encryptedPassword = encryptedPassword;
        this.lastUsedDevice = lastUsedDevice;
        this.salt = salt;
        this.ID = ID;
        this.password = password;
        this.additionalInfo = additionalInfo;
        this.available = available;
        this.primaryPassword = primaryPassword;
    }

    /**
     * Getter for user table name in database
     * @return table name
     */
    public abstract String getTableName();

    /**
     * Getter for login
     * @return login name
     */
    public String getLogin() {
        return login;
    }

    /**
     * Setter for login
     * @param login login name
     */
    public void setLogin(String login) {
        this.login = login;
    }


    /**
     * Getter for encrypted password
     * @return encurypted password
     */
    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    /**
     * Setter for encrypted password
     * @param encryptedPassword encrypted password
     */
    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    /**
     * Getter for last used device of this used
     * @return last used device of this user
     */
    public String getLastUsedDevice() {
        return lastUsedDevice;
    }

    /**
     * Setter for last used device of this used
     * @param lastUsedDevice last used device of this user
     */
    public void setLastUsedDevice(String lastUsedDevice) {
        this.lastUsedDevice = lastUsedDevice;
    }

    /**
     * Getter for salt
     * @return salt used to encrypt password
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Setter for salt
     * @param salt salt used to encrypt password
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * Getter for ID
     * @return Users ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Setter for ID
     * @param ID ID
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     *  Getter for not encrypted password
     * @return not encrypted password (plain text)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for password
     * @param password password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter for additional information of administrator
     * @return additional information of administrator
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Setter for additional information of administrator
     * @param additionalInfo additional information of administrator
     */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * Getter for available
     * @return 1 if available 0 if disabled
     */
    public int getAvailable() {
        return available;
    }

    /**
     * Setter for available
     * @param available 1 if available 0 if disabled
     */
    public void setAvailable(int available) {
        this.available = available;
    }

    /**
     * Getter for primary password
     * @return primary password
     */
    public String getPrimaryPassword() {
        return primaryPassword;
    }

    /**
     * Setter for primary password
     * @param primaryPassword primary password of user
     */
    public void setPrimaryPassword(String primaryPassword) {
        this.primaryPassword = primaryPassword;
    }
}
