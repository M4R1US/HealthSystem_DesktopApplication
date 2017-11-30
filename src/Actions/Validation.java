package Actions;

/**
 *<h2>Created by Marius Baltramaitis on 18-Dec-16.</h2>
 *
 * <p>Class used to validate various data types</p>
 */
public final class Validation {

    private static final String LEGAL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ _";
    private static final String NORWEGIAN_CHARS = "æøåÆØÅ";
    private static String legalSmallChars = LEGAL_CHARS.toLowerCase();
    private static String numbers = "1234567890";
    private static String allLegalNameChars = LEGAL_CHARS + legalSmallChars + NORWEGIAN_CHARS;
    private static String allLegalTextChars = allLegalNameChars + ",-!.?:()" + numbers + "\t" + "\n";
    private static String allLegalPharmacyNameChars = LEGAL_CHARS + NORWEGIAN_CHARS + legalSmallChars + numbers;
    private static String legalMedicineNameChars = legalSmallChars + LEGAL_CHARS + numbers;


    /**
     * Method for int validation
     * @param text text to validate
     * @param length length of int
     * @return true if text contains only numbers and equivalent length, false otherwise
     */
    private static boolean validInt(String text,int length)
    {
        if(text.length() != length)
            return false;
        try
        {
            Integer.parseInt(text);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /**
     * Method for int validation
     * @param text text to validate
     * @param minLength min length of text
     * @param maxLength max length of text
     * @param allowNulls true if null texts are allowed
     * @return true if text contains only numbers and is valid by given parameters,false otherwise
     */
    public static boolean validInt(String text, int minLength, int maxLength,boolean allowNulls)
    {
        if(text != null && !allowNulls && minLength == maxLength)
            return validInt(text,minLength);

        if(!nonDependantValidationCheck(text,minLength,maxLength,allowNulls))
            return false;

        try {

            Integer.parseInt(text);

        } catch (NumberFormatException e)
        {
            return false;
        }

        return true;

    }

    /**
     *  Method to check if pharmacy name is valid
     * @param text text to check
     * @param minLength min length of text
     * @param maxLength max length of text
     * @param allowNulls true if text can be null
     * @return true if text is valid by given parameters, false otherwise
     */
    public static boolean validPharmacyName(String text, int minLength,int maxLength,boolean allowNulls)
    {
        if(!nonDependantValidationCheck(text,minLength,maxLength,allowNulls))
            return false;

        for(char x : text.toCharArray())
        {
            if(!allLegalPharmacyNameChars.contains(""+x))
                return false;
        }

        return true;
    }

    /**
     *  Method to check if pharmacy name is valid
     * @param text text to check
     * @param minLength min length of text
     * @param maxLength max length of text
     * @param allowNulls true if text can be null
     * @return true if text is valid by given parameters, false otherwise
     */
    public static boolean validMedicineName(String text, int minLength,int maxLength, boolean allowNulls)
    {
        if(!nonDependantValidationCheck(text,minLength,maxLength,allowNulls))
            return false;

        for(char x : text.toCharArray())
        {
            if(!legalMedicineNameChars.contains(""+x))
                return false;
        }

        return true;
    }

    /**
     * Additional text check in order to avoid duplicates in code
     * @param text text to validate
     * @param minLength min length of text
     * @param maxLength max length of text
     * @param allowNulls true if text can be null
     * @return true if text is valid by given parameters, false otherwise
     */
    private static boolean nonDependantValidationCheck(String text,int minLength,int maxLength,boolean allowNulls)
    {
        if(text.isEmpty() && !allowNulls)
            return false;
        if(text.isEmpty() && allowNulls)
            return true;
        if(minLength > maxLength)
            throw new IllegalArgumentException("minLength > maxLength.. I am confused.");

        if(text.toCharArray().length > maxLength || text.toCharArray().length < minLength)
            return false;

        return true;
    }

    /**
     * Method to check if text is valid for user first names. This method is used to validate users first name and generate login later
     * @param text text to validate
     * @param minLength min length of text
     * @param maxLength max length of text
     * @param allowNulls true if text can be null
     * @return true if text is valid by given parameters, false otherwise
     */
    public static boolean validLegalNameChars(String text, int minLength, int maxLength,boolean allowNulls)
    {
       if(!nonDependantValidationCheck(text,minLength,maxLength,allowNulls))
           return false;

        for(char x : text.toCharArray())
        {
            if(!allLegalNameChars.contains(""+x))
                return false;
        }

        return true;
    }


    /**
     * Method for text validation
     * @param text text to validate
     * @param minLength min length of text
     * @param maxLength max length of text
     * @param allowNulls true if text can be null
     * @return true if text is valid by given parameters, false otherwise
     */
    public static boolean validTextInput(String text, int minLength, int maxLength,boolean allowNulls)
    {
       if(!nonDependantValidationCheck(text,minLength,maxLength,allowNulls))
        return false;

        for(char x : text.toCharArray())
        {
            if(!allLegalTextChars.contains(""+x))
                return false;
        }

        return true;
    }


}
