package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import jaemisseo.man.SocketMan

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
@TerminalValueProtocol(['ip', 'port', 'msg'])
class TestSocket extends TaskHelper{

    @Value('timeout')
    Integer timeout

    @Value('charset')
    String charset

    @Value('ip')
    String ip

    @Value('port')
    String port

    @Value('msg')
    String msg



    @Override
    Integer run(){
        timeout  = timeout ?: 1000
        charset  = charset ?: 'euc-kr'
        ip       = ip ?: '127.0.0.1'
        port     = port ?: '5000'
        msg      = msg ?: null
        String response

        //START
        logger.debug "<REQUEST> - CHECK"
        logger.debug " - IP      : ${ip}"
        logger.debug " - PORT    : ${port}"
        logger.debug " - TIMEOUT : ${timeout}"
        logger.debug " - CHARSET : ${charset}"
        logger.debug " - MSG     : ${msg}"
        logger.debug ""

        //RUN
        logger.debug "<Socket>"
        logger.debug "Sending..."
        new SocketMan()
                .setTimeout(timeout)
                .setCharset(charset)
                .connect("${ip}:${port}"){ SocketMan socketMan ->
                    logger.debug "1. Connect To Server - OK\n"
                    if (msg){
                        socketMan.send(msg)
                        logger.debug "2. Send Message To Server - OK\n"
                    }else{
                        logger.debug "2. Send Message To Server - NO MESSAGE, NO SEND\n"
                    }

                    socketMan.disconnect()
                    logger.debug "3. Disconnected From Server - OK\n"
                }
        logger.debug "Done"

        //RESPONSE
        logger.debug "<RESPONSE (Sorry, Not Supported)>\n${response}"

        //FINISH
        return STATUS_TASK_DONE
    }

//    boolean testSocketServer(){
//        SocketMan socketMan = new SocketMan()
//        logger.debug '///////////////////////////////////'
//        logger.debug '//// socket receiver try to run ///'
//        logger.debug '///////////////////////////////////'
//        socketMan.setModeIndependent(true)
//                .setTimeout(3000)
//                .setCharset('euc-kr')
//                .echoServer(socketServerPort){
//            logger.debug '///////////////////////////'
//            logger.debug '///// Socket Receiver /////'
//            logger.debug '///////////////////////////'
//            String msg = it.receivedMsg
//            String charset = it.charset
//            lastRepoReq = "LENGTH:${msg.length()}\nLENGTH(byte):${msg.getBytes(charset).length}\n${msg}"
//            repoReqList << new Request(index:++index, date:new Date(), dataStr:"${lastRepoReq}")
//            if (repoReqList.size() >= 11) repoReqList.remove(0)
//
//            // LOG
//            logger.debug '///// Socket Message'
//            repoReqList.each{
//                logger.debug JsonOutput.toJson(it)
//            }
//            logger.debug ''
//        }
//        logger.debug '///////////////////////////////////'
//        logger.debug '//// socket receiver is running ///'
//        logger.debug '///////////////////////////////////'
//        return true
//    }

}
