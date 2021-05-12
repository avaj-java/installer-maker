package install.util.encryptor

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.DESedeKeySpec
import java.security.Key

class DES128Util implements EncryptionUtil{

    /*************************
     * Let's Test
     *  - 16 or 24byte key
     *************************/
    public static void main(String[] args) throws Exception{
        String plainText = 'haha$hoho%di2git$spe^cial@cha6r$#~~meta~~stream~~';
        String password = "12345678901234561234567890123456";

        String encryptedText = doEncrypt(plainText, password);
        String decryptedText = doDecrypt(encryptedText, password);
        System.out.println ( "01. PLAINTEXT : " +plainText );
        System.out.println ( "01. ENCRYPT   : " +encryptedText );
        System.out.println ( "01. DECRYPT   : " +decryptedText );

        assert plainText == decryptedText;
    }

    /*************************
     * Static - encrypt
     *************************/
    public static String doEncrypt(String content) throws Exception{
        return new DES128Util().encrypt(content);
    }

    public static String doEncrypt(String content, String key) throws Exception{
        return new DES128Util(key).encrypt(content);
    }

    /*************************
     * Static - decrypt
     *************************/
    public static String doDecrypt(String content) throws Exception{
        return new DES128Util().decrypt(content);
    }

    public static String doDecrypt(String content, String key) throws Exception{
        return new DES128Util(key).decrypt(content);
    }



    /*************************
     * Implement
     *************************/
    DES128Util() {
    }

    DES128Util(String key) {
        if (key)
            this.key = key
    }

    String key = "ab_booktv_abcd09"
    String salt
    String charset = 'UTF-8'
    long iterations = 0



    @Override
    String encrypt(String content) {
        return runEncrypt(content, key)
    }

    @Override
    String decrypt(String encryptedContent) {
        return runDecrypt(encryptedContent, key)
    }


    public static String runEncrypt(String ID, String key) throws Exception {
        if (ID == null || ID.length() == 0)
            return "";

        String instance = (key.length() == 24) ? "DESede/ECB/PKCS5Padding" : "DES/ECB/PKCS5Padding";
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(instance);
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, getProperKey(key));
        String amalgam = ID;

        byte[] inputBytes1 = amalgam.getBytes("UTF8");
        byte[] outputBytes1 = cipher.doFinal(inputBytes1);
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        String outputStr1 = encoder.encode(outputBytes1);
        return outputStr1;
    }

    public static String runDecrypt(String codedID, String key) throws Exception {
        if (codedID == null || codedID.length() == 0)
            return "";

        String instance = (key.length() == 24) ? "DESede/ECB/PKCS5Padding" : "DES/ECB/PKCS5Padding";
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(instance);
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, getProperKey(key));
        sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();

        byte[] inputBytes1 = decoder.decodeBuffer(codedID);
        byte[] outputBytes2 = cipher.doFinal(inputBytes1);

        String strResult = new String(outputBytes2, "UTF8");
        return strResult;
    }


    /**
     * 키값
     * 24바이트인 경우 TripleDES 아니면 DES
     * @return
     * @throws java.lang.Exception
     */
    public static Key getProperKey(String key) throws Exception {
        return (key.length() == 24) ? getKey2(key) : getKey1(key);
    }

    /**
     * 지정된 비밀키를 가지고 오는 메서드 (DES)
     * require Key Size : 16 bytes
     */
    public static Key getKey1(String keyValue) throws Exception {
        DESKeySpec desKeySpec = new DESKeySpec(keyValue.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        Key key = keyFactory.generateSecret(desKeySpec);
        return key;
    }

    /**
     * 지정된 비밀키를 가지고 오는 메서드 (TripleDES)
     * require Key Size : 24 bytes
     */
    public static Key getKey2(String keyValue) throws Exception {
        DESedeKeySpec desKeySpec = new DESedeKeySpec(keyValue.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        Key key = keyFactory.generateSecret(desKeySpec);
        return key;
    }




}
