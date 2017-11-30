package CustomExceptions;


/**
 * <h2>Created by Marius Baltramaitis on 09/10/2017.</h2>
 *
 * <p>Exception thrown when credentials are invalid</p>
 */
public class InvalidCredentialsException extends Throwable {

    /**
     * Default constructor
     * @param message message to print
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
