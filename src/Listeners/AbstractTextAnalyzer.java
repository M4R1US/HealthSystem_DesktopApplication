package Listeners;

import SavedVariables.FinalConstants;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Border;

/**
 * <h2> Created by Marius Baltramaitis on 02/06/2017.</h2>
 * <p>
 *     Abstract class for custom text listeners with general methods mainly for text validation purposes.
 * </p>
 */
public abstract class AbstractTextAnalyzer {

    /**
     * Iteration throw text
     * @param text Text to read
     * @param legalInputCharacters String with all possible legal letters/Symbols
     * @return false if text contains characters that are not inside legalInputCharacters, true otherwise
     */
    protected boolean containsIllegalCharacters(String text, String legalInputCharacters)
    {
        for(char x : text.toCharArray())
        {
            if(!legalInputCharacters.contains(x+""))
                return true;
        }

        return false;
    }

    /**
     * Picks the right border for textInputControl depending on given condition and type of textInputControl
     * @param condition condition for choosing border
     * @param inputField filed where border will be placed around
     * @return null if inputControl in not supported, otherwise - border depending on condition
     */
    protected Border borderStyle(boolean condition, TextInputControl inputField)
    {
        if(inputField instanceof TextField && condition)
            return FinalConstants.GREEN_TEXTFIELD_BORDER;
        if(inputField instanceof TextField && !condition)
            return FinalConstants.RED_TEXTFIELD_BORDER;
        if(inputField instanceof TextArea && condition)
            return FinalConstants.GREEN_TEXTAREA_BORDER;
        if(inputField instanceof TextArea && !condition)
            return FinalConstants.RED_TEXTAREA_BORDER;

        return null;

    }

}
