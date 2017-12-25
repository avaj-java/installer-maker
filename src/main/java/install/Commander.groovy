package install

import ch.qos.logback.classic.Level
import install.exception.WantToRestartException
import jaemisseo.man.FileMan
import jaemisseo.man.configuration.Config
import install.employee.Hoya
import install.job.InstallerMaker
import install.job.Installer
import jaemisseo.man.PropMan
import jaemisseo.man.TimeMan
import jaemisseo.man.configuration.annotation.type.Bean
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Bean
class Commander {

    static final String APPLICATION_INSTALLER_MAKER = 'installer-maker'
    static final String APPLICATION_INSTALLER = 'installer'
    static final String APPLICATION_HOYA = 'hoya'

    static String getApplicationName(PropMan propmanExternal){
        List<String> specialValueList = propmanExternal.get('--')
        String applicationName = 'installer-maker'
        if (specialValueList.contains(APPLICATION_INSTALLER)){
            applicationName = APPLICATION_INSTALLER
        }else if (specialValueList.contains(APPLICATION_HOYA)){
            applicationName = APPLICATION_HOYA
        }
        return applicationName
    }

    Commander(){
    }

    Commander(Config config, TimeMan timeman){
        this.config = config
        this.timeman = timeman
        init()
    }

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    Config config
    TimeMan timeman



    void init(){
        //- Try to get from User's FileSystem
        generatePropMan('installer-maker')
        generatePropMan('installer')
        generatePropMan('hoya')
    }

    void generatePropMan(String fileName){
        PropMan propmanExternal = config.propGen.getExternalProperties()
        PropMan propmanDefault = config.propGen.getDefaultProperties()
        File propertiesFile
        String propertiesDir = propmanExternal.get('properties.dir') ?: propmanDefault.get('user.dir')
        if (propertiesDir)
            propertiesFile = FileMan.find(propertiesDir, "${fileName}.default", ["properties"])
        if (propertiesFile)
            config.propGen.genSingletonPropManFromFileSystem(fileName, propertiesFile.path)
        else
            config.propGen.genSingletonPropManFromResource(fileName, "defaultProperties/${fileName}.default.properties")
    }



    void run(){
        //- Properties
        PropMan propmanExternal = config.propGen.getExternalProperties()
        PropMan propmanDefault = config.propGen.getDefaultProperties()

        //- CommandList & TaskList called by user
        List<String> commandCalledByUserList = config.commandCalledByUserList
        List<String> taskCalledByUserList = config.taskCalledByUserList

        //- Check Mode Help
        boolean modeHelp = propmanExternal.getBoolean(['help', 'h'])
        boolean modeExecSelf = propmanExternal.getBoolean('mode.exec.self')
        boolean hasCommand = !!commandCalledByUserList
        boolean hasTask = !!taskCalledByUserList

        /** --OPTION **/
        List<String> specialValueList = propmanExternal.get('--')
        //- Set Application Identity
        String applicationName = getApplicationName(propmanExternal)
        propmanDefault.set('application.name', applicationName)
        //- Set Log
        if (specialValueList.contains('error')){
            propmanExternal.set('log.level.console', 'error')
            if (specialValueList.contains('log.file'))
                propmanExternal.set('log.level.file', 'error')
        }else if (specialValueList.contains('debug')){
            propmanExternal.set('log.level.console', 'debug')
            if (specialValueList.contains('log.file'))
                propmanExternal.set('log.level.file', 'debug')
        }else if (specialValueList.contains('trace')){
            propmanExternal.set('log.level.console', 'trace')
            if (specialValueList.contains('log.file'))
                propmanExternal.set('log.level.file', 'trace')
        }


        /** [Command] Auto Command **/
        if (!hasCommand && !hasTask){
            //- Check External Property
            logExternalProperty(propmanExternal)

            if ([APPLICATION_INSTALLER_MAKER].contains(applicationName))
                config.command(['help'])
            if ([APPLICATION_INSTALLER].contains(applicationName))
                config.command(['ask', 'install'])
            if ([APPLICATION_HOYA].contains(applicationName))
                config.command(['hoya'])

        /** [Command] **/
        }else if (hasCommand && !modeHelp){
            //- Check External Property
            logExternalProperty(propmanExternal)
            // -[Command] Start
            startCommand(commandCalledByUserList, applicationName)
            if (modeExecSelf)
                return
            // -[Command] Finish
            finishCommand(commandCalledByUserList, timeman.stop().getTime())

        /** [Task / Help] **/
        }else if ((!hasCommand && hasTask) || (hasCommand && modeHelp)){

            if ([APPLICATION_INSTALLER_MAKER, APPLICATION_HOYA].contains(applicationName)){
                config.command('doSomething')
            }
            if ([APPLICATION_INSTALLER].contains(applicationName)){
                if (['version', 'system'].contains(taskCalledByUserList[0]))
                    config.command('doSomething')
            }
        }

        /** [Finish] INSTALLER-MAKER **/
        config.logGen.logFinished()
    }



