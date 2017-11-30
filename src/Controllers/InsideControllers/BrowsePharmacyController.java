package Controllers.InsideControllers;

import Actions.WindowLauncher;
import Animations2D.SynchronizationAnimation;
import AppUsers.AbstractApplicationUser;
import AppUsers.Pharmacy;
import Interfaces.BrowsePaneAdditionalImplementation;
import Interfaces.DatabaseSensorAttachable;
import Registers.PharmacyRegister;
import SavedVariables.DynamicVariables;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 03/05/2017.</h2>
 *
 * <p>Inside controller in administrator dashboard responsible for browsing through browse-pharmacy section</p>
 */
public class BrowsePharmacyController extends InsideController implements BrowsePaneAdditionalImplementation, DatabaseSensorAttachable {

    @FXML
    public BorderPane BrowsePharmacyRootPane,LoadingPane;
    public TextField NameInput;
    public HBox BrowseTopPane;
    public VBox InfoVBox;
    public SynchronizationAnimation SyncAnimation;
    public BorderPane PreviewPane;
    public GridPane BrowseCenterGridPane;
    public ListView<Pharmacy> BrowseListView;
    public ImageView ImageSquare;
    public Label TopLabel,TypeLabel,NameLabel,SearchLabel,EditLabel;

    private Consumer<Boolean> parentLockFunction;
    private PharmacyRegister pharmacyRegister;
    private ArrayList<Pharmacy> foundData;
    private ExecutorService browsePharmacyThreadPool;
    private Stage mainStage;
    private double width,height;


    /**
     *  {@inheritDoc}
     */
    @Override
    public void setWidth(double ProvidedParentWidth)
    {
        width = ProvidedParentWidth -100;
        BrowsePharmacyRootPane.setMaxWidth(width);
        NameInput.setMinWidth((width-220));
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight)
    {
        height = ProvidedParentHeight - 160;
        BrowsePharmacyRootPane.setMaxHeight(height);
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
        super.contrastEffect(BrowsePharmacyRootPane);
        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());

        PreviewPane.widthProperty().addListener(this.previewPaneWidthListener(InfoVBox,TopLabel,NameLabel,TypeLabel));

        PreviewPane.heightProperty().addListener((observable, oldValue, newValue) -> {

            double newHeight = newValue.doubleValue() -100;
            InfoVBox.setPrefHeight(newHeight);
        });

        mainStage = ((Stage)resources.getObject("Stage"));

        SearchLabel.setOnMouseClicked(event -> browsePharmacyDatabaseRequest());

        BrowseListView.itemsProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null)
                BrowseListView.getSelectionModel().select(0);
        });

        BrowseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue != null)
            {
                TypeLabel.setText(newValue.getName());
                NameLabel.setText(newValue.getAddress());
            }
        });

        EditLabel.setOnMouseClicked( event -> {
            if(BrowseListView.getSelectionModel().getSelectedItem() != null)
            {
                mainStage.setFullScreen(false);
                WindowLauncher.launchEditWindow(BrowseListView.getSelectionModel().getSelectedItem(),editFunction());

            }
        });
        NameInput.setOnKeyPressed(event ->{
            if((event.getCode() == KeyCode.ENTER))
                browsePharmacyDatabaseRequest();

        });
    }

    /**
     * Edit function updating pharmacy list view
     * @return consumer that is accepting new pharmacy object and replacing old one
     */
    private Consumer<AbstractApplicationUser> editFunction()
    {
        return (user) -> {
            Pharmacy pharmacy  = (Pharmacy) user;
            BrowseListView.getItems().replaceAll((Pharmacy p) -> {
                if(p.getID().equalsIgnoreCase(pharmacy.getID()))
                    return (Pharmacy) user;
                return p;
            });
        };
    }


    /**
     * <p>
     *     Request data from database<br>
     *     Asking for all rows matching input based on pharmacy name
     * </p>
     * @see PharmacyRegister#find(String)
     */
    private void browsePharmacyDatabaseRequest() {

        String name = NameInput.getText();

        pharmacyRegister = new PharmacyRegister();

        attachProcessSensor(pharmacyRegister,SyncAnimation,LoadingPane,parentLockFunction,browsePharmacyThreadPool);

        browsePharmacyThreadPool = Executors.newWorkStealingPool();
        browsePharmacyThreadPool.submit(() ->{
            foundData = pharmacyRegister.find(name);
            Platform.runLater(() -> {
                BrowseListView.getItems().clear();
                BrowseListView.getItems().addAll(foundData);
            });
        });

    }
}
