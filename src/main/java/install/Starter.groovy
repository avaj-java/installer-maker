package install

import install.configuration.annotation.type.Data
import install.configuration.Config
import install.configuration.InstallerLogGenerator
import install.data.PropertyProvider
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


        /*****
         * Config
         *****/
        config.scan()
        PropertyProvider provider = config.findInstanceByAnnotation(Data)
        provider.propGen = config.propGen
        config.inject()
        config.init()

        /*****
         * Command
         *****/
        // Your command from Command Line
        List<String> userCommandList = propmanExternal.get('') ?: []
        config.command(userCommandList)

        // Run Task Directly (Doing Other Task with Command Line Options)
        config.command('doSomething')

        /*****
         * Version Check
         *****/
        if (propmanExternal.getBoolean(['version', 'v'])){
            logGen.logVersion(propmanDefault)
            System.exit(0)
        }

        /*****
         * System Check
         *****/
        if (propmanExternal.getBoolean(['system', 's'])){
            logGen.logSystem(propmanDefault)
            System.exit(0)
        }

        logGen.logFinished()
    }

}

