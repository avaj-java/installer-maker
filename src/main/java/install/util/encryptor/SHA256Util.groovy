package install.util.encryptor

import java.security.MessageDigest

class SHA256Util implements EncryptionUtil{

    /*************************
     * Let's Test
     *  -
     *************************/
    public static void main(String[] args) throws Exception {
        String plainText = 'haha$hoho%di2git$spe^cial@cha6r$#~~meta~~stream~~';
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
        return new SHA256Util().encrypt(content);
    }

    public static String doEncrypt(String content, String key) throws Exception{
        return new SHA256Util(key).encrypt(content);
    }




    /*************************
     * Implement
     *************************/
    public SHA256Util() {
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
