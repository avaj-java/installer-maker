package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.RestMan

/**
 * Created by sujkim on 2017-03-10.
 */
class TaskTestEMail extends TaskUtil{

    TaskTestEMail(PropMan propman){
        this.propman = propman
    }



    @Override
    void run(String propertyPrefix){

        String host      = propman.get('host')
        String port     = propman.get('port')
        String username   = propman.get('username')
        String password   = propman.get('password')
        Map smtpAuth    = propman.parse('smtpAuth')

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

