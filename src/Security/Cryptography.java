package Security;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

/**
 * <h2>Created by Marius Baltramaitis on 07/10/2017.</h2>
 *
 * <p>Class for encrypting/decrypting data</p>
 */
public class Cryptography {

    /**
     * Default constructor
     */
    public Cryptography() {
    }

    /**
     * Method to encrypt single text
     *
     * @param textToEncrypt text to encrypt
     * @param aes128Key     aes128 bits key
     * @return encrypted text
     * @throws Exception if encryption couldn't be done
     */
    public String encrypt(String textToEncrypt, String aes128Key) throws Exception {

        Key aesKey = new SecretKeySpec(aes128Key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(textToEncrypt.getBytes());
        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedString = encoder.encodeToString(encrypted);
        return encryptedString;
    }

    /**
     * Method to decrypt encrypted text
     *
     * @param encryptedText text to decrypt
     * @param aes128Key     aes128 bits key
     * @return decrypted text
     * @throws Exception if decryption couldn't be done
     */
    public String decrypt(String encryptedText, String aes128Key) throws Exception {
        Key aesKey = new SecretKeySpec(aes128Key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        Base64.Decoder decoder = Base64.getDecoder();
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        String decrypted = new String(cipher.doFinal(decoder.decode(encryptedText)));
        return decrypted;
    }
}
