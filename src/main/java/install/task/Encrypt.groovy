package install.task

import install.annotation.Task
import install.annotation.Value
import install.util.TaskUtil
import temp.util.Encryptor
import temp.util.SEEDUtil

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
class Encrypt extends TaskUtil{

    @Value(property='value', method='getString')
    String value

    @Value(property='method', method='getString')
    String method



    @Override
    Integer run(){
        method = method ?: "SEED"

        logMiddleTitle 'START ENCRYPT'

        println "<REQUEST>"
        println "METHOD : ${method}"
        println "VALUE  : ${value}"
        println ""

        //Encrypt
        String encryptedText

        switch (method){
            case "SEED":
                encryptedText = SEEDUtil.getSeedDecrypt(value, SEEDUtil.getSeedRoundKey("1234567890123456"))
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

        logMiddleTitle 'FINISHED CHECK REST'

        return STATUS_TASK_DONE
    }

}

