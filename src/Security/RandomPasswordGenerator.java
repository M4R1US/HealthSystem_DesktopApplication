package Security;

import java.util.Random;

/**
 * <h2>Created by Marius B on 09-Feb-17.</h2>
 * <p>Class to generate random password</p>
 */
public final class RandomPasswordGenerator {

    private static String letters = "qwertyuiopasdfghjklzxcvbnm";
    private static String integers = "1234567890";
    private static final String passwordChars = letters + letters.toUpperCase() + integers;
    private static final int PW_LENGTH = 8;
    private static Random random;

    /**
     * Method to generate random password
     * @return generated random password
     */
    public static String generatePassword()
    {
        random = new Random();
        String password = "";
        for(int i = 0; i < PW_LENGTH; i++)
        {
            password += passwordChars.charAt(random.nextInt(passwordChars.length()));
        }
        return password;
    }

}
