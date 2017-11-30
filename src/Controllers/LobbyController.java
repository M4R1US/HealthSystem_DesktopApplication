package Controllers;

import Actions.Authentication;
import Animations2D.SynchronizationAnimation;
import AppUsers.*;
import Classes.Browse;
import Enums.ApplicationUser;
import Classes.CustomResourceBundle;
import SavedVariables.FinalConstants;
import Actions.StageAction;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Marius Baltramaitis on 13-Dec-16.
 */
public class LobbyController implements Initializable, EventHandler<KeyEvent>  {

    @FXML
    public ImageView MinimizeIcon, ExitIcon,DoctorIcon,AdminIcon;
    public VBox AdminVBox, DoctorVBox,PatientVBox,PharmacyVBox;
    public HBox LoginButtonHBox,BackHBox,MinimizeHBox,ExitHBox,QuitButtonHBox,LobbyTitlePane;
    public Label AuthenticationTitleLabel,SourceLabel,MainPageLabel,loginDetailsInfo;
    public PasswordField PWField;
    public TextField UsernameField;
    public SynchronizationAnimation SyncAnimation;
    public BorderPane MainPane,HoverPane;
    private StageAction stageAction;
    private Stage mainStage;
    private ArrayList<VBox> lobbyTabs;
    public ApplicationUser selectedUser;
    public boolean enabledHotKeys = true;
    private CustomResourceBundle resourceBundle;
    public ExecutorService authenticationThreadPool;
    public Authentication<?> authentication;
    private AbstractApplicationUser target;


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        stageAction = new StageAction();
        lobbyTabs = new ArrayList<>();
        resourceBundle = (CustomResourceBundle)resources;
        Collections.addAll(lobbyTabs,DoctorVBox,PharmacyVBox,PatientVBox,AdminVBox);
        mainStage = (Stage)resourceBundle.getObject("Stage");

        mainStage.addEventHandler(KeyEvent.KEY_PRESSED,this);
        selectedUser = ApplicationUser.Doctor;
        switchTabs(0);
        setEvents();
        authenticationThreadPool = Executors.newCachedThreadPool();

        BackHBox.setOnMouseClicked(event -> {
            HoverPane.setVisible(false);
            MainPane.setVisible(true);
        });

    }


    /**
     * This method sets events on nodes, based of their purpose
     * e.g : Minimize icon will minimize stage etc.
     * @since 1.0
     */
    private void setEvents()
    {
        DoctorVBox.setOnMouseClicked(event -> switchTabs(0));
        PharmacyVBox.setOnMouseClicked(event -> switchTabs(1));
        PatientVBox.setOnMouseClicked(event -> switchTabs(2));
        AdminVBox.setOnMouseClicked(event -> switchTabs(3));
        MinimizeIcon.setOnMouseClicked(event -> stageAction.minimize(MinimizeIcon));
        MinimizeHBox.setOnMouseClicked(event -> stageAction.minimize(MinimizeIcon));
        ExitIcon.setOnMouseClicked(event -> mainStage.close());
        ExitHBox.setOnMouseClicked(event -> mainStage.close());
        LoginButtonHBox.setOnMouseClicked(event ->authenticate());
        QuitButtonHBox.setOnMouseClicked(event -> mainStage.close());
        mainStage.setOnHidden(event -> {
            if(authenticationThreadPool != null)
                authenticationThreadPool.shutdown();
        });
        mainStage.setOnHiding(event -> {
            if(authenticationThreadPool != null)
                authenticationThreadPool.shutdown();
        });

        MainPageLabel.setOnMouseClicked(event -> Browse.openMainPage());
        SourceLabel.setOnMouseClicked(event -> Browse.openGithub());

    }

    private void authenticate()
    {
        String login = UsernameField.getText();
        String password = PWField.getText();
        if(login.isEmpty() || password.isEmpty())
        {
            loginDetailsInfo.setText("Please fill all fields");
            return;
        }


        switch(selectedUser)
        {
            case Admin:
                target = new Administrator(login,password);
                authentication = new Authentication<>(target);
                break;

            case Doctor:
                target = new Doctor(login,password);
                authentication = new Authentication<>(target);
                break;

            case Patient:
                target = new Patient(login,password);
                authentication = new Authentication<>(target);
                break;

            case Pharmacy:
                target = new Pharmacy(login,password);
                authentication = new Authentication<>(target);
                break;


        }


        if(authentication != null)
        {
            authentication.connectionProperty().addListener((observable, oldValue, newValue) -> {
                loginDetailsInfo.setText(newValue.getFirst());

                if(newValue.getSecond().doubleValue() > 0 && newValue.getSecond().doubleValue() < 1.0 &&  !SyncAnimation.running())
                {
                    SyncAnimation.run();
                    enabledHotKeys = false;
                    SyncAnimation.setImage(new Image("Icons/x64/hourglass.png"));
                    BackHBox.setVisible(false);
                    MainPane.setVisible(false);
                    HoverPane.setVisible(true);
                }
                if(newValue.getSecond().doubleValue() < 0)
                {
                    SyncAnimation.setImage(new Image("Icons/x64/error_marked.png"));
                    BackHBox.setVisible(true);
                    SyncAnimation.stop();
                    enabledHotKeys = true;
                }

            });

            authenticationThreadPool.execute(() ->{
                if(authentication.authenticated())
                    Platform.runLater(() -> mainStage.close());
            });
        }

    }


    @Override
    public void handle(KeyEvent event)
    {
        if(!enabledHotKeys)
            return;

        if(event.getCode() == KeyCode.ESCAPE)
            stageAction.exit();

        if(event.isAltDown() && event.getCode() != null)
        {
            switch (event.getCode()){

                case  DOWN:
                    switchTabs(selectedUser.ordinal()+1);
                    break;

                case UP:
                    switchTabs(selectedUser.ordinal()-1);
                    break;

                case C:
                    authenticate();
                    break;

                default:
                    break;
            }
        }
    }

    private void switchTabs(int index)
    {
        if(index == -1)
            index = 3;
        if(index > ApplicationUser.values().length-1)
            index = 0;

        selectedUser = ApplicationUser.values()[index];
        resetLobbyTabsDesign();
        switch(selectedUser)
        {
            case Admin:
                AuthenticationTitleLabel.setText("Authentication [Admin]");
                AdminVBox.setBackground(new Background(new BackgroundFill(FinalConstants.LOBBY_TAB_SELECTED_COLOR,null,null)));
                break;
            case Doctor:
                AuthenticationTitleLabel.setText("Authentication [Doctor]");
                DoctorVBox.setBackground(new Background(new BackgroundFill(FinalConstants.LOBBY_TAB_SELECTED_COLOR,null,null)));
                break;

            case Patient:
                AuthenticationTitleLabel.setText("Authentication [Patient]");
                PatientVBox.setBackground(new Background(new BackgroundFill(FinalConstants.LOBBY_TAB_SELECTED_COLOR,null,null)));
                break;

            case Pharmacy:
                AuthenticationTitleLabel.setText("Authentication [Pharmacy]");
                PharmacyVBox.setBackground(new Background(new BackgroundFill(FinalConstants.LOBBY_TAB_SELECTED_COLOR,null,null)));
                break;

            default:
                break;

        }
    }

    private void resetLobbyTabsDesign()
    {
        lobbyTabs.forEach((VBox lobbyTab) -> lobbyTab.setBackground(new Background(new BackgroundFill(FinalConstants.LOBBY_TAB_DEFAULT_COLOR,null,new Insets(0,0,0,0)))));
    }


}
