package CustomCache;

import SavedVariables.FinalConstants;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * <h2>Created by Marius Baltramaitis on 05/06/2017.</h2>
 *
 * <p>This register is made to hold users images locally in order to avoid unnecessary tcp requests </p>
 * @see NetworkThreads.RequestImage
 */
public final class ImageCacheRegister {

    private static ArrayList<ImageCache> register = new ArrayList();

    /**
     *
     * @param id users id
     * @param type users type
     * @return functional interface with implementation to find user based of parameters
     */
    private static synchronized Supplier<ImageCache> finder(String id,String type)
    {
        return () -> {
            for(ImageCache imageCache : register)
            {
                if(imageCache.getID().equalsIgnoreCase(id) && imageCache.getUserType().equalsIgnoreCase(type))
                    return imageCache;
            }
            return null;
        };
    }

    /**
     * Method to insert image cache object inside register
     * @param imageCache object to insert
     */
    public static synchronized void insert(ImageCache imageCache)
    {
        if(register.size() >= FinalConstants.IMAGE_CACHE_MAX_SIZE)
            register.remove(0);

        if(finder(imageCache.getID(),imageCache.getUserType()).get() == null)
            register.add(imageCache);

    }

    /**
     * Method to clear register
     */
    public static synchronized void clear() {register.clear();}

    /**
     * @return size of register (arrayList)
     */
    public static synchronized int size() {return register.size();}

    /**
     * Method to find user
     * @param ID ID of user
     * @param type user type
     * @return Image itself if exists, null otherwise
     * @see ImageCacheRegister#finder(String, String)
     */
    public static synchronized ImageCache find(String ID, String type)
    {
        return finder(ID,type).get();
    }

}
