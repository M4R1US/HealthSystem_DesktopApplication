package Controllers;

import Actions.WindowLauncher;
import AppUsers.AbstractApplicationUser;
import Classes.ConsoleOutput;
import Classes.GenericPair;
import Interfaces.ScreenDimensionPropertySensor;
import NetworkObjects.UserReference;
import NetworkThreads.TCPUserDeviceConnection;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 03/04/2017.</h2>
 * <p>This is abstract class of all dashboard controllers with common methods/implementations<br>
 *     T - Any non abstract sub class of AbstractApplicationUser
 * </p>
 * @see AbstractApplicationUser
 */
public abstract class AbstractDashboardController<T extends AbstractApplicationUser> implements Initializable, ScreenDimensionPropertySensor {

    private TCPUserDeviceConnection tcp_userDeviceConnection;
    private Timeline timeline;

    /**
     * Initialize application user, registering data in dashboard controllers
     * @param t any sub class of AbstractApplicationUser
     * @see AbstractApplicationUser
     */
    abstract void postInitialize(T t);

    /**
     * Special method to set events and attach listeners for fxml nodes
     */
    abstract void setEvents();

    /**
     * Instructions what to do when deny signal from server is received
     */
    abstract void denied();

    /**
     * Lock function to lock user input when database transaction is happening
     * @return functional interface with instructions what to do with controls/nodes when database transaction is happening
     */
    abstract Consumer<Boolean> lockFunction();

    /**
     * Instrictions what to do based on connection
     * @param connected true if connected, false otherwise
     */
    abstract void connectionStatus(boolean connected);

    /**
     * Method to inform user interface when next connection is scheduled
     * @param seconds amount seconds until next connection attempt is scheduled
     */

    abstract void reconnectionTimer(int seconds);

    /**
     * Method for setting sub panes inside center of root pane
     * @param fxmlSource path of pane and its children to insert inside center
     */

    abstract void addSubPane(String fxmlSource);

    /**
     * Method of swapping panes inside dashboards
     * @param actionPane true if new pane is going to be action pane,false otherwise
     */
    abstract void swapPanes(boolean actionPane);

    /**
     * Method with instructions how user interface should be interrupted once connection is missed@
     */
    abstract void noConnection();

    /**
     * Method to launch image clipboard
     * @param imgAction function with instructions what to do with captured image
     * @see WindowLauncher#getImageClipBoardWindow()
     */
    protected void launchImageClipBoard(Consumer<Image> imgAction)
    {
        GenericPair<Stage, ImageClipBoardController> imageClipBoardWindow = WindowLauncher.getImageClipBoardWindow();
        Stage clipboardStage = imageClipBoardWindow.getFirst();
        ImageClipBoardController imageClipBoardController = imageClipBoardWindow.getSecond();
        imageClipBoardController.setOnUploadFunction(imgAction);
        clipboardStage.show();
    }

    /**
     * Connection to server
     * @param stage main window of application
     * @param userReference user referance
     * @see UserReference
     */
    protected void connectToMainServer(Stage stage, UserReference userReference)
    {
        if(stage == null || userReference == null)
            return;

        try
        {
            tcp_userDeviceConnection = new TCPUserDeviceConnection(stage,userReference);
            tcp_userDeviceConnection.start();

            tcp_userDeviceConnection.connectionSensor.addListener((observable, oldValue, newValue) ->  connectionStatus(newValue));

            tcp_userDeviceConnection.reconnectTimeSensor.addListener((observable, oldValue, newValue) -> {
                if(newValue != null)
                reconnectionTimer(newValue);
            });

            tcp_userDeviceConnection.statusProperty.addListener((observable, oldValue, newValue) -> {
                if(!newValue)
                {
                    tcp_userDeviceConnection.disconnect();
                    denied();
                }

            });

        } catch (Exception e)
        {
            ConsoleOutput.print("Couldn't connect to the server. Host is probably down for some reason");
        }

    }


    /**
     * Method used to abort connection with server (usually when logging out or exiting program)
     */
    protected void disconnectFromMainServer()
    {
        if(tcp_userDeviceConnection != null)
            tcp_userDeviceConnection.cancel();

    }


    /**
     * Method for safe exit for all dashboards
     * @param root parent
     */
    protected void safeExit(Parent root)
    {
        GenericPair<Stage, ConfirmationWindowController> confirmationWindowGenericPair = WindowLauncher.getConfirmationWindow();
        Stage confirmStage = confirmationWindowGenericPair.getFirst();
        ConfirmationWindowController informationController = confirmationWindowGenericPair.getSecond();
        informationController.setOnAppearFunction( () -> root.setDisable(true));
        informationController.setOnDisappearFunction( () -> root.setDisable(false));
        informationController.setOnYesButton( () -> {
            disconnectFromMainServer();
            Platform.exit();
        });
        informationController.setOnNoButton( () -> informationController.close());
        informationController.setText("Would you like to exit?");
        confirmStage.show();
    }

    /**
     * Safe logout method for all dashboards
     * @param runningWindow main window
     * @param root root
     */
    protected void logout(Stage runningWindow,Parent root)
    {
        GenericPair<Stage, ConfirmationWindowController> confirmationWindowGenericPair = WindowLauncher.getConfirmationWindow();
        Stage confirmStage = confirmationWindowGenericPair.getFirst();
        ConfirmationWindowController informationController = confirmationWindowGenericPair.getSecond();
        informationController.setOnAppearFunction( () ->  root.setDisable(true));
        informationController.setOnDisappearFunction( () -> root.setDisable(false));
        informationController.setOnYesButton( () -> {
            runningWindow.setFullScreen(false);
            runningWindow.close();
            informationController.close();
            disconnectFromMainServer();
            WindowLauncher.launchLobby();
        });
        informationController.setOnNoButton( () -> informationController.close());
        informationController.setText("Would you like to logout?");
        confirmStage.show();
    }

    /**
     * Opacity contrast effect for given pane
     * @param p pane to apply
     */
    protected void contrastEffect(Pane p)
    {
        p.getChildren().forEach(node -> node.setOpacity(0));
        timeline = new Timeline(new KeyFrame(Duration.millis(50),
                event -> {
                    p.getChildren().forEach(children -> {
                        if(children.getOpacity() < 1)
                        {
                            children.setOpacity(children.getOpacity() + 0.05);
                            return;
                        }
                        timeline.stop();
                    });

                }),
                new KeyFrame(Duration.millis(50)));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Event handler when switching between action pane and base pane
     * @param actionGridPane action grid pane
     * @param basePane base pane
     * @return event with switch pane instructions based on arguments
     */
    protected EventHandler<KeyEvent> actionPaneHotKeyHandler(GridPane actionGridPane,Pane basePane)
    {
        return event -> {

            boolean isOpen = actionGridPane.isVisible();
            if(event.getCode() == KeyCode.CONTROL)
            {
                actionGridPane.setVisible(!isOpen);
                basePane.setVisible(isOpen);
            }
        };
    }
}
