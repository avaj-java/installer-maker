package install.util.encryptor

import org.apache.commons.codec.binary.Base64

class Base64Util  implements EncryptionUtil{

    /*************************
     * Let's Test
     *  -
     *************************/
    public static void main(String[] args) throws Exception{
        String plainText = '하하하$호호%숫2자$특^수@문6자$~~meta~~stream~~';

        String encryptedText = doEncrypt(plainText);
        String decryptedText = doDecrypt(encryptedText);
        System.out.println ( "01. PLAINTEXT : " +plainText );
        System.out.println ( "01. ENCRYPT   : " +encryptedText );
        System.out.println ( "01. DECRYPT   : " +decryptedText );

        assert plainText == decryptedText;
    }

    /*************************
     * Static - encrypt
     *************************/
    public static String doEncrypt(String content) throws Exception{
        return new Base64Util().encrypt(content);
    }

    /*************************
     * Static - decrypt
     *************************/
    public static String doDecrypt(String content) throws Exception{
        return new Base64Util().decrypt(content);
    }



    /*************************
     * Implement
     *************************/
    Base64Util(){
    }

    String charset = "UTF-8"



    String encrypt(String content){
        String base64EncryptedContent = Base64.encodeBase64String(content.getBytes(charset))
        return base64EncryptedContent
    }

    String decrypt(String base64EncryptedContent){
        byte[] valueDecoded= Base64.decodeBase64(base64EncryptedContent)
        return new String(valueDecoded, charset)
    }

}
