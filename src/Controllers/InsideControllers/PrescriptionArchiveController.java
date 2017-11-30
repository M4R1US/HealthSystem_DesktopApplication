package Controllers.InsideControllers;

import Actions.NodeModification;
import Actions.WindowLauncher;
import Animations2D.SynchronizationAnimation;
import Animations2D.TextAnimation;
import AppUsers.AbstractApplicationUser;
import Classes.Prescription;
import Interfaces.DatabaseSensorAttachable;
import Registers.PrescriptionRegister;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
 * <h2>Created by Marius Baltramaitis on 24/10/2017.</h2>
 * <p>This controller is responsible for showing all available prescriptions of application user</p>
 */
public class PrescriptionArchiveController extends InsideController implements DatabaseSensorAttachable {


    @FXML
    public GridPane PrescriptionArchiveGridPane,InformationGridPane;
    public StackPane PrescriptionArchiveBaseStackPane,CenterStackPane;
    public ListView<Prescription> PrescriptionListView;
    public SynchronizationAnimation SyncAnimation;
    public BorderPane LoadingPane,PrescriptionBasePane,SpecialNotesPane;
    public HBox TopHBox,DoctorHBox;
    public Label TopLabel,DoctorNameLabel,PatientNameLabel;
    public Circle DoctorImageCircle,PatientImageCircle;
    public Label PrescriptionInformationLabel,SpecialNotesLabel,MedicineNameLabel,StatusLabel;
    public ImageView MedicineIcon;
    public TextArea SpecialNotesTextArea;

    private double width,height;
    private AbstractApplicationUser user;
    private ArrayList<Prescription> foundData;

    private ExecutorService browsePrescriptionThreadPool;
    private PrescriptionRegister prescriptionRegister;
    private Consumer<Boolean> parentLockFunction;
    public Stage currentStage;


    /**
     *  {@inheritDoc}
     */
    @Override
    public void setWidth(double ProvidedParentWidth)
    {
        width = ProvidedParentWidth-100;
        PrescriptionArchiveBaseStackPane.setMaxWidth(width);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight)
    {
        height = ProvidedParentHeight - 160;
        PrescriptionArchiveBaseStackPane.setMaxHeight(height);
    }


    /**
     *  {@inheritDoc}
     */

    @Override
    public void setParentLockFunction(Consumer<Boolean> lockFunction) {
        parentLockFunction = lockFunction;
    }


    /**
     * Initializes local variables from fxml file, also attaches events for corresponding nodes
     * @param location fxml path
     * @param resources CustomResourceBundle object with additional resources
     * @see Classes.CustomResourceBundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        user = (AbstractApplicationUser)resources.getObject("User");
        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());

        PrescriptionBasePane.widthProperty().addListener((observable, oldValue, newValue) ->{
            TopHBox.setPrefWidth(newValue.doubleValue());
            TopLabel.setPrefWidth(newValue.doubleValue()-70);
        });

        DoctorImageCircle.setFill(new ImagePattern(new Image("Icons/DefaultProfileIcons/doctor-male-circle.png")));
        PatientImageCircle.setFill(new ImagePattern(new Image("Icons/DefaultProfileIcons/boy-circle.png")));

        PrescriptionInformationLabel.setTextFill(FinalConstants.PRESCRIPTION_SELECTION_LABEL_SELECTED_COLOR);
        SpecialNotesLabel.setTextFill(FinalConstants.PRESCRIPTION_SELECTION_LABEL_DEFAULT_COLOR);
        PrescriptionInformationLabel.setText("∙ Prescription information");

        PrescriptionInformationLabel.setOnMouseClicked(event -> {
            PrescriptionInformationLabel.setText("∙ Prescription information");
            SpecialNotesLabel.setText("Special notes");
            SpecialNotesPane.setVisible(false);
            InformationGridPane.setVisible(true);
            PrescriptionInformationLabel.setTextFill(FinalConstants.PRESCRIPTION_SELECTION_LABEL_SELECTED_COLOR);
            SpecialNotesLabel.setTextFill(FinalConstants.PRESCRIPTION_SELECTION_LABEL_DEFAULT_COLOR);
        });

        SpecialNotesLabel.setOnMouseClicked(event -> {
            SpecialNotesLabel.setText("∙ Special Notes");
            PrescriptionInformationLabel.setText("Prescription information");
            InformationGridPane.setVisible(false);
            SpecialNotesPane.setVisible(true);
            PrescriptionInformationLabel.setTextFill(FinalConstants.PRESCRIPTION_SELECTION_LABEL_DEFAULT_COLOR);
            SpecialNotesLabel.setTextFill(FinalConstants.PRESCRIPTION_SELECTION_LABEL_SELECTED_COLOR);

        });


        CenterStackPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            SpecialNotesPane.setMaxWidth(newValue.doubleValue()*0.85);
        });

        MedicineNameLabel.setOnMouseClicked(event ->  {
            if(PrescriptionListView.getSelectionModel().selectedItemProperty().get() != null)
            {
                currentStage.setFullScreen(false);
                WindowLauncher.launchMedicineInformationWindow(PrescriptionListView.getSelectionModel().selectedItemProperty().get().getMedicine());
            }
        });


        PrescriptionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null)
            {
                super.sendImageRequest(newValue.getDoctor().getID(),"Doctor",doctorImageRequest());
                DoctorNameLabel.setText(newValue.getDoctor().getTitle() + " : " + newValue.getDoctor().getFirstName() + " " + newValue.getDoctor().getLastName());
                super.sendImageRequest(newValue.getPatient().getID(),"Patient",patientImageRequest());
                PatientNameLabel.setText(newValue.getPatient().getFirstName() + " " + newValue.getPatient().getLastName());
                MedicineIcon.setImage(NodeModification.medicineIconSupplier(newValue.getMedicine().getType()).get());
                MedicineNameLabel.setText(newValue.getMedicine().getName());
                String statusText = (newValue.getPharmacy() != null) ? "Status : Sold at " + newValue.getPharmacy().getName() : "Status : Not sold";
                StatusLabel.setText(statusText);
                String SpecialNotes = (newValue.getSpecialNotes() == null) ? "None" : newValue.getSpecialNotes();
                SpecialNotesTextArea.setText(SpecialNotes);
                Platform.runLater(new TextAnimation(DoctorNameLabel));
                Platform.runLater(new TextAnimation(PatientNameLabel));
                Platform.runLater(new TextAnimation(MedicineNameLabel));
            }
        });

        DoctorHBox.widthProperty().addListener((observable, oldValue, newValue) -> {

            double imageRadius = (newValue.doubleValue() *0.2 ) > 32 ? 32 : newValue.doubleValue()*0.2;
                DoctorImageCircle.setRadius(imageRadius);
                PatientImageCircle.setRadius(imageRadius);
        });

        browsePrescriptionDatabaseRequest();
    }

    /**
     * Method for database request of all available prescription of this user
     * @see PrescriptionRegister#findBy(AbstractApplicationUser, boolean)
     */
    private void browsePrescriptionDatabaseRequest()
    {

        prescriptionRegister = new PrescriptionRegister();

        attachProcessSensor(prescriptionRegister,SyncAnimation,LoadingPane,parentLockFunction,browsePrescriptionThreadPool);

        browsePrescriptionThreadPool = Executors.newWorkStealingPool();
        browsePrescriptionThreadPool.submit(() ->{
            foundData = prescriptionRegister.findBy(user,true);
            Platform.runLater(() -> {
                PrescriptionListView.getItems().clear();
                PrescriptionListView.getItems().addAll(foundData);
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
