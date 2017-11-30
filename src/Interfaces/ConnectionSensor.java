package Interfaces;

/**
 * <h2>Created by Marius Baltramaitis on 23/06/2017.</h2>
 * <p>
 *     Interface used to update user interface while connection transaction is still in process. Implementation must be defined inside functional interface using regular lambda expression.
 * </p>
 */
@FunctionalInterface
public interface ConnectionSensor {

    /**
     * Method for applying listener
     * @param text Text to be visible for application user
     * @param progress Approximate progress status measured from -1.0 to 1.0 [Example : 0.3 equivalent to 30%, values less than 0 symbolises connection failure]
     */
    void apply(String text,Double progress);
}
