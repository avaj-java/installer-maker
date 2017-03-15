package install.task

/**
 * Created by sujkim on 2017-02-27.
 */
class TaskTestPort extends TaskUtil{

    @Override
    void run(Map prop) {
        //Ready

        def rangeFrom   = (prop['port'] ?: prop['from'] ?: 0) as int
        def rangeTo     = (prop['to'] ?: rangeFrom) as int
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
    }



    private Map getUsingPortMap(Integer rangeFrom){
        return getUsingPortMap(rangeFrom, rangeFrom)
    }

    private Map getUsingPortMap(Integer rangeFrom, Integer rangeTo){
        Map portMap = [:]
        for (int port=rangeFrom; port<=rangeTo; port++){
            try{
                Socket s = new Socket("127.0.0.1", port)
                System.out.println(port + " is Being Used")
                portMap[port] = "is Being Used"
                s.close()
            }catch (Exception e){
                System.out.println(port + " is No Use")
                portMap[port] = null
            }
        }
        return portMap
    }

}
