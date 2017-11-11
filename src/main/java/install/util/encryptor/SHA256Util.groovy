package install.util.encryptor

import java.security.MessageDigest

class SHA256Util implements EncryptionUtil{

    public static void main(String[] args) throws Exception {
        //Info
        String content = "java12345^&*()하하호호"
        //Run
        SHA256Util util = new SHA256Util()
        String e = util.encrypt(content)
        String d = util.decrypt(e)
        //Log
        println content
        println e
        println d
    }

    

    public SHA256Util(String key) {
    }

    String key
    String salt
    String charset = 'UTF-8'
    String iterations = 1

    

    @Override
    String encrypt(String content) {
        StringBuffer sbuf = new StringBuffer()

        //Define
        MessageDigest digest = MessageDigest.getInstance("SHA-256")
        digest.reset()
        //Salt
        digest.update()

        //Encription
        byte[] contentBytes = digest.digest(content.getBytes(charset))

        //Iteration (Encription)
        for (int i=0; i<iterations; i++){
            digest.reset()
            contentBytes = digest.digest(contentBytes)
        }

        for(int i=0; i<contentBytes.length; i++){
            byte tmpStrByte = contentBytes[i]
            String tmpEncTxt = Integer.toString((tmpStrByte & 0xff) + 0x100, 16).substring(1)
            sbuf.append(tmpEncTxt)
        }

        return sbuf.toString()
    }

    @Override
    String decrypt(String encryptedContent) {
        return null
    }
}
