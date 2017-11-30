package Classes;

import Actions.WindowLauncher;
import SavedVariables.DynamicVariables;
import SavedVariables.FinalConstants;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <h2>Created by Marius Baltramaitis</h2>
 *
 * <p>
 *     Main class
 * </p>
 */

public class main extends Application {

    public Stage stage;

    /**
     * <p>
     *  Initially called in main method. <br>
     *  This method is used to launch Lobby window
     *  @see Controllers.LobbyController
     * </p>
     * @param primaryStage {@link Stage} Stage that has been sent here directly
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        DynamicVariables.setIPV4();
        WindowLauncher.launchLobby();
    }



    /**
     *  <p>
     *      Main method of application
     *  </p>
     * @param args default arguments
     */
    public static void main(String[] args) {
        loadFonts();
        loadEncryptionKey();
        launch(args);
    }

    /**
     * Method to load various fonts
     */
    public static void loadFonts()
    {

        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-Black.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-BlackItalic.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-Bold.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-BoldItalic.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-ExtraBold.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-ExtraBoldItalic.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-Italic.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-Light.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-LightItalic.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-Medium.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-MediumItalic.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-Regular.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-SemiBold.ttf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Decalotype-SemiBoldItalic.ttf"),20);

        Font.loadFont(main.class.getResourceAsStream("/Fonts/Akrobat-Black.otf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Akrobat-Bold.otf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Akrobat-ExtraBold.otf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Akrobat-ExtraLight.otf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Akrobat-Light.otf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Akrobat-Regular.otf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Akrobat-SemiBold.otf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/Akrobat-Thin.otf"),20);
        Font.loadFont(main.class.getResourceAsStream("/Fonts/digital-7.ttf"),20);

    }

    /**
     * Method to read encryption key from file and load it to application
     */
    private static void loadEncryptionKey()
    {
        try {
            DynamicVariables.AES_128_KEY= Files.readAllLines(Paths.get(FinalConstants.AES_128_KEY_PATH)).get(0);
            ConsoleOutput.print("Successfully scanned Key file");

        }catch (Exception e) {ConsoleOutput.print(main.class.getName(),e.getMessage());}
    }
}
