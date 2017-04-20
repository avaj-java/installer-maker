package install.task

import com.jaemisseo.man.PropMan
import temp.util.SEEDUtil

/**
 * Created by sujkim on 2017-03-10.
 */
class TaskDecrypt extends TaskUtil{

    TaskDecrypt(PropMan propman){
        this.propman = propman
    }



    @Override
    Integer run(){

        String value = propman.getString('value')

        // 암복호화에 사용할 키 배열생성
        int[] seedKey = SEEDUtil.getSeedRoundKey("1234567890123456");


        logMiddleTitle 'START DECRYPT'

        println "<REQUEST>"
        println "${value}"
        println ""

        //Encrypt
        String encryptedText = SEEDUtil.getSeedDecrypt(value, seedKey)

        //LOG
        println "<RESULT>"
        println encryptedText

        logMiddleTitle 'FINISHED CHECK REST'

        return STATUS_TASK_DONE
    }

}

