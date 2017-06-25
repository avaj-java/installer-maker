package install.task

import install.annotation.Task
import install.annotation.Value
import install.util.TaskUtil

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
class TestEMail extends TaskUtil{

    @Value('host')
    String host

    @Value('port')
    String port

    @Value('username')
    String username

    @Value('password')
    String password

    @Value(property='smtpAuth', method='parse')
    Map smtpAuth



    @Override
    Integer run(){

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

        return STATUS_TASK_DONE
    }

}

