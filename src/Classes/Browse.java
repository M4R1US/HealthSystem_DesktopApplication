package Classes;

import java.awt.*;
import java.net.URI;

/**
 * <h2>Created by Marius Baltramaitis on 17/07/2017.</h2>
 *  <p>Class to open http links</p>
 */
public class Browse {

    /**
     * Opens link of my github profile
     */
    public static void openGithub()
    {
        if(Desktop.isDesktopSupported())
           openPage("https://github.com/M4R1US");
    }

    /**
     * Opens http://www.health-system.eu
     */
    public static void openMainPage()
    {
        if(Desktop.isDesktopSupported())
            openPage("http://www.health-system.eu");
    }

    /**
     * Opens http link
     * @param page link to open
     */
    private static void openPage(String page)
    {
        try {   Desktop.getDesktop().browse(new URI(page));     } catch (Exception e) {     e.printStackTrace();    }
    }

}
