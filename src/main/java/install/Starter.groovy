package install

import install.configuration.Config
import jaemisseo.man.PropMan
import jaemisseo.man.TimeMan
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Starter {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

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



    /*************************
     * START INSTALL
     * @param args
     * @throws Exception
     *************************/
    static void main(String[] args) throws Exception{
        /** [Start] INSTALLER-MAKER **/
        // -TimeChecker
        TimeMan timeman
        Starter starter
        Config config

        try {
            /** [Config] **/
            timeman = new TimeMan().init().start()
            starter = new Starter()
            config = new Config().setup(args)
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

            if (!hasCommand && !hasTask){
                /** [Run] Specific Command **/
                if ([APPLICATION_MACGYVER].contains(applicationName))
                    config.command(['macgyver'])
                if ([APPLICATION_INSTALLER].contains(applicationName))
                    config.command(['ask', 'install'])
                if ([APPLICATION_INSTALLER_MAKER].contains(applicationName))
                    config.command(['help'])

            }else if (hasCommand && !modeHelp){
                /** [Command] **/
                if ([APPLICATION_INSTALLER_MAKER].contains(applicationName)){
                    // -[Command] Start
                    starter.startCommand(commandCalledByUserList, config)
                    if (modeExecSelf)
                        return
                    // -[Command] Finish
                    starter.finishCommand(commandCalledByUserList, timeman.stop().getTime())
                }

            }else if ((!hasCommand && hasTask) || (hasCommand && modeHelp)){
                /** [Run] Specific Task or Help **/
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

        }catch(Exception e){
            /** [Error] **/
            starter.logError(e)
        }
    }



    /*************************
     * COMMAND
     *   - Your command from Command Line
     * @param config
     *************************/
    void startCommand(List<String> commandCalledByUserList, Config config){
        config.command(commandCalledByUserList)
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
//        e.printStackTrace()
    }

}

