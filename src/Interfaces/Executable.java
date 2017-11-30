package Interfaces;

/**
 * <h2>Created by Marius Baltramaitis on 07-Mar-17.</h2>
 * <p>
 *     Functional interface without return value and any arguments
 * </p>
 */
@FunctionalInterface
public interface Executable {

    /**
     * Executes defined function
     */
    void execute();
}
