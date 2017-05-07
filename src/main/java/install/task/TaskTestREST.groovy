package install.task

import jaemisseo.man.PropMan
import jaemisseo.man.RestMan

/**
 * Created by sujkim on 2017-03-10.
 */
class TaskTestREST extends TaskUtil{

    TaskTestREST(PropMan propman){
        this.propman = propman
    }



    @Override
    Integer run(){
        String url      = get('url')
        String method   = get('method') ?: "POST"
        String type     = get('type')
        String accept   = get('accept')
        Map paramMap    = parse('param')
        Map headerMap   = parse('header')

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

