package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.util.TaskUtil
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

        RestMan restman = new RestMan().addHeader(headerMap)
        if (type)
            restman.setType(type)
        if (accept)
            restman.setAccept(accept)

        //START
        logMiddleTitle 'START TESTREST'
        println "<REQUEST> - CHECK"
        println " - URL       : ${url}"
        println " - METHOD    : ${method}"
        println " - TYPE      : ${restman.type}"
        println " - ACCEPT    : ${restman.accept}"
        println " - PARAMETER : ${paramMap}"
        println " - HEADER    : ${headerMap}"
        println ""

        //RUN
        println "<REST>"
        println "Requesting..."
        String response =  restman.request(url, method, paramMap)
        println "Done"

        //RESPONSE
        println "<RESPONSE>"
        println response

        //FINISH
        logMiddleTitle 'FINISHED TESTREST'
        return STATUS_TASK_DONE
    }

}

