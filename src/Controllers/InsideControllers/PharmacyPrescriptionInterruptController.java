package Controllers.InsideControllers;

import Actions.NodeModification;
import Animations2D.SynchronizationAnimation;
import Animations2D.TextAnimation;
import AppUsers.Patient;
import AppUsers.Pharmacy;
import Classes.GenericPair;
import Classes.Prescription;
import Interfaces.BrowsePaneAdditionalImplementation;
import Interfaces.DatabaseSensorAttachable;
import Registers.PatientRegister;
import Registers.PrescriptionRegister;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 26/11/2017.</h2>
 * <p>This controller is responsible for pharmacy registration inside pharmacy controller</p>
 */
public class PharmacyPrescriptionInterruptController extends InsideController implements BrowsePaneAdditionalImplementation, DatabaseSensorAttachable {

    @FXML
    public BorderPane BrowsePatientPane,PreviewPane,LoadingPane,ConfirmationPane;
    public GridPane PrescriptionArchiveGridPane;
    public TextField NameInput;
    public VBox InfoVBox;
    public Label TopLabel,NameLabel,TypeLabel,SearchLabel,LookupLabel,DoctorNameLabel,PatientNameLabel,MedicineNameLabel;
    public Circle BrowsePatientImageLookupCircle,DoctorImageCircle,PatientImageCircle;
    public ListView<Patient> PatientBrowseListView;
    public ListView<Prescription> PrescriptionListView;
    public SynchronizationAnimation SyncAnimation;
    public ImageView ImageSquare;
    public CheckBox ConfirmationCheckBox;
    public Button BackButton,SellButton;
    public ImageView MedicineIcon;

    private String firstName,lastName = null;
    private PatientRegister patientRegister;
    private ExecutorService browsePatientThreadPool,browsePrescriptionThreadPool,sellPrescriptionThreadPool;
    private ArrayList<Patient> patientList;
    private Consumer<Boolean> parentLockFunction;
    private PrescriptionRegister prescriptionRegister;
    private ArrayList<Prescription> prescriptionList;
    private String defaultAvatarPath;
    private Pharmacy pharmacy;

    private double width,height;

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setWidth(double ProvidedParentWidth) {

        width = ProvidedParentWidth -100;
        BrowsePatientPane.setMaxWidth(width);
        PrescriptionArchiveGridPane.setMaxWidth(width);
        NameInput.setMinWidth((width-220));

    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight) {

        height = ProvidedParentHeight-160;
        BrowsePatientPane.setMaxHeight(height);
        PrescriptionArchiveGridPane.setMaxHeight(height);
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

        super.contrastEffect(BrowsePatientPane);
        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());


        pharmacy = (Pharmacy)resources.getObject("User");

        browsePrescriptionThreadPool = Executors.newWorkStealingPool();

        DoctorImageCircle.setFill(new ImagePattern(new Image("/Icons/DefaultProfileIcons/doctor-male-circle.png")));
        PatientImageCircle.setFill(new ImagePattern(new Image("/Icons/DefaultProfileIcons/boy-circle.png")));

        PreviewPane.widthProperty().addListener(this.previewPaneWidthListener(InfoVBox,TopLabel,NameLabel,TypeLabel));

        PreviewPane.widthProperty().addListener((observable, oldValue, newValue) -> LookupLabel.setPrefWidth(newValue.doubleValue()));

        PreviewPane.heightProperty().addListener(this.previewPaneHeightListener(InfoVBox,BrowsePatientImageLookupCircle));

        SearchLabel.setOnMouseClicked(event -> browsePatientDatabaseRequest());

        NameInput.setOnKeyPressed(event ->{
            if((event.getCode() == KeyCode.ENTER))
                browsePatientDatabaseRequest();

        });

