package install

import install.configuration.annotation.type.Data
import install.configuration.Config
import install.configuration.InstallerLogGenerator
import install.data.PropertyProvider
import jaemisseo.man.PropMan
import jaemisseo.man.TimeMan
import jaemisseo.man.util.PropertiesGenerator

class Starter {

    /*************************
     * START INSTALL
     * @param args
     * @throws Exception
     *************************/
    static void main(String[] args) throws Exception{
        /** Start INSTALLER-MAKER with TimeChecker **/
        TimeMan timeman = new TimeMan().init().start()

        //Config
        Config config = new Config()
        config.makeProperties(args)
        config.makeLoger()

        /** [Command] **/
        new Starter().startCommand(config)

        /** [Command] Finish **/
        PropMan propmanExternal = config.propGen.getExternalProperties()
        if ( !propmanExternal.getBoolean('mode.exec.self') ){
            List installerCommandList = propmanExternal.get('') ?: []
            if (installerCommandList){
                //Show ElapseTime
                println """
                - Command    : ${installerCommandList.join(', ')} 
                - ElapseTime : ${timeman.stop().getTime()}s"""
            }
        }

        /** [Task] Start - Run Task Directly (Doing Other Task with Command Line Options) **/
        config.command('doSomething')

        /** Finish INSTALLER-MAKER **/
        config.logGen.logFinished()
    }



    /*************************
     * COMMAND
     * @param config
     *************************/
    void startCommand(Config config){
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
        provider.logGen = config.logGen
        config.inject()
        config.init()

        /*****
         * Command
         *****/
        //- Your command from Command Line
        List<String> userCommandList = propmanExternal.get('') ?: []
        config.command(userCommandList)
    }

}

