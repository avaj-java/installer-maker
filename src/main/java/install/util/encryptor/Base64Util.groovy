package install.util.encryptor

import org.apache.commons.codec.binary.Base64

class Base64Util  implements EncryptionUtil{


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