        BrowsePatientImageLookupCircle.fillProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null) {
                BrowsePatientImageLookupCircle.setFill(new ImagePattern(new Image(FinalConstants.DEFAULT_MALE_IMG_SRC)));
                BrowsePatientImageLookupCircle.setVisible(true);
            }

        });

        PatientBrowseListView.itemsProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null)
                PatientBrowseListView.getSelectionModel().select(0);
        });

        PatientBrowseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue != null)
            {
                NameLabel.setText(newValue.getFirstName() + " " + newValue.getLastName());
                super.sendImageRequest(newValue.getID(),"Patient", patientImageLookup());
                String type = newValue.getSex().equalsIgnoreCase("Female") ? "Ms" : "Mr";
                TypeLabel.setText(type);
            }
        });

        LookupLabel.setOnMouseClicked(event ->{
            if(PatientBrowseListView.getSelectionModel().getSelectedItem() != null)
                swapPane(1);
            else
                java.awt.Toolkit.getDefaultToolkit().beep();

        });



        BackButton.setOnMouseClicked(event -> swapPane(0));

        SellButton.setOnMouseClicked(event ->{
            if(!ConfirmationCheckBox.isSelected() || PrescriptionListView.getSelectionModel().getSelectedItem() == null)
            {
                ConfirmationCheckBox.setTextFill(Paint.valueOf(FinalConstants.RED_COLOR));
                java.awt.Toolkit.getDefaultToolkit().beep();

            }
            else
                markAsSoldDatabaseRequest();

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
                Platform.runLater(new TextAnimation(DoctorNameLabel));
                Platform.runLater(new TextAnimation(PatientNameLabel));
                Platform.runLater(new TextAnimation(MedicineNameLabel));
            }
        });


    }

    /**
     * <p>
     *     Request data from database<br>
     *     Asking for all rows matching input based on first name and last name
     *     Later collecting data if patient is not disabled by administration
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
            parentLockFunction.accept(true);
            patientList = patientRegister.find(firstName,lastName,true);
            Platform.runLater(() -> {
                PatientBrowseListView.getItems().clear();
                PatientBrowseListView.getItems().addAll(patientList);
                parentLockFunction.accept(false);
            });
        });

    }

    /**
     * <p>
     *     Request data from database<br>
     *         Asking for all unsold prescriptions based of selected patient
     * </p>
     * @see PrescriptionRegister#findAvailableAndNotSold(String)
     */
    private void browsePrescriptionDatabaseRequest()
    {
        prescriptionRegister = new PrescriptionRegister();

        attachProcessSensor(prescriptionRegister,SyncAnimation,LoadingPane,parentLockFunction,browsePrescriptionThreadPool);

        browsePrescriptionThreadPool = Executors.newWorkStealingPool();
        browsePrescriptionThreadPool.submit(() -> {
           parentLockFunction.accept(true);
           prescriptionList = prescriptionRegister.findAvailableAndNotSold(PatientBrowseListView.getSelectionModel().getSelectedItem().getID());
            Platform.runLater(() -> {
                PrescriptionListView.getItems().clear();
                PrescriptionListView.getItems().addAll(prescriptionList);
                parentLockFunction.accept(false);
            });
        });

    }

    /**
     * Database request to mark selected prescription sold
     */
    private void markAsSoldDatabaseRequest()
    {
        prescriptionRegister = new PrescriptionRegister();

        sellPrescriptionThreadPool = Executors.newWorkStealingPool();

        prescriptionRegister.addListener((text, progress) -> {
            if (progress < 1.0 && progress >= 0) {
                if(SyncAnimation != null)
                    SyncAnimation.run();

                LoadingPane.setVisible(true);
                if (parentLockFunction != null)
                    parentLockFunction.accept(true);
            }

            if(progress >= 1.0)
            {
                LoadingPane.setVisible(false);
                swapPane(2);
            }
        });

        sellPrescriptionThreadPool.submit(() ->{
            parentLockFunction.accept(true);
            prescriptionRegister.markAsSold(PrescriptionListView.getSelectionModel().getSelectedItem().getID(),pharmacy.getID());
            parentLockFunction.accept(false);
        });
    }


    /**
     * Swaping panes depending on given index
     * @param targetIndex index of pane
     */
    private void swapPane(int targetIndex)
    {
        BrowsePatientPane.setVisible(false);
        ConfirmationPane.setVisible(false);
        PrescriptionArchiveGridPane.setVisible(false);

        if(targetIndex > 2)
            throw new IllegalArgumentException("Invalid index!");

        switch (targetIndex)
        {
            case 0:
                BrowsePatientPane.setVisible(true);
                break;

            case 1:
                PrescriptionArchiveGridPane.setVisible(true);
                browsePrescriptionDatabaseRequest();
                break;

            case 2:
                ConfirmationPane.setVisible(true);
                break;
        }

    }

    /**
     * Consumer function for patient image lookup
     * @return functional interface with instructions what to do with image
     */
    private Consumer<Image> patientImageLookup()
    {

        return (Image receivedImage) -> {

            if(receivedImage != null)
            {
                ImageSquare.setVisible(false);
                BrowsePatientImageLookupCircle.setVisible(true);
                BrowsePatientImageLookupCircle.setFill(new ImagePattern(receivedImage));
                BrowsePatientImageLookupCircle.setStroke(FinalConstants.CIRCLE_IMAGE_STROKE);
                return;
            }

            if(PatientBrowseListView.getSelectionModel().getSelectedItem() != null)
            {
                BrowsePatientImageLookupCircle.setVisible(false);
                defaultAvatarPath = (PatientBrowseListView.getSelectionModel().getSelectedItem().getSex().equalsIgnoreCase("Male")) ? "/Icons/DefaultProfileIcons/boy-circle.png" : "/Icons/DefaultProfileIcons/girl-circle.png";
                BrowsePatientImageLookupCircle.setStroke(null);
                ImageSquare.setImage(new Image(defaultAvatarPath));
                ImageSquare.setVisible(true);
            }

        };
    }

    /**
     * Consumer function for doctor image lookup
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
