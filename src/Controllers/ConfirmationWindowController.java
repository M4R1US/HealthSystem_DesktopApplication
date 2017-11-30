package Controllers;

import Interfaces.Executable;
import com.sun.istack.internal.NotNull;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * <h2>Created by Marius Baltramaitis on 07-Mar-17.</h2>
 * <p>Controller used to confirm different actions in application</p>
 */
public class ConfirmationWindowController implements Initializable {

    @FXML
    public BorderPane ConfirmationWindowRootPane;
    public TextFlow InformationTextFlow;
    public Button YesButton,NoButton;

    public Stage thisStage;
    private Executable onAppearFunction,onDisappearFunction;


    /**
     * Initializing events
     * @param location path of fxml file
     * @param resources additional resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        YesButton.setOnMouseEntered(event -> YesButton.setOpacity(0.5));
        YesButton.setOnMouseExited(event -> YesButton.setOpacity(1));
        NoButton.setOnMouseEntered(event -> NoButton.setOpacity(0.5));
        NoButton.setOnMouseExited(event -> NoButton.setOpacity(1));
        YesButton.setCursor(Cursor.HAND);
        NoButton.setCursor(Cursor.HAND);

    }

    /**
     * Method for setting text in the middle
     * @param information text to set in the middle
     */
    public void setText(String information)
    {
        thisStage = (Stage) ConfirmationWindowRootPane.getScene().getWindow();
        if(onAppearFunction != null)
            onAppearFunction.execute();
            Text t = new Text(information + "\n");
            t.setFill(Paint.valueOf("#fff"));
            t.setFont(Font.font(17));
            InformationTextFlow.getChildren().add(t);
    }

    /**
     * @param e function to be executed once yes button is clicked
     */
    public void setOnYesButton(@NotNull Executable e)
    {
        YesButton.setOnMouseClicked(event -> {
           if(onDisappearFunction != null)
               onDisappearFunction.execute();

            e.execute();
        });
    }

    /**
     * @param e function to be executed once no button is clicked
     */
    public void setOnNoButton(@NotNull Executable e)
    {
        NoButton.setOnMouseClicked(event -> {
            if(onDisappearFunction != null)
                onDisappearFunction.execute();

           e.execute();
        });
    }


    /**
     * @param onAppear function to be executed once window appears
     */
    public void setOnAppearFunction(@NotNull Executable onAppear) { onAppearFunction = onAppear; }


    /**
     * @param onDisappear function to be executed once window disappears
     */
    public void setOnDisappearFunction(@NotNull Executable onDisappear) { onDisappearFunction = onDisappear;}

    /**
     * Method for closing this stage
     */
    public void close() { thisStage.close(); }

}
