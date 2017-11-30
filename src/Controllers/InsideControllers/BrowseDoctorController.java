package Controllers.InsideControllers;

import Actions.WindowLauncher;
import Animations2D.SynchronizationAnimation;
import AppUsers.AbstractApplicationUser;
import AppUsers.Doctor;
import Classes.GenericPair;
import Interfaces.DatabaseSensorAttachable;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import Interfaces.BrowsePaneAdditionalImplementation;
import Registers.DoctorRegister;
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
 * <h2>Created by Marius Baltramaitis on 27/04/2017.</h2>
 * <p>Inside controller in administrator dashboard responsible for browsing through browse-doctor section</p>
 */
public class BrowseDoctorController extends InsideController implements BrowsePaneAdditionalImplementation, DatabaseSensorAttachable {

    @FXML
    public BorderPane BrowseDoctorRootPane,LoadingPane;
    public TextField NameInput;
    public HBox BrowseTopPane;
    public VBox InfoVBox;
    public Circle ImageCircle;
    public SynchronizationAnimation SyncAnimation;
    public BorderPane PreviewPane;
    public GridPane BrowseCenterGridPane;
    public ListView<Doctor> BrowseListView;
    public ImageView ImageSquare;
    public Label SearchLabel,TopLabel,TypeLabel,NameLabel,EditLabel,PrescriptionsLabel;

    private Consumer<Boolean> parentLockFunction;
    private final double DEFAULT_IMAGE_RADIUS = 50;
    private DoctorRegister doctorRegister;
    private ArrayList<Doctor> foundData;
    private ExecutorService browseDoctorThreadPool;
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
        BrowseDoctorRootPane.setMaxWidth(width);
        NameInput.setMinWidth((width-220));
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight)
    {
        height = ProvidedParentHeight - 160;
        BrowseDoctorRootPane.setMaxHeight(height);
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
                String defaultAvatarPath = (BrowseListView.getSelectionModel().getSelectedItem().getSex().equalsIgnoreCase("Male")) ? "/Icons/DefaultProfileIcons/doctor-male-circle.png" : "/Icons/DefaultProfileIcons/doctor-female-circle.png";
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
        super.contrastEffect(BrowseDoctorRootPane);
        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());

        ImageCircle.setRadius(DEFAULT_IMAGE_RADIUS);


        ImageCircle.fillProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null)
            {
                ImageCircle.setFill(new ImagePattern(new Image(FinalConstants.DEFAULT_MALE_IMG_SRC)));
                ImageCircle.setVisible(true);
            }

        });

        mainStage = ((Stage)resources.getObject("Stage"));

        PreviewPane.widthProperty().addListener(this.previewPaneWidthListener(InfoVBox,TopLabel,NameLabel,TypeLabel));

        PreviewPane.heightProperty().addListener(this.previewPaneHeightListener(InfoVBox,ImageCircle));

        SearchLabel.setOnMouseClicked(event -> browseDoctorDatabaseRequest());

        BrowseListView.itemsProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null)
                BrowseListView.getSelectionModel().select(0);
        });

        BrowseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue != null)
            {
                TypeLabel.setText(newValue.getTitle());
                NameLabel.setText(newValue.getFirstName() + " " + newValue.getLastName());
                super.sendImageRequest(newValue.getID(),"Doctor", imageLookup());
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
                browseDoctorDatabaseRequest();

        });

        PrescriptionsLabel.setOnMouseClicked(event -> {
            if(BrowseListView.getSelectionModel().getSelectedItem() != null)
            {
                mainStage.setFullScreen(false);
                WindowLauncher.launchPrescriptionArchiveWindow(BrowseListView.getSelectionModel().getSelectedItem());
            }
        });

    }

    /**
     * Edit function updating doctor list view
     * @return consumer that is accepting new doctor object and replacing old one
     */
    private Consumer<AbstractApplicationUser> editFunction()
    {
        return (user) -> {
            Doctor doctor  = (Doctor)user;
            BrowseListView.getItems().replaceAll((Doctor d) -> {
                if(d.getID().equalsIgnoreCase(doctor.getID()))
                    return (Doctor)user;
                return d;
            });
        };
    }


    /**
     * <p>
     *     Request data from database<br>
     *     Asking for all rows matching input based on first name and last name
     * </p>
     * @see DoctorRegister#find(String, String)
     */
    private void browseDoctorDatabaseRequest() {

        GenericPair<String,String> firstNameAndLastName =  splitInput(NameInput.getText());
        firstName = firstNameAndLastName.getFirst();
        lastName = firstNameAndLastName.getSecond();

        doctorRegister = new DoctorRegister();

        attachProcessSensor(doctorRegister,SyncAnimation,LoadingPane,parentLockFunction,browseDoctorThreadPool);

        browseDoctorThreadPool = Executors.newWorkStealingPool();
        browseDoctorThreadPool.submit(() ->{
            foundData = doctorRegister.find(firstName,lastName);
            Platform.runLater(() -> {
                BrowseListView.getItems().clear();
                BrowseListView.getItems().addAll(foundData);
            });
        });

    }

}
