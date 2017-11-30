package Interfaces;

/**
 * <h2>Created by Marius Baltramaitis on 26/11/2017.</h2>
 *
 * <p>Interface used to inform registers whether finished or not</p>
 */
@FunctionalInterface
public interface DatabaseTransactionFinishedProperty {

    /**
     * Update method
     * @param status true if finished, false otherwise
     */
    void update(boolean status);
}
