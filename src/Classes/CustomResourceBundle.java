package Classes;

import java.util.*;
import java.util.function.Consumer;

/**
 * <h2>Created by Marius Baltramaitis on 22/03/2017.</h2>
 * <p>Resource bundle is used in different controllers to initialize(bypass) variables</p>
 * <p>Resource bundle has list og GenericPairs of objects and strings, where string is the key to access corresponding object</p>
 * @see GenericPair
 */
public class CustomResourceBundle extends ResourceBundle {

    public ArrayList<GenericPair<?,String>> resourceList;

    /**
     * Constructor taking on or few GenericPair of object and string parameters
     * @param objectsWithKeys pair with object and string key
     * @see GenericPair
     */
    public CustomResourceBundle(GenericPair<?,String>... objectsWithKeys)
    {
        resourceList = new ArrayList<>();
        
        Consumer<GenericPair<?,String >> consumer = (GenericPair<?,String> pair) -> resourceList.add(pair);

        for (GenericPair<?,String> objectsWithKey : objectsWithKeys)
            consumer.accept(objectsWithKey);

    }

    /**
     * Method to receive object
     * @param key key of object
     * @return Object if exist with given key
     */
    @Override
    protected Object handleGetObject(String key)
    {

        for(GenericPair<?,? super String> g : resourceList)
        {
            if(g.getSecond().equals(key))
            {
                return g.getFirst();
            }
        }

        return null;
    }

    /**
     * Method to get all keys of this resource bundle
     * @return ArrayList of all keys or null if resource bundle is empty
     */
    public ArrayList<String> getAllKeys()
    {
        ArrayList<String> keys = new ArrayList<>();
        Consumer<GenericPair<?,String>> consumer = (GenericPair<?,String> pair) -> keys.add(pair.getSecond());

        resourceList.forEach(consumer);

        return keys;
    }

    /**
     * Getter for list
     * @return list of GenericPair of object and key
     */
    public ArrayList<GenericPair<?,String>> getList()
    {
        return resourceList;
    }



    /**
     * @deprecated Use another built getKeys() method to get String array with keys
     * @throws UnsupportedOperationException always, since this function is not supported.
     * @return null
     */
    @Override
    public Enumeration<String> getKeys() {
        throw new UnsupportedOperationException("Use another built getAllKeys() method to get String array with keys");
    }

    /**
     * Iteration through list, collecting data to one String
     * @return String of keys and object names
     */
    public String toString()
    {
        String output = "";
        for(int i = 0; i < resourceList.size(); i++)
        {
            output+= "Object: " + resourceList.get(i).getFirst().toString() + " , KEY: " + resourceList.get(i).getSecond() +" , at index " +i+System.lineSeparator();
        }
        return output;
    }
}
