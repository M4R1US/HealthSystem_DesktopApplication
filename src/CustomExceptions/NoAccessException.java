package CustomExceptions;


/**
 * <h2>Created by Marius Baltramaitis on 09/10/2017.</h2>
 *
 * <p>Exception thrown when access for user is denied</p>
 */
public class NoAccessException extends Throwable {

     /**
     * Default constructor
     * @param message message to print
     */
    public NoAccessException(String message)
    {
        super(message);
    }
}
