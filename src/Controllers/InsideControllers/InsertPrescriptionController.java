package Controllers.InsideControllers;

import Actions.NodeModification;
import Actions.Validation;
import Actions.WindowLauncher;
import Animations2D.SynchronizationAnimation;
import AppUsers.Doctor;
import AppUsers.Patient;
import Classes.GenericPair;
import Classes.Medicine;
import Classes.Prescription;
import CustomCache.ImageCacheRegister;
import Interfaces.BrowsePaneAdditionalImplementation;
import Interfaces.DatabaseSensorAttachable;
import Listeners.TextPropertyListener;
import Registers.MedicineRegister;
import Registers.PatientRegister;
import Registers.PrescriptionRegister;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 09/09/2017.</h2>
 * <p>Controller responsible for prescription registration doctor dashboard</p>

 */
public class InsertPrescriptionController extends InsideController implements BrowsePaneAdditionalImplementation, DatabaseSensorAttachable {


    @FXML
    public StackPane SelectPatientStackPane,SelectMedicineStackPane;
    public BorderPane InsertPrescriptionBasePane,UploadPane,SearchPatientPane,SearchMedicinePane,PatientLoadPane,PatientInformationPane,MedicineInformationPane,MedicineLoadPane,MedicinePreviewPane,ConfirmationPane;
    public GridPane InsertPrescriptionTopSelectionBarGridPane;
    public Pagination CustomPagination;
    public GridPane SelectMedicinePane,SpecialNotesPane;
    public TextField PatientNameInput,MedicineNameInput;
    public ListView<Patient> PatientBrowseListView;
    public ListView<Medicine> MedicineBrowseListView;
    public SynchronizationAnimation SyncAnimation, MedicineSyncAnimation,UploadSyncAnimation;
    public ImageView BrowsePaneSearchIcon,BrowseMedicinePaneSearchIcon,MedicineIcon,SelectedMedicineIcon;
    public Label NextToMedicinePaneLabel,NameLabel,PatientLabel,MedicineLookupLabel,MedicineNameLabel,NextToNotesPaneLabel,MedicineLabel,UploadLabel,LoadingLabel;
    public Circle PatientAvatarCircle,SelectedPatientImageCircle;
    public VBox CenterSplitVBox;
    public TextArea SpecialNotesTextArea;
    public HBox SelectPatientHBox,SelectMedicineHBox,ConfirmationHBox;
    public CheckBox ConfirmCheckBox;

    public ArrayList<Label> switchLabels;
    public ArrayList<HBox> switchHBoxes;
    private double width,height;
    private int currentPaginationIndex = 0;
    private Consumer<Boolean> parentLockFunction;
    private String firstName,lastName = null;
    private PatientRegister patientRegister;
    private MedicineRegister medicineRegister;
    private ExecutorService browsePatientThreadPool,browseMedicineThreadPool,uploadPrescriptionThreadPool;
    private ArrayList<Patient> patientList;
    private ArrayList<Medicine> medicineList;
    private Patient selectedPatient;
    private Doctor currentUser;
    private Medicine selectedMedicine;
    private Stage currentStage;
    private Image selectedMedicineImage;
    private Prescription prescription;
    private PrescriptionRegister prescriptionRegister;



