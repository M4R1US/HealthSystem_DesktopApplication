package CustomCache;

import com.sun.istack.internal.NotNull;
import javafx.scene.image.Image;

/**
 * <h2>Created by Marius Baltramaitis on 05/06/2017.</h2>
 *
 * <p>Class holding users picture and information about him/her</p>
 */
public class ImageCache {

    private String ID, userType;
    private Image image;

    /**
     * Constructor taking necessary parameters
     * @param ID Users ID
     * @param userType type of user
     * @param image image itself
     */
    public ImageCache(@NotNull  String ID, @NotNull String userType, Image image) {
        this.ID = ID;
        this.userType = userType;
        this.image = image;
    }


    /**
     * Getter for ID
     * @return ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Getter for user type
     * @return userType
     */

    public String getUserType() {
        return userType;
    }

    /**
     * Getter for image
     * @return image itself
     */
    public Image getImage() {
        return image;
    }

    /**
     * Setter for image
     * @param image image itself
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * toString method checking if parameter is equal to this class
     * @param o object to compare
     * @return true if data from this class equal o. data
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof ImageCache))
            return false;

        return ((ImageCache) o).getID().equalsIgnoreCase(ID) && ((ImageCache) o).getUserType().equalsIgnoreCase(userType);

    }
}
