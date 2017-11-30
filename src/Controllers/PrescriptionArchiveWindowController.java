package Controllers;

import Actions.NodeModification;
import Actions.WindowLauncher;
import Animations2D.SynchronizationAnimation;
import Animations2D.TextAnimation;
import AppUsers.AbstractApplicationUser;
import Classes.Prescription;
import Interfaces.DatabaseSensorAttachable;
import Interfaces.ServerImageRequestImplementation;
import Registers.PrescriptionRegister;
import SavedVariables.FinalConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
 * <h2>Created by Marius Baltramaitis on 25/11/2017.</h2>
 *
 * <p>Controller for prescription archive window</p>
 */
public class PrescriptionArchiveWindowController implements Initializable, DatabaseSensorAttachable, ServerImageRequestImplementation {

    @FXML
    public GridPane PrescriptionArchiveGridPane,InformationGridPane;
    public StackPane PrescriptionArchiveBaseStackPane,CenterStackPane;
    public ListView<Prescription> PrescriptionListView;
    public SynchronizationAnimation SyncAnimation;
    public BorderPane LoadingPane,PrescriptionWindowBasePane;
    public HBox TopHBox,DoctorHBox;
    public Circle DoctorImageCircle,PatientImageCircle;
    public Label MedicineNameLabel,StatusLabel,TopLabel,DoctorNameLabel,PatientNameLabel,AvailableLabel;
    public ImageView MedicineIcon;
    public Button AvailabilityButton;


    private ExecutorService browseImageThreadPool,prescriptionAvailabilityThreadPool;
    private AbstractApplicationUser user;
    private ArrayList<Prescription> foundData;

    private ExecutorService browsePrescriptionThreadPool;
    private PrescriptionRegister prescriptionRegister;
    public Stage currentStage;




    /**
     * Initializes local variables from fxml file, also attaches events for corresponding nodes
     * @param location fxml path
     * @param resources CustomResourceBundle object with additional resources
     * @see Classes.CustomResourceBundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        user = (AbstractApplicationUser)resources.getObject("User");
        browseImageThreadPool = Executors.newWorkStealingPool();
        prescriptionAvailabilityThreadPool= Executors.newWorkStealingPool();

        PrescriptionWindowBasePane.widthProperty().addListener((observable, oldValue, newValue) ->{
            TopHBox.setPrefWidth(newValue.doubleValue());
            TopLabel.setPrefWidth(newValue.doubleValue()-70);
        });

        DoctorImageCircle.setFill(new ImagePattern(new Image("Icons/DefaultProfileIcons/doctor-male-circle.png")));
        PatientImageCircle.setFill(new ImagePattern(new Image("Icons/DefaultProfileIcons/boy-circle.png")));


        MedicineNameLabel.setOnMouseClicked(event ->  {
            if(PrescriptionListView.getSelectionModel().selectedItemProperty().get() != null)
            {
                currentStage.setFullScreen(false);
                WindowLauncher.launchMedicineInformationWindow(PrescriptionListView.getSelectionModel().selectedItemProperty().get().getMedicine());
            }
        });

        AvailabilityButton.setOnMouseClicked(event -> prescriptionAvailabilityDatabaseRequest());


        PrescriptionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null)
            {
                sendImageRequest(newValue.getDoctor().getID(),"Doctor",doctorImageRequest(),browseImageThreadPool,"Administrator" + user.getLogin());
                DoctorNameLabel.setText(newValue.getDoctor().getTitle() + " : " + newValue.getDoctor().getFirstName() + " " + newValue.getDoctor().getLastName());
                sendImageRequest(newValue.getPatient().getID(),"Patient",patientImageRequest(),browseImageThreadPool,"Administrator" + user.getLogin());
                PatientNameLabel.setText(newValue.getPatient().getFirstName() + " " + newValue.getPatient().getLastName());
                MedicineIcon.setImage(NodeModification.medicineIconSupplier(newValue.getMedicine().getType()).get());
                MedicineNameLabel.setText(newValue.getMedicine().getName());
                String statusText = (newValue.getPharmacy() != null) ? "Pharmacy Status : Sold at " + newValue.getPharmacy().getName() : "Pharmacy Status : Not sold";
                StatusLabel.setText(statusText);
                Platform.runLater(new TextAnimation(DoctorNameLabel));
                Platform.runLater(new TextAnimation(PatientNameLabel));
                Platform.runLater(new TextAnimation(MedicineNameLabel));
                String availabilityText = newValue.getAvailable() == 1 ? "Disable" : "Enable";
                String availableInformationText = newValue.getAvailable() == 1 ? "Status : available" : "Status : disabled";
                AvailableLabel.setText(availableInformationText);
                AvailabilityButton.setText(availabilityText);
            }
        });

        DoctorHBox.widthProperty().addListener((observable, oldValue, newValue) -> {

            double imageRadius = (newValue.doubleValue() *0.2 ) > 32 ? 32 : newValue.doubleValue()*0.2;
            DoctorImageCircle.setRadius(imageRadius);
            PatientImageCircle.setRadius(imageRadius);
        });

        browsePrescriptionDatabaseRequest(0);
    }

    /**
     * Lock function to lock user input when database transaction is happening
     * @return functional interface with instructions what to do with controls/nodes when database transaction is happening
     */
    private Consumer<Boolean> lockFunction()
    {
        return(Boolean locked) -> {
            currentStage.setOnCloseRequest(event -> {
                if(locked)
                    event.consume();
            });
        };
    }

    /**
     * Method that is used for enabling/disabling prescriptions
     */
    private void prescriptionAvailabilityDatabaseRequest()
    {
        prescriptionRegister = new PrescriptionRegister();

        attachProcessSensor(prescriptionRegister,SyncAnimation,LoadingPane,lockFunction(),prescriptionAvailabilityThreadPool);
        int selected = PrescriptionListView.getSelectionModel().getSelectedIndex();

        prescriptionAvailabilityThreadPool = Executors.newWorkStealingPool();
        prescriptionAvailabilityThreadPool.submit(() -> {
            Prescription prescription = PrescriptionListView.getSelectionModel().getSelectedItem();
            int availability = prescription.getAvailable() == 1 ? 0 : 1;

            prescriptionRegister.attachFinishSensor((boolean success) ->{
                if(success)
                    browsePrescriptionDatabaseRequest(selected);

            });

            prescriptionRegister.updateAvailability(Integer.parseInt(PrescriptionListView.getSelectionModel().getSelectedItem().getID()), availability);

        });


    }

    /**
     * Database request to gather prescription information from user selected in list view
     * @param selectedIndex index of selected user
     */
    private void browsePrescriptionDatabaseRequest(int selectedIndex)
    {

        prescriptionRegister = new PrescriptionRegister();

        attachProcessSensor(prescriptionRegister,SyncAnimation,LoadingPane,lockFunction(),browsePrescriptionThreadPool);

        browsePrescriptionThreadPool = Executors.newWorkStealingPool();
        browsePrescriptionThreadPool.submit(() ->{
            foundData = prescriptionRegister.findBy(user,false);
            Platform.runLater(() -> {
                PrescriptionListView.getItems().clear();
                PrescriptionListView.getItems().addAll(foundData);
                TopLabel.setText("Last 200 prescriptions of " + user.toString());
                if( selectedIndex > -1)
                    PrescriptionListView.getSelectionModel().select(selectedIndex);
            });
        });

        Platform.runLater(() ->currentStage = (Stage)PrescriptionArchiveBaseStackPane.getScene().getWindow());
    }

    /**
     * Consumer function for patient image lookup
     * @return functional interface with instructions what to do with image
     */
    private Consumer<Image> doctorImageRequest()
    {
        return (Image receivedImage) -> {

            if(receivedImage != null)
            {
                DoctorImageCircle.setFill(new ImagePattern(receivedImage));
                DoctorImageCircle.setStroke(FinalConstants.CIRCLE_IMAGE_STROKE);
                return;
            }

            if(PrescriptionListView.getSelectionModel().getSelectedItem() != null)
            {
                String defaultAvatarPath = (PrescriptionListView.getSelectionModel().getSelectedItem().getDoctor().getSex().equalsIgnoreCase("Male")) ? "/Icons/DefaultProfileIcons/doctor-male-circle.png" : "/Icons/DefaultProfileIcons/doctor-female-circle.png";
                DoctorImageCircle.setStroke(null);
                DoctorImageCircle.setFill(new ImagePattern(new Image(defaultAvatarPath)));
            }
        };
    }


    /**
     * Consumer function for patient image lookup
     * @return functional interface with instructions what to do with image
     */
    private Consumer<Image> patientImageRequest()
    {
        return (Image receivedImage) -> {

            if(receivedImage != null)
            {
                PatientImageCircle.setFill(new ImagePattern(receivedImage));
                PatientImageCircle.setStroke(FinalConstants.CIRCLE_IMAGE_STROKE);
                return;
            }

            if(PrescriptionListView.getSelectionModel().getSelectedItem() != null)
            {
                String defaultAvatarPath = (PrescriptionListView.getSelectionModel().getSelectedItem().getPatient().getSex().equalsIgnoreCase("Male")) ? "/Icons/DefaultProfileIcons/boy-circle.png" : "/Icons/DefaultProfileIcons/girl-circle.png";
                PatientImageCircle.setStroke(null);
                PatientImageCircle.setFill(new ImagePattern(new Image(defaultAvatarPath)));
            }
        };
    }
}
