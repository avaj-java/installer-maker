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
        Config config = new Config().setup('install', args)

        /** Commander **/
        Commander commander
        try{
            commander = new Commander(config, timeman)
            commander.run()

        }catch(Exception e){
            commander.logError(e)
            System.exit(-1)
        }
    }

}

