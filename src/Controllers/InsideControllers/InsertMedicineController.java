package Controllers.InsideControllers;

import Actions.NodeModification;
import Actions.Validation;
import Animations2D.SynchronizationAnimation;
import Classes.ConsoleOutput;
import Classes.Medicine;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import Listeners.TextPropertyListener;
import Registers.MedicineRegister;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 10/08/2017.</h2>
 * <p>Controller responsible for insert-medicine section inside administrator dashboard</p>
 */
public class InsertMedicineController extends InsideController {

    @FXML
    public BorderPane InsertMedicineRootPane,MedicineDetailsPane,SynchronizationPane,TabletPane,CapsulePane,PowderPane,LotionPane,DropsPane,InjectionPane,MedicineDescriptionPane,ConfirmationPane;
    public ImageView B_PrescriptionsCheckImage,A_PrescriptionsCheckImage,MedicineTypeImage,TabletImageView,CapsuleImageView,PowderImageView,LotionImageView,DropsImageView,InjectionImageView;
    public HBox RightArrowHBox,LeftArrowHBox,B_Prescriptions_CheckHBox,A_Prescriptions_CheckHBox,PaginationTopHBox;
    public Label PreviewLabel,UsageLabel,MedicineDescriptionLabel,PersonalInfoTitleLabel,LicenseLabel,MedicineNameLabel,ConfirmationLabel,ProcessStatusLabel,ProcessBottomLabel;
    public Pagination CustomPagination;
    public TextField MedicineNameField;
    public TextArea DescriptionInfoTextArea,UsageTextArea,SideEffectsTextArea;
    public GridPane MedicineTypeNamePane,A_PrescriptionsPane,B_PrescriptionsPane;
    public CheckBox ConfirmCheckBox;
    public SynchronizationAnimation SyncAnimation;


    private Consumer<Boolean> parentLockFunction;
    private double width,height;
    private String selectedLicense = "A";
    private String medicineType = "Tablet";
    private int currentPaginationIndex = 0;
    private Node currentPaginationNode;
    private Set<Node> paginationTopIconsSet;
    private boolean locked,finishedSuccessfully = false;
    private Set<Node> medicineTypeTiles;
    private Medicine medicine;
    private MedicineRegister medicineRegister;
    private ExecutorService registerThreadPool;
    private Image successUpload,failedUpload,syncImage;

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setWidth(double ProvidedParentWidth)
    {
        width = ProvidedParentWidth -100;
        InsertMedicineRootPane.setMaxWidth(width);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight)
    {
        height = ProvidedParentHeight-160;
        InsertMedicineRootPane.setMaxHeight(height);
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
        super.contrastEffect(InsertMedicineRootPane);
        medicine = new Medicine();
        paginationTopIconsSet = PaginationTopHBox.lookupAll(".PaginationIconHBoxes");
        medicineTypeTiles = MedicineTypeNamePane.lookupAll(".MedicineTypeTiles");
        successUpload = new Image("/Icons/x64/success.png");
        failedUpload = new Image("/Icons/x64/error.png");
        syncImage = new Image("/Icons/x64/synchronization.png");
        medicineTypeTiles.forEach(tile ->{
            ((Region)tile).setBackground(FinalConstants.DEFAULT_MEDICINE_TYPE_TILE_BACKGROUND);
            tile.setOnMouseClicked(event -> medicineTypeReflection(tile));
        });
        selectMedicineLicense(0);
        medicineTypeReflection(TabletPane);

        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());

        currentPaginationNode = MedicineDetailsPane;

        LeftArrowHBox.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(currentPaginationIndex-1));
        RightArrowHBox.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(currentPaginationIndex+1));

        CustomPagination.setPageFactory(this::paginationFactoryReflection);
        CustomPagination.setOnMouseClicked(event -> event.consume());

        InsertMedicineRootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            PersonalInfoTitleLabel.setPrefWidth(newValue.doubleValue()-300);
            MedicineDescriptionLabel.setPrefWidth(newValue.doubleValue());
            UsageLabel.setPrefWidth(newValue.doubleValue());
            PreviewLabel.setPrefWidth(newValue.doubleValue());

        });

        // Text input validation
        MedicineNameField.textProperty().addListener(new TextPropertyListener(MedicineNameField,(String) -> Validation.validMedicineName(MedicineNameField.getText(),FinalConstants.MEDICINE_NAME_MIN_LENGTH,FinalConstants.MEDICINE_NAME_MAX_LENGTH,false)));
        UsageTextArea.textProperty().addListener(new TextPropertyListener(UsageTextArea,(String) -> Validation.validTextInput(UsageTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true)));
        SideEffectsTextArea.textProperty().addListener(new TextPropertyListener(SideEffectsTextArea,(String) -> Validation.validTextInput(SideEffectsTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true)));
        DescriptionInfoTextArea.textProperty().addListener(new TextPropertyListener(DescriptionInfoTextArea,(String) -> Validation.validTextInput(DescriptionInfoTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true)));

        // License selection

        A_PrescriptionsPane.setOnMouseClicked(event -> selectMedicineLicense(0));
        B_PrescriptionsPane.setOnMouseClicked(event -> selectMedicineLicense(1));

        CustomPagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
        setDefaultBorders();

        SynchronizationPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            ProcessStatusLabel.setPrefWidth(newValue.doubleValue());
            ProcessBottomLabel.setPrefWidth(newValue.doubleValue());
        });

    }

    /**
     * Method responsible for uploading data to database after collecting and validating user input
     */
    private void sendInsertRequest()
    {
        SyncAnimation.setImage(syncImage);
        medicineRegister = new MedicineRegister();
        registerThreadPool = Executors.newCachedThreadPool();

        medicineRegister.addListener((String processText,Double processValue) -> {
            Text text = new Text(processText + System.lineSeparator());
            text.setFont(FinalConstants.TERMINAL_FONT);
            text.setFill(FinalConstants.DEFAULT_TERMINAL_TEXT_COLOR);

            if(processValue < 1.0 && processValue >= 0)
            {
                SyncAnimation.run();
                ProcessStatusLabel.setText("Uploading data " + processValue*100 + "%");
                parentLockFunction.accept(true);
                CustomPagination.setDisable(true);
                locked = true;

            }

            if(processValue >= 1.0){
                SyncAnimation.stop();
                ProcessStatusLabel.setText("Transaction finished successfully");
                ProcessBottomLabel.setText("You may now close this section.");
                SyncAnimation.setImage(successUpload);
                parentLockFunction.accept(false);
                CustomPagination.setDisable(false);
                registerThreadPool.shutdownNow();
                locked = false;
                finishedSuccessfully = true;
            }

            if(processValue < 0)
            {
                SyncAnimation.stop();
                ProcessStatusLabel.setText("Error on uploading data!");
                ProcessBottomLabel.setText("Transaction finished unsuccessfully, contact with Marius.");
                SyncAnimation.setImage(failedUpload);
                parentLockFunction.accept(false);
                CustomPagination.setDisable(false);
                registerThreadPool.shutdownNow();
                locked = false;
            }
        });

        registerThreadPool.submit(() ->medicineRegister.insert(medicine));

    }

    /**
     * Method for overriding default styles of borders
     */
    private void setDefaultBorders()
    {
        MedicineNameField.setBorder(FinalConstants.BLUE_TEXTFIELD_DESIGNED_BORDER);
        DescriptionInfoTextArea.setBorder(FinalConstants.BLUE_TEXTAREA_BORDER);
        SideEffectsTextArea.setBorder(FinalConstants.BLUE_TEXTAREA_BORDER);
        UsageTextArea.setBorder(FinalConstants.BLUE_TEXTAREA_BORDER);

    }

    /**
     * Registering license based on index
     * @param index 0 for license A, 1 for license B
     */
    private void selectMedicineLicense(int index)
    {

        if( index > 1 || index < 0)
            return;

        if(index == 0)
        {
            B_PrescriptionsCheckImage.setVisible(false);
            A_PrescriptionsCheckImage.setVisible(true);
            selectedLicense = "A";
            B_Prescriptions_CheckHBox.setBackground(FinalConstants.defaultPrescriptionHBoxBackground);
            A_Prescriptions_CheckHBox.setBackground(FinalConstants.selectedPrescriptionHBoxBackground);
        }

        if(index == 1)
        {
            B_PrescriptionsCheckImage.setVisible(true);
            A_PrescriptionsCheckImage.setVisible(false);
            selectedLicense = "B";
            B_Prescriptions_CheckHBox.setBackground(FinalConstants.selectedPrescriptionHBoxBackground);
            A_Prescriptions_CheckHBox.setBackground(FinalConstants.defaultPrescriptionHBoxBackground);
        }
    }


    /**
     * Method for pagination reflection
     * @param targetIndex targeted index
     * @return Node depending on given index, null if index is not valid
     */
    private Node paginationFactoryReflection(Integer targetIndex)
    {
        if(targetIndex > currentPaginationIndex+1 || targetIndex < currentPaginationIndex-1 || locked || finishedSuccessfully)
            return null;

        switch (targetIndex)
        {
            case 0:

                MedicineDetailsPane.setVisible(true);
                currentPaginationIndex = 0;
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return MedicineDetailsPane;

            case 1:

                if((!Validation.validMedicineName(MedicineNameField.getText(),FinalConstants.MEDICINE_NAME_MIN_LENGTH,FinalConstants.MEDICINE_NAME_MAX_LENGTH,false) || (!Validation.validTextInput(DescriptionInfoTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true))))
                {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }
                medicine.setName(MedicineNameField.getText());
                String description = (DescriptionInfoTextArea.getText().isEmpty() ? "Undefined" : DescriptionInfoTextArea.getText());
                medicine.setDescription(description);
                MedicineTypeNamePane.setVisible(true);
                currentPaginationIndex = 1;
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return MedicineTypeNamePane;

            case 2:

                MedicineDescriptionPane.setVisible(true);
                currentPaginationIndex = 2;
                medicine.setLicense(selectedLicense);
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return MedicineDescriptionPane;

            case 3:

                if((!Validation.validTextInput(UsageTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true) || (!Validation.validTextInput(SideEffectsTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true))))
                {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }

                medicine.setType(medicineType);
                String usage = (UsageTextArea.getText().isEmpty() ? "Undefined" : UsageTextArea.getText());
                String sideEffects = (SideEffectsTextArea.getText().isEmpty() ? "Undefined" : SideEffectsTextArea.getText());
                medicine.setUsage(usage);
                medicine.setSideEffect(sideEffects);
                ConfirmationPane.setVisible(true);
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                MedicineNameLabel.setText(medicine.getType() + " " +medicine.getName());
                String licenseText = medicine.getLicense().equalsIgnoreCase("A" ) ? "License for strong prescriptions [A]" : "License for weak prescriptions [B]";
                LicenseLabel.setText(licenseText);
                currentPaginationIndex = 3;
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return ConfirmationPane;

            case 4:

                if(!ConfirmCheckBox.isSelected())
                {
                    ConfirmationLabel.setTextFill(Paint.valueOf(FinalConstants.RED_COLOR));
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }

                SynchronizationPane.setVisible(true);

                currentPaginationIndex= 4;
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                sendInsertRequest();
                return SynchronizationPane;

            default:
                return currentPaginationNode;
        }

    }

    /**
     * Method to change selected medicine type background so that difference between selected and unselected boxes will be visible
     * @param selector selected tile node
     */
    private void medicineTypeReflection(Node selector)
    {
        medicineTypeTiles.forEach((tile) -> ((Region)tile).setBorder(null));
        medicineTypeTiles.forEach(tile -> ((Region)tile).setBackground(FinalConstants.DEFAULT_MEDICINE_TYPE_TILE_BACKGROUND));
        switch (selector.getId())
        {
            case "TabletPane":
                medicineType = "Tablet";
                TabletPane.setBackground(FinalConstants.SELECTED_MEDICINE_TYPE_TILE_BACKGROUND);
                TabletPane.setBorder(FinalConstants.SELECTED_MEDICINE_TILE_BORDER);
                MedicineTypeImage.setImage(TabletImageView.getImage());
                break;

            case "CapsulePane":
                medicineType = "Capsule";
                CapsulePane.setBackground(FinalConstants.SELECTED_MEDICINE_TYPE_TILE_BACKGROUND);
                CapsulePane.setBorder(FinalConstants.SELECTED_MEDICINE_TILE_BORDER);
                MedicineTypeImage.setImage(CapsuleImageView.getImage());
                break;

            case "PowderPane":
                medicineType = "Powder";
                PowderPane.setBackground(FinalConstants.SELECTED_MEDICINE_TYPE_TILE_BACKGROUND);
                PowderPane.setBorder(FinalConstants.SELECTED_MEDICINE_TILE_BORDER);
                MedicineTypeImage.setImage(PowderImageView.getImage());
                break;

            case "LotionPane":
                medicineType = "Lotion";
                LotionPane.setBackground(FinalConstants.SELECTED_MEDICINE_TYPE_TILE_BACKGROUND);
                LotionPane.setBorder(FinalConstants.SELECTED_MEDICINE_TILE_BORDER);
                MedicineTypeImage.setImage(LotionImageView.getImage());
                break;

            case "DropsPane":
                medicineType = "Drops";
                DropsPane.setBackground(FinalConstants.SELECTED_MEDICINE_TYPE_TILE_BACKGROUND);
                DropsPane.setBorder(FinalConstants.SELECTED_MEDICINE_TILE_BORDER);
                MedicineTypeImage.setImage(DropsImageView.getImage());
                break;

            case "InjectionPane":
                medicineType = "Injection";
                InjectionPane.setBackground(FinalConstants.SELECTED_MEDICINE_TYPE_TILE_BACKGROUND);
                InjectionPane.setBorder(FinalConstants.SELECTED_MEDICINE_TILE_BORDER);
                MedicineTypeImage.setImage(InjectionImageView.getImage());
                break;

            default:
                medicineType = "Unknown";
                ConsoleOutput.print("Medicine type " + medicineType);
                break;


        }

    }

}
