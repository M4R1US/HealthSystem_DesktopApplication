package SavedVariables;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.geometry.Insets;
import javafx.scene.text.Font;

/**
 * <h2>Created by Marius Baltramaitis on 03-Feb-17.</h2>
 *
 * <p>This class is holder for static final variables that can not be changed in runtime</p>
 */
public final class FinalConstants {

    //  Sizes  //

    public static final int LOBBY_SCENE_WIDTH = 500;
    public static final int LOBBY_SCENE_HEIGHT = 500;
    public static final int SETTINGS_SCENE_HEIGHT = 500;
    public static final int SETTINGS_SCENE_WIDTH = 500;

    public static final int DASHBOARD_SCENE_HEIGHT = 700;
    public static final int DASHBOARD_SCENE_WIDTH = 1200;

    public static final int MEDICINE_WINDOW_HEIGHT = 600;
    public static final int MEDICINE_WINDOW_WIDTH = 1000;

    public static final int PRESCRIPTION_ARCHIVE_WINDOW_WIDTH = 1100;
    public static final int PRESCRIPTION_ARCHIVE_WINDOW_HEIGHT = 600;

    public static final int IMAGE_CLIPBOARD_WIDTH = 1200;
    public static final int IMAGE_CLIPBOARD_HEIGHT = 700;
    public static final int CONFIRMATION_WINDOW_WIDTH = 300;
    public static final int CONFIRMATION_WINDOW_HEIGHT = 100;

    public static final int IMAGE_CACHE_MAX_SIZE = 200;

    public static final int NAME_MAX_LENGTH = 34;
    public static final int MEDICINE_NAME_MAX_LENGTH = 45;
    public static final int MEDICINE_NAME_MIN_LENGTH = 2;
    public static final int NAME_MIN_LENGTH = 2;
    public static final int ADDITIONAL_INFORMATION_MIN_LENGTH = 0;
    public static final int ADDITIONAL_INFORMATION_MAX_LENGTH = 200;
    public static final int SPECIAL_NOTES_MIN_LENGTH = 0;
    public static final int SPECIAL_NOTES_PRESCRIPTION_MAX_LENGTH = 2000;

    //  Hosts and ports:   //

    public static final String HOST_DOMAIN_NAME = "Health-System.eu";
    public static final int TCP_DEVICE_CONNECTION_PORT = 5554;
    public static final int SMS_INPUT_PORT = 5555;
    public static final int TCP_IMAGE_REQUEST_PORT = 5556;
    public static final int TCP_IMAGE_DELIVER_PORT = 5557;
    public static final int FIREWALL_PORT = 5558;

    //Timeouts in milliseconds

    public static final int IMAGE_TRANSFER_TIMEOUT = 5000; // 5 seconds

    //Sleep
    public static final int SCHEDULED_TASK_SLEEP_MILLISECONDS = 1000; // 1 second

    // Icon PATHS //
    public static final String APP_ICON_SRC = "Icons/x32/heartbeat.png";
    public static final String DEFAULT_MALE_IMG_SRC = "Icons/DefaultProfileIcons/man.png";
    public static final String DEFAULT_FEMALE_IMG_SRC = "Icons/DefaultProfileIcons/woman.png";


    // Colors and backgrounds //

