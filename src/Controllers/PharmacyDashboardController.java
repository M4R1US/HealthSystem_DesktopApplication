package Controllers;

import Actions.StageAction;
import Actions.WindowLauncher;
import AppUsers.Pharmacy;
import Classes.Browse;
import Classes.ConsoleOutput;
import Classes.CustomResourceBundle;
import Classes.GenericPair;
import Controllers.InsideControllers.InformationController;
import Controllers.InsideControllers.InsideController;
import NetworkObjects.UserReference;
import SavedVariables.DynamicVariables;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 25/08/2017.</h2>
 * <p>Pharmacy dashboard</p>
 */
public class PharmacyDashboardController extends AbstractDashboardController<Pharmacy> {

    @FXML
    public BorderPane RootPane,DisablePane,ConnectionErrorPane;
    public ImageView SettingsIcon,DownArrow;
    public HBox HomeHBox,MinimizeHBox,ActionHBox,MaximizeHBox,LogoutHBox,UserTitleHBox,ExitHBox,SettingsButtonHBox;
    public Label DashboardTitleUserLabel,MedicineLookupLabel,MarkPrescriptionLabel,PrescriptionArchiveLabel,DashboardTitleLabel,MainPageLabel,SourceLabel,ReconnectionTimerLabel;
    public GridPane TopGridPane,ActionGridPane;

    private Region middlePane = null;
    public StageAction stageAction;
    public InsideController insideController;
    private FXMLLoader fxmlLoader;
    private static double width,height;
    private Stage mainStage;
    private CustomResourceBundle customResourceBundle;
    private Pharmacy currentUser;
    private boolean available = true;


    /**
     *  {@inheritDoc}
     */
    @Override
    public void postInitialize(Pharmacy pharmacy)
    {
        currentUser = pharmacy;
        UserReference userReference = new UserReference(pharmacy.getLogin(),pharmacy.getName(),"Pharmacy",(byte)1);
        super.connectToMainServer(mainStage,userReference);
        DashboardTitleUserLabel.setText(pharmacy.getName());
        addSubPane("FXML/InnerFxml/InformationWindowFXML.fxml");
        MaximizeHBox.setOnMouseClicked(event -> Platform.runLater(() -> stageAction.maximize(RootPane)));
        MinimizeHBox.setOnMouseClicked(event -> stageAction.minimize(RootPane));
        LogoutHBox.setOnMouseClicked(event -> super.logout(mainStage, RootPane));
        ExitHBox.setOnMouseClicked(event -> super.safeExit(RootPane));

    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        stageAction = new StageAction();
        width = RootPane.getWidth();
        height = RootPane.getHeight();
        customResourceBundle = (CustomResourceBundle)resources;
        mainStage = (Stage)customResourceBundle.getObject("Stage");
        currentUser = (Pharmacy) resources.getObject("User");
        DynamicVariables.screenSensor = this;

        postInitialize(currentUser);
        setEvents();

    }

    /**
     *  {@inheritDoc}
     */
    public void setEvents()
    {

        RootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            width = newValue.doubleValue();
            UserTitleHBox.setMinWidth(newValue.doubleValue()-50-75-150-200);
            DashboardTitleLabel.setPrefWidth(newValue.doubleValue()-150-100-125);
            RootPane.requestFocus();
            if(insideController != null)
                insideController.setWidth(newValue.doubleValue());
        });

        RootPane.heightProperty().addListener(observable -> {
            height = RootPane.getHeight();
            if(insideController != null)
                insideController.setHeight(height);
        });


        SettingsButtonHBox.setOnMouseClicked(event ->{
            mainStage.setFullScreen(false);
            WindowLauncher.launchSettingsWindow(currentUser);
        });

        HomeHBox.setOnMouseClicked(event -> {
            if(insideController != null && !(insideController instanceof InformationController))
                addSubPane("FXML/InnerFxml/InformationWindowFXML.fxml");
        });



        mainStage.setOnCloseRequest(event -> {
            event.consume();
            super.safeExit(RootPane);
        });


        mainStage.setOnHidden(event -> super.disconnectFromMainServer());


        MainPageLabel.setOnMouseClicked(event -> Browse.openMainPage());
        SourceLabel.setOnMouseClicked(event -> Browse.openGithub());

        DownArrow.setOnMouseClicked(event -> swapPanes(false));
        ActionHBox.setOnMouseClicked(event -> swapPanes(true));

        PrescriptionArchiveLabel.setOnMouseClicked(event -> {
            swapPanes(false);
            addSubPane("FXML/InnerFxml/PrescriptionArchiveFXML.fxml");
        });

        mainStage.addEventHandler(KeyEvent.KEY_PRESSED,super.actionPaneHotKeyHandler(ActionGridPane,RootPane));

        MarkPrescriptionLabel.setOnMouseClicked(event -> {
            swapPanes(false);
            addSubPane("FXML/InnerFxml/PharmacyPrescriptionInterruptFXML.fxml");


        });

        MedicineLookupLabel.setOnMouseClicked(event -> {
            mainStage.setFullScreen(false);
            WindowLauncher.launchMedicineInformationWindow(null);
        });

    }


    /**
     *  {@inheritDoc}
     */
    @Override
    public Consumer<Boolean> lockFunction()
    {
        return (Boolean locked) -> {
            SettingsIcon.setDisable(locked);
        };
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void swapPanes(boolean actionPane)
    {
        ActionGridPane.setVisible(actionPane);
        RootPane.setVisible(!actionPane);
    }


    /**
     *  {@inheritDoc}
     */
    @Override
    public void addSubPane(String fxmlSource)
    {
        GenericPair<Pharmacy,String> userReference = new GenericPair<>(currentUser,"User");

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
        RootPane.setCenter(middlePane);
        insideController = fxmlLoader.getController();
        insideController.setParentLockFunction(lockFunction());
        insideController.setUserIdentity(currentUser.getName());
        RootPane.requestFocus();

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
        RootPane.setVisible(false);
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
                RootPane.setVisible(true);
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
        RootPane.setVisible(false);
        DisablePane.setVisible(false);
        ConnectionErrorPane.setVisible(true);

    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void reconnectionTimer(int seconds) {Platform.runLater(() ->ReconnectionTimerLabel.setText("" + seconds + " seconds"));}
}
