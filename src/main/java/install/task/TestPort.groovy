package install.task

import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil

/**
 * Created by sujkim on 2017-02-27.
 */
@Task
@TerminalValueProtocol(['port'])
class TestPort extends TaskUtil{

    @Value('ip')
    String ip

    @Value('port')
    String port



    @Override
    Integer run(){
        //RUN
        if (ip)
            testPort(ip, port)
        else
            testPort(port)

        //FINISH
        return STATUS_TASK_DONE
    }




    boolean testPort(String ipport){
        if (!ipport){
            logger.warn "Empty port number"
            return false
        }

        boolean result
        List<Integer> testPortList = []
        //Analysis IP
        String ip
        String port
        if (ipport.contains(":")){
            List<String> elements = ipport.split(":").toList()
            ip = elements[0]
            port = elements[1]
        }else{
            port = ipport
        }
        //Analysis Port Range
        if (port.contains("-")){
            List elements = port.split("-")
            int from = Integer.parseInt(elements[0])
            int to = Integer.parseInt(elements[1])
            testPortList = new IntRange(from, to).toList()
        }else{
            testPortList << Integer.parseInt(port)
        }
        //Test Port
        result = testPortList.every{ Integer portNumber -> testPort(ip, portNumber)}
        return result
    }

    boolean testPort(String ip, Integer port){
        ip = ip ?: '127.0.0.1'
        try{
            Socket s = new Socket(ip, port)
            s.close()
            //Already Used
            logger.info("  - Port ${port} is in use.")
            return true
        }catch(Exception e){
            //No Usage
            logger.info("  - Port ${port} is not used.")
            return false
        }
    }

}
