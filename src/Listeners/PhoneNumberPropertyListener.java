package Listeners;

import SavedVariables.FinalConstants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Border;


/**
 * <h2>Created by Marius Baltramaitis on 30-Jan-17</h2>
 *
 * <p>Special listener for phone number input validation</p>
 */
public class PhoneNumberPropertyListener extends AbstractTextAnalyzer implements ChangeListener<String> {

    private TextInputControl inputControl;
    private final String INTEGERS = "0123456789";
    private int maxLength,minLength;


    /**
     * Constructor taking necessary parameters
     * @param inputControl TextInputControl where text should be taken from
     * @param minLength min length of number
     * @param maxLength max length of number
     */
    public PhoneNumberPropertyListener(TextInputControl inputControl, int minLength, int maxLength)
    {
        this.inputControl = inputControl;
        this.maxLength = maxLength;
        this.minLength = minLength;
    }


    /**
     * Overriding method to validate text
     * @param observable observable itself
     * @param oldValue old text value of inputControl
     * @param newValue current text value of inputControl
     */
    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        if(newValue.length() > maxLength)
        {
            java.awt.Toolkit.getDefaultToolkit().beep();
            inputControl.setText(oldValue);
            return;
        }

        if(super.containsIllegalCharacters(newValue,INTEGERS))
        {
            java.awt.Toolkit.getDefaultToolkit().beep();
            inputControl.setText(oldValue);
            return;
        }


        Border validationBorder = (newValue.length() < minLength) ? FinalConstants.RED_TEXTFIELD_BORDER : FinalConstants.GREEN_TEXTFIELD_BORDER;
        inputControl.setBorder(validationBorder);

    }

}
