package Classes;

/**
 * <h2>Created by Marius Baltramaitis on 10/08/2017.</h2>
 *
 * <p>Medicine class</p>
 */
public class Medicine {

    private String ID,name,description,usage,license,sideEffect,type;
    private int available;

    /**
     * Default constructor
     */
    public Medicine() {}

    /**
     *
     * @param ID Medicine ID
     * @param name name
     * @param description description
     * @param usage usage
     * @param license license
     * @param sideEffect side effects
     * @param type medicine type
     */
    public Medicine(String ID, String name, String description, String usage, String license, String sideEffect, String type) {
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.license = license;
        this.sideEffect = sideEffect;
        this.type = type;
    }

    /**
     * Getter for ID
     * @return ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Setter for ID
     * @param ID ID
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * Getter for Name
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for description
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for description
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for usage
     * @return usage
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Setter for usage
     * @param usage usage
     */
    public void setUsage(String usage) {
        this.usage = usage;
    }

    /**
     * Getter for license
     * @return license
     */
    public String getLicense() {
        return license;
    }

    /**
     * Setter for license
     * @param license license
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * Getter for side effect
     * @return side effect
     */
    public String getSideEffect() {
        return sideEffect;
    }

    /**
     * Setter for side effect
     * @param sideEffect side effect
     */
    public void setSideEffect(String sideEffect) {
        this.sideEffect = sideEffect;
    }

    /**
     * Getter for type
     * @return medicine type
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for type
     * @param type type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for available
     * @return 1 if available 0 if disabled
     */
    public int getAvailable() {
        return available;
    }

    /**
     * Setter for available
     * @param available 1 if available 0 if disabled
     */
    public void setAvailable(int available) {
        this.available = available;
    }

    /**
     *
     * @return type with name and license in one String
     */
    @Override
    public String toString()
    {
        return getType() + " " + getName() + " [" + license + "]";
    }
}
