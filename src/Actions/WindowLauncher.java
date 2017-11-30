package Actions;

import AppUsers.*;
import Classes.ConsoleOutput;
import Classes.CustomResourceBundle;
import Classes.GenericPair;
import Classes.Medicine;
import Controllers.*;
import SavedVariables.FinalConstants;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 18-Dec-16.</h2>
 * <p>
 *     Class responsible for launching different windows (Stages).
 * </p>
 */
public final class WindowLauncher {


    /**
     * <p>
     *     Method for opening administration dashboard
     * </p>
     * @param admin administrator object with data inside
     * @see CustomResourceBundle
     * @see GenericPair
     */
    public static void launchAdminDashboard(Administrator admin)
    {
        Stage dashboardWindow = new Stage();
        dashboardWindow.setMinHeight(FinalConstants.DASHBOARD_SCENE_HEIGHT);
        dashboardWindow.setMinWidth(FinalConstants.DASHBOARD_SCENE_WIDTH);
        GenericPair<Administrator,String> adminObject = new GenericPair<>(admin,"User");
        GenericPair<Stage,String> adminDashboardStage = new GenericPair<>(dashboardWindow,"Stage");
        CustomResourceBundle adminResources = new CustomResourceBundle(adminObject,adminDashboardStage);
        Pane dashboardPane = null;
        FXMLLoader fxmlLoader = new FXMLLoader(WindowLauncher.class.getClassLoader().getResource("FXML/AdminDashboardFXML.fxml"),adminResources);
        try
        {
            dashboardPane = fxmlLoader.load();
        } catch (Exception e)
        {
            ConsoleOutput.print(WindowLauncher.class.getClass().getName(),"Error on loading fxml: "+ e.getMessage());
            ConsoleOutput.print("Cause : " + e.getCause());
        }

        Scene dashboardScene = new Scene(dashboardPane,FinalConstants.DASHBOARD_SCENE_WIDTH,FinalConstants.DASHBOARD_SCENE_HEIGHT);
        dashboardWindow.setScene(dashboardScene);
        dashboardWindow.initStyle(dashboardStyleDependingOnOS());
        setDockParameters("Administrator");
        dashboardWindow.getIcons().add(new Image(FinalConstants.APP_ICON_SRC));
        ConsoleOutput.print(WindowLauncher.class.getClass().getName(),"Attempting to launch Admin Dashboard");
        AdminDashboardController adminDashboardController = fxmlLoader.getController();
        fxmlLoader.setController(adminDashboardController);
        dashboardWindow.setTitle("Health System");
        dashboardWindow.show();

    }

    /**
     * <p>
     *     Method for opening administration dashboard
     * </p>
     * @param doctor administrator object with data inside
     * @see CustomResourceBundle
     * @see GenericPair
     */
    public static void launchDoctorDashboard(Doctor doctor)
    {
        Stage dashboardWindow = new Stage();
        dashboardWindow.setMinHeight(FinalConstants.DASHBOARD_SCENE_HEIGHT);
        dashboardWindow.setMinWidth(FinalConstants.DASHBOARD_SCENE_WIDTH);
        GenericPair<Doctor,String> doctorObject = new GenericPair<>(doctor,"User");
        GenericPair<Stage,String> doctorDashboardStage = new GenericPair<>(dashboardWindow,"Stage");
        CustomResourceBundle doctorResources = new CustomResourceBundle(doctorObject,doctorDashboardStage);
        Pane dashboardPane = null;
        FXMLLoader fxmlLoader = new FXMLLoader(WindowLauncher.class.getClassLoader().getResource("FXML/DoctorDashboardFXML.fxml"),doctorResources);
        try
        {
            dashboardPane = fxmlLoader.load();
        } catch (Exception e)
        {
            ConsoleOutput.print(WindowLauncher.class.getClass().getName(),"Error on loading fxml: "+ e.getMessage());
            ConsoleOutput.print(WindowLauncher.class.getName(), e.getCause());
        }

        Scene dashboardScene = new Scene(dashboardPane,FinalConstants.DASHBOARD_SCENE_WIDTH,FinalConstants.DASHBOARD_SCENE_HEIGHT);
        dashboardWindow.setScene(dashboardScene);
        dashboardWindow.initStyle(dashboardStyleDependingOnOS());
        setDockParameters("Doctor dashboard");
        dashboardWindow.getIcons().add(new Image(FinalConstants.APP_ICON_SRC));
        ConsoleOutput.print(WindowLauncher.class.getClass().getName(),"Attempting to launch doctor dashboard");
        DoctorDashboardController doctorDashboardController = fxmlLoader.getController();
        fxmlLoader.setController(doctorDashboardController);
        dashboardWindow.setTitle("Health System");
        dashboardWindow.show();
    }

