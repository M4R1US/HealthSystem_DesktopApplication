package Interfaces;

import CustomCache.ImageCache;
import CustomCache.ImageCacheRegister;
import NetworkObjects.ImageObject;
import NetworkThreads.RequestImage;
import javafx.scene.image.Image;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 25/11/2017.</h2>
 *
 * <p>Interface for image request from server</p>
 */
public interface ServerImageRequestImplementation {


    /**
     * Image request from server
     * @param ID user ID
     * @param userType user type
     * @param imageConsumer function with instructions of what to do with received image
     * @param browseImageThreadPool image thread pool
     * @param userIdentity user identity (usually login)
     */
    default void sendImageRequest(String ID, String userType, Consumer<Image> imageConsumer, ExecutorService browseImageThreadPool,String userIdentity)
    {
        // Cache lookup. Maybe picture is already downloaded before.
        ImageCache cacheLookup = ImageCacheRegister.find(ID,userType);
        if(cacheLookup != null)
        {
            imageConsumer.accept(cacheLookup.getImage());
            return;
        }
        // If not, making thread and asking server for image
        ImageObject imageRequestObject = new ImageObject(ID,userType,userIdentity,"browsing pictures");
        browseImageThreadPool.submit(new RequestImage(imageRequestObject,imageConsumer));
    }
}
