package install.task

import install.util.TaskUtil
import temp.util.Encryptor
import temp.util.SEEDUtil

/**
 * Created by sujkim on 2017-03-10.
 */
class Decrypt extends TaskUtil{

    @Override
    Integer run(){

        String value = propman.getString('value')
        String method = propman.getString('method') ?: "SEED"

        logMiddleTitle 'START DECRYPT'

        println "<REQUEST>"
        println "METHOD : ${method}"
        println "VALUE  : ${value}"
        println ""

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
        println "<RESULT>"
        println decryptedText

        logMiddleTitle 'FINISHED CHECK REST'

        return STATUS_TASK_DONE
    }

}

