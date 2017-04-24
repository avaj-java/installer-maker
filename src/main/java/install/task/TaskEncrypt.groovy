package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.RestMan
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

        // 암복호화에 사용할 키 배열생성
        int[] seedKey = SEEDUtil.getSeedRoundKey("1234567890123456");


        logMiddleTitle 'START ENCRYPT'

        println "<REQUEST>"
        println "${value}"
        println ""

        //Encrypt
        String encryptedText = SEEDUtil.getSeedEncrypt(value, seedKey)

        //LOG
        println "<RESULT>"
        println encryptedText

        logMiddleTitle 'FINISHED CHECK REST'

        return STATUS_TASK_DONE
    }

}