    /**
     *  Sets image and dock text for macOS users
     * @param DockIconBadge text over image inside dock
     */
    private static void setDockParameters(String DockIconBadge)
    {
        DeviceProperties.detectOS();
        DeviceProperties.OSType osType = DeviceProperties.detectedOS;

        if(osType == DeviceProperties.OSType.MacOS)
        {
            try {

                BufferedImage image= ImageIO.read(WindowLauncher.class.getClassLoader().getResourceAsStream("Icons/x64/dock_img.png"));
                com.apple.eawt.Application.getApplication().setDockIconImage(image);
                com.apple.eawt.Application.getApplication().setDockIconBadge(DockIconBadge);

            } catch (Exception e)
            {
                ConsoleOutput.print(WindowLauncher.class.getClass().getName(),e);
            }
        }
    }


    /**
     * Method for picking right stage style depending of OS
     * @return StageStyle depending on OS
     */
    private static StageStyle dashboardStyleDependingOnOS()
    {
        DeviceProperties.detectOS();
        DeviceProperties.OSType osType = DeviceProperties.detectedOS;
        switch (osType) {
            
            case Windows:
                return StageStyle.DECORATED;

            case MacOS:
                return StageStyle.UNIFIED;

            case Linux:
                return StageStyle.UNIFIED;

            case Other:
                return StageStyle.UNIFIED;

            default:
                return StageStyle.UNIFIED;
        }

    }


    /**
     * Getter of confirmation window controller and stage (window)
     * @return Generic pair of Stage and ConfirmationController
     * @see GenericPair
     * @see ConfirmationWindowController
     */
    public static GenericPair<Stage, ConfirmationWindowController> getConfirmationWindow()
    {
        ConsoleOutput.print("Attempting to launching FXML/ConfirmationWindowFXML.fxml");
        BorderPane confirmationPane = null;
        FXMLLoader fxmlLoader = null;
        Stage infoStage = new Stage();
        try
        {
            fxmlLoader = new FXMLLoader(WindowLauncher.class.getClassLoader().getResource("FXML/ConfirmationWindowFXML.fxml"));
            confirmationPane = fxmlLoader.load();
        } catch (IOException e) { ConsoleOutput.print("Error on launching FXML/ConfirmationWindowFXML.fxml",e);}

        Scene infoScene = new Scene(confirmationPane,FinalConstants.CONFIRMATION_WINDOW_WIDTH,FinalConstants.CONFIRMATION_WINDOW_HEIGHT);
        infoStage.setScene(infoScene);
        infoStage.setResizable(false);
        infoStage.setAlwaysOnTop(true);
        infoStage.initStyle(StageStyle.UNDECORATED);
        ConfirmationWindowController confirmationWindowController = fxmlLoader.getController();

        return new GenericPair(infoStage, confirmationWindowController);
    }

    /**
     * Getter of imageClipBoard window controller and stage (window)
     * @return Generic pair with Stage And ImageClipBoardController
     * @see GenericPair
     * @see ImageClipBoardController
     */
    public static GenericPair<Stage, ImageClipBoardController> getImageClipBoardWindow()
    {
        ConsoleOutput.print("Attempting to launch FXML/ImageClipBoardFXML.fxml");
        BorderPane clipboardPane = null;
        FXMLLoader fxmlLoader = null;
        Stage clipboardStage = new Stage();
        clipboardStage.setMaxHeight(FinalConstants.IMAGE_CLIPBOARD_HEIGHT);
        clipboardStage.setMaxWidth(FinalConstants.IMAGE_CLIPBOARD_WIDTH);
        clipboardStage.setMinHeight(FinalConstants.IMAGE_CLIPBOARD_HEIGHT);
        clipboardStage.setMinWidth(FinalConstants.IMAGE_CLIPBOARD_WIDTH);
        GenericPair<Stage,String> windowPair = new GenericPair<>(clipboardStage,"Stage");
        CustomResourceBundle resources = new CustomResourceBundle(windowPair);

        try
        {
            fxmlLoader = new FXMLLoader(WindowLauncher.class.getClassLoader().getResource("FXML/ImageClipBoardFXML.fxml"),resources);
            clipboardPane = fxmlLoader.load();
        } catch (IOException e) { ConsoleOutput.print(WindowLauncher.class.getName(),e); }

        Scene clipBoardScene = new Scene(clipboardPane,FinalConstants.IMAGE_CLIPBOARD_WIDTH,FinalConstants.IMAGE_CLIPBOARD_HEIGHT);
        clipboardStage.setScene(clipBoardScene);
        clipboardStage.setResizable(false);
        clipboardStage.initStyle(StageStyle.UNDECORATED);
        clipboardStage.getScene().setFill(null);
        clipboardStage.centerOnScreen();
        ImageClipBoardController imageClipBoardController = fxmlLoader.getController();

        return new GenericPair(clipboardStage,imageClipBoardController);
    }

    /**
     * Opens fxml file for lobbyController and launches Stage (Window)
     */
    public static void launchLobby()
    {
        Parent root = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        Stage lobbyStage = new Stage();
        Scene lobbyScene;
        CustomResourceBundle resourceBundle = new CustomResourceBundle(new GenericPair(lobbyStage,"Stage"));
        try {
            root = fxmlLoader.load(WindowLauncher.class.getClassLoader().getResource("FXML/LobbyWindowXML.fxml"),resourceBundle);

        } catch (Exception e)
        {
            ConsoleOutput.print(WindowLauncher.class.toString(),e.getMessage());
        }
        lobbyScene = new Scene(root,FinalConstants.LOBBY_SCENE_WIDTH,FinalConstants.LOBBY_SCENE_HEIGHT);
        lobbyStage.setScene(lobbyScene);
        lobbyStage.setTitle("Health System");
        lobbyStage.initStyle(StageStyle.UTILITY);
        lobbyStage.setResizable(false);
        lobbyStage.getScene().setFill(null);
        lobbyStage.getIcons().add(new Image("Icons/x32/heartbeat.png"));
        setDockParameters("Login now");
        lobbyStage.show();
        DeviceProperties.detectOS();
    }

    /**
     * Method to launch settings window controller
     * @param user application user object with data that will be sent to controller
     */
    public static void launchSettingsWindow(AbstractApplicationUser user)
    {
        Parent root = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        Stage settingsStage = new Stage();
        Scene settingsScene;
        CustomResourceBundle resourceBundle = new CustomResourceBundle(new GenericPair(user,"User"));
        try {
            root = fxmlLoader.load(WindowLauncher.class.getClassLoader().getResource("FXML/SettingsControllerFXML.fxml"),resourceBundle);

        } catch (Exception e)
        {
            ConsoleOutput.print(WindowLauncher.class.toString(),e.getMessage());
        }
        settingsScene = new Scene(root,FinalConstants.SETTINGS_SCENE_WIDTH,FinalConstants.SETTINGS_SCENE_HEIGHT);
        settingsStage.setScene(settingsScene);
        settingsStage.setTitle("Health-System.eu account settings");
        settingsStage.getIcons().add(new Image("Icons/x32/heartbeat.png"));
        settingsStage.setResizable(false);
        settingsStage.initStyle(StageStyle.UTILITY);
        settingsStage.show();
    }