    /**
     * Initializes local variables from fxml file, also attaches events for corresponding nodes
     * @param location fxml path
     * @param resources CustomResourceBundle object with additional resources
     * @see Classes.CustomResourceBundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        prescription = new Prescription();
        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());

        CustomPagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
        CustomPagination.setPageFactory(this::paginationReflection);
        CustomPagination.setCurrentPageIndex(currentPaginationIndex);

        SelectedPatientImageCircle.setFill(new ImagePattern(new Image("Icons/DefaultProfileIcons/man.png")));

        currentUser = (Doctor)resources.getObject("User");

        prescription.setDoctor(currentUser);

        switchLabels = new ArrayList<>();
        switchHBoxes = new ArrayList<>();
        InsertPrescriptionTopSelectionBarGridPane.lookupAll(".SwitchLabels").forEach(node -> switchLabels.add((Label)node));
        InsertPrescriptionTopSelectionBarGridPane.lookupAll(".SwitchHBoxes").forEach(node -> switchHBoxes.add((HBox)node));

        SpecialNotesTextArea.setBorder(FinalConstants.BLUE_TEXTAREA_BORDER);
        InsertPrescriptionBasePane.widthProperty().addListener((observable, oldValue, newValue) -> switchLabels.forEach(switchLabel -> switchLabel.setPrefWidth(((newValue.doubleValue()-20)/4)-70)));
        switchHBoxes.forEach(switchHBox -> switchHBox.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(switchHBoxes.indexOf(switchHBox))));
        SearchPatientPane.widthProperty().addListener((observable, oldValue, newValue) -> PatientNameInput.setPrefWidth(newValue.doubleValue()-70));
        SearchMedicinePane.widthProperty().addListener((observable, oldValue, newValue) -> MedicineNameInput.setPrefWidth(newValue.doubleValue()-70));
        PatientInformationPane.widthProperty().addListener((observable, oldValue, newValue) -> NextToMedicinePaneLabel.setPrefWidth(newValue.doubleValue()-50));
        MedicineInformationPane.widthProperty().addListener((observable, oldValue, newValue) -> NextToNotesPaneLabel.setPrefWidth(newValue.doubleValue()-50));
        ConfirmationPane.widthProperty().addListener((observable, oldValue, newValue) -> UploadLabel.setPrefWidth(newValue.doubleValue()-50));
        MedicinePreviewPane.widthProperty().addListener((observable, oldValue, newValue) -> MedicineLookupLabel.setPrefWidth(newValue.doubleValue()));
        SelectPatientHBox.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(1));

        PatientAvatarCircle.setFill(new ImagePattern(new Image("/Icons/DefaultProfileIcons/user-shape.png")));

        BrowsePaneSearchIcon.setOnMouseClicked(event -> browsePatientDatabaseRequest());

        BrowseMedicinePaneSearchIcon.setOnMouseClicked(event -> browseMedicineDatabaseRequest());

        SpecialNotesTextArea.textProperty().addListener(new TextPropertyListener(SpecialNotesTextArea,(String) -> Validation.validTextInput(SpecialNotesTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.SPECIAL_NOTES_PRESCRIPTION_MAX_LENGTH,true)));


        MedicineNameInput.setOnKeyPressed(event -> {
            if((event.getCode() == KeyCode.ENTER))
                browseMedicineDatabaseRequest();
        });

        PatientNameInput.setOnKeyPressed(event -> {
            if((event.getCode() == KeyCode.ENTER))
                browsePatientDatabaseRequest();
        });

        PatientBrowseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue != null)
            {
                selectedPatient = newValue;
                NameLabel.setText(newValue.getFirstName() + " " + newValue.getLastName());
                super.sendImageRequest(newValue.getID(),"Patient", patientImageLookup());
            }
        });

        SelectMedicineHBox.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(2));

        ConfirmationHBox.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(3));

        MedicineBrowseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue != null) {
                selectedMedicine = newValue;
                MedicineNameLabel.setText(newValue.getName());
                selectedMedicineImage = NodeModification.medicineWhiteIconSupplier(newValue.getType()).get();
                MedicineIcon.setImage(NodeModification.medicineIconSupplier(newValue.getType()).get());
            }
        });

        MedicineLookupLabel.setOnMouseClicked(event -> {
            if(selectedMedicine == null)
            {
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            }
            else
            {
                currentStage.setFullScreen(false);
                WindowLauncher.launchMedicineInformationWindow(selectedMedicine);
            }

        });

    }


    /**
     *  {@inheritDoc}
     */
    @Override
    public void setWidth(double ProvidedParentWidth)
    {
        width = ProvidedParentWidth-100;
        InsertPrescriptionBasePane.setMaxWidth(width);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight)
    {
        height = ProvidedParentHeight - 160;
        InsertPrescriptionBasePane.setMaxHeight(height);
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
                PatientAvatarCircle.setVisible(true);
                PatientAvatarCircle.setFill(new ImagePattern(receivedImage));
                PatientAvatarCircle.setStroke(FinalConstants.CIRCLE_IMAGE_STROKE);
                return;
            }

            if(PatientBrowseListView.getSelectionModel().getSelectedItem() != null)
            {
                String defaultAvatarPath = (PatientBrowseListView.getSelectionModel().getSelectedItem().getSex().equalsIgnoreCase("Male")) ? "/Icons/DefaultProfileIcons/boy-circle.png" : "/Icons/DefaultProfileIcons/girl-circle.png";
                PatientAvatarCircle.setFill(new ImagePattern(new Image(defaultAvatarPath)));
                PatientAvatarCircle.setStroke(null);
            }

        };
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setParentLockFunction(Consumer<Boolean> lockFunction)
    {
        this.parentLockFunction = lockFunction;
    }

    /**
     * Method for pagination reflection
     * @param targetIndex targeted index
     * @return Node depending on given index, null if index is not valid
     */
    private Node paginationReflection(Integer targetIndex)
    {
        if((currentPaginationIndex > targetIndex+1) || (currentPaginationIndex < targetIndex-1))
            return null;


        switch(targetIndex)
        {
            case 0:
                switchHBoxes.forEach(switchLabel -> switchLabel.setBackground(null));
                switchHBoxes.get(0).setBackground(FinalConstants.SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND);
                currentPaginationIndex = 0;
                return SelectPatientStackPane;

            case 1:
                if(selectedPatient == null)
                {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }
                prescription.setPatient(selectedPatient);
                PatientLabel.setText(selectedPatient.getFirstName().substring(0,1) + ". " + selectedPatient.getLastName());
                Image selectedImage = ImageCacheRegister.find(selectedPatient.getID(),"Patient").getImage();
                String imagePath;
                if(selectedImage == null)
                {
                    imagePath = (selectedPatient.getSex().equalsIgnoreCase("Male") ? "Icons/DefaultProfileIcons/boy-circle.png" : "Icons/DefaultProfileIcons/girl-circle.png");
                    selectedImage = new Image(imagePath);
                }

                SelectedPatientImageCircle.setFill(new ImagePattern(selectedImage));
                switchHBoxes.forEach(switchLabel -> switchLabel.setBackground(null));
                switchHBoxes.get(0).setBackground(FinalConstants.SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND);
                switchHBoxes.get(1).setBackground(FinalConstants.SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND);
                currentPaginationIndex = 1;
                return SelectMedicineStackPane;

            case 2:
                if(selectedMedicine == null)
                {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }
                prescription.setMedicine(selectedMedicine);
                MedicineLabel.setText(selectedMedicine.getName());
                SelectedMedicineIcon.setImage(selectedMedicineImage);
                switchHBoxes.forEach(switchLabel -> switchLabel.setBackground(null));
                switchHBoxes.get(0).setBackground(FinalConstants.SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND);
                switchHBoxes.get(1).setBackground(FinalConstants.SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND);
                switchHBoxes.get(2).setBackground(FinalConstants.SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND);
                currentPaginationIndex = 2;
                return SpecialNotesPane;

            case 3:
                if(!Validation.validTextInput(SpecialNotesTextArea.getText(),FinalConstants.SPECIAL_NOTES_MIN_LENGTH,FinalConstants.SPECIAL_NOTES_PRESCRIPTION_MAX_LENGTH,true))
                {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }
                if(!ConfirmCheckBox.isSelected())
                {
                    ConfirmCheckBox.setTextFill(Paint.valueOf(FinalConstants.RED_COLOR));
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }

                if(!SpecialNotesTextArea.getText().isEmpty())
                    prescription.setSpecialNotes(SpecialNotesTextArea.getText());

                switchHBoxes.forEach(switchLabel -> switchLabel.setBackground(null));
                switchHBoxes.get(0).setBackground(FinalConstants.SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND);
                switchHBoxes.get(1).setBackground(FinalConstants.SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND);
                switchHBoxes.get(2).setBackground(FinalConstants.SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND);
                switchHBoxes.get(3).setBackground(FinalConstants.SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND);
                currentPaginationIndex = 3;
                uploadPrescriptionDatabaseRequest();
                return UploadPane;

        }

        return null;
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
        GenericPair<String,String> firstNameAndLastName =  splitInput(PatientNameInput.getText());
        firstName = firstNameAndLastName.getFirst();
        lastName = firstNameAndLastName.getSecond();

        patientRegister = new PatientRegister();

        attachProcessSensor(patientRegister,SyncAnimation,PatientLoadPane,parentLockFunction,browsePatientThreadPool);

        browsePatientThreadPool = Executors.newWorkStealingPool();
        browsePatientThreadPool.submit(() ->{
            patientList = patientRegister.find(firstName,lastName,true);
            Platform.runLater(() -> {
                PatientBrowseListView.getItems().clear();
                PatientBrowseListView.getItems().addAll(patientList);
            });
        });

    }


    /**
     * <p>
     *     Request data from database<br>
     *     Asking for all rows matching input based on name and license
     *     Later collecting data if medicine is not disabled by administration
     * </p>
     * @see MedicineRegister#find(String, String, boolean)
     */
    private void browseMedicineDatabaseRequest()
    {
        medicineRegister = new MedicineRegister();

        attachProcessSensor(medicineRegister,MedicineSyncAnimation,MedicineLoadPane,parentLockFunction,browseMedicineThreadPool);

        browseMedicineThreadPool = Executors.newWorkStealingPool();

        browseMedicineThreadPool.submit(() -> {
           medicineList = medicineRegister.find(MedicineNameInput.getText(),currentUser.getLicense(),false);
           Platform.runLater(() -> {
               MedicineBrowseListView.getItems().clear();
               MedicineBrowseListView.getItems().addAll(medicineList);
           });
        });

        currentStage = (Stage)InsertPrescriptionBasePane.getScene().getWindow();
    }

    /**
     * Method to upload prescription after collecting and validating data from user
     */
    private void uploadPrescriptionDatabaseRequest()
    {
        prescriptionRegister = new PrescriptionRegister();

        prescriptionRegister.addListener((text, progress) -> {
            if(progress > 0 && progress < 1)
            {
                UploadSyncAnimation.run();
                parentLockFunction.accept(true);
            }

            if(progress == 1)
            {
                UploadSyncAnimation.stop();
                parentLockFunction.accept(false);
                CustomPagination.setPageFactory(param ->  UploadPane);
                UploadSyncAnimation.setImage(new Image("Icons/x64/success.png"));
                LoadingLabel.setText("Done! Prescription is uploaded. You can now close this section");

            }

            if(progress < 0)
            {
                UploadSyncAnimation.stop();
                parentLockFunction.accept(false);
                UploadSyncAnimation.setImage(new Image("Icons/x64/error.png"));
                LoadingLabel.setText("Something went wrong. Try to upload later today");
            }

        });

        uploadPrescriptionThreadPool = Executors.newWorkStealingPool();

        uploadPrescriptionThreadPool.submit(() -> Platform.runLater(() -> {
            prescriptionRegister.insert(prescription);

        }));
    }
}
