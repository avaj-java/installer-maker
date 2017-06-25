package install.task

import install.annotation.Task
import install.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.PropMan
import jaemisseo.man.RestMan

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
class TestREST extends TaskUtil{

    @Value('url')
    String url

    @Value('method')
    String method

    @Value('type')
    String type

    @Value('accept')
    String accept

    @Value(property='param', method='parse')
    Map paramMap

    @Value(property='header', method='parse')
    Map headerMap



    @Override
    Integer run(){
        method = method ?: "POST"

        //REQUEST & GET RESPONSE
        RestMan restman = new RestMan().addHeader(headerMap)
        if (type)
            restman.setType(type)
        if (accept)
            restman.setAccept(accept)

        logMiddleTitle 'START CHECK REST'

        println "<REQUEST> - CHECK"
        println " - URL       : ${url}"
        println " - METHOD    : ${method}"
        println " - TYPE      : ${restman.type}"
        println " - ACCEPT    : ${restman.accept}"
        println " - PARAMETER : ${paramMap}"
        println " - HEADER    : ${headerMap}"
        println ""

        String response =  restman.request(url, method, paramMap)


        //LOG
        println "<RESPONSE>\n${response}"

        logMiddleTitle 'FINISHED CHECK REST'

        return STATUS_TASK_DONE
    }

}