    /**
     * <p>
     *     Method for opening patient dashboard
     * </p>
     * @param patient patient object with data inside
     * @see CustomResourceBundle
     * @see GenericPair
     */
    public static void launchPatientDashboard(Patient patient)
    {
        Stage dashboardWindow = new Stage();
        dashboardWindow.setMinHeight(FinalConstants.DASHBOARD_SCENE_HEIGHT);
        dashboardWindow.setMinWidth(FinalConstants.DASHBOARD_SCENE_WIDTH);
        GenericPair<Patient,String> doctorObject = new GenericPair<>(patient,"User");
        GenericPair<Stage,String> doctorDashboardStage = new GenericPair<>(dashboardWindow,"Stage");
        CustomResourceBundle patientResources = new CustomResourceBundle(doctorObject,doctorDashboardStage);
        Pane dashboardPane = null;
        FXMLLoader fxmlLoader = new FXMLLoader(WindowLauncher.class.getClassLoader().getResource("FXML/PatientDashboardFXML.fxml"),patientResources);
        try
        {
            dashboardPane = fxmlLoader.load();
        } catch (Exception e)
        {
            ConsoleOutput.print(WindowLauncher.class.getClass().getName(),"Error on loading fxml: "+ e.getMessage());
            ConsoleOutput.print("Cause : " + e.getCause());
        }

        Scene dashboardScene = new Scene(dashboardPane,FinalConstants.DASHBOARD_SCENE_WIDTH,FinalConstants.DASHBOARD_SCENE_HEIGHT);
        dashboardWindow.setScene(dashboardScene);
        dashboardWindow.initStyle(dashboardStyleDependingOnOS());
        setDockParameters("Patient dashboard");
        dashboardWindow.getIcons().add(new Image(FinalConstants.APP_ICON_SRC));
        ConsoleOutput.print(WindowLauncher.class.getClass().getName(),"Attempting to launch patient dashboard");
        PatientDashboardController patientDashboardController = fxmlLoader.getController();
        fxmlLoader.setController(patientDashboardController);
        dashboardWindow.setTitle("Health System");
        dashboardWindow.show();
    }

    /**
     * Method to launch edit window
     * @param abstractApplicationUser ApplicationUser object with data to be sent to edit window controller
     * @param onEditFunction function to be executed once user object is changed and data is uploaded to database
     */
    public static void launchEditWindow(AbstractApplicationUser abstractApplicationUser, Consumer<AbstractApplicationUser> onEditFunction)
    {
        Parent root = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        Stage settingsStage = new Stage();
        Scene settingsScene;
        CustomResourceBundle resourceBundle = new CustomResourceBundle(new GenericPair(abstractApplicationUser,"User"),new GenericPair<>(onEditFunction,"editFunction"));
        try {
            root = fxmlLoader.load(WindowLauncher.class.getClassLoader().getResource("FXML/EditDetailsController.fxml"),resourceBundle);

        } catch (Exception e)
        {
            ConsoleOutput.print(WindowLauncher.class.toString(),e.getMessage());
        }
        settingsScene = new Scene(root,FinalConstants.SETTINGS_SCENE_WIDTH,FinalConstants.SETTINGS_SCENE_HEIGHT);;
        settingsStage.setScene(settingsScene);
        settingsStage.setTitle("Health-System.eu user settings");
        settingsStage.getIcons().add(new Image("Icons/x32/heartbeat.png"));
        settingsStage.setResizable(false);
        settingsStage.setFullScreen(false);
        settingsStage.initStyle(StageStyle.UTILITY);
        settingsStage.show();
    }

    /**
     * Method to launch medicine information window
     * @param medicine medicine object with data to be sent to medicineInformation controller
     */
    public static void launchMedicineInformationWindow(Medicine medicine)
    {
        Parent root = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        Stage settingsStage = new Stage();
        Scene settingsScene;
        try {
            if(medicine != null)
                root = fxmlLoader.load(WindowLauncher.class.getClassLoader().getResource("FXML/MedicineInformationFXML.fxml"),new CustomResourceBundle(new GenericPair(medicine,"Medicine")));
            else
                root = fxmlLoader.load(WindowLauncher.class.getClassLoader().getResource("FXML/MedicineInformationFXML.fxml"));

        } catch (Exception e)
        {
            ConsoleOutput.print(WindowLauncher.class.toString(),e.getMessage());
        }
        settingsScene = new Scene(root,FinalConstants.MEDICINE_WINDOW_WIDTH,FinalConstants.MEDICINE_WINDOW_HEIGHT);
        settingsStage.setScene(settingsScene);
        settingsStage.setTitle("Medicine lookup");
        settingsStage.getIcons().add(new Image("Icons/x32/drug_white.png"));
        settingsStage.setResizable(false);
        settingsStage.setFullScreen(false);
        settingsStage.initStyle(StageStyle.UTILITY);
        settingsStage.show();
    }

