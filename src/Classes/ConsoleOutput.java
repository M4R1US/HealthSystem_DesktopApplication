package Classes;

/**
 * Created by Marius Baltramaitis on 13-Dec-16.
 * @author Marius Baltramaitis
 * @since 1.0
 * <p>
 *     This final class is written just to print out information to terminal/console.
 * </p>
 */


public final class ConsoleOutput {



    /**
     * Prints message to console/terminal
     * @param message message that will be printed in terminal/console during runtime
     */
    public static void print(String message)
    {
        System.out.println(message + System.lineSeparator());
    }

    /**
     * Prints message to console/terminal together with classname
     * @param classname Name of class calling this method
     * @param message message that will be printed in terminal/console during runtime
     */

    public static void print(String classname,String message)
    {
        System.out.print("Class: " + classname + " Message: " + message + System.lineSeparator());
    }


    /**
     * Prints {@link Throwable} message together with class name
     * @param classname Name of class calling this method
     * @param throwable Throwable that occurs on exception
     * @see Throwable
     */
    public static void print(String classname, Throwable throwable)
    {
        System.out.print("Class: " + classname + " threw exception with message " + throwable.getMessage() + System.lineSeparator());
    }

}
