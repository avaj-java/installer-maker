package install

import jaemisseo.man.configuration.Config
import install.employee.MacGyver
import install.job.InstallerMaker
import install.job.Installer
import jaemisseo.man.PropMan
import jaemisseo.man.TimeMan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

class Commander {

    static final String APPLICATION_INSTALLER_MAKER = 'installer-maker'
    static final String APPLICATION_INSTALLER = 'installer'
    static final String APPLICATION_MACGYVER = 'macgyver'

    static String getApplicationName(PropMan propmanExternal){
        List<String> specialValueList = propmanExternal.get('--')
        String applicationName = 'installer-maker'
        if (specialValueList.contains(APPLICATION_INSTALLER)){
            applicationName = APPLICATION_INSTALLER
        }else if (specialValueList.contains(APPLICATION_MACGYVER)){
            applicationName = APPLICATION_MACGYVER
        }
        return applicationName
    }

    Commander(Config config, TimeMan timeman){
        this.config = config
        this.timeman = timeman
        init(config)
    }

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    Config config
    TimeMan timeman



    void init(Config config){
        config.propGen.genSingletonPropManFromResource('installer-maker', 'defaultProperties/installer-maker.default.properties')
        config.propGen.genSingletonPropManFromResource('installer', 'defaultProperties/installer.default.properties')
        config.propGen.genSingletonPropManFromResource('macgyver', 'defaultProperties/macgyver.default.properties')
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

        //- Application Identity
        String applicationName = getApplicationName(propmanExternal)
        propmanDefault.set('application.name', applicationName)

        /** [Command] Auto Command **/
        if (!hasCommand && !hasTask){
            //- Check External Property
            logExternalProperty(propmanExternal)

            if ([APPLICATION_INSTALLER_MAKER].contains(applicationName))
                config.command(['help'])
            if ([APPLICATION_INSTALLER].contains(applicationName))
                config.command(['ask', 'install'])
            if ([APPLICATION_MACGYVER].contains(applicationName))
                config.command(['macgyver'])

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

            if ([APPLICATION_INSTALLER_MAKER, APPLICATION_MACGYVER].contains(applicationName)){
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
     * COMMAND
     *   - Your command from Command Line
     * @param config
     *************************/
    void startCommand(List<String> commandCalledByUserList, String applicationName){
        commandCalledByUserList.each{
            if (config.hasCommand(it)){
                switch (applicationName){
                    case APPLICATION_INSTALLER_MAKER:
                        config.command(it)
                        break
                    case APPLICATION_INSTALLER:
                        config.command(it)
                        break
                    case APPLICATION_MACGYVER:
                        config.command(it)
                        break
                    default:
                        throw new Exception("Invalid approch [${it}]")
                        break
                }
            }else{
                switch (applicationName){
                    case APPLICATION_INSTALLER_MAKER:
                        InstallerMaker builder = config.findInstance(InstallerMaker)
                        builder.commandName = it
                        config.command(InstallerMaker)
                        break
                    case APPLICATION_INSTALLER:
                        Installer installer = config.findInstance(Installer)
                        installer.commandName = it
                        config.command(Installer)
                        break
                    case APPLICATION_MACGYVER:
                        MacGyver macgyver = config.findInstance(MacGyver)
                        macgyver.commandName = it
                        config.command(MacGyver)
                        break
                    default:
                        throw new Exception("Invalid approch [${it}]")
                        break
                }
            }
        }
    }

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
            rootLogger.debug('Error', e)
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
