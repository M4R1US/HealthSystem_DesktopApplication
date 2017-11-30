package Animations2D;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * <h2>Created by Marius Baltramaitis on 30/04/2017.</h2>
 *
 * <p>Thread of rotating image clockwise</p>
 */
public class SynchronizationAnimation extends ImageView implements Runnable {

    private Timeline timeline;
    private final double rotationAngle = 8.0;
    private boolean running = false;

    /**
     * Default constructor
     */
    public SynchronizationAnimation()
    {
        timeline = new Timeline(new KeyFrame(Duration.millis(50),
                event ->{
                    this.setRotate(getRotate()+rotationAngle);
                    //ConsoleOutput.print("Animating");
                }),
                new KeyFrame(Duration.millis(50)));
        timeline.setCycleCount(Animation.INDEFINITE);
    }


    /**
     * Method to start animation
     */
    public void run()
    {
        if(!running)
        {
            timeline.play();
            running = true;
        }
    }

    /**
     * @return true if animation is running
     */
    public boolean running()
    {
        return running;
    }

    /**
     * Method for stopping animation
     */
    public void stop()
    {
        if(running)
        {
            timeline.stop();
            running = false;
            this.setRotate(0);
        }
    }

}
