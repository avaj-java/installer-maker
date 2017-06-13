package install

import install.configuration.Config
import install.configuration.InstallerLogGenerator
import jaemisseo.man.PropMan
import jaemisseo.man.util.PropertiesGenerator

class Starter {

    /*************************
     * START INSTALL
     * @param args
     * @throws Exception
     *************************/
    static void main(String[] args) throws Exception{
        //Config
        Config config = new Config()
        config.makeProperties(args)
        config.makeLoger()

        //Start
        new Starter().start(config)
    }



    /*************************
     * START
     * @param config
     *************************/
    void start(Config config){
        PropertiesGenerator propGen = config.propGen
        InstallerLogGenerator logGen = config.logGen
        PropMan propmanDefault = propGen.getDefaultProperties()
        PropMan propmanExternal = propGen.getExternalProperties()

        logGen.logStart(propmanDefault)

        /*****
         * Command
         *****/
        config.scan()
        config.init()
        config.command()

        /*****
         * Version Check
         *****/
        if (propmanExternal.getBoolean('version') || propmanExternal.getBoolean('v')){
            logGen.logVersion(propmanDefault)
            System.exit(0)
        }

        /*****
         * Run Task Directly
         * - Doing Other Task with Command Line Options
         *****/
        config.command('doSomething')

        logGen.logFinished()
    }

}

