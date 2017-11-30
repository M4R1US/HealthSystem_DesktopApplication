package Controllers;

import Animations2D.SynchronizationAnimation;
import Classes.ConsoleOutput;
import Classes.Medicine;
import Interfaces.DatabaseSensorAttachable;
import Registers.MedicineRegister;
import SavedVariables.FinalConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Marius Baltramaitis on 07/09/2017.
 */
public class MedicineInformationController implements Initializable, DatabaseSensorAttachable {


    @FXML
    public BorderPane MedicineInfoBasePane, LoadingPane,InformationPane;
    public Label TopLabel,MedicineNameLabel,SearchLabel;
    public TextField NameInput;
    public GridPane CenterGridPane;
    public ListView<Medicine> BrowseListView;
    public SynchronizationAnimation SyncAnimation;
    public Pagination CustomPagination;
    public Pane DescriptionPane,UsagePane,SideEffectPane;
    public TextArea DescriptionTextArea,UsageTextArea,SideEffectTextArea;
    public GridPane SwitchInformationGridPane;


    private String searchText;
    private MedicineRegister medicineRegister;
    private ExecutorService medicineDatabaseTransactionThreadPool;
    private ArrayList<Medicine> foundData;
    private ArrayList<Label> switchLabels;
    private Medicine medicine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        switchLabels = new ArrayList<>();
        SwitchInformationGridPane.lookupAll(".SwitchLabels").forEach(node -> switchLabels.add((Label)node));

        switchLabels.forEach(switchLabel -> switchLabel.setOnMouseClicked(event -> CustomPagination.setCurrentPageIndex(switchLabels.indexOf(switchLabel))));


        SearchLabel.setOnMouseClicked(event -> browseMedicineDatabaseRequest());

        try {
            medicine = (Medicine)resources.getObject("Medicine");
            NameInput.setText(medicine.getName());
            searchText = medicine.getName();
            browseMedicineDatabaseRequest();
            ConsoleOutput.print(getClass().getName(), "Received " + medicine.toString());


        } catch (NullPointerException e) {
            ConsoleOutput.print(getClass().getName(), "No medicine arguments are received");
        }


        InformationPane.widthProperty().addListener((observable, oldValue, newValue) -> MedicineNameLabel.setPrefWidth(newValue.doubleValue()));

        NameInput.setOnKeyPressed(event -> {
            if((event.getCode() == KeyCode.ENTER))
                browseMedicineDatabaseRequest();

        });

        DescriptionPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            DescriptionTextArea.setPrefWidth(newValue.doubleValue());
            UsageTextArea.setPrefWidth(newValue.doubleValue());
            SideEffectTextArea.setPrefWidth(newValue.doubleValue());
        });

        BrowseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue != null) {
                MedicineNameLabel.setText(newValue.getName());
                DescriptionTextArea.setText(newValue.getDescription());
                UsageTextArea.setText(newValue.getUsage());
                SideEffectTextArea.setText(newValue.getSideEffect());
            }

        });

        CustomPagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
        CustomPagination.setPageFactory(this::paginationFactoryReflection);
        CustomPagination.setCurrentPageIndex(0);
    }

    private void browseMedicineDatabaseRequest()
    {
        searchText = NameInput.getText();
        medicineRegister = new MedicineRegister();
        attachProcessSensor(medicineRegister,SyncAnimation,LoadingPane,null,medicineDatabaseTransactionThreadPool);
        medicineDatabaseTransactionThreadPool = Executors.newWorkStealingPool();
        medicineDatabaseTransactionThreadPool.submit(() ->{
            foundData = medicineRegister.find(searchText,null,false);
            Platform.runLater(() -> {
                BrowseListView.getItems().clear();
                BrowseListView.getItems().addAll(foundData);
                if(BrowseListView.getItems().size() != 0)
                    BrowseListView.getSelectionModel().select(0);
            });

        });
    }


    private Node paginationFactoryReflection(Integer targetIndex)
    {
        switch (targetIndex)
        {
            case 0 :
                switchLabels.forEach(label -> label.setBackground(null));
                switchLabels.get(0).setBackground(FinalConstants.SELECTED_SWITCH_LABEL_BACKGROUND);
                return DescriptionPane;

            case 1:
                switchLabels.forEach(label -> label.setBackground(null));
                switchLabels.get(1).setBackground(FinalConstants.SELECTED_SWITCH_LABEL_BACKGROUND);
                return UsagePane;

            case 2:
                switchLabels.forEach(label -> label.setBackground(null));
                switchLabels.get(2).setBackground(FinalConstants.SELECTED_SWITCH_LABEL_BACKGROUND);
                return SideEffectPane;

        }

        return null;
    }
}
