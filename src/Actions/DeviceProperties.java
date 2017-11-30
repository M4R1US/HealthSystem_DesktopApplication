package Actions;

import Classes.ConsoleOutput;

import java.util.Locale;

/**
 * <h2>Created by Marius Baltramaitis</h2>
 */
public final class DeviceProperties {

    public static String OS_FULL_NAME = "";
    public static String DEVICE_NAME = "";

    /**
     * Types of Operating Systems
     */
    public enum OSType {
        Windows, MacOS, Linux, Other
    }


    // cached result of OS detection
    public static OSType detectedOS;


    /**
     * Detects operating system
     */
    public static void detectOS()
    {
        if(detectedOS != null)
            return;

        OS_FULL_NAME = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

        try {

            DEVICE_NAME = System.getProperty("user.name");

        } catch (Exception e)
        {
            DEVICE_NAME = "Unknown";
            ConsoleOutput.print("Couldn't detect device user");
        }

        ConsoleOutput.print("DEVICE NAME " + DEVICE_NAME);

        if ((OS_FULL_NAME.indexOf("mac") >= 0) || (OS_FULL_NAME.indexOf("darwin") >= 0))
        {
            detectedOS = OSType.MacOS;
            return;
        }

        if (OS_FULL_NAME.indexOf("win") >= 0)
        {
            detectedOS = OSType.Windows;
            return;
        }


        if (OS_FULL_NAME.indexOf("nux") >= 0)
        {
            detectedOS = OSType.Linux;
            return;
        }
            detectedOS = OSType.Other;


    }
}