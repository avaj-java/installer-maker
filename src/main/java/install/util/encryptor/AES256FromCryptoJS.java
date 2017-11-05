package install.util.encryptor;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Arrays;
import java.util.Random;

public class AES256FromCryptoJS {

    /**
     * Try Test
     * @param args
     * @throws UnsupportedEncodingException
     * @throws GeneralSecurityException
     * @throws DecoderException
     */
    public static void main(String[] args) throws UnsupportedEncodingException, GeneralSecurityException, DecoderException {
        String plainText = "Hello. Tester~!";
        String password = "607db3fd8d54429bb2f80adb6a978ebe";

        String encryptedText = encrypt(plainText, password);
        String decryptedText = decrypt(encryptedText, password);
        System.out.println ( "01. PLAINTEXT : " +plainText );
        System.out.println ( "01. ENCRYPT   : " +encryptedText );
        System.out.println ( "01. DECRYPT   : " +decryptedText );

        assert plainText == decryptedText;
    }



    /**
     * Encrypt
     * @param plaintext plain string
     * @param passphrase passphrase
     * @return
     */
    public static String encrypt(String plaintext, String passphrase) throws InvalidKeyException {
        try {
            final int keySize = 256;
            final int ivSize = 128;

            // Create empty key and iv
            byte[] key = new byte[keySize / 8];
            byte[] iv = new byte[ivSize / 8];

            // Create random salt
            byte[] saltBytes = generateSalt(8);

            // Derive key and iv from passphrase and salt
            EvpKDF(passphrase.getBytes("UTF-8"), keySize, ivSize, saltBytes, key, iv);

            // Actual encrypt
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));

            /**
             * Create CryptoJS-like encrypted string from encrypted data
             * This is how CryptoJS do:
             * 1. Create new byte array to hold ecrypted string (b)
             * 2. Concatenate 8 bytes to b
             * 3. Concatenate salt to b
             * 4. Concatenate encrypted data to b
             * 5. Encode b using Base64
             */
            byte[] sBytes = "Salted__".getBytes("UTF-8");
            byte[] b = new byte[sBytes.length + saltBytes.length + cipherBytes.length];
            System.arraycopy(sBytes, 0, b, 0, sBytes.length);
            System.arraycopy(saltBytes, 0, b, sBytes.length, saltBytes.length);
            System.arraycopy(cipherBytes, 0, b, sBytes.length + saltBytes.length, cipherBytes.length);

//            byte[] base64b = Base64.encode(b, Base64.DEFAULT);
            byte[] base64b = Base64.encodeBase64(b);

            return new String(base64b);
        } catch (InvalidKeyException ike) {
            throw ike;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Decrypt
     * Thanks Artjom B. for this: http://stackoverflow.com/a/29152379/4405051
     * @param ciphertext encrypted string
     * @param passphrase passphrase
     */
    public static String decrypt(String ciphertext, String passphrase) throws InvalidKeyException{
        try {
            final int keySize = 256;
            final int ivSize = 128;

            byte[] ctBytes = Base64.decodeBase64(ciphertext.getBytes("UTF-8"));

            // 8bit is start with 'Salted__'
            byte[] saltBytes = Arrays.copyOfRange(ctBytes, 8, 16);
//            System.out.println( Hex.encodeHexString(saltBytes) );

            byte[] ciphertextBytes = Arrays.copyOfRange(ctBytes, 16, ctBytes.length);

            byte[] key = new byte[keySize / 8];
            byte[] iv = new byte[ivSize / 8];
            EvpKDF(passphrase.getBytes("UTF-8"), keySize, ivSize, saltBytes, key, iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] recoveredPlaintextBytes = cipher.doFinal(ciphertextBytes);

            return new String(recoveredPlaintextBytes);
        } catch (InvalidKeyException ike) {
            throw ike;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] EvpKDF(byte[] password, int keySize, int ivSize, byte[] salt, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
        return EvpKDF(password, keySize, ivSize, salt, 1, "MD5", resultKey, resultIv);
    }

    private static byte[] EvpKDF(byte[] password, int keySize, int ivSize, byte[] salt, int iterations, String hashAlgorithm, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
        keySize = keySize / 32;
        ivSize = ivSize / 32;
        int targetKeySize = keySize + ivSize;
        byte[] derivedBytes = new byte[targetKeySize * 4];
        int numberOfDerivedWords = 0;
        byte[] block = null;
        MessageDigest hasher = MessageDigest.getInstance(hashAlgorithm);
        while (numberOfDerivedWords < targetKeySize) {
            if (block != null) {
                hasher.update(block);
            }
            hasher.update(password);
            // Salting 
            block = hasher.digest(salt);
            hasher.reset();
            // Iterations : (key stretching)
            for (int i = 1; i < iterations; i++) {
                block = hasher.digest(block);
                hasher.reset();
            }
            System.arraycopy(block, 0, derivedBytes, numberOfDerivedWords * 4, Math.min(block.length, (targetKeySize - numberOfDerivedWords) * 4));
            numberOfDerivedWords += block.length / 4;
        }
        System.arraycopy(derivedBytes, 0, resultKey, 0, keySize * 4);
        System.arraycopy(derivedBytes, keySize * 4, resultIv, 0, ivSize * 4);
        return derivedBytes; // key + iv
    }

    /**
     * @return a new pseudorandom salt of the specified length
     */
    private static byte[] generateSalt(int length) {
        Random r = new SecureRandom();
        byte[] salt = new byte[length];
        r.nextBytes(salt);
        return salt;
    }

}