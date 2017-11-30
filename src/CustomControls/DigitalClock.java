package CustomControls;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * <h2>Created by Marius Baltramaitis on 26-Dec-16.</h2>
 *
 * <p>Class of fancy digital clock placed inside every dashboard</p>
 */
public class DigitalClock extends Label {

    private static DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final String FONT_PATH = getClass().getClassLoader().getResource("Fonts/digital-7.ttf").toExternalForm();
    public Font digital;

    /**
     * Default constructor
     */
    public DigitalClock()
    {
        bindToTime();
        digital = Font.loadFont(FONT_PATH,30);
        this.setStyle("-fx-font-family: " + digital.getFamily() + ";");

    }


    /**
     * Binding label to time
     */
    private void bindToTime()
    {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0),
                event -> setText(LocalTime.now().format(TIME))),
                new KeyFrame(Duration.seconds(1)));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

}
