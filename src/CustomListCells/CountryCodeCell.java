package CustomListCells;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;

/**
 * <h2>Created by Marius Baltramaitis on 15-Feb-17.</h2>
 *
 * <p>Special class for country code cells, with flag and country code number</p>
 */
public class CountryCodeCell extends HBox {

    private String iconSrc;
    private String countryCodeDisplayText;
    private ImageView countryFlagImageView;
    private Label countryCodeLabel;
    private String countryCode;

    /**
     * Constructor to make valid cell
     * @param iconSrc icon path of flag image
     * @param countryCodeDisplayText text to display aside image
     * @param countryCode country code itself
     */
    public CountryCodeCell(String iconSrc, String countryCodeDisplayText, String countryCode)
    {
        countryFlagImageView = new ImageView(new Image(iconSrc));
        countryCodeLabel = new Label(countryCodeDisplayText);
        countryCodeLabel.setTextFill(Paint.valueOf("#f8f8f8"));
        countryCodeLabel.setMinWidth(50);
        countryCodeLabel.setAlignment(Pos.CENTER_RIGHT);
        this.countryCode = countryCode;
        this.setMinWidth(70);
        this.getChildren().add(countryFlagImageView);
        this.getChildren().add(countryCodeLabel);
        this.iconSrc = iconSrc;
        this.countryCodeDisplayText = countryCodeDisplayText;
    }



    /**
     * Getter for image path
     * @return image path
     */
    public String getIconSrc()
    {
        return iconSrc;
    }

    /**
     * Getter for country code display text
     * @return display text Ex [+ 47 ]
     */
    public String getCountryCodeDisplayText()
    {
        return countryCodeDisplayText;
    }

    /**
     * Getter for image view of image flag
     * @return imageView object of country image
     */
    public ImageView getCountryFlagImageView()
    {
        return countryFlagImageView;
    }

    /**
     * Getter for country code label
     * @return Label of country code
     */
    public Label getCountryCodeLabel()
    {
        return countryCodeLabel;
    }

    /**
     * Getter for country code to validate phone number
     * @return country code itself
     */
    public String getCountryCode() {return countryCode;}
}
