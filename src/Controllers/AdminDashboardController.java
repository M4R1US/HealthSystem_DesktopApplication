package Controllers;

import Actions.StageAction;
import Actions.WindowLauncher;
import AppUsers.Administrator;
import Classes.Browse;
import Classes.ConsoleOutput;
import Classes.CustomResourceBundle;
import Classes.GenericPair;
import Controllers.InsideControllers.InformationController;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import Controllers.InsideControllers.InsideController;
import NetworkObjects.ImageObject;
import NetworkObjects.UserReference;
import NetworkThreads.RequestImage;
import NetworkThreads.SubmitImage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;


import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 18-Dec-16.</h2>
 * <p>Administration dashboard</p>
 */
public class AdminDashboardController extends AbstractDashboardController<Administrator> {

    @FXML
    public BorderPane AdminDashboardRootPane,DisablePane,ConnectionErrorPane;
    public ImageView SettingsIcon,DownArrow;
    public HBox HomeHBox, ActionHBox, MinimizeHBox,MaximizeHBox,LogoutHBox,UserTitleHBox,ExitHBox,SettingsButtonHBox;
    public Circle ProfileImageCircle;
    public StackPane BaseStackPane;
    public Label BrowsePatientLabel,BrowseMedicineLabel,BrowsePharmacyLabel,InsertDoctorLabel,InsertPharmacyLabel,InsertPatientLabel,InsertMedicineLabel,DashboardTitleUserLabel,DashboardTitleLabel,MainPageLabel,SourceLabel,ReconnectionTimerLabel,BrowseDoctorLabel;
    public GridPane TopGridPane;
    public Label SelectLabel;
    public GridPane ActionGridPane;

    private Region middlePane = null;
    public StageAction stageAction;
    public InsideController insideController;
    private FXMLLoader fxmlLoader;
    private static double width,height;
    private Stage mainStage;
    private CustomResourceBundle customResourceBundle;
    private ImageObject userProfilePictureObject;
    private ExecutorService imageRequestService, imageDeliverService;
    private RequestImage requestService;
    private Administrator currentUser;
    private boolean available = true;

    /**
     *  {@inheritDoc}
     */
    @Override
    public void postInitialize(Administrator administrator)
    {
        currentUser = administrator;
        String avatarPath = (administrator.getSex().equals("Male") ? FinalConstants.DEFAULT_MALE_IMG_SRC : FinalConstants.DEFAULT_FEMALE_IMG_SRC);
        UserReference userReference = new UserReference(administrator.getLogin(),administrator.getFirstName() + " " + administrator.getLastName(),"Admin",(byte)1);
        super.connectToMainServer(mainStage,userReference);
        ProfileImageCircle.setFill(new ImagePattern(new Image(avatarPath)));
        userProfilePictureObject = new ImageObject(administrator.getID(),"Admin",currentUser.getFirstName() + " " + currentUser.getLastName().charAt(0),"asking for profile picture");
        requestService = new RequestImage(userProfilePictureObject,updateImageFunction());
        DashboardTitleUserLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        imageRequestService = Executors.newWorkStealingPool();
        imageRequestService.execute(requestService);
        addSubPane("FXML/InnerFxml/InformationWindowFXML.fxml");


    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        stageAction = new StageAction();
        width = AdminDashboardRootPane.getWidth();
        height = AdminDashboardRootPane.getHeight();
        customResourceBundle = (CustomResourceBundle)resources;
        mainStage = (Stage)customResourceBundle.getObject("Stage");
        currentUser = (Administrator)resources.getObject("User");
        DynamicVariables.screenSensor = this;

        postInitialize(currentUser);
        setEvents();

    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setEvents()
    {

        AdminDashboardRootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            width = newValue.doubleValue();
            UserTitleHBox.setMinWidth(newValue.doubleValue()-50-75-150-200);
            DashboardTitleLabel.setPrefWidth(newValue.doubleValue()-150-100-125);
            AdminDashboardRootPane.requestFocus();
            if(insideController != null)
                insideController.setWidth(newValue.doubleValue());
        });

        AdminDashboardRootPane.heightProperty().addListener(observable -> {
            height = AdminDashboardRootPane.getHeight();
            if(insideController != null)
                insideController.setHeight(height);
        });


        SettingsButtonHBox.setOnMouseClicked(event ->{
            mainStage.setFullScreen(false);
            WindowLauncher.launchSettingsWindow(currentUser);
        });


        ProfileImageCircle.setOnMouseClicked(event -> super.launchImageClipBoard(uploadImageFunction()));


        mainStage.setOnCloseRequest(event -> {
            event.consume();
            super.safeExit(AdminDashboardRootPane);
        });

