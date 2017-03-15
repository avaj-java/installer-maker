package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.RestMan

/**
 * Created by sujkim on 2017-03-10.
 */
class TaskTestREST extends TaskUtil{

    @Override
    void run(Map prop){
        PropMan propman = new PropMan(prop)
        String url      = propman.get('url')
        String type     = propman.get('type')
        String method   = propman.get('method') ?: "POST"
        String accept   = propman.get('accept')
        Map paramMap    = propman.parse('param')
        Map headerMap   = propman.parse('header')

        logMiddleTitle 'START CHECK REST'

        println "<REQUEST> - CHECK"
        println " - URL       : ${url}"
        println " - METHOD    : ${method}"
        println " - PARAMETER : ${paramMap}"
        println " - HEADER    : ${headerMap}"
        println ""

        //REQUEST & GET RESPONSE
        String response = new RestMan()
                                .addHeader(headerMap)
                                .request(url, paramMap, method)

        //LOG
        println "<RESPONSE>\n${response}"

        logMiddleTitle 'FINISHED CHECK REST'
    }

}

