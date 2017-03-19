package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.SocketMan

/**
 * Created by sujkim on 2017-03-10.
 */
class TaskTestSocket extends TaskUtil{

    TaskTestSocket(PropMan propman){
        this.propman = propman
    }



    @Override
    void run(String propertyPrefix){
        Integer timeout = propman.get('timeout') ?: 1000
        String charset  = propman.get('charset') ?: 'euc-kr'
        String ip       = propman.get('ip') ?: '127.0.0.1'
        String port     = propman.get('port') ?: '5000'
        String msg      = propman.get('msg') ?: null
        String response

        logMiddleTitle 'START CHECK SOCKET'
        println "<REQUEST> - CHECK"
        println " - IP      : ${ip}"
        println " - PORT    : ${port}"
        println " - TIMEOUT : ${timeout}"
        println " - CHARSET : ${charset}"
        println " - MSG     : ${msg}"
        println ""

        //SEND
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

        println "<RESPONSE (Sorry, Not Supported)>\n${response}"
        logMiddleTitle 'FINISHED CHECK SOCKET'
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
