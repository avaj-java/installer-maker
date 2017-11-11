package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import install.util.encryptor.AES256FromCryptoJS
import install.util.encryptor.AESUtil
import install.util.encryptor.DES128Util
import install.util.encryptor.MD5Util
import install.util.encryptor.SEEDUtil
import install.util.encryptor.SHA1Util
import install.util.encryptor.SHA256Util

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
@TerminalValueProtocol(['method', 'value'])
class Decrypt extends TaskUtil{

    @Value(name='method', caseIgnoreValidList=['aes','aes256','des128','seed128'])
    String method

    @Value('value')
    String value

    @Value('key')
    String key

    @Value('salt')
    String salt

    @Value('charset')
    String charset

    @Value('iterations')
    long iterations

    static final String error1 = "It can not decrypt."


    @Override
    Integer run(){
        String decryptedText

        logger.debug "METHOD : ${method}"
        logger.debug "VALUE  : ${value}"
        logger.debug ""

        //Decrypt
        switch (method.toUpperCase()){
            case Encrypt.AES:
                decryptedText = new AESUtil().decrypt(value)
                break
            case Encrypt.AES256:
                decryptedText = new AES256FromCryptoJS().decrypt(value)
                break
            case Encrypt.DES128:
                decryptedText = new DES128Util().decrypt(value)
                break
            case Encrypt.SEED128:
                decryptedText = SEEDUtil.getSeedDecrypt(value, SEEDUtil.getSeedRoundKey("1234567890123456"))
                break

            case Encrypt.MD5:
                throw new Exception(error1)
                decryptedText = new MD5Util().decrypt(value)
                break
            case Encrypt.SHA1:
                throw new Exception(error1)
                decryptedText = new SHA1Util().decrypt(value)
                break
            case Encrypt.SHA256:
                throw new Exception(error1)
                decryptedText = new SHA256Util().decrypt(value)
                break
            default:
                break
        }

        //LOG
        logger.debug "<RESULT>"
        logger.debug decryptedText

        //Set 'answer' and 'value' Property
        set('value', decryptedText)
        setPropValue()

        return STATUS_TASK_DONE
    }

}

