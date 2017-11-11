package install.util.encryptor

import org.apache.commons.codec.binary.Base64

class Base64Util  implements EncryptionUtil{

    public static void main(String[] args) throws Exception {
        //Info
        String content = "java12345^&*()하하호호"
        //Run
        Base64Util util = new Base64Util()
        String e = util.encrypt(content)
        String d = util.decrypt(e)
        //Log
        println content
        println e
        println d
        //Assert
        assert content == d
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