    public static final String STRONG_RED_COLOR = "#D80027";
    public static final String RED_COLOR = "#ffb2b2";
    public static final String GREEN_COLOR = "#21B6A8";
    public static final Color LOBBY_TAB_DEFAULT_COLOR = Color.rgb(44,62,80,0.15);
    public static final Color LOBBY_TAB_SELECTED_COLOR = Color.rgb(189,189,189,0.1);
    public static final Color DEFAULT_TERMINAL_TEXT_COLOR = Color.rgb(255,255,255,0.7);
    public static final Color DEFAULT_BLUE_COLOR = Color.rgb(0,51,102,0.5);
    public static final Color SelectedBackgroundColor = Color.rgb(0, 51, 102, 0.6);
    public static final Color SELECTED_MEDICINE_TYPE_TILE = Color.rgb(115,38,80,0.1);
    public static final Color DEFAULT_MEDICINE_TYPE_TILE = Color.rgb(44,62,80,0.1);
    public static final Paint defaultBackgroundPaint = Paint.valueOf("#adadad");
    public static final Color CIRCLE_IMAGE_STROKE = Color.rgb(0,51,102,0.2);
    public static final Color PRESCRIPTION_SELECTION_LABEL_SELECTED_COLOR =Color.rgb(255,255,255,0.6);
    public static final Color PRESCRIPTION_SELECTION_LABEL_DEFAULT_COLOR =Color.rgb(52,73,94,0.7);
    public static final Background selectedPrescriptionHBoxBackground = new Background(new BackgroundFill(SelectedBackgroundColor,null,null));
    public static final Background defaultPrescriptionHBoxBackground = new Background(new BackgroundFill(defaultBackgroundPaint,null,null));
    public static final Background DEFAULT_MEDICINE_TYPE_TILE_BACKGROUND = new Background(new BackgroundFill(DEFAULT_MEDICINE_TYPE_TILE,null,null));
    public static final Background SELECTED_MEDICINE_TYPE_TILE_BACKGROUND = new Background(new BackgroundFill(SELECTED_MEDICINE_TYPE_TILE,null,null));
    public static final Background SELECTED_SWITCH_LABEL_BACKGROUND = new Background(new BackgroundFill(Color.rgb(0,0,0,0.10),null,null));
    public static final Background SELECTED_PRESCRIPTION_SWITCH_LABEL_BACKGROUND = new Background(new BackgroundFill(Color.rgb(69,122,136,0.8),null,null));


    /* FONTS */

    public static final Font TERMINAL_FONT = Font.loadFont(FinalConstants.class.getResourceAsStream("/Fonts/Decalotype-Light.ttf"),20);

    // File paths

    // change this later
    public static final String AES_128_KEY_PATH = "aes128.key";


    //Validation Borders
    public static final Border BLUE_TEXTFIELD_DESIGNED_BORDER = new Border(new BorderStroke(DEFAULT_BLUE_COLOR,BorderStrokeStyle.SOLID,new CornerRadii(0.0),new BorderWidths(0,0,1,0)));
    public static final Border SELECTED_GENDER_BORDER = new Border(new BorderStroke(DEFAULT_BLUE_COLOR, BorderStrokeStyle.SOLID,new CornerRadii(2.0),null));
    public static final Border RED_TEXTFIELD_BORDER = new Border(new BorderStroke(Paint.valueOf(STRONG_RED_COLOR), BorderStrokeStyle.SOLID,new CornerRadii(0.0),new BorderWidths(0,0,2,0)));
    public static final Border GREEN_TEXTFIELD_BORDER = new Border(new BorderStroke(Paint.valueOf(GREEN_COLOR), BorderStrokeStyle.SOLID,new CornerRadii(0.0),new BorderWidths(0,0,2,0)));
    public static final Border PAGINATION_ICON_SELECTED_BORDER = new Border(new BorderStroke(Paint.valueOf("#f8f8f8"),BorderStrokeStyle.SOLID,CornerRadii.EMPTY,new BorderWidths(0,0,2,0),new Insets(0,0,10,0)));
    public static final Border BLUE_TEXTAREA_BORDER = new Border(new BorderStroke(DEFAULT_BLUE_COLOR, BorderStrokeStyle.SOLID,new CornerRadii(0.0),new BorderWidths(2,2,2,2)));
    public static final Border GREEN_TEXTAREA_BORDER = new Border(new BorderStroke(Paint.valueOf(GREEN_COLOR), BorderStrokeStyle.SOLID,new CornerRadii(0.0),new BorderWidths(2,2,2,2)));
    public static final Border RED_TEXTAREA_BORDER = new Border(new BorderStroke(Paint.valueOf(STRONG_RED_COLOR), BorderStrokeStyle.SOLID,new CornerRadii(0.0),new BorderWidths(2,2,2,2)));
    public static final Border SELECTED_MEDICINE_TILE_BORDER = new Border(new BorderStroke(Color.rgb(0,0,0,0.7), BorderStrokeStyle.SOLID,new CornerRadii(0.0),new BorderWidths(2,2,2,2)));

    public static final Border DISABLE_BUTTON_RED_BORDER = new Border(new BorderStroke(Color.rgb(219,10,91,0.3),BorderStrokeStyle.SOLID,new CornerRadii(2),new BorderWidths(1,1,1,1)));
    public static final Border DISABLE_BUTTON_GREEN_BORDER = new Border(new BorderStroke(Color.rgb(134,226,213,0.7),BorderStrokeStyle.SOLID,new CornerRadii(2),new BorderWidths(1,1,1,1)));

}
