package Controllers.InsideControllers;

import AppUsers.*;
import CustomCache.ImageCache;
import CustomCache.ImageCacheRegister;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 28/08/2017.</h2>
 *
 * <p>Information controller is responsible for showing server information, user information inside any dashboard<br>
 *     This class is used in all dashboards
 * </p>
 */
public class InformationController extends InsideController {


    @FXML
    public BorderPane InfoPane;
    public Label AboutTitleLabel,ProtocolsLabel,ServerInformationLabel,IPV4Label,DeviceTunnelLabel,VirtualGsmWireLabel,ImageSenderLabel,ImageReceiverLabel,FirewallLabel,WelcomeLabel,LastUsedDeviceLabel,HostLabel,MiddleLabel;
    public Pagination CustomPagination;
    public GridPane InformationCenterGridPane;
    public BorderPane ServerInformationPane,UserInformationPane;
    public Circle AvatarCircle;

    private double width,height;
    private AbstractApplicationUser applicationUser;
    private Consumer<Boolean> lockFunction;
    private ImageCache imageCache;
    private Image userImage;


    /**
     *  {@inheritDoc}
     */
    @Override
    public void setWidth(double ProvidedParentWidth)
    {
        width = ProvidedParentWidth-100;
        InfoPane.setMaxWidth(width);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight)
    {
        height = ProvidedParentHeight - 160;
        InfoPane.setMaxHeight(height);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setParentLockFunction(Consumer<Boolean> lockFunction) {
        this.lockFunction = lockFunction;
    }

    /**
     * Initializes local variables from fxml file, also attaches events for corresponding nodes
     * @param location fxml path
     * @param resources CustomResourceBundle object with additional resources
     * @see Classes.CustomResourceBundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        super.contrastEffect(InfoPane);
        InfoPane.setOpacity(0.4);
        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());
        applicationUser = (AbstractApplicationUser) resources.getObject("User");
        paginationFactoryReflection(0);
        CustomPagination.setPageFactory(this::paginationFactoryReflection);

        HostLabel.setText(FinalConstants.HOST_DOMAIN_NAME);
        IPV4Label.setText(DynamicVariables.IPV4);
        DeviceTunnelLabel.setText(DynamicVariables.IPV4+":"+ FinalConstants.TCP_DEVICE_CONNECTION_PORT);
        VirtualGsmWireLabel.setText(DynamicVariables.IPV4+":"+ FinalConstants.SMS_INPUT_PORT);
        ImageSenderLabel.setText(DynamicVariables.IPV4+":"+ FinalConstants.TCP_IMAGE_REQUEST_PORT);
        ImageReceiverLabel.setText(DynamicVariables.IPV4+":"+ FinalConstants.TCP_IMAGE_DELIVER_PORT);
        FirewallLabel.setText(DynamicVariables.IPV4+":"+ FinalConstants.FIREWALL_PORT);

        InfoPane.widthProperty().addListener((observable, oldValue, newValue) -> AboutTitleLabel.setPrefWidth(newValue.doubleValue()));

        CustomPagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);

        ServerInformationPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            ProtocolsLabel.setPrefWidth(newValue.doubleValue());
            ServerInformationLabel.setPrefWidth(newValue.doubleValue());
        });

    }

    /**
     * Method for pagination reflection
     * @param targetIndex targeted index
     * @return Node depending on given index
     */
    private Node paginationFactoryReflection(Integer targetIndex)
    {
        switch (targetIndex)
        {
            case 0:
                InformationCenterGridPane.setVisible(true);
                return InformationCenterGridPane;

            case 1:
                if(applicationUser instanceof Doctor)
                {
                    imageCache = ImageCacheRegister.find(applicationUser.getID(),"Doctor");
                    WelcomeLabel.setText("Welcome Dr " + ((Doctor)applicationUser).getFirstName() + " " + ((Doctor)applicationUser).getLastName() + "!");
                    MiddleLabel.setText("We are happy having You! Prescriptions are important for our daily life.");
                }

                if(applicationUser instanceof Administrator)
                {
                    imageCache = ImageCacheRegister.find(applicationUser.getID(),"Admin");
                    WelcomeLabel.setText("Welcome " + ((Administrator)applicationUser).getFirstName() + " " + ((Administrator)applicationUser).getLastName() + "!");
                    MiddleLabel.setText("We are happy having You! Take care of our users");
                }
                if(applicationUser instanceof Patient)
                {
                    imageCache = ImageCacheRegister.find(applicationUser.getID(),"Patient");
                    WelcomeLabel.setText("Welcome " + ((Patient)applicationUser).getFirstName() + " " + ((Patient)applicationUser).getLastName() + "!");
                    MiddleLabel.setText("We are happy having You! Keep eye of medicine description before using it!");

                }

                LastUsedDeviceLabel.setText("Last login from : " + applicationUser.getLastUsedDevice());

                if(imageCache != null)
                    userImage = imageCache.getImage();

                if(userImage != null)
                {
                    AvatarCircle.setFill(new ImagePattern(userImage));
                    AvatarCircle.setVisible(true);
                }

                else {
                    AvatarCircle.setFill(new ImagePattern(new Image("/Icons/DefaultProfileIcons/user-shape.png")));
                    AvatarCircle.setStyle("-fx-stroke: null");
                    AvatarCircle.setVisible(true);
                }

                if(applicationUser instanceof Pharmacy)
                {
                    AvatarCircle.setStyle("-fx-stroke: null");
                    AvatarCircle.setVisible(false);
                    MiddleLabel.setText("Hello there.You are responsible for selling prescriptions!");

                }

                UserInformationPane.setVisible(true);
                return UserInformationPane;

            default:
                return null;
        }
    }
}
