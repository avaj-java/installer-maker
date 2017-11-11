package install.util.encryptor

interface EncryptionUtil {

    String encrypt(String content)

    String decrypt(String encryptedContent)

}