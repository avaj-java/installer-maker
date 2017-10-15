package install

import install.configuration.annotation.type.Data
import install.configuration.Config
import install.configuration.InstallerLogGenerator
import install.data.PropertyProvider
import jaemisseo.man.PropMan
import jaemisseo.man.TimeMan
import jaemisseo.man.util.PropertiesGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Starter {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*************************
     * START INSTALL
     * @param args
     * @throws Exception
     *************************/
    static void main(String[] args) throws Exception{
        /** [Start] INSTALLER-MAKER **/
        // -TimeChecker
        TimeMan timeman = new TimeMan().init().start()

        /** [Config] **/
        Config config = new Config()
        config.scan()

        config.makeProperties(args)
        config.makeLogger()

        PropertyProvider provider = config.findInstanceByAnnotation(Data)
        provider.propGen = config.propGen
        provider.logGen = config.logGen
        config.inject()
        config.init()
        PropMan propmanExternal = config.propGen.getExternalProperties()

        /** [Command] **/
        if ( !propmanExternal.getBoolean('help') ){
            // -[Command] Start
            new Starter().startCommand(config)

            // -[Command] Finish
            if ( !propmanExternal.getBoolean('mode.exec.self') ){
                List installerCommandList = propmanExternal.get('') ?: []
                new Starter().finishCommand(installerCommandList, timeman.stop().getTime())
            }
        }

        /** [Task] **/
        // -Run Task Directly (Doing Other Task with Command Line Options) **/
        config.command('doSomething')

        /** [Finish] INSTALLER-MAKER **/
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
         * Command
         *****/
        //- Your command from Command Line
        List<String> userCommandList = propmanExternal.get('') ?: []
        config.command(userCommandList)
    }

    void finishCommand(List installerCommandList, double elapseTime){
        if (installerCommandList){
            //Show ElapseTime
            logger.info """
                - Command    : ${installerCommandList.join(', ')} 
                - ElapseTime : ${elapseTime}s"""
        }
    }

}