        ProfileImageCircle.fillProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null)
            {
                String avatarPath = (currentUser.getSex().equals("Male") ? FinalConstants.DEFAULT_MALE_IMG_SRC : FinalConstants.DEFAULT_FEMALE_IMG_SRC);
                ProfileImageCircle.setFill(new ImagePattern(new Image(avatarPath)));

            }
        });

        mainStage.setOnHidden(event -> super.disconnectFromMainServer());

        HomeHBox.setOnMouseClicked(event -> {
            if(insideController != null && !(insideController instanceof InformationController))
                addSubPane("FXML/InnerFxml/InformationWindowFXML.fxml");
        });


        MainPageLabel.setOnMouseClicked(event -> Browse.openMainPage());
        SourceLabel.setOnMouseClicked(event -> Browse.openGithub());

        DownArrow.setOnMouseClicked(event -> swapPanes(false));
        ActionHBox.setOnMouseClicked(event -> swapPanes(true));

        MaximizeHBox.setOnMouseClicked(event -> Platform.runLater(() -> stageAction.maximize(AdminDashboardRootPane)));
        MinimizeHBox.setOnMouseClicked(event -> stageAction.minimize(AdminDashboardRootPane));
        LogoutHBox.setOnMouseClicked(event -> super.logout(mainStage,AdminDashboardRootPane));
        ExitHBox.setOnMouseClicked(event -> super.safeExit(AdminDashboardRootPane));

        mainStage.addEventHandler(KeyEvent.KEY_PRESSED,super.actionPaneHotKeyHandler(ActionGridPane,AdminDashboardRootPane));

        InsertDoctorLabel.setOnMouseClicked(event -> {
            swapPanes(false);
            addSubPane("FXML/InnerFxml/InsertDoctorFXML.fxml");
        });

        InsertPatientLabel.setOnMouseClicked(event -> {
            swapPanes(false);
            addSubPane("FXML/InnerFxml/InsertPatientFXML.fxml");
        });

        InsertPharmacyLabel.setOnMouseClicked(event -> {
            swapPanes(false);
            addSubPane("FXML/InnerFxml/InsertPharmacyFXML.fxml");
        });

        InsertMedicineLabel.setOnMouseClicked(event -> {
            swapPanes(false);
            addSubPane("FXML/InnerFxml/InsertMedicineFXML.fxml");
        });

        BrowseDoctorLabel.setOnMouseClicked(event -> {
            swapPanes(false);
            addSubPane("FXML/InnerFxml/BrowseDoctorFXML.fxml");
        });


        BrowseMedicineLabel.setOnMouseClicked(event -> {
            swapPanes(false);
            addSubPane("FXML/InnerFxml/BrowseMedicineFXML.fxml");
        });

        BrowsePatientLabel.setOnMouseClicked(event -> {
            swapPanes(false);
            addSubPane("FXML/InnerFxml/BrowsePatientFXML.fxml");
        });

        BrowsePharmacyLabel.setOnMouseClicked(event -> {
            swapPanes(false);
            addSubPane("FXML/InnerFxml/BrowsePharmacyFXML.fxml");
        });

    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void swapPanes(boolean actionPane)
    {
        ActionGridPane.setVisible(actionPane);
        AdminDashboardRootPane.setVisible(!actionPane);
    }


    /**
     *  {@inheritDoc}
     */
    @Override
    public Consumer<Boolean> lockFunction()
    {
        return (Boolean locked) -> {
                ProfileImageCircle.setDisable(locked);
                SettingsIcon.setDisable(locked);
        };
    }

    /**
     * Server request to update users profile picture
     * @return functional interface with instructions how image should be uploaded to server
     */
    private Consumer<Image> uploadImageFunction()
    {
        String messageToServer = currentUser.getSex().equalsIgnoreCase("male") ? "updating his profile picture" : "updating hers profile picture";
        return (Image capturedImage) -> {
            if(capturedImage != null)
            {
                ProfileImageCircle.setFill(new ImagePattern(capturedImage));
                userProfilePictureObject.setMessageToServer(messageToServer);
                SubmitImage uploadToServer = new SubmitImage(userProfilePictureObject,capturedImage);
                imageDeliverService = Executors.newWorkStealingPool();
                imageDeliverService.execute(uploadToServer);
            }
        };

    }

    /**
     * @return functional interface with instructions how to change user image in this dashboard
     */
    private Consumer<Image> updateImageFunction()
    {
        return (Image capturedImage) -> {
            if(capturedImage != null)
                ProfileImageCircle.setFill(new ImagePattern(capturedImage));

            else
                ProfileImageCircle.setStroke(null);
        };
    }




    /**
     *  {@inheritDoc}
     */
    @Override
    public void addSubPane(String fxmlSource)
    {
        GenericPair<Administrator,String> userReference = new GenericPair<>(currentUser,"User");

        ConsoleOutput.print("Adding test sub pane!");
        CustomResourceBundle customResourceBundle = new CustomResourceBundle(new GenericPair<>(mainStage,"Stage"),userReference);
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlSource),customResourceBundle);
        try
        {
            middlePane = fxmlLoader.load();
        } catch (Exception e)
        {
            ConsoleOutput.print(this.getClass().getName(),"Error on loading fxml: "+ e.getMessage());
            ConsoleOutput.print("Cause : " + e.getCause());
            e.printStackTrace();
        }
        AdminDashboardRootPane.setCenter(middlePane);
        insideController = fxmlLoader.getController();
        insideController.setParentLockFunction(lockFunction());
        insideController.setUserIdentity(currentUser.getFirstName() +  "," + currentUser.getLastName());
        AdminDashboardRootPane.requestFocus();

    }


    /**
     *  {@inheritDoc}
     */
    public double getWidth() { return width;}
    /**
     *  {@inheritDoc}
     */
    public double getHeight() { return height;}


    /**
     *  {@inheritDoc}
     */
    @Override
    public void denied() {
        AdminDashboardRootPane.setVisible(false);
        DisablePane.setVisible(true);
        available = false;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void connectionStatus(boolean connected)
    {
        if(!connected && available)
            noConnection();
        else
        {
            if(available)
            {
                AdminDashboardRootPane.setVisible(true);
                DisablePane.setVisible(false);
                ConnectionErrorPane.setVisible(false);
            }

        }

    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void noConnection()
    {
        AdminDashboardRootPane.setVisible(false);
        DisablePane.setVisible(false);
        ConnectionErrorPane.setVisible(true);

    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void reconnectionTimer(int seconds) {Platform.runLater(() ->ReconnectionTimerLabel.setText("" + seconds + " seconds"));}

}
