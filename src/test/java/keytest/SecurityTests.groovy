package keytest

import install.util.encryptor.crypto.RSAGreatMan
import install.util.encryptor.crypto.Security
import org.junit.Test

import java.security.KeyPair
import java.security.NoSuchAlgorithmException

/**
 * @author minhyeok
 */
public class SecurityTests {

    @Test
    public void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPair keyPair = Security.generateKeyPair();
        assert keyPair.getPrivate() != null;
        assert keyPair.getPublic() != null;
    }

    @Test
    public void encryptAndDecrypt() throws NoSuchAlgorithmException {
        String plainText = "{}";
        KeyPair keyPair = Security.generateKeyPair();

        byte[] encodedPublicKey = keyPair.getPublic().getEncoded();
        byte[] encodedPrivateKey = keyPair.getPrivate().getEncoded();

        String cipherText = Security.encrypt(plainText, encodedPublicKey);
        String decryptedText = Security.decrypt(cipherText, encodedPrivateKey);

        println " - text: ${plainText}"
        println " - enc : ${cipherText}"
        println " - dec : ${decryptedText}"
        assert plainText == decryptedText;
    }

    @Test
    public void signAndVerify() throws NoSuchAlgorithmException {
        String plainText = "{}";
        KeyPair keyPair = Security.generateKeyPair();

        byte[] encodedPrivateKey = keyPair.getPrivate().getEncoded();
        byte[] encodedPublicKey = keyPair.getPublic().getEncoded();

        String signature = Security.sign(plainText, encodedPrivateKey);
        println " - signature: ${signature}"
        assert signature != null;

        boolean result = Security.verify(plainText, signature, encodedPublicKey);
        assert result == true;
    }

    @Test(expected = RSAGreatMan.InvalidSignatureException.class)
    public void signAndVerifyButGoogleSecurityException() throws NoSuchAlgorithmException {
        String plainText = "{}";
        KeyPair keyPair = Security.generateKeyPair();
        KeyPair otherKeyPair = Security.generateKeyPair();

        byte[] encodedPrivateKey = keyPair.getPrivate().getEncoded();
        byte[] encodedPublicKey = otherKeyPair.getPublic().getEncoded();

        String signature = Security.sign(plainText, encodedPrivateKey);
        assert signature != null;

        Security.verify(plainText, signature, encodedPublicKey);
    }
}