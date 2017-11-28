package install.util.encryptor

import java.security.MessageDigest

class MD5Util implements EncryptionUtil{

    /*************************
     * Let's Test
     *  -
     *************************/
    public static void main(String[] args) throws Exception {
        String plainText = '하하하$호호%숫2자$특^수@문6자$#~~meta~~stream~~';
        String password = "12345678901234561234567890123456";

        String encryptedText = doEncrypt(plainText, password);
        System.out.println ( "01. PLAINTEXT : " +plainText );
        System.out.println ( "01. ENCRYPT   : " +encryptedText );

        assert plainText != encryptedText;
    }

    /*************************
     * Static - encrypt
     *************************/
    public static String doEncrypt(String content) throws Exception{
        return new MD5Util().encrypt(content);
    }

    public static String doEncrypt(String content, String key) throws Exception{
        return new MD5Util(key).encrypt(content);
    }




    /*************************
     * Implement
     *************************/
    MD5Util(){
    }

    MD5Util(key){
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
