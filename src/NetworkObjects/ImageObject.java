package NetworkObjects;
import java.io.Serializable;

/**
 * <h2>Created by Marius Baltramaitis on 21-Jan-17.</h2>
 *
 * <p>Image object to exchange pictures between client and server</p>
 */
public class ImageObject implements Serializable {

    private String ID;
    private String type;
    private String name;
    private String messageToServer;
    private String messageFromServer;
    private byte [] personImageBytes;


    /**
     * Constructor with necessary data input
     * @param ID ID equivalent to database id of users image
     * @param type type of user (example Doctor)
     * @param name name of user
     * @param messageToServer message to server
     */
    public ImageObject(String ID, String type, String name, String messageToServer)
    {
        this.ID = ID;
        this.type = type;
        this.name = name;
        this.messageToServer = messageToServer;
    }

    /**
     * Getter for ID
     * @return ID of user
     */
    public String getID()
    {
        return ID;
    }

    /**
     * Setter for ID
     * @param ID ID to set
     */
    public void setID(String ID)
    {
        this.ID = ID;
    }

    /**
     * Getter of message to server
     * @return message to server
     */
    public String getMessageToServer()
    {
        return messageToServer;
    }

    /**
     * Setter of message to server
     * @param messageToServer message to server
     */
    public void setMessageToServer(String messageToServer)
    {
        this.messageToServer = messageToServer;
    }

    /**
     * Getter of message from server
     * @return message from server
     */
    public String getMessageFromServer()
    {
        return messageFromServer;
    }

    /**
     * Setter of message from server
     * @param messageFromServer message from server
     */
    public void setMessageFromServer(String messageFromServer)
    {
        this.messageFromServer = messageFromServer;
    }

    /**
     * Getter of byte array of user image
     * @return byte array of user image
     */
    public byte[] getPersonImageBytes() { return personImageBytes; }

    /**
     * Setter for byte array of user image
     * @param personImageBytes byte array of user image
     */
    public void setPersonImageBytes(byte[] personImageBytes)
    {
        this.personImageBytes = personImageBytes;
    }

    /**
     * Getter for name
     * @return name
     */
    public String getName() { return name; }

    /**
     * Setter for name
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for user type
     * @return user type
     */
    public String getType() { return type; }

    /**
     * Setter for user type
     * @param type user type
     */
    public void setType(String type) { this.type = type; }

}
