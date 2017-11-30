package Interfaces;

import Classes.ConsoleOutput;
import Classes.GenericPair;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Created by Marius Baltramaitis on 16/08/2017.
 */
public interface BrowsePaneAdditionalImplementation {

    /**
     * Special method for browse panes to resize nodes in special order
     * @param InfoVBox VBox everything is inside
     * @param TopLabel top label
     * @param NameLabel name label
     * @param TypeLabel type label
     * @return functional interface with instructions to resize nodes and apply design
     */
    default ChangeListener<Number> previewPaneWidthListener(VBox InfoVBox,Label TopLabel,Label NameLabel,Label TypeLabel)
    {
        return (observable, oldValue, newValue) -> {
            double doctorTypeFontSize = newValue.doubleValue()/20;
            double doctorNameFontSize = newValue.doubleValue()/15;
            TypeLabel.setStyle("-fx-font-size: " + doctorTypeFontSize);
            NameLabel.setStyle("-fx-font-size: " + doctorNameFontSize);
            TopLabel.setPrefWidth(newValue.doubleValue());
            InfoVBox.setPrefWidth(newValue.doubleValue()-50);
        };
    }

    /**
     * Special method for browse panes to resize nodes in special order
     * @param InfoVBox VBox everything is inside
     * @param ImageCircle Image circle
     * @return functional interface with instructions to resize nodes
     */
    default ChangeListener<Number> previewPaneHeightListener(VBox InfoVBox, Circle ImageCircle)
    {
        final double DEFAULT_IMAGE_RADIUS = 50;
        final double MAX_IMAGE_RADIUS = 80;

        return (observable, oldValue, newValue) -> {
            double newHeight = newValue.doubleValue() -100;
            InfoVBox.setPrefHeight(newHeight);
            double radius = (newHeight-200 > MAX_IMAGE_RADIUS) ? MAX_IMAGE_RADIUS : (newHeight-200)/2;
            if(radius < DEFAULT_IMAGE_RADIUS)
                radius = DEFAULT_IMAGE_RADIUS;
            ImageCircle.setRadius(radius);
        };
    }

    /**
     * Default method to split text by comma
     * @param text text to split
     * @return pair of two strings
     */
    default GenericPair<String,String> splitInput(String text)
    {
        String firstName = null;
        String lastName = null;

        if (text != null || !text.equals("")) {
            String browseInformation[] = text.split(",");
            try {

                firstName = browseInformation[0];
                lastName = browseInformation[1];

            } catch (ArrayIndexOutOfBoundsException e) {

                ConsoleOutput.print("First name and last name information is not provided properly. Expect more result from database");
            }

            ConsoleOutput.print("First name " + firstName, " last name" + lastName);
        }

        return new GenericPair<>(firstName,lastName);
    }
}
