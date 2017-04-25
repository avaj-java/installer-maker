package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.RestMan
import temp.util.Encryptor
import temp.util.SEEDUtil

/**
 * Created by sujkim on 2017-03-10.
 */
class TaskEncrypt extends TaskUtil{

    TaskEncrypt(PropMan propman){
        this.propman = propman
    }



    @Override
    Integer run(){

        String value = propman.getString('value')
        String method = propman.getString('method') ?: "SEED"

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