    /*************************
     * COMMAND START
     *   - Your command from Command Line
     * @param config
     *************************/
    void startCommand(List<String> commandCalledByUserList, String applicationName){
        try{
            switch (applicationName){
                case APPLICATION_INSTALLER_MAKER:
                    InstallerMaker builder = config.findInstance(InstallerMaker)
                    commandCalledByUserList.each{
                        if (config.hasCommand(it)){
                            config.command(it)
                        }else{
                            builder.commandName = it
                            config.command(InstallerMaker)
                        }
                    }
                    break
                case APPLICATION_INSTALLER:
                    Installer installer = config.findInstance(Installer)
                    commandCalledByUserList.each{
                        if (config.hasCommand(it)){
                            config.command(it)
                        }else{
                            installer.commandName = it
                            config.command(Installer)
                        }
                    }
                    break
                case APPLICATION_HOYA:
                    Hoya hoya = config.findInstance(Hoya)
                    commandCalledByUserList.each{
                        if (config.hasCommand(it)){
                            config.command(it)
                        }else{
                            hoya.commandName = it
                            config.command(Hoya)
                        }
                    }
                    break
                default:
                    throw new Exception("Invalid approch")
                    break
            }
        }catch(Exception e){
            if (e instanceof WantToRestartException
            || (e.cause && e.cause instanceof WantToRestartException)
            ){
                startCommand(commandCalledByUserList, applicationName)
            }else{
                throw e
            }
        }
    }



    /*************************
     * FINISH START
     * @param e
     *************************/
    void finishCommand(List<String> commandCalledByUserList, double elapseTime){
        //Show ElapseTime
        logger.info """
            - Command    : ${commandCalledByUserList.join(', ')} 
            - ElapseTime : ${elapseTime}s"""
    }



    /*************************
     * LOG ERROR
     * @param e
     *************************/
    void logError(Exception e){
        //Start Color Log Pattern
        config.logGen.setupConsoleLoggerColorPattern('red')

        Throwable cause = e.getCause()
        String indent = '\t- '
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)
        String errorClass = e.toString()?.trim()
        String errorMessage = e.getMessage()?.trim()

        rootLogger.error("<< Error >>")
        if (errorMessage)
            rootLogger.error indent + errorMessage

        if (cause){
            String errorCauseMessage = cause.getMessage()
            if (errorCauseMessage)
                rootLogger.error indent + errorCauseMessage
            while((cause = cause.getCause()) != null){
                errorCauseMessage = cause.getMessage()
                if (errorCauseMessage)
                    rootLogger.error indent + errorCauseMessage
            }
        }

        if ([Level.DEBUG, Level.TRACE].contains( config.logGen.getConsoleLogLevel() )){
            rootLogger.error('Error', e)
            //Finish Color Log Pattern
            config.logGen.setupBeforeConsoleLoggerPattern()
        }else{
            rootLogger.detachAppender('CONSOLE')
            rootLogger.debug('Error', e)
        }

    }

    /*************************
     * LOG arguments from terminal
     * @param e
     *************************/
    void logExternalProperty(PropMan propmanExternal){
        //TODO:INFO => DEBUG
        logger.debug(" [ CHECK External Option ] ")

        //Command Properties
        List commandProperties = propmanExternal['']
        commandProperties.each{ String commandOption ->
            logger.debug("${commandOption}")
        }

        //Properties
        propmanExternal.properties.each{
            if (!['--', ''].contains(it.key))
                logger.debug("${it.key}=${it.value}")
        }

        //Special Properties
        List specialProperties = propmanExternal['--']
        specialProperties.each{ String specialOption ->
            logger.debug("--${specialOption}")
        }
        logger.debug("")
    }

}
