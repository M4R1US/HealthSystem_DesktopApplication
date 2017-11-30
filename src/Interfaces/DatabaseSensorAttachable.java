package Interfaces;

import Animations2D.SynchronizationAnimation;
import javafx.application.Platform;
import javafx.scene.layout.Region;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 08/09/2017.</h2>
 *
 * <p>Special interface to attach database sensor</p>
 */
public interface DatabaseSensorAttachable {

    /**
     * @param databaseRegister any database register
     * @param synchronizationAnimation any synchronization animation
     * @param loadingPane pane where synchronization animation is moving
     * @param parentLockFunction parent lock function
     * @param databaseThreadPool any database thread pool
     */
    default void attachProcessSensor(DatabaseRegisterImplementation databaseRegister, SynchronizationAnimation synchronizationAnimation, Region loadingPane, Consumer<Boolean> parentLockFunction, ExecutorService databaseThreadPool)
    {

        databaseRegister.addListener((String processText,Double processValue) -> {
            if (processValue < 1.0 && processValue >= 0) {
                if(synchronizationAnimation != null)
                    synchronizationAnimation.run();

                loadingPane.setVisible(true);
                if (parentLockFunction != null)
                    parentLockFunction.accept(true);
            }

            if (processValue >= 1.0 || processValue < 0) {
                if(synchronizationAnimation != null)
                    Platform.runLater(() -> synchronizationAnimation.stop());

                loadingPane.setVisible(false);
                if (parentLockFunction != null)
                    parentLockFunction.accept(false);

                if(databaseThreadPool != null)
                    databaseThreadPool.shutdownNow();
            }
        });
    }

}
