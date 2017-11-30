package Actions;

import SavedVariables.FinalConstants;
import CustomListCells.CountryCodeCell;
import com.sun.istack.internal.NotNull;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Supplier;


/**
 * <h2>Created by Marius Baltramaitis on 15-Dec-16.</h2>
 *
 * <p>Class with various methods to for modification nodes from user interface</p>
 */

public class NodeModification {

    /**
     * Method to set border of pagination icon by given index
     * @param index index of selected icon
     * @param paginationTopIconsSet list of all icons
     */
    public static void setBorderToPaginationIcon(Integer index, Set<Node> paginationTopIconsSet)
    {
        ArrayList<Node> iconList = new ArrayList<>();
        iconList.addAll(paginationTopIconsSet);
        if(paginationTopIconsSet == null || index > paginationTopIconsSet.size())
            return;

        iconList.forEach(iconHBox -> {
            ((HBox) iconHBox).setBorder(null);
            ((HBox) iconHBox).setOpacity(0.5);

        });
        ((HBox) iconList.get(index)).setBorder(FinalConstants.PAGINATION_ICON_SELECTED_BORDER);
        ((HBox) iconList.get(index)).setOpacity(1);

    }


    /**
     * Method to initialize custom combo box
     * @param comboBox combo box itself
     * @param cells one or few list cells that will be placed inside combo box
     * @see CountryCodeCell
     */
    public static void initializePhoneNumberComboBox(@NotNull  ComboBox<CountryCodeCell> comboBox, @NotNull CountryCodeCell... cells)
    {

        for(CountryCodeCell x : cells)
            comboBox.getItems().add(x);

        comboBox.getSelectionModel().select(0);
        comboBox.setCellFactory((ListView<CountryCodeCell> param) -> new ListCell<CountryCodeCell>() {

            @Override
            protected void updateItem(CountryCodeCell item, boolean empty) {
                if (empty)
                    super.updateItem(cells[0], empty);
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    CountryCodeCell current = new CountryCodeCell(item.getIconSrc(), item.getCountryCode(),item.getCountryCode());
                    setGraphic(current);
                }
            }
        });
    }

    /**
     * Supplier of medicine type grey image
     * @param medicineType type of medicine
     * @return functional interface of medicine type icon supplier
     */
    public static Supplier<Image> medicineIconSupplier(String medicineType)
    {
        return () -> {

            String imagePath;

            switch (medicineType)
            {
                case "Powder":
                    imagePath = "/Icons/x64/powder_marked.png";
                    break;

                case "Tablet":
                    imagePath = "/Icons/x64/2-pills_marked.png";
                    break;

                case "Capsule":
                    imagePath = "/Icons/x64/pill-capsule_marked.png";
                    break;

                case "Lotion":
                    imagePath = "/Icons/x64/lotion_marked.png";
                    break;

                case "Drops" :
                    imagePath = "/Icons/x64/drops_marked.png";
                    break;

                case "Injection":
                    imagePath = "/Icons/x64/injection_marked.png";
                    break;

                default:
                    return null;
            }

            return new Image(imagePath);

        };
    }

    /**
     * Supplier of medicine type white image
     * @param medicineType type of medicine
     * @return functional interface of medicine type icon supplier
     */
    public static Supplier<Image> medicineWhiteIconSupplier(String medicineType)
    {
        return () -> {

            Image img = null;

            switch (medicineType)
            {
                case "Powder" :
                    img = new Image("Icons/x64/powder-bottle.png");
                    break;

                case "Tablet" :
                    img = new Image("Icons/x64/2-pills.png");
                    break;

                case "Capsule" :
                    img = new Image("Icons/x64/pill-capsule.png");
                    break;

                case "Lotion" :
                    img = new Image("Icons/x64/body-lotion.png");
                    break;

                case "Drops" :
                    img = new Image("Icons/x64/dropper.png");
                    break;

                case "Injection" :
                    img = new Image("Icons/x64/injection.png");
                    break;

            }

            return img;
        };
    }
}
