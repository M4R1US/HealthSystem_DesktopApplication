package SavedVariables;

import Classes.ConsoleOutput;
import Controllers.AbstractDashboardController;
import Controllers.DoctorDashboardController;
import Interfaces.ScreenDimensionPropertySensor;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <h2>Created by Marius Baltramaitis on 07/10/2017.</h2>
 *
 * <p>This class is holder for static dynamic variables that can be changed in runtime</p>
 */
public final class DynamicVariables {

    public static String IPV4;
    public static String databaseLogin;
    public static String databasePassword;
    public static String AES_128_KEY;
    public static String LITHUANIAN_NUMBER;
    public static String NORWEGIAN_NUMBER;
    public static String ACCOUNT_SID;
    public static String AUTH_TOKEN;
    public static ScreenDimensionPropertySensor screenSensor;


    /**
     * Method to find ip version 4
     */
    public static void setIPV4() {try {   IPV4 =  InetAddress.getByName(FinalConstants.HOST_DOMAIN_NAME).getHostAddress();    } catch (UnknownHostException e) {      ConsoleOutput.print(e.getMessage());    }}
}
