package Controllers.InsideControllers;

import Actions.NodeModification;
import Actions.WindowLauncher;
import Animations2D.SynchronizationAnimation;
import Classes.GenericPair;
import Classes.Medicine;
import Controllers.ConfirmationWindowController;
import Interfaces.BrowsePaneAdditionalImplementation;
import Interfaces.DatabaseSensorAttachable;
import Registers.MedicineRegister;
import SavedVariables.DynamicVariables;
import javafx.application.Platform;
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
 * <p>Inside controller in administrator dashboard responsible for browsing through browse-medicine section</p>
 */
public class BrowseMedicineController extends InsideController implements BrowsePaneAdditionalImplementation, DatabaseSensorAttachable {

    public BorderPane BrowseMedicineRootPane, LoadingPane;
    public TextField NameInput;
    public HBox BrowseTopPane;
    public VBox InfoVBox;
    public SynchronizationAnimation SyncAnimation;
    public BorderPane PreviewPane;
    public GridPane BrowseCenterGridPane;
    public ListView<Medicine> BrowseListView;
    public ImageView ImageSquare;
    public Label TopLabel, TypeLabel, NameLabel, StatusLabel,SearchLabel;

    private Consumer<Boolean> parentLockFunction;
    private MedicineRegister medicineRegister;
    private ArrayList<Medicine> foundData;
    private ExecutorService medicineDatabaseTransactionThreadPool;
    private String searchText;
    private double width, height;


    /**
     *  {@inheritDoc}
     */
    @Override
    public void setWidth(double ProvidedParentWidth)
    {
        width = ProvidedParentWidth -100;
        BrowseMedicineRootPane.setMaxWidth(width);
        NameInput.setMinWidth((width-220));
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public void setHeight(double ProvidedParentHeight)
    {
        height = ProvidedParentHeight - 160;
        BrowseMedicineRootPane.setMaxHeight(height);
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
        super.contrastEffect(BrowseMedicineRootPane);
        setWidth(DynamicVariables.screenSensor.getWidth());
        setHeight(DynamicVariables.screenSensor.getHeight());


        PreviewPane.widthProperty().addListener(this.previewPaneWidthListener(InfoVBox, TopLabel, NameLabel, TypeLabel));

        PreviewPane.heightProperty().addListener((observable, oldValue, newValue) -> {

            double newHeight = newValue.doubleValue() - 100;
            InfoVBox.setPrefHeight(newHeight);
        });


        StatusLabel.setOnMouseClicked(event -> availability());

        SearchLabel.setOnMouseClicked(event ->{
            searchText = NameInput.getText();
            browseMedicineDatabaseRequest();
        });

        BrowseListView.itemsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
                BrowseListView.getSelectionModel().select(0);
        });

        BrowseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue != null) {
                TypeLabel.setText(newValue.getType());
                NameLabel.setText(newValue.getName());
                ImageSquare.setImage(NodeModification.medicineIconSupplier(newValue.getType()).get());

                switch (newValue.getAvailable()) {
                    case 1:
                        StatusLabel.setText("Disable");
                        break;

                    case 0:
                        StatusLabel.setText("Enable");
                }

            }
        });

        NameInput.setOnKeyPressed(event ->{
            if((event.getCode() == KeyCode.ENTER))
            {
                searchText = NameInput.getText();
                browseMedicineDatabaseRequest();
            }

        });

    }


    /**
     * Database request for change availability of selected medicine in list view
     * <p>
     *     This method is dynamic, based on medicine availability status. It is going to disable medicine if it is enabled <br>
     *     The same principe applies to disabled medicine. If it is disabled it is going to be enabled after sql transaction
     *
     * </p>
     */
    private void availability()
    {
        if(BrowseListView.getSelectionModel().getSelectedItem() == null)
            return;

        GenericPair<Stage, ConfirmationWindowController> windowPair = WindowLauncher.getConfirmationWindow();
        Stage confirmationStage = windowPair.getFirst();
        ConfirmationWindowController confirmationWindowController = windowPair.getSecond();
        Medicine medicine = BrowseListView.getSelectionModel().getSelectedItem();
        confirmationWindowController.setOnYesButton(() -> {

            if(medicine != null)
            {
                int availability = (medicine.getAvailable() == 1) ? 0 : 1;

                MedicineRegister medicineRegister = new MedicineRegister();
                attachProcessSensor(medicineRegister,SyncAnimation,LoadingPane,parentLockFunction,medicineDatabaseTransactionThreadPool);

                medicineDatabaseTransactionThreadPool = Executors.newWorkStealingPool();
                medicineDatabaseTransactionThreadPool.submit(() ->Platform.runLater(() -> {
                    medicineRegister.availability(medicine.getID(),availability);
                    browseMedicineDatabaseRequest();
                }));
            }

            confirmationStage.close();

        });

        confirmationWindowController.setOnNoButton(() -> confirmationStage.close());
        String text = (medicine.getAvailable() == 1) ? "Disable " + medicine.getName() + " ?" :  "Enable " + medicine.getName() + " ?";
        confirmationWindowController.setText(text);
        confirmationStage.show();
    }


    /**
     * <p>
     *     Request data from database<br>
     *     Asking for all rows matching input based on medicine name, license and availability
     * </p>
     * @see MedicineRegister#find(String, String, boolean)
     */
    private void browseMedicineDatabaseRequest()
    {
        medicineRegister = new MedicineRegister();
        attachProcessSensor(medicineRegister,SyncAnimation,LoadingPane,parentLockFunction,medicineDatabaseTransactionThreadPool);
        medicineDatabaseTransactionThreadPool = Executors.newWorkStealingPool();
        medicineDatabaseTransactionThreadPool.submit(() ->{
            foundData = medicineRegister.find(searchText,null,true);
            Platform.runLater(() -> {
                BrowseListView.getItems().clear();
                BrowseListView.getItems().addAll(foundData);
                if(BrowseListView.getItems().size() != 0)
                    BrowseListView.getSelectionModel().select(0);
            });

        });
    }
}
