package Actions;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Created by Marius B on 15-Dec-16.
 * <p>
 *     Simple class to handle action of stage (Window)
 * </p>
 */
public class StageAction {

    /**
     * Exit method to close application
     */
    public void exit()
    {
        Platform.exit();
    }


    /**
     * Minimize method to minimize stage
     * @param child any node that is placed inside stage
     */
    public void minimize(Node child) {
        Stage stage = (Stage)child.getScene().getWindow();
        stage.setFullScreen(false);
        stage.setIconified(true);
    }

    /**
     * Full screen method
     * @param child any node that is placed inside stage
     */
    public void maximize(Node child)
    {
        Stage currentWindow = (Stage)(child).getScene().getWindow();
        currentWindow.setFullScreen(!currentWindow.isFullScreen());

    }
}