    /**
     * Method to launch prescription archive window
     * @param user application user object with data to be sent to controller
     */
    public static void launchPrescriptionArchiveWindow(AbstractApplicationUser user)
    {
        Parent root = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        Stage settingsStage = new Stage();
        Scene settingsScene;
        try {
            root = fxmlLoader.load(WindowLauncher.class.getClassLoader().getResource("FXML/AdminPrescriptionLookupFXML.fxml"),new CustomResourceBundle(new GenericPair(user,"User")));

        } catch (Exception e)
        {
            ConsoleOutput.print(WindowLauncher.class.toString(),e.getMessage());
        }
        settingsScene = new Scene(root,FinalConstants.PRESCRIPTION_ARCHIVE_WINDOW_WIDTH,FinalConstants.PRESCRIPTION_ARCHIVE_WINDOW_HEIGHT);
        settingsStage.setScene(settingsScene);
        settingsStage.setTitle("Prescription lookup");
        settingsStage.getIcons().add(new Image("Icons/x64/prescription_marked.png"));
        settingsStage.setMinWidth(FinalConstants.PRESCRIPTION_ARCHIVE_WINDOW_WIDTH);
        settingsStage.setMinHeight(FinalConstants.PRESCRIPTION_ARCHIVE_WINDOW_HEIGHT);
        settingsStage.setFullScreen(false);
        settingsStage.show();
    }

    /**
     * <p>
     *     Method for opening pharmacy dashboard
     * </p>
     * @param pharmacy pharmacy object with data inside
     * @see CustomResourceBundle
     * @see GenericPair
     */
    public static void launchPharmacyDashboard(Pharmacy pharmacy)
    {
        Stage dashboardWindow = new Stage();
        dashboardWindow.setMinHeight(FinalConstants.DASHBOARD_SCENE_HEIGHT);
        dashboardWindow.setMinWidth(FinalConstants.DASHBOARD_SCENE_WIDTH);
        GenericPair<Pharmacy,String> pharmacyObject = new GenericPair<>(pharmacy,"User");
        GenericPair<Stage,String> doctorDashboardStage = new GenericPair<>(dashboardWindow,"Stage");
        CustomResourceBundle pharmacyResources = new CustomResourceBundle(pharmacyObject,doctorDashboardStage);
        Pane dashboardPane = null;
        FXMLLoader fxmlLoader = new FXMLLoader(WindowLauncher.class.getClassLoader().getResource("FXML/PharmacyDashboardFXML.fxml"),pharmacyResources);
        try
        {
            dashboardPane = fxmlLoader.load();
        } catch (Exception e)
        {
            ConsoleOutput.print(WindowLauncher.class.getClass().getName(),"Error on loading fxml: "+ e.getMessage());
            ConsoleOutput.print("Cause : " + e.getCause());
        }

        Scene dashboardScene = new Scene(dashboardPane,FinalConstants.DASHBOARD_SCENE_WIDTH,FinalConstants.DASHBOARD_SCENE_HEIGHT);
        dashboardWindow.setScene(dashboardScene);
        dashboardWindow.initStyle(dashboardStyleDependingOnOS());
        setDockParameters("Pharmacy dashboard");
        dashboardWindow.getIcons().add(new Image(FinalConstants.APP_ICON_SRC));
        ConsoleOutput.print(WindowLauncher.class.getClass().getName(),"Attempting to launch pharmacy dashboard");
        PharmacyDashboardController pharmacyDashboardController = fxmlLoader.getController();
        fxmlLoader.setController(pharmacyDashboardController);
        dashboardWindow.setTitle("Health System");
        dashboardWindow.show();
    }

}
