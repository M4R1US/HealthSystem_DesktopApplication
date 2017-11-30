package Controllers.InsideControllers;

import Interfaces.ServerImageRequestImplementation;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 24-Dec-16.</h2>
 *
 * <p>Abstract class of all inside controllers</p>
 */
public abstract class InsideController implements Initializable, ServerImageRequestImplementation {

    private ExecutorService browseImageThreadPool;
    private String userIdentity;

    /**
     * Default constructor initializing browse image thread pool
     */
    public InsideController()
    {
        browseImageThreadPool = Executors.newWorkStealingPool();
    }

    private Timeline paneTimeline;

    /**
     * Method to restrict width of base pane
     * @param ProvidedParentWidth max width provided by parent pane
     */
    public abstract void setWidth(double ProvidedParentWidth);

    /**
     * Method to restrict height of base pane
     * @param ProvidedParentHeight max height provided by parent pane
     */
    public abstract void setHeight(double ProvidedParentHeight);

    /**
     * Lock function provided from base controller
     * @param lockFunction lock function from base controller
     */
    public abstract void setParentLockFunction(Consumer<Boolean> lockFunction);

    /**
     * Setter for user identity
     * @param name user identity (typically first name )
     */
    public void setUserIdentity(String name) { this.userIdentity = name; }

    /**
     * General method for all instances to request image from server
     * @param ID user ID
     * @param userType user type
     * @param imageConsumer consumer function which accepts Image from server thread
     * @see NetworkThreads.RequestImage
     */
    protected void sendImageRequest(String ID, String userType, Consumer<Image> imageConsumer)
    {
        sendImageRequest(ID,userType,imageConsumer,browseImageThreadPool,userIdentity);
    }


    /**
     * Opacity contrast effect for given pane
     * @param p pane to apply
     */
    protected void contrastEffect(Pane p)
    {
        p.getChildren().forEach(node -> node.setOpacity(0));
        paneTimeline = new Timeline(new KeyFrame(Duration.millis(50),
                event -> {
                    p.getChildren().forEach(children -> {
                        if(children.getOpacity() < 1)
                        {
                            children.setOpacity(children.getOpacity() + 0.05);
                            return;
                        }
                        paneTimeline.stop();
                    });

                }),
                new KeyFrame(Duration.millis(50)));

        paneTimeline.setCycleCount(Animation.INDEFINITE);
        paneTimeline.play();
    }


}
