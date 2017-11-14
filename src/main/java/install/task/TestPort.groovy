package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
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

    @Value('from')
    String from

    @Value('to')
    String to



    @Override
    Integer run(){
        //Ready
        def rangeFrom   = (port ?: from ?: 0) as int
        def rangeTo     = (to ?: rangeFrom) as int
        def ip = ip ?: "127.0.0.1"
        // - No Reverse
        if (rangeFrom > rangeTo){
            def temp = rangeTo
            rangeTo = rangeFrom
            rangeFrom = temp
        }

        //START
        logger.debug "START TESTPORT (${rangeFrom}${(rangeFrom!=rangeTo)?' to '+rangeTo:''})"

        //RUN
        Map portMap = getUsingPortMap(rangeFrom, rangeTo, ip)
        logger.debug "\n - Port Count (You Can Use): ${portMap.findAll{ !it.value }.size()}"

        //FINISH
        return STATUS_TASK_DONE
    }



    private Map getUsingPortMap(Integer rangeFrom){
        return getUsingPortMap(rangeFrom, rangeFrom)
    }

    private Map getUsingPortMap(Integer rangeFrom, Integer rangeTo, String ip){
        Map portMap = [:]
        for (int port=rangeFrom; port<=rangeTo; port++){
            if (testPort(ip, port)){
                portMap[port] = "is Being Used"
            }else{
                portMap[port] = null

            }
        }
        return portMap
    }

    boolean testPort(String ipport){
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
