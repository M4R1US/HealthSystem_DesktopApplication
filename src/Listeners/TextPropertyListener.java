package Listeners;

import com.sun.istack.internal.NotNull;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;

import java.util.function.Function;


/**
 * <h2>Created by Marius Baltramaitis on 05-Feb-17</h2>
 * <p>Listener for text input depending of input control object type</p>
 */
public class TextPropertyListener extends AbstractTextAnalyzer implements ChangeListener<String>{

    public String formattedString = "";
    private TextInputControl inputField;
    private Function<String,Boolean> validationFunction;

    /**
     * Constructor taking necessary parameters
     * @param inputField textInputObject where text should be taken from
     * @param validationFunction validation function
     */
    public TextPropertyListener(@NotNull TextInputControl inputField, @NotNull Function<String,Boolean> validationFunction)
    {
        this.inputField = inputField;
        this.validationFunction = validationFunction;
    }

    /**
     *  Overriding method to validate input
     * @param observable observable itself
     * @param oldValue old String value
     * @param newValue current String value
     */
    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        if(newValue.isEmpty() || newValue == null)
            return;

        inputField.setBorder(super.borderStyle(validationFunction.apply(newValue),inputField));
        formattedString = newValue.substring(0,1).toUpperCase() + newValue.substring(1);
        inputField.setText(formattedString);
    }

}
