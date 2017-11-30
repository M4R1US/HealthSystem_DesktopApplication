package Classes;

/**
 * <p>
 *     This class holds two type objects <br>
 *
 *         T1 and T2 are any type objects
 * </p>
 * Created by Marius Baltramaitis on 07-Mar-17.
 */
public class GenericPair<T1, T2> {

    private T1 first;
    private T2 second;


    /**
     * Constructor taking parameters of two objects
     * @param first First object
     * @param second Second object
     */
    public GenericPair(T1 first, T2 second)
    {
        this.first = first;
        this.second = second;
    }

    /**
     * Getter for first object
     * @return #T1
     */
    public T1 getFirst() { return first; }


    /**
     * Getter for second object
     * @return #T2
     */
    public T2 getSecond()
    {
        return second;
    }
}
