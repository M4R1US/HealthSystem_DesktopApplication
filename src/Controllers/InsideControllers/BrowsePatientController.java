package Controllers.InsideControllers;

import Actions.WindowLauncher;
import Animations2D.SynchronizationAnimation;
import AppUsers.AbstractApplicationUser;
import AppUsers.Patient;
import Classes.GenericPair;
import Interfaces.DatabaseSensorAttachable;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import Interfaces.BrowsePaneAdditionalImplementation;
import Registers.PatientRegister;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 03/05/2017.</h2>
 * <p>Inside controller in administrator dashboard responsible for browsing through browse-patient section</p>

 */
public class BrowsePatientController extends InsideController implements BrowsePaneAdditionalImplementation, DatabaseSensorAttachable {


    @FXML
    public BorderPane BrowsePatientRootPane,LoadingPane;
    public TextField NameInput;
    public VBox InfoVBox;
    public Circle ImageCircle;
    public SynchronizationAnimation SyncAnimation;
    public BorderPane PreviewPane;
    public GridPane BrowseCenterGridPane;
    public ListView<Patient> BrowseListView;
    public ImageView ImageSquare;
    public Label TopLabel,TypeLabel,NameLabel,EditLabel,PrescriptionsLabel,SearchLabel;

    private Consumer<Boolean> parentLockFunction;
    private final double DEFAULT_IMAGE_RADIUS = 50;
    private PatientRegister patientRegister;
    private ArrayList<Patient> foundData;
    private ExecutorService browsePatientThreadPool;
    private String firstName,lastName = null;
    private Stage mainStage;
    private double width,height;


    /**
     *  {@inheritDoc}
     */
    @Override
    public void setWidth(double ProvidedParentWidth)
    {
        width = ProvidedParentWidth -100;
        BrowsePatientRootPane.setMaxWidth(width);
        NameInput.setMinWidth((width-220));
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight)
    {
        height = ProvidedParentHeight - 160;
        BrowsePatientRootPane.setMaxHeight(height);
    }


    /**
     * Consumer function for image lookup
     * @return functional interface with instructions what to do with image
     */
    private Consumer<Image> imageLookup()
    {

        return (Image receivedImage) -> {

            if(receivedImage != null)
            {
                ImageSquare.setVisible(false);
                ImageCircle.setVisible(true);
                ImageCircle.setFill(new ImagePattern(receivedImage));
                ImageCircle.setStroke(FinalConstants.CIRCLE_IMAGE_STROKE);
                return;
            }

            if(BrowseListView.getSelectionModel().getSelectedItem() != null)
            {
                ImageCircle.setVisible(false);
                String defaultAvatarPath = (BrowseListView.getSelectionModel().getSelectedItem().getSex().equalsIgnoreCase("Male")) ? "/Icons/DefaultProfileIcons/boy-circle.png" : "/Icons/DefaultProfileIcons/girl-circle.png";
                ImageCircle.setStroke(null);
                ImageSquare.setImage(new Image(defaultAvatarPath));
                ImageSquare.setVisible(true);
            }

        };
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setParentLockFunction(Consumer<Boolean> lockFunction)
    {
        parentLockFunction = lockFunction;
    }


    /**
     * Initializes local variables from fxml file, also attaches events for corresponding nodes
     * @param location fxml path
     * @param resources CustomResourceBundle object with additional resources
     * @see Classes.CustomResourceBundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        super.contrastEffect(BrowsePatientRootPane);
        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());

        ImageCircle.setRadius(DEFAULT_IMAGE_RADIUS);

        mainStage = ((Stage)resources.getObject("Stage"));

        ImageCircle.fillProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null)
            {
                ImageCircle.setFill(new ImagePattern(new Image(FinalConstants.DEFAULT_MALE_IMG_SRC)));
                ImageCircle.setVisible(true);
            }

        });
        
        PreviewPane.widthProperty().addListener(this.previewPaneWidthListener(InfoVBox,TopLabel,NameLabel,TypeLabel));

        PreviewPane.heightProperty().addListener(this.previewPaneHeightListener(InfoVBox,ImageCircle));

        SearchLabel.setOnMouseClicked(event -> browsePatientDatabaseRequest());

        BrowseListView.itemsProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null)
                BrowseListView.getSelectionModel().select(0);
        });

        BrowseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue != null)
            {
                NameLabel.setText(newValue.getFirstName() + " " + newValue.getLastName());
                String type = newValue.getSex().equalsIgnoreCase("Female") ? "Ms" : "Mr";
                TypeLabel.setText(type);
                super.sendImageRequest(newValue.getID(),"Patient", imageLookup());
            }
        });

        EditLabel.setOnMouseClicked( event -> {
            if(BrowseListView.getSelectionModel().getSelectedItem() != null)
            {
                mainStage.setFullScreen(false);
                WindowLauncher.launchEditWindow(BrowseListView.getSelectionModel().getSelectedItem(),editFunction());

            }
        });

        NameInput.setOnKeyPressed(event ->{
            if((event.getCode() == KeyCode.ENTER))
                browsePatientDatabaseRequest();

        });

        PrescriptionsLabel.setOnMouseClicked(event -> {
            if( BrowseListView.getSelectionModel().getSelectedItem() != null)
            {
                mainStage.setFullScreen(false);
                WindowLauncher.launchPrescriptionArchiveWindow(BrowseListView.getSelectionModel().getSelectedItem());
            }
        });

    }

    /**
     * Edit function updating patient list view
     * @return consumer that is accepting new patient object and replacing old one
     */
    private Consumer<AbstractApplicationUser> editFunction()
    {
        return (user) -> {
            Patient patient  = (Patient)user;
            BrowseListView.getItems().replaceAll((Patient p) -> {
                if(p.getID().equalsIgnoreCase(patient.getID()))
                    return (Patient)user;
                return p;
            });
        };
    }



    /**
     * <p>
     *     Request data from database<br>
     *     Asking for all rows matching input based on first name and last name and availability
     * </p>
     * @see PatientRegister#find(String, String, boolean)
     */
    private void browsePatientDatabaseRequest()
    {
        GenericPair<String,String> firstNameAndLastName =  splitInput(NameInput.getText());
        firstName = firstNameAndLastName.getFirst();
        lastName = firstNameAndLastName.getSecond();

        patientRegister = new PatientRegister();

        attachProcessSensor(patientRegister,SyncAnimation,LoadingPane,parentLockFunction,browsePatientThreadPool);

        browsePatientThreadPool = Executors.newWorkStealingPool();
        browsePatientThreadPool.submit(() ->{
            foundData = patientRegister.find(firstName,lastName,false);
            Platform.runLater(() -> {
                BrowseListView.getItems().clear();
                BrowseListView.getItems().addAll(foundData);
            });
        });

    }
}
