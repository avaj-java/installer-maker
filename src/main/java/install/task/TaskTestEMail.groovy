package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.RestMan

/**
 * Created by sujkim on 2017-03-10.
 */
class TaskTestEMail extends TaskUtil{

    @Override
    void run(){

        String host         = get('host')
        String port         = get('port')
        String username     = get('username')
        String password     = get('password')
        Map smtpAuth        = parse('smtpAuth')

        logMiddleTitle 'START CHECK REST'
        println 'Not Supported Yet'

        println "<REQUEST> - CHECK"
        println " - host        : ${host}"
        println " - port        : ${port}"
        println " - username    : ${username}"
        println " - password    : ${password}"
        println " - smtpAuth    : ${smtpAuth}"
        println ""

        //REQUEST & GET RESPONSE

        //LOG
        println "<RESPONSE>\n${}"

        logMiddleTitle 'FINISHED CHECK REST'
    }

}

