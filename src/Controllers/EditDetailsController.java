package Controllers;

import Actions.Validation;
import Animations2D.SynchronizationAnimation;
import AppUsers.*;
import SavedVariables.FinalConstants;
import CustomCache.ImageCache;
import CustomCache.ImageCacheRegister;
import Listeners.TextPropertyListener;
import NetworkObjects.ImageObject;
import NetworkThreads.SubmitImage;
import Registers.AbstractRegister;
import Registers.DoctorRegister;
import Registers.PatientRegister;
import Registers.PharmacyRegister;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <h2>Created by Marius Baltramaitis on 18/08/2017.</h2>
 * <p>Edit details controller is used to edit details of application users. This controller is attached to edit window which is only available for administrators</p>
 */
public class EditDetailsController implements Initializable {

    @FXML
    public Pagination Pagination;
    public BorderPane DetailsPane,AdditionalInfoChangePane,AvatarPane,EditDetailsPaginationPane;
    public TextField FirstField, SecondField;
    public Label TopLabel,AvatarBottomLabel,TransactionText;
    public TextArea AdditionalInfoTextArea;
    public Circle AvatarCircle;
    public Button DisablePictureButton,ChangeAdditionalInfoButton,BackButton,ChangeDetailsButton,DisableButton,ResetPasswordButton;
    public SynchronizationAnimation SyncAnimation;
    public GridPane EditLoadPane;

    private AbstractApplicationUser applicationUser;
    private String userType = "Unknown";
    private ImageCache imageCache;
    private Image userAvatarImage;
    private Function<String,Boolean> firstNameFieldTextInputValidationFunction,lastNameFieldTextInputValidationFunction,addressFieldTextInputValidationFunction,additionalInformationValidationFunction,pharmacyNameFieldTextInputValidationFunction;
    private AbstractRegister<? extends AbstractApplicationUser> register;
    private ExecutorService changeDetailsThreadPool;
    private Image successTransactionImage,failTransactionImage,syncImage;
    private AbstractApplicationUser copy;
    private Consumer<AbstractApplicationUser> onEditFunction;


    /**
     * Initializing fxml nodes, attaching listeners and setting events
     * @param location path of fxml file
     * @param resources additional resources
     * @see Classes.CustomResourceBundle
     * @see Actions.WindowLauncher#launchEditWindow(AbstractApplicationUser, Consumer)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {

        successTransactionImage = new Image("/Icons/x64/success.png");
        failTransactionImage = new Image("/Icons/x64/error.png");
        syncImage = new Image("/Icons/x64/synchronization.png");

        Pagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
        Pagination.setCurrentPageIndex(0);
        Pagination.setPageFactory(this::paginationFactory);
        applicationUser = ((AbstractApplicationUser) resources.getObject("User"));
        onEditFunction = ((Consumer<AbstractApplicationUser>)(resources.getObject("editFunction")));
        additionalInformationValidationFunction = (String) -> Validation.validTextInput(AdditionalInfoTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true);
        firstNameFieldTextInputValidationFunction = (String) -> Validation.validLegalNameChars(FirstField.getText(), FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false);
        lastNameFieldTextInputValidationFunction = (String) -> Validation.validLegalNameChars(SecondField.getText(), FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false);
        pharmacyNameFieldTextInputValidationFunction = (String) -> Validation.validPharmacyName(FirstField.getText(),FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false);
        addressFieldTextInputValidationFunction = (String) -> Validation.validTextInput(SecondField.getText(),FinalConstants.NAME_MIN_LENGTH,FinalConstants.NAME_MAX_LENGTH,false);
        DisablePictureButton.setOnMouseClicked(event -> disableImage());

        ChangeAdditionalInfoButton.setOnMouseClicked(event -> {
            if(!additionalInformationValidationFunction.apply(AdditionalInfoTextArea.getText()))
            {
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            }
            copy = applicationUser;
            copy.setAdditionalInfo(AdditionalInfoTextArea.getText());
            submitChanges(copy);
        });

        ChangeDetailsButton.setOnMouseClicked(event -> changeNames());

        DisableButton.setOnMouseClicked(event -> {
            int availability = (applicationUser.getAvailable() == 1) ? 0 : 1;
            copy = applicationUser;
            copy.setAvailable(availability);
            submitChanges(copy);
        });


        FirstField.textProperty().addListener(new TextPropertyListener(FirstField, firstNameFieldTextInputValidationFunction));
        SecondField.textProperty().addListener(new TextPropertyListener(SecondField, lastNameFieldTextInputValidationFunction));
        AdditionalInfoTextArea.textProperty().addListener(new TextPropertyListener(AdditionalInfoTextArea,additionalInformationValidationFunction));
        BackButton.setOnMouseClicked(event -> {
            EditDetailsPaginationPane.setVisible(true);
            EditLoadPane.setVisible(false);
            SyncAnimation.setImage(syncImage);
        });

        EditLoadPane.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue)
               switchButtonText();
        });

        ResetPasswordButton.setOnMouseClicked(event -> {
            copy = applicationUser;
            copy.setEncryptedPassword(copy.getPrimaryPassword());
            submitChanges(copy);
        });

        postInitialize(applicationUser);

    }

    /**
     * Method for switching text and border of disable/enable buttons
     */
    private void switchButtonText()
    {
        switch (applicationUser.getAvailable())
        {
            case 0 :
                DisableButton.setText("Enable Account");
                DisableButton.setBorder(FinalConstants.DISABLE_BUTTON_GREEN_BORDER);
                break;

            case 1:
                DisableButton.setText("Disable Account");
                DisableButton.setBorder(FinalConstants.DISABLE_BUTTON_RED_BORDER);
                break;
        }
    }


