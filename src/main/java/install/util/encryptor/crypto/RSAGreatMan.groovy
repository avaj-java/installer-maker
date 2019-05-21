package install.util.encryptor.crypto

import groovy.json.JsonOutput
//import org.apache.commons.net.util.Base64
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class RSAGreatMan {

    private static final int DEFAULT_KEY_SIZE = 2048;

    private static final String KEY_FACTORY_ALGORITHM = "RSA";

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private static final String CHARSET = "UTF-8";

    String publicKeyAsHexString = ''
    String privateKeyAsHexString = ''

    class InvalidSignatureException extends RuntimeException {
        InvalidSignatureException(String message) {
            super(message);
        }
    }


    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        RSAGreatMan rsaman = new RSAGreatMan()
        rsaman.makeKeyPair()
        rsaman.test("암호화된 문자열 abcdefg hijklmn")
        rsaman.test('hi Test Ya')
        rsaman.test('이거슨 테스트야')
        rsaman.test('하하aa 11테스트 하는중 12451번째')
        rsaman.test(' ㄹ%@52 ㅏ하하 도도여뱌쟈탸 ../ TEST /..; ')
    }





    /**************************************************
     *
     *  Test
     *
     **************************************************/
    void test(String text){
        test(text, this.publicKeyAsHexString, privateKeyAsHexString)
    }

    void test(String text, String publicKeyAsHexString, String privateKeyAsHexString){
        String encrypted = RSAGreatMan.encrypt(text, publicKeyAsHexString)
        String decrypted = RSAGreatMan.decrypt(encrypted, privateKeyAsHexString)
        String signature = RSAGreatMan.sign(encrypted, privateKeyAsHexString)
        println """
         - text       : ${text}"
         - publicKey  : ${publicKeyAsHexString}
         - privateKey : ${privateKeyAsHexString}
         - encrypted  : ${encrypted}
         - signature  : ${signature}
        """
        assert text == decrypted
        assert RSAGreatMan.verify(encrypted, signature, publicKeyAsHexString)
    }



    /**************************************************
     *
     *  KeyPair Generator
     *
     **************************************************/
    static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_FACTORY_ALGORITHM);
        generator.initialize(DEFAULT_KEY_SIZE, new SecureRandom());
        return generator.generateKeyPair()
    }

    RSAGreatMan makeKeyPair(){
        KeyPair pair = generateKeyPair()
        this.publicKeyAsHexString = byteArrayToHex(pair.getPublic().getEncoded())
        this.privateKeyAsHexString = byteArrayToHex(pair.getPrivate().getEncoded())
        return this
    }


    /**************************************************
     *
     *  Encrypt
     *
     **************************************************/
    String encrypt(String text){
        encrypt(text, this.publicKeyAsHexString)
    }

    static String encrypt(String text, String publicKeyAsHexString) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return encrypt(text, hexToByteArray(publicKeyAsHexString))
    }

    static String encrypt(String text, byte[] publicKeyBytes) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        PublicKey publicKey = generatePublicKey(publicKeyBytes);
        try {
            Cipher cipher = Cipher.getInstance(KEY_FACTORY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] bytes = cipher.doFinal(text.getBytes(CHARSET));
//            return Base64.getEncoder().encodeToString(bytes);
            return Base64.encodeBase64String(bytes);
//        } catch (NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
        } catch (NoSuchPaddingException e){ throw new RuntimeException(e); }catch( InvalidKeyException e){ throw new RuntimeException(e); }catch( UnsupportedEncodingException e){ throw new RuntimeException(e); }catch( IllegalBlockSizeException e){ throw new RuntimeException(e); }catch( BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**************************************************
     *
     *  Decrypt
     *
     **************************************************/
    String decrypt(String text){
        decrypt(text, this.privateKeyAsHexString)
    }

    static String decrypt(String text, String privateKeyAsHexString) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return decrypt(text, hexToByteArray(privateKeyAsHexString))
    }

    static String decrypt(String encryptedText, byte[] privateKeyBytes) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException{
        PrivateKey privateKey = generatePrivateKey(privateKeyBytes);
        try {
//            byte[] bytes = Base64.getDecoder().decode(cipherText);
            byte[] bytes = Base64.decodeBase64(encryptedText);
            Cipher cipher = Cipher.getInstance(KEY_FACTORY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(bytes), CHARSET);
//        } catch (NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
        } catch (NoSuchPaddingException e){ throw new RuntimeException(e); }catch( InvalidKeyException e){ throw new RuntimeException(e); }catch( UnsupportedEncodingException e){ throw new RuntimeException(e); }catch( IllegalBlockSizeException e){ throw new RuntimeException(e); }catch( BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }



    /**************************************************
     *
     *  Sign
     *
     **************************************************/
    String sign(Object object){
        String text = JsonOutput.toJson(object)
        return sign(text, this.privateKeyAsHexString)
    }

    String sign(String text){
        return sign(text, this.privateKeyAsHexString)
    }

    /**
     * Create signature by signing
     * @param text the signed JSON string (signed, not encrypted)
     * @param encodedPrivateKey the base64-encoded private key to use for signing.
     * @return signature text
     */
    static String sign(Object object, String privateKeyAsHexString) {
        String text = JsonOutput.toJson(object)
        return sign(text, hexToByteArray(privateKeyAsHexString))
    }

    static String sign(String text, String privateKeyAsHexString) {
        return sign(text, hexToByteArray(privateKeyAsHexString))
    }

    static String sign(String text, byte[] encodedPrivateKey) {
        try {
            Signature privateSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
            privateSignature.initSign(generatePrivateKey(encodedPrivateKey));
            privateSignature.update(text.getBytes(CHARSET));
            byte[] signature = privateSignature.sign();
//            return Base64.getEncoder().encodeToString(signature);
            return Base64.encodeBase64String(signature);
//        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
        } catch (NoSuchAlgorithmException e){ throw new RuntimeException(e); } catch (InvalidKeyException e){ throw new RuntimeException(e); } catch (UnsupportedEncodingException e){ throw new RuntimeException(e); } catch (SignatureException e){
            throw new RuntimeException(e);
        }
    }

    /**************************************************
     *
     *  Verify
     *
     **************************************************/
    boolean verify(String text, String signature){
        return verify(text, signature, hexToByteArray(this.publicKeyAsHexString))
    }

    /**
     * Verifies that the data was signed with the given signature, and returns
     * the verified purchase. The data is in JSON format and signed
     * and product ID of the purchase.
     * @param text the signed JSON string (signed, not encrypted)
     * @param signature the signature for the data, signed with the private key
     * @param encodedPublicKey the base64-encoded public key to use for verifying.
     * @return result for verification
     */
    static boolean verify(String text, String signature, String publicKeyAsHexString) {
        return verify(text, signature, hexToByteArray(publicKeyAsHexString))
    }

    static boolean verify(String text, String signature, byte[] encodedPublicKey) {
        PublicKey publicKey = generatePublicKey(encodedPublicKey);
        return verifySignarue(text, signature, publicKey);
    }

    private static boolean verifySignarue(String text, String signature, PublicKey publicKey) {
        Signature sig;
        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(text.getBytes());
//            if (!sig.verify(Base64.getDecoder().decode(signature)))
            if (!sig.verify(Base64.decodeBase64(signature)))
                throw new InvalidSignatureException("It was awesome! Signature hasn't be invalid");
//        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
        } catch (NoSuchAlgorithmException e){ throw new RuntimeException(e); }catch( InvalidKeyException e){ throw new RuntimeException(e); }catch( SignatureException e) {
            throw new RuntimeException(e);
        }
        return true;
    }



    /**************************************************
     *
     *  Util
     *
     **************************************************/
    private static PublicKey generatePublicKey(byte[] encodedPublicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(encodedPublicKey));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static PrivateKey generatePrivateKey(byte[] encodedPrivateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedPrivateKey));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // hex string to byte[]
    public static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }
        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }

    // byte[] to hex sting
    public static String byteArrayToHex(byte[] ba) {
        if (ba == null || ba.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer(ba.length * 2);
        String hexNumber;
        for (int x = 0; x < ba.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xff & ba[x]);

            sb.append(hexNumber.substring(hexNumber.length() - 2));
        }
        return sb.toString();
    }

}
