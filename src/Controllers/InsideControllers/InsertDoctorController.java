package Controllers.InsideControllers;

import Actions.NodeModification;
import Actions.Validation;
import AppUsers.Doctor;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import CustomListCells.CountryCodeCell;
import Listeners.PhoneNumberPropertyListener;
import Listeners.TextPropertyListener;
import Registers.DoctorRegister;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 24-Dec-16.</h2>
 * <p>Controller responsible for insert-doctor section inside administrator dashboard</p>

 */
public class InsertDoctorController extends InsideController {


    @FXML
    public BorderPane InsertDoctorRootPane,DoctorTypeListPane,PhoneNumberPane,ConfirmationPane,TerminalPane;
    public VBox MaleGenderVBox,FemaleGenderVBox;
    public TextField FirstNameField, LastNameField,PhoneNumberField;
    public ComboBox<CountryCodeCell> CountryComboBox;
    public TextArea AdditionalInfoTextArea;
    public HBox LeftArrowHBox,RightArrowHBox,PaginationTopHBox,A_Prescriptions_CheckHBox,B_Prescriptions_CheckHBox,PrescriptionChildrenHBox,TerminalPaneTopHBox;
    public ImageView LeftIcon,RightIcon,A_PrescriptionsCheckImage,B_PrescriptionsCheckImage;
    public TextFlow Terminal;
    public Label PersonalInfoTitleLabel,ReviewLabel, ListTitleLabel,ProgressLabel,PersonalDetailsTopLabel,SmsPaneTopLabel,PersonalDetailsLabel,GenderLabel,LicenseLabel,LoginDetailsLabel, ConfirmationLabel;
    public Pagination CustomPagination;
    public Pane DoctorPersonalDetailsPane;
    public ListView<String> BrowseListView;
    public CheckBox SmsDeliverCheckBox,ConfirmCheckBox;
    public GridPane PersonalDetailsInputPane,A_PrescriptionsPane,B_PrescriptionsPane;
    public ScrollPane TerminalScrollPane;

    private Doctor doctor;
    private ExecutorService registerThreadPool;
    private DoctorRegister doctorRegister;
    private NumberFormat decimalFormatter;
    private Set<Node> paginationTopIconsSet;
    private int currentPaginationIndex = 0;
    private String selectedGender = "Male";
    private String selectedLicense = "A";
    private boolean smsDelivering,locked,finishedSuccessfully = false;
    private Node currentPaginationNode;
    public CountryCodeCell NorwegianCell,LithuanianCell;
    private Consumer<Boolean> parentLockFunction;
    private double width,height;

