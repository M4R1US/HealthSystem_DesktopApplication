package Animations2D;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * <h2>Created by Marius Baltramaitis on 24/11/2017.</h2>
 *
 * <p>Illusion of writing text</p>
 */
public class TextAnimation implements Runnable {

    private char[] textChars;
    private Label textLabel;

    /**
     * Constructor taking necessary parameters
     * @param textLabel label to read text from
     */
    public TextAnimation(Label textLabel) {
        this.textLabel = textLabel;
    }


    /**
     * Method to launch animation
     */
    @Override
    public void run() {

        textChars = textLabel.getText().toCharArray();
        textLabel.setText("");
        Timeline nodeTimeline = new Timeline(new KeyFrame(Duration.millis(20),
                event -> {
                    if(textLabel.getText().toCharArray().length < textChars.length)
                    {
                        textLabel.setText(textLabel.getText() + textChars[textLabel.getText().length()]);
                        return;
                    }
                }),
                new KeyFrame(Duration.millis(20)));

        nodeTimeline.setCycleCount(textChars.length);
        nodeTimeline.play();

        nodeTimeline.setOnFinished(event -> {
            if(!textLabel.getText().equalsIgnoreCase(String.valueOf(textChars)))
                textLabel.setText(String.valueOf(textChars));
        });

    }
}
