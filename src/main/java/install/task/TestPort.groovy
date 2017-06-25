package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.util.TaskUtil

/**
 * Created by sujkim on 2017-02-27.
 */
@Task
class TestPort extends TaskUtil{

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
        // - No Reverse
        if (rangeFrom > rangeTo){
            def temp = rangeTo
            rangeTo = rangeFrom
            rangeFrom = temp
        }

        //DO TEST
        logMiddleTitle "START CHECK PORT (${rangeFrom}${(rangeFrom!=rangeTo)?' to '+rangeTo:''})"
        Map portMap = getUsingPortMap(rangeFrom, rangeTo)
        println "\n - Port Count (You Can Use): ${portMap.findAll{ !it.value }.size()}"

        return STATUS_TASK_DONE
    }



    private Map getUsingPortMap(Integer rangeFrom){
        return getUsingPortMap(rangeFrom, rangeFrom)
    }

    private Map getUsingPortMap(Integer rangeFrom, Integer rangeTo){
        Map portMap = [:]
        for (int port=rangeFrom; port<=rangeTo; port++){
            try{
                Socket s = new Socket("127.0.0.1", port)
                println(port + " is Being Used")
                portMap[port] = "is Being Used"
                s.close()
            }catch (Exception e){
                println(port + " is No Use")
                portMap[port] = null
            }
        }
        return portMap
    }

}
