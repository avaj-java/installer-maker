package install

import install.configuration.Config
import jaemisseo.man.TimeMan
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Starter {

    static final Logger logger = LoggerFactory.getLogger(getClass());



    /*************************
     * START INSTALL
     * @param args
     * @throws Exception
     *************************/
    static void main(String[] args) throws Exception{
        /** [Start] INSTALLER-MAKER **/
        TimeMan timeman     // -TimeChecker
        Config config
        Commander commander

        try {
            /** [Config] **/
            timeman = new TimeMan().init().start()
            config = new Config().setup(args)
        }catch(Exception e){
            /** [Error] **/
            logger.error('Error on Starter', e)
        }

        try{
            commander = new Commander(config, timeman)
            commander.run()

        }catch(Exception e){
            /** [Error] **/
            commander.logError(e)
        }
    }

}

