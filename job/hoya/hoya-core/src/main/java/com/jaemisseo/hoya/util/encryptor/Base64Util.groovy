package com.jaemisseo.hoya.util.encryptor

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
        String base64EncryptedContent = java.util.Base64.encodeBase64String(content.getBytes(charset))
        return base64EncryptedContent
    }

    String decrypt(String base64EncryptedContent){
        byte[] valueDecoded= java.util.Base64.decodeBase64(base64EncryptedContent)
        return new String(valueDecoded, charset)
    }

}