    /**
     * Default constructor
     */
    public InsertDoctorController()
    {
        doctor = new Doctor();
        decimalFormatter = new DecimalFormat("#0.0");
        NorwegianCell = new CountryCodeCell("Icons/x16/NO.png","+47","[NO +47]");
        LithuanianCell = new CountryCodeCell("Icons/x16/LT.png","+370","[LT +370]");
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setWidth(double ProvidedParentWidth)
    {
        width = ProvidedParentWidth -100;
        InsertDoctorRootPane.setMaxWidth(width);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight)
    {
        height = ProvidedParentHeight-160;
        InsertDoctorRootPane.setMaxHeight(height);
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
        super.contrastEffect(InsertDoctorRootPane);
        selectPrescriptionLicense(0);
        paginationTopIconsSet = PaginationTopHBox.lookupAll(".PaginationIconHBoxes");
        MaleGenderVBox.setBorder(FinalConstants.SELECTED_GENDER_BORDER);
        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());
        currentPaginationNode = DoctorPersonalDetailsPane;

        LeftArrowHBox.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(currentPaginationIndex-1));
        RightArrowHBox.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(currentPaginationIndex+1));
        InsertDoctorRootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            PersonalInfoTitleLabel.setPrefWidth(newValue.doubleValue()-300);
            PersonalDetailsTopLabel.setPrefWidth(newValue.doubleValue());
            ListTitleLabel.setPrefWidth(newValue.doubleValue());
            SmsPaneTopLabel.setPrefWidth(newValue.doubleValue());
            ReviewLabel.setPrefWidth(newValue.doubleValue());
        });



        DoctorTypeListPane.heightProperty().addListener((observable, oldValue, newValue) -> BrowseListView.setPrefHeight(newValue.doubleValue()));

        setDefaultBorders();

        TerminalPane.heightProperty().addListener((observable, oldValue, newValue) -> TerminalScrollPane.setPrefHeight(newValue.doubleValue()));

        MaleGenderVBox.setOnMouseClicked(event -> {
            FemaleGenderVBox.setBorder(Border.EMPTY);
            MaleGenderVBox.setBorder(FinalConstants.SELECTED_GENDER_BORDER);
            selectedGender = "Male";
        });

        FemaleGenderVBox.setOnMouseClicked(event -> {
            MaleGenderVBox.setBorder(Border.EMPTY);
            FemaleGenderVBox.setBorder(FinalConstants.SELECTED_GENDER_BORDER);
            selectedGender = "Female";
        });

        CustomPagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);

        CustomPagination.setPageFactory(this::paginationFactoryReflection);
        CustomPagination.setOnMouseClicked(event -> event.consume());

        A_PrescriptionsPane.setOnMouseClicked(event -> selectPrescriptionLicense(0));
        B_PrescriptionsPane.setOnMouseClicked(event -> selectPrescriptionLicense(1));

        BrowseListView.getSelectionModel().select(0);

        BrowseListView.selectionModelProperty().addListener(observable -> CustomPagination.setCurrentPageIndex(2));


        NodeModification.initializePhoneNumberComboBox(CountryComboBox,NorwegianCell,LithuanianCell);

        SmsDeliverCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue)
            {
                PhoneNumberField.setDisable(false);
                CountryComboBox.setDisable(false);
                smsDelivering = true;
            }
            else {
                PhoneNumberField.setDisable(true);
                CountryComboBox.setDisable(true);
                smsDelivering = false;
            }

        });


        //Text input validation
        FirstNameField.textProperty().addListener(new TextPropertyListener(FirstNameField,(String) -> Validation.validLegalNameChars(FirstNameField.getText(),FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false)));
        LastNameField.textProperty().addListener(new TextPropertyListener(LastNameField,(String) -> Validation.validLegalNameChars(LastNameField.getText(),FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false)));
        AdditionalInfoTextArea.textProperty().addListener(new TextPropertyListener(AdditionalInfoTextArea,(String) -> Validation.validTextInput(AdditionalInfoTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true)));
        PhoneNumberField.textProperty().addListener(new PhoneNumberPropertyListener(PhoneNumberField,8,8));



        InsertDoctorRootPane.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue && registerThreadPool != null)
                registerThreadPool.shutdownNow();
        });


    }

    /**
     * Method for overriding default styles of borders
     */
    private void setDefaultBorders()
    {
        FirstNameField.setBorder(FinalConstants.BLUE_TEXTFIELD_DESIGNED_BORDER);
        LastNameField.setBorder(FinalConstants.BLUE_TEXTFIELD_DESIGNED_BORDER);
        AdditionalInfoTextArea.setBorder(FinalConstants.BLUE_TEXTAREA_BORDER);
        PhoneNumberField.setBorder(FinalConstants.BLUE_TEXTFIELD_DESIGNED_BORDER);
    }


    /**
     * Method responsible for uploading data to database after collecting and validating user input
     */
    private void sendInsertRequest()
    {
        doctorRegister = new DoctorRegister();
        registerThreadPool = Executors.newWorkStealingPool();

        doctorRegister.addListener((String processText,Double processValue) -> {
            Text text = new Text(processText + System.lineSeparator());
            text.setFont(FinalConstants.TERMINAL_FONT);
            text.setFill(FinalConstants.DEFAULT_TERMINAL_TEXT_COLOR);
            Terminal.getChildren().add(text);
            ProgressLabel.setText(decimalFormatter.format(processValue.doubleValue()*100) + " %");

            if(processValue < 1.0)
            {
                parentLockFunction.accept(true);
                CustomPagination.setDisable(true);
                locked = true;

            }

            if(processValue >= 1.0){
                parentLockFunction.accept(false);
                CustomPagination.setDisable(false);
                registerThreadPool.shutdownNow();
                locked = false;
                finishedSuccessfully = true;
                Text finishedText = new Text("You can now close this section");
                finishedText.setFont(FinalConstants.TERMINAL_FONT);
                finishedText.setFill(FinalConstants.DEFAULT_TERMINAL_TEXT_COLOR);
                Terminal.getChildren().add(finishedText);
            }
        });

        registerThreadPool.submit(() ->{
            String phoneNumber = (smsDelivering) ? CountryComboBox.getSelectionModel().getSelectedItem().getCountryCodeDisplayText() + PhoneNumberField.getText() : null;
            doctorRegister.insert(doctor,phoneNumber);
        });

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

                DoctorPersonalDetailsPane.setVisible(true);
                currentPaginationIndex = 0;
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return DoctorPersonalDetailsPane;

            case 1:

                if((!Validation.validLegalNameChars(FirstNameField.getText(),FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false) || (!Validation.validLegalNameChars(LastNameField.getText(),FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false)) || (!Validation.validTextInput(AdditionalInfoTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true))))
                {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }


                doctor.setFirstName(FirstNameField.getText());
                doctor.setLastName(LastNameField.getText());
                doctor.setSex(selectedGender);
                doctor.setAdditionalInfo(AdditionalInfoTextArea.getText());
                DoctorTypeListPane.setVisible(true);
                currentPaginationIndex = 1;
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return DoctorTypeListPane;

            case 2:

                doctor.setTitle(BrowseListView.getSelectionModel().getSelectedItem());
                doctor.setLicense(selectedLicense);
                PhoneNumberPane.setVisible(true);
                currentPaginationIndex = 2;
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return PhoneNumberPane;

            case 3:

                if(smsDelivering && !Validation.validInt(PhoneNumberField.getText(),8,8,false))
                {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }

                ConfirmationPane.setVisible(true);
                PersonalDetailsLabel.setText(doctor.getTitle() + ", " + doctor.getFirstName() + " " + doctor.getLastName());
                GenderLabel.setText(doctor.getSex());
                String licenseText = (doctor.getLicense().equalsIgnoreCase("A")) ? "[A] for strong prescriptions" : "[B] for weaker prescriptions";
                LicenseLabel.setText(licenseText);
                String loginDeliveryText = (PhoneNumberField.getText() != null && !PhoneNumberField.getText().isEmpty() && smsDelivering) ? "Login details will be delivered to " + CountryComboBox.getSelectionModel().getSelectedItem().getCountryCode() + PhoneNumberField.getText().substring(0,PhoneNumberField.getText().length()-3) + "***" : "Login details will be show later on screen";
                LoginDetailsLabel.setText(loginDeliveryText);
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

                TerminalPane.setVisible(true);
                currentPaginationIndex= 4;
                Terminal.getChildren().clear();
                sendInsertRequest();
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return TerminalPane;

            default:
                return currentPaginationNode;
        }

    }

    /**
     * Registering license based on index
     * @param index 0 for license A, 1 for license B
     */
    private void selectPrescriptionLicense(int index)
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

}
