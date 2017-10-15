package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import jaemisseo.man.RestMan

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
@TerminalValueProtocol(['url', 'method', 'param'])
class TestREST extends TaskUtil{

    @Value('url')
    String url

    @Value('method')
    String method

    @Value('type')
    String type

    @Value('accept')
    String accept

    @Value('param')
    Map paramMap

    @Value('header')
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
        logger.debug "<REQUEST> - CHECK"
        logger.debug " - URL       : ${url}"
        logger.debug " - METHOD    : ${method}"
        logger.debug " - TYPE      : ${restman.type}"
        logger.debug " - ACCEPT    : ${restman.accept}"
        logger.debug " - PARAMETER : ${paramMap}"
        logger.debug " - HEADER    : ${headerMap}"
        logger.debug ""

        //RUN
        logger.debug "<REST>"
        logger.debug "Requesting..."
        String response =  restman.request(url, method, paramMap)
        logger.debug "Done"

        //RESPONSE
        logger.debug "<RESPONSE>"
        logger.debug response

        //FINISH
        return STATUS_TASK_DONE
    }

}

