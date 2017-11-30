package Interfaces;

/**
 * <h2>Created by Marius Baltramaitis on 08/09/2017.</h2>
 *
 * <p>Sensor implementation for database registers</p>
 */
public interface DatabaseRegisterImplementation {

    /**
     * @param sensor Connection sensor to apply
     * @see ConnectionSensor
     */
    void addListener(ConnectionSensor sensor);

    /**
     * Method to update connection sensor if its initialized
     * @param connectionInformation text to apply
     * @param progressValue value to apply
     */
    void updateProcess(String connectionInformation,Double progressValue);
}
