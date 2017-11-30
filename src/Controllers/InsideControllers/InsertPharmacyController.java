package Controllers.InsideControllers;

import Actions.NodeModification;
import Actions.Validation;
import AppUsers.Pharmacy;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import CustomListCells.CountryCodeCell;
import Listeners.PhoneNumberPropertyListener;
import Listeners.TextPropertyListener;
import Registers.PharmacyRegister;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 05/08/2017.</h2>
 * <p>Controller responsible for insert-pharmacy section inside administrator dashboard</p>
 */

public class InsertPharmacyController extends InsideController {

    @FXML
    public BorderPane InsertPharmacyRootPane,PhoneNumberPane,ConfirmationPane,TerminalPane;
    public TextField NameField, AddressField,PhoneNumberField;
    public ComboBox<CountryCodeCell> CountryComboBox;
    public TextArea AdditionalInfoTextArea;
    public HBox PaginationTopHBox,LeftArrowHBox,RightArrowHBox;
    public TextFlow Terminal;
    public Label PreviewLabel,PharmacyDetailsTopLabel,PersonalInfoTitleLabel,TerminalTopLabel,ProgressLabel,SmsPaneTopLabel, PharmacyDetailsLabel, AddressLabel,LoginDetailsLabel, ConfirmationLabel;
    public Pagination CustomPagination;
    public Pane PharmacyDetailsPane;
    public CheckBox SmsDeliverCheckBox,ConfirmCheckBox;
    public GridPane PharmacyDetailsInputPane;
    public ScrollPane TerminalScrollPane;

    private Pharmacy pharmacy;
    private ExecutorService registerThreadPool;
    private PharmacyRegister pharmacyRegister;
    private NumberFormat decimalFormatter;
    private Set<Node> paginationTopIconsSet;
    private int currentPaginationIndex = 0;
    private boolean smsDelivering,locked,finishedSuccessfully = false;
    private Node currentPaginationNode;
    public CountryCodeCell NorwegianCell,LithuanianCell;
    private Consumer<Boolean> parentLockFunction;
    private double width,height;

    /**
     * Default constructor
     */
    public InsertPharmacyController()
    {
        pharmacy = new Pharmacy();
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
        InsertPharmacyRootPane.setMaxWidth(width);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight)
    {
        height = ProvidedParentHeight-160;
        InsertPharmacyRootPane.setMaxHeight(height);
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
        super.contrastEffect(InsertPharmacyRootPane);
        setDefaultBorders();
        paginationTopIconsSet = PaginationTopHBox.lookupAll(".PaginationIconHBoxes");
        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());
        currentPaginationNode = PharmacyDetailsPane;
        LeftArrowHBox.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(currentPaginationIndex-1));
        RightArrowHBox.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(currentPaginationIndex+1));
        InsertPharmacyRootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            PersonalInfoTitleLabel.setPrefWidth(newValue.doubleValue()-260);
            PharmacyDetailsTopLabel.setPrefWidth(newValue.doubleValue());
            SmsPaneTopLabel.setPrefWidth(newValue.doubleValue());
            PreviewLabel.setPrefWidth(newValue.doubleValue());
        });


        TerminalPane.heightProperty().addListener((observable, oldValue, newValue) -> TerminalScrollPane.setPrefHeight(newValue.doubleValue()));


        CustomPagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);

        CustomPagination.setPageFactory(this::paginationFactoryReflection);
        CustomPagination.setOnMouseClicked(event -> event.consume());


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
        NameField.textProperty().addListener(new TextPropertyListener(NameField,(String) -> Validation.validPharmacyName(NameField.getText(),FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false)));
        AddressField.textProperty().addListener(new TextPropertyListener(AddressField,(String) -> Validation.validTextInput(AddressField.getText(),FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false)));
        AdditionalInfoTextArea.textProperty().addListener(new TextPropertyListener(AdditionalInfoTextArea,(String) -> Validation.validTextInput(AdditionalInfoTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true)));
        PhoneNumberField.textProperty().addListener(new PhoneNumberPropertyListener(PhoneNumberField,8,8));



        InsertPharmacyRootPane.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue && registerThreadPool != null)
                registerThreadPool.shutdownNow();
        });


    }


    /**
     * Method responsible for uploading data to database after collecting and validating user input
     */
    private void sendInsertRequest()
    {
        pharmacyRegister = new PharmacyRegister();
        registerThreadPool = Executors.newWorkStealingPool();

        pharmacyRegister.addListener((String processText,Double processValue) -> {
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
            pharmacyRegister.insert(pharmacy,phoneNumber);
        });

    }

    /**
     * Method for overriding default styles of borders
     */
    private void setDefaultBorders()
    {
        NameField.setBorder(FinalConstants.BLUE_TEXTFIELD_DESIGNED_BORDER);
        AddressField.setBorder(FinalConstants.BLUE_TEXTFIELD_DESIGNED_BORDER);
        AdditionalInfoTextArea.setBorder(FinalConstants.BLUE_TEXTAREA_BORDER);
        PhoneNumberField.setBorder(FinalConstants.BLUE_TEXTFIELD_DESIGNED_BORDER);
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
                PharmacyDetailsPane.setVisible(true);
                currentPaginationIndex = 0;
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return PharmacyDetailsPane;

            case 1:
                if((!Validation.validPharmacyName(NameField.getText(),FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false) || (!Validation.validTextInput(AddressField.getText(),FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false)) || (!Validation.validTextInput(AdditionalInfoTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true))))
                {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }

                pharmacy.setName(NameField.getText());
                pharmacy.setAddress(AddressField.getText());
                pharmacy.setAdditionalInfo(AdditionalInfoTextArea.getText());
                PhoneNumberPane.setVisible(true);
                currentPaginationIndex = 1;
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return PhoneNumberPane;

            case 2:
                if(smsDelivering && !Validation.validInt(PhoneNumberField.getText(),8,8,false))
                {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return null;
                }
                ConfirmationPane.setVisible(true);
                PharmacyDetailsLabel.setText(pharmacy.getName());
                AddressLabel.setText(pharmacy.getAddress());
                String loginDeliveryText = (PhoneNumberField.getText() != null && !PhoneNumberField.getText().isEmpty() && smsDelivering) ? "Login details will be delivered to " + CountryComboBox.getSelectionModel().getSelectedItem().getCountryCode() + PhoneNumberField.getText().substring(0,PhoneNumberField.getText().length()-3) + "***" : "Login details will be show later on screen";
                LoginDetailsLabel.setText(loginDeliveryText);
                currentPaginationIndex = 2;
                NodeModification.setBorderToPaginationIcon(targetIndex,paginationTopIconsSet);
                return ConfirmationPane;


            case 3:
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

}
