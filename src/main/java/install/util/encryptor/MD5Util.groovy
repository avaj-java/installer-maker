package install.util.encryptor

import java.security.MessageDigest

class MD5Util implements EncryptionUtil{

    public static void main(String[] args) throws Exception {
        //Info
        String content = "java12345^&*()하하호호"
        //Run
        MD5Util util = new MD5Util()
        String e = util.encrypt(content)
        String d = util.decrypt(e)
        //Log
        println content
        println e
        println d
    }



    @Override
    String encrypt(String content) {
        StringBuffer sbuf = new StringBuffer()

        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        mDigest.update(content.getBytes())
        byte[] bytes = mDigest.digest()

        for(int i=0; i < bytes.length; i++){
            String tmpEncTxt = Integer.toHexString((int)bytes[i] & 0x00ff)
            sbuf.append(tmpEncTxt)
        }
        return sbuf.toString()
    }

    @Override
    String decrypt(String encryptedContent) {
        return null
    }

}
