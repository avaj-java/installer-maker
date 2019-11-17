package install

import jaemisseo.man.TimeMan
import jaemisseo.man.configuration.Config
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Starter {

    static final Logger logger = LoggerFactory.getLogger(getClass());



    /*************************
     * START INSTALLER-MAKER
     * @param args
     * @throws Exception
     *************************/
    static void main(String[] args) throws Exception{
        /** TimeChecker **/
        TimeMan timeman = new TimeMan().init().start()

        /** Config **/
        logger.debug "===== start config ${new Date().getTime()}"
        Config config = new Config().setup('install', args)
        logger.debug "===== finish config ${new Date().getTime()}"

        /** Commander **/
        Commander commander
        try{
            logger.debug "===== start commander ${new Date().getTime()}"
            commander = new Commander(config, timeman)
            commander.run()
            logger.debug "===== finish commander ${new Date().getTime()}"

        }catch(Exception e){
            commander.logError(e)
            System.exit(-1)
        }

        System.exit(0)
    }

}

