package NetworkObjects;

import java.io.Serializable;

/**
 * <h2>Created by Marius Baltramaitis on 06/10/2017.</h2>
 *
 * <p>Object to exchange encrypted data(via tcp protocol), runtime variables so user can gain access to database.</p>
 *
 * <p>Database login credentials and gsm phone numbers are stored in files</p>
 */
public class FirewallCredentials implements Serializable {

    // Implement numbers, and gsm access keys
    private String LithuanianNumber,NorwegianNumber,Login,Type,Password,DatabaseLoginName,DatabaseLoginPassword,AuthToken,AccountSID;

    /**
     * Default constructor
     */
    public FirewallCredentials() {}

    /**
     * Getter for lithuanian number
     * @return Lithuanian gsm number for output sms
     */
    public String getLithuanianNumber() {
        return LithuanianNumber;
    }

    /**
     * Setter for lithuanian number
     * @param lithuanianNumber setter for lithuanian number
     */
    public void setLithuanianNumber(String lithuanianNumber) {
        LithuanianNumber = lithuanianNumber;
    }

    /**
     * Getter for norwegian number
     * @return Norwegian gsm number for output sms
     */
    public String getNorwegianNumber() {
        return NorwegianNumber;
    }


    /**
     * Setter for norwegian number
     * @param norwegianNumber setter for norwegian number
     */
    public void setNorwegianNumber(String norwegianNumber) {
        NorwegianNumber = norwegianNumber;
    }

    /**
     * Getter for user login
     * @return login name
     */
    public String getLogin() {
        return Login;
    }

    /**
     * Setter for login
     * @param login login name
     */
    public void setLogin(String login) {
        Login = login;
    }

    /**
     * getter for user type
     * @return setter for user type
     */
    public String getType() {
        return Type;
    }

    /**
     * Setter for user type
     * @param type user type
     */
    public void setType(String type) {
        Type = type;
    }

    /**
     * Getter for password
     * @return setter for password
     */
    public String getPassword() {
        return Password;
    }

    /**
     * Setter for password
     * @param password password to set
     */
    public void setPassword(String password) {
        Password = password;
    }

    /**
     * Getter for database login name
     * @return database login name
     */
    public String getDatabaseLoginName() {
        return DatabaseLoginName;
    }

    /**
     * Setter for database login name
     * @param databaseLoginName database login name
     */
    public void setDatabaseLoginName(String databaseLoginName) {
        DatabaseLoginName = databaseLoginName;
    }

    /**
     * Getter for database login password
     * @return database login password
     */
    public String getDatabaseLoginPassword() {
        return DatabaseLoginPassword;
    }

    /**
     * Setter for database login password
     * @param databaseLoginPassword database login password
     */
    public void setDatabaseLoginPassword(String databaseLoginPassword) {
        DatabaseLoginPassword = databaseLoginPassword;
    }

    /**
     * Getter for auth token from sms provider
     * @return auth token from sms provider
     */
    public String getAuthToken() {
        return AuthToken;
    }

    /**
     * Setter auth token from sms provider
     * @param authToken auth token from sms provider
     */
    public void setAuthToken(String authToken) {
        AuthToken = authToken;
    }

    /**
     * Getter for account sid from sms provider
     * @return account sid from sms provider
     */
    public String getAccountSID() {
        return AccountSID;
    }

    /**
     * Setter for account sid from sms provider
     * @param accountSID account sid from sms provider
     */
    public void setAccountSID(String accountSID) {
        AccountSID = accountSID;
    }
}
