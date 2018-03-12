package install.util

import ch.qos.logback.classic.Level
import install.exception.WantToRestartException
import jaemisseo.man.PropMan
import jaemisseo.man.TimeMan
import jaemisseo.man.configuration.Config
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CommanderUtil {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    static final String APPLICATION_INSTALLER_MAKER = 'installer-maker'
    static final String APPLICATION_INSTALLER = 'installer'
    static final String APPLICATION_HOYA = 'hoya'

    Config config
    TimeMan timeman


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



    /*************************
     * RUN COMMAND
     *   - Your command from Command Line
     * @param config
     *************************/
    void runCommand(String command){
        runCommand([command])
    }

    void runCommand(List<String> commandList){
        runCommand(commandList, null)
    }

    void runCommand(List<String> commandList, Class jobClass){
        def jobInstance = (jobClass) ? config.findInstance(jobClass) : null
        try{
            commandList.each{
                if (config.hasCommand(it)){
                    config.command(it)
                }else{
                    jobInstance.commandName = it
                    config.command(jobClass)
                }
            }

        }catch(Exception e){
            if (e instanceof WantToRestartException
            || (e.cause && e.cause instanceof WantToRestartException)
            ){
                runCommand(commandList, jobClass)
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