    /**
     * Method for pagination reflection
     * @param targetedIndex targeted index
     * @return Node depending on given index, null if index is not valid
     */
    private Node paginationFactory(Integer targetedIndex)
    {
        switch (targetedIndex)
        {

            case 0:
                DetailsPane.setVisible(true);
                return DetailsPane;

            case 1:
                AdditionalInfoChangePane.setVisible(true);
                return AdditionalInfoChangePane;

            case 2:
                if(applicationUser instanceof Pharmacy)
                    return null;

                AvatarPane.setVisible(true);
                return AvatarPane;

            default:
                return null;
        }
    }

    /**
     * Asking ImageCacheRegister for user image
     * @param abstractApplicationUser user object
     * @see ImageCacheRegister
     * @see ImageCache
     * @see AbstractApplicationUser
     */
    private void detectImage(AbstractApplicationUser abstractApplicationUser)
    {

        if(abstractApplicationUser instanceof Doctor)
            userType = "Doctor";
        if(abstractApplicationUser instanceof Patient)
            userType = "Patient";

        imageCache = ImageCacheRegister.find(abstractApplicationUser.getID(),userType);
        userAvatarImage= imageCache.getImage();
        if((imageCache != null) && (userAvatarImage != null))
            AvatarCircle.setFill(new ImagePattern(userAvatarImage));
        else
        {
            AvatarCircle.setFill(new ImagePattern(new Image("/Icons/AdditionalImages/question-mark.png")));
            AvatarCircle.setStyle("-fx-stroke: null;");
        }

    }


    /**
     * Method to initialize connection sensor in order to follow database connection status
     */
    private void initializeConnectionSensor()
    {
        if(register == null)
            return;

        register.addListener((String text,Double processValue) -> {
            TransactionText.setText(text);

            if(processValue < 1 && processValue > 0)
            {
                EditDetailsPaginationPane.setVisible(false);
                EditLoadPane.setVisible(true);
                BackButton.setVisible(false);
                SyncAnimation.run();
            }

            if(processValue >= 1)
            {
                SyncAnimation.stop();
                SyncAnimation.setImage(successTransactionImage);
                BackButton.setVisible(true);
                changeDetailsThreadPool.shutdownNow();
                applicationUser = copy;
                postInitialize(applicationUser);
                if(onEditFunction != null)
                    onEditFunction.accept(copy);
            }

            if(processValue <= -1)
            {
                SyncAnimation.stop();
                SyncAnimation.setImage(failTransactionImage);
                BackButton.setVisible(true);
                changeDetailsThreadPool.shutdownNow();
            }


        });
    }


    /**
     * Database request to change user information
     * @param abstractApplicationUser user object with data to upload to database
     * @see AbstractApplicationUser
     */
    private void submitChanges(AbstractApplicationUser abstractApplicationUser)
    {
        changeDetailsThreadPool = Executors.newCachedThreadPool();

        if(applicationUser instanceof Doctor)
        {
            register = new DoctorRegister();
            initializeConnectionSensor();
            changeDetailsThreadPool.submit(() -> (((DoctorRegister)register).update(((Doctor)abstractApplicationUser))));
        }

        if(applicationUser instanceof Pharmacy)
        {
            register = new PharmacyRegister();
            initializeConnectionSensor();
            changeDetailsThreadPool.submit(() -> (((PharmacyRegister)register).update(((Pharmacy)abstractApplicationUser))));
        }

        if(applicationUser instanceof Patient)
        {
            register = new PatientRegister();
            initializeConnectionSensor();
            changeDetailsThreadPool.submit(() -> (((PatientRegister)register).update(((Patient)abstractApplicationUser))));
        }

    }


