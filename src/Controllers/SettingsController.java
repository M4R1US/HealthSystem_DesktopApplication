package Controllers;

import Actions.Validation;
import Animations2D.SynchronizationAnimation;
import AppUsers.*;
import SavedVariables.FinalConstants;
import CustomCache.ImageCacheRegister;
import Listeners.TextPropertyListener;
import Registers.*;
import Security.BCrypt;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <h2>Created by Marius Baltramaitis on 12/08/2017.</h2>
 *
 * <p>Controller of settings window</p>
 */
public class SettingsController implements Initializable {

    @FXML
    public Pagination Pagination;
    public BorderPane PasswordChangePane,AdditionalInfoChangePane,CacheRegisterPane,SettingsPaginationPane;
    public Label CacheImageCountLabel,TitleLabel,TransactionText,InvalidDetailsLabel;
    public Button ClearImageCacheButton,ChangeAdditionalInfoButton,BackButton,ChangePasswordButton;
    public TextArea AdditionalInfoTextArea;
    public SynchronizationAnimation SyncAnimation;
    public GridPane LoadingPane;
    public TextField NewPasswordField,RepeatPasswordField;

    private AbstractApplicationUser applicationUser;
    private ExecutorService updateDetailsPool;
    private AbstractRegister<? extends AbstractApplicationUser> register;
    private Image successTransactionImage,failTransactionImage,syncImage;



    /**
     * Initializing fxml nodes, attaching listeners and setting events
     * @param location path of fxml file
     * @param resources additional resources
     * @see Classes.CustomResourceBundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        successTransactionImage = new Image("/Icons/x64/success.png");
        failTransactionImage = new Image("/Icons/x64/error.png");
        syncImage = new Image("/Icons/x64/synchronization.png");

        Pagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
        Pagination.setPageFactory(this::paginationFactoryReflection);
        CacheImageCountLabel.setText(ImageCacheRegister.size()+"");

        ClearImageCacheButton.setOnMouseClicked(event -> {
            ImageCacheRegister.clear();
            CacheImageCountLabel.setText(ImageCacheRegister.size()+"");
        });

        BackButton.setOnMouseClicked(event -> {
            SettingsPaginationPane.setVisible(true);
            LoadingPane.setVisible(false);
            SyncAnimation.setImage(syncImage);
        });

        ChangePasswordButton.setOnMouseClicked(event -> {
            updatePassword();
        });

        applicationUser = (AbstractApplicationUser)resources.getObject("User");

        AdditionalInfoTextArea.setText(applicationUser.getAdditionalInfo());

        ChangeAdditionalInfoButton.setOnMouseClicked(event -> updateAdditionalInformation());

        AdditionalInfoTextArea.textProperty().addListener(new TextPropertyListener(AdditionalInfoTextArea,(String) -> Validation.validTextInput(AdditionalInfoTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true)));

    }

    /**
     * Method for pagination reflection
     * @param targetIndex targeted index
     * @return Node depending on given index, null if index is not valid
     */
    private Node paginationFactoryReflection(Integer targetIndex)
    {
        switch (targetIndex)
        {
            case 0:
                TitleLabel.setText("Password settings");
                PasswordChangePane.setVisible(true);
                return PasswordChangePane;

            case 1:
                TitleLabel.setText("Additional information settings");
                AdditionalInfoChangePane.setVisible(true);
                return AdditionalInfoChangePane;

            case 2:
                TitleLabel.setText("Image cache settings");
                CacheRegisterPane.setVisible(true);
                return CacheRegisterPane;
        }


        return null;
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
                SettingsPaginationPane.setVisible(false);
                LoadingPane.setVisible(true);
                BackButton.setVisible(false);
                SyncAnimation.run();
            }

            if(processValue >= 1)
            {
                SyncAnimation.stop();
                SyncAnimation.setImage(successTransactionImage);
                BackButton.setVisible(true);
                updateDetailsPool.shutdownNow();

            }

            if(processValue <= -1)
            {
                SyncAnimation.stop();
                SyncAnimation.setImage(failTransactionImage);
                BackButton.setVisible(true);
                updateDetailsPool.shutdownNow();
            }

        });
    }


    /**
     * Method to update users password
     */
    private void updatePassword()
    {
        if(NewPasswordField.getText().equals("") || RepeatPasswordField.getText().equals(""))
        {
            InvalidDetailsLabel.setVisible(true);
            InvalidDetailsLabel.setText("Please fill all fields!");
            return;
        }
        if((!NewPasswordField.getText().equals(RepeatPasswordField.getText())))
        {
            InvalidDetailsLabel.setVisible(true);
            InvalidDetailsLabel.setText("Passwords didn't mach!");
            java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }

        applicationUser.setEncryptedPassword(BCrypt.hashpw(NewPasswordField.getText(),applicationUser.getSalt()));
        updateUser();

    }

    /**
     * Detecting user type an sending database request to update users information
     */
    private void updateUser()
    {
        updateDetailsPool = Executors.newWorkStealingPool();

        if(applicationUser instanceof Administrator)
        {
            register = new AdministratorRegister();
            initializeConnectionSensor();
            updateDetailsPool.execute(() -> ((AdministratorRegister)register).update((Administrator)applicationUser));
        }

        if(applicationUser instanceof Doctor)
        {
            register = new DoctorRegister();
            initializeConnectionSensor();
            updateDetailsPool.execute(() -> ((DoctorRegister)register).update((Doctor) applicationUser));
        }

        if(applicationUser instanceof Patient)
        {
            register = new PatientRegister();
            initializeConnectionSensor();
            updateDetailsPool.execute(() -> ((PatientRegister)register).update((Patient) applicationUser));
        }

        if(applicationUser instanceof Pharmacy)
        {
            register = new PharmacyRegister();
            initializeConnectionSensor();
            updateDetailsPool.execute(() -> ((PharmacyRegister)register).update((Pharmacy) applicationUser));
        }
    }

    /**
     * Method to update users additional information
     */
    private void updateAdditionalInformation()
    {
        if(!Validation.validTextInput(AdditionalInfoTextArea.getText(),FinalConstants.ADDITIONAL_INFORMATION_MIN_LENGTH,FinalConstants.ADDITIONAL_INFORMATION_MAX_LENGTH,true))
        {
            java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }

        String additionalInfo = AdditionalInfoTextArea.getText().isEmpty() ? "No additional information" : AdditionalInfoTextArea.getText();
        applicationUser.setAdditionalInfo(additionalInfo);
        updateUser();

    }

}
