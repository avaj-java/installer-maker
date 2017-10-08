package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import temp.util.Encryptor
import temp.util.SEEDUtil

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
@TerminalValueProtocol(['value', 'method'])
class Encrypt extends TaskUtil{

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

        logMiddleTitle '<ENCRYPT>'
        println "METHOD : ${method}"
        println "VALUE  : ${value}"
        println ""

        //Encrypt
        String encryptedText

        switch (method){
            case "SEED":
                encryptedText = SEEDUtil.getSeedEncrypt(value, SEEDUtil.getSeedRoundKey("1234567890123456"))
                break
            case "AES":
                encryptedText = new Encryptor().encrypt(value)
                break
            default:
                break
        }

        //LOG
        println "<RESULT>"
        println encryptedText

        logMiddleTitle 'FINISHED ENCRYPT'

        return STATUS_TASK_DONE
    }

}

