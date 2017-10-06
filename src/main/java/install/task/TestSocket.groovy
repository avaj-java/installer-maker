package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import jaemisseo.man.SocketMan

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
@TerminalValueProtocol(['ip', 'port', 'msg'])
class TestSocket extends TaskUtil{

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
        logMiddleTitle 'START SOCKET'
        println "<REQUEST> - CHECK"
        println " - IP      : ${ip}"
        println " - PORT    : ${port}"
        println " - TIMEOUT : ${timeout}"
        println " - CHARSET : ${charset}"
        println " - MSG     : ${msg}"
        println ""

        //RUN
        println "<Socket>"
        println "Sending..."
        new SocketMan()
                .setTimeout(timeout)
                .setCharset(charset)
                .connect("${ip}:${port}"){ SocketMan socketMan ->
                    println "1. Connect To Server - OK\n"
                    if (msg){
                        socketMan.send(msg)
                        println "2. Send Message To Server - OK\n"
                    }else{
                        println "2. Send Message To Server - NO MESSAGE, NO SEND\n"
                    }

                    socketMan.disconnect()
                    println "3. Disconnected From Server - OK\n"
                }
        println "Done"

        //RESPONSE
        println "<RESPONSE (Sorry, Not Supported)>\n${response}"

        //FINISH
        logMiddleTitle 'FINISHED SOCKET'
        return STATUS_TASK_DONE
    }

//    boolean testSocketServer(){
//        SocketMan socketMan = new SocketMan()
//        println '///////////////////////////////////'
//        println '//// socket receiver try to run ///'
//        println '///////////////////////////////////'
//        socketMan.setModeIndependent(true)
//                .setTimeout(3000)
//                .setCharset('euc-kr')
//                .echoServer(socketServerPort){
//            println '///////////////////////////'
//            println '///// Socket Receiver /////'
//            println '///////////////////////////'
//            String msg = it.receivedMsg
//            String charset = it.charset
//            lastRepoReq = "LENGTH:${msg.length()}\nLENGTH(byte):${msg.getBytes(charset).length}\n${msg}"
//            repoReqList << new Request(index:++index, date:new Date(), dataStr:"${lastRepoReq}")
//            if (repoReqList.size() >= 11) repoReqList.remove(0)
//
//            // LOG
//            println '///// Socket Message'
//            repoReqList.each{
//                println JsonOutput.toJson(it)
//            }
//            println ''
//        }
//        println '///////////////////////////////////'
//        println '//// socket receiver is running ///'
//        println '///////////////////////////////////'
//        return true
//    }

}