    /**
     * Method to change application users name or adrdess or both
     */
    private void changeNames()
    {

        AbstractApplicationUser copy = applicationUser;
        changeDetailsThreadPool = Executors.newCachedThreadPool();


        if(applicationUser instanceof Doctor)
        {
            if(!firstNameFieldTextInputValidationFunction.apply(FirstField.getText()) || !lastNameFieldTextInputValidationFunction.apply(SecondField.getText()))
            {
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            }
            register = new DoctorRegister();
            initializeConnectionSensor();
            ((Doctor) copy).setFirstName(FirstField.getText());
            ((Doctor) copy).setLastName(SecondField.getText());
            changeDetailsThreadPool.submit(() -> ((DoctorRegister)register).update(((Doctor)copy)));
        }

        if(applicationUser instanceof Pharmacy)
        {
            if(!pharmacyNameFieldTextInputValidationFunction.apply(FirstField.getText()) || !addressFieldTextInputValidationFunction.apply(SecondField.getText()))
            {
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            }
            register = new PharmacyRegister();
            initializeConnectionSensor();
            ((Pharmacy) copy).setName(FirstField.getText());
            ((Pharmacy) copy).setAddress(SecondField.getText());
            changeDetailsThreadPool.submit(() -> ((PharmacyRegister)register).update(((Pharmacy) copy)));
        }

        if(applicationUser instanceof Patient)
        {
            if(!firstNameFieldTextInputValidationFunction.apply(FirstField.getText()) || !lastNameFieldTextInputValidationFunction.apply(SecondField.getText()))
            {
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            }

            register = new PatientRegister();
            initializeConnectionSensor();
            ((Patient) copy).setFirstName(FirstField.getText());
            ((Patient) copy).setLastName(SecondField.getText());
            changeDetailsThreadPool.submit(() -> ((PatientRegister)register).update(((Patient) copy)));
        }
    }

    /**
     * Server request method to disable image
     */
    private void disableImage()
    {
        if(userAvatarImage == null)
            return;

        ImageObject disableImageObject = new ImageObject(applicationUser.getID(),userType,((Person)applicationUser).getFirstName() + " " + ((Person)applicationUser).getFirstName(),"block image request from administration");
        SubmitImage disableFunction = new SubmitImage(disableImageObject,new Image("/Icons/AdditionalImages/disabledImg.png"));
        disableFunction.run();
        AvatarCircle.setFill(new ImagePattern(new Image("/Icons/AdditionalImages/disabledImg.png")));
    }

    /**
     * Initializes application user object and sets data to labels and other textInputs
     * @param abstractApplicationUser object with data to initialize
     */
    public void postInitialize(AbstractApplicationUser abstractApplicationUser)
    {
        AdditionalInfoTextArea.setText(abstractApplicationUser.getAdditionalInfo());

        if(abstractApplicationUser instanceof Person)
        {
            String firstName = ((Person) abstractApplicationUser).getFirstName();
            String lastName = ((Person) abstractApplicationUser).getLastName();
            FirstField.setText(firstName);
            SecondField.setText(lastName);
            TopLabel.setText(firstName + " " + lastName);
            detectImage(abstractApplicationUser);
            AvatarBottomLabel.setText(firstName + " " + lastName);

        }

        if(abstractApplicationUser instanceof Pharmacy)
        {

            String name = ((Pharmacy) abstractApplicationUser).getName();
            String address = ((Pharmacy) abstractApplicationUser).getAddress();
            FirstField.textProperty().addListener(new TextPropertyListener(FirstField, pharmacyNameFieldTextInputValidationFunction));
            SecondField.textProperty().addListener(new TextPropertyListener(SecondField, addressFieldTextInputValidationFunction));
            FirstField.setText(name);
            SecondField.setText(address);
            TopLabel.setText(name);
            AvatarBottomLabel.setText(name);
            Pagination.setMaxPageIndicatorCount(2);
        }

        copy = abstractApplicationUser;

        switchButtonText();

    }
}
