package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import install.util.encryptor.Encryptor
import install.util.encryptor.SEEDUtil

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
@TerminalValueProtocol(['value', 'method'])
class Decrypt extends TaskUtil{

    @Value('method')
    String method

    @Value('value')
    String value

    @Value('key')
    String key

    @Value('salt')
    String salt

    @Value('charset')
    String charset  



    @Override
    Integer run(){
        method = method ?: "SEED"

        logger.debug "METHOD : ${method}"
        logger.debug "VALUE  : ${value}"
        logger.debug ""

        //Encrypt
        String decryptedText

        switch (method){
            case "SEED":
                decryptedText = SEEDUtil.getSeedDecrypt(value, SEEDUtil.getSeedRoundKey("1234567890123456"))
                break
            case "AES":
                decryptedText = new Encryptor().decrypt(value)
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

