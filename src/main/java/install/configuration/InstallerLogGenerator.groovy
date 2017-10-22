package install.configuration

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import jaemisseo.man.PropMan
import org.fusesource.jansi.Ansi
import org.slf4j.LoggerFactory

/**
 * Created by sujkim on 2017-03-29.
 */
class InstallerLogGenerator {

    InstallerLogGenerator(){
        logLevelStackTraceList << getConsoleLogLevel()
        logPatternStackTraceList << getConsoleLogPattern()
    }



    final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    String consoleAppenderName = "CONSOLE"
    String simpleFileAppenderName = "FILE_SIMPLE"
    String choiceFileAppenderName = "FILE_CHOICE"

    String logFileExtension = 'log'
    List<String> logLevelStackTraceList = []
    List<String> logPatternStackTraceList = []
//    List userControlLoggerList = ['install', 'jaemisseo.man']



    /*************************
     *
     * Get
     *
     *************************/
    Level getConsoleLogLevel(){
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        ConsoleAppender consoleAppender = rootLogger.getAppender(consoleAppenderName)
        String nowConsoleLogLevel
        consoleAppender.getCopyOfAttachedFiltersList().each{ Filter filter ->
            if (filter instanceof ThresholdFilter)
                nowConsoleLogLevel = ((ThresholdFilter) filter).level
        }
        return Level.valueOf(nowConsoleLogLevel)
    }

    String getConsoleLogPattern(){
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        ConsoleAppender consoleAppender = rootLogger.getAppender(consoleAppenderName)
        String nowConsoleLogPattern = ((PatternLayoutEncoder)consoleAppender.encoder).pattern
        return nowConsoleLogPattern
    }



    /*************************
     *
     * INIT
     *
     *************************/
    boolean initConsoleLoggerPattern(){
        String pattern = logPatternStackTraceList[0]
        return setupConsoleLogger(null, pattern)
    }

    boolean initConsoleLoggerLevel(){
        String pattern = logLevelStackTraceList[0]
        return setupConsoleLogger(null, pattern)
    }



    /*************************
     *
     * SETUP
     *
     *************************/
    boolean setupConsoleLogger(String logLevel){
        return setupConsoleLoggerLevel(logLevel)
    }

    boolean setupConsoleLoggerLevel(String logLevel){
        return setupConsoleLogger(logLevel, null)
    }

    boolean setupConsoleLoggerPattern(String pattern){
        return setupConsoleLogger(null, pattern)
    }

    boolean setupConsoleLogger(String logLevel, String pattern){
        if (!logLevel && !pattern)
            return false

        // -Console Appender
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)

        /** Convert ConsoleAppender's ThresholdFilter **/
        ConsoleAppender consoleAppender = rootLogger.getAppender(consoleAppenderName)
        //Set withJansi
        consoleAppender.setWithJansi(true)
        //Change ThresholdFilter
        if (logLevel){
            changeAppenderThresholdFilterLevel(consoleAppender, logLevel)
            logLevelStackTraceList << logLevel
        }
        //Change Encoder
        if (pattern){
            changeAppenderEncoder(consoleAppender, pattern)
            logPatternStackTraceList << pattern
        }

        /** Convert ConsoleAppender's ThresholdFilter in SiftingAppender **/
        //Not Good
//        SiftingAppender siftingAppender = rootLogger.getAppender(consoleAppenderName)
//        siftingAppender.appenderTracker.allComponents().each{
//            if (it instanceof ConsoleAppender){
//                println it
//                println it.withJansi
//                //Set withJansi
//                it.setWithJansi(true)
//                //Change ThresholdFilter
//                changeAppenderThresholdFilterLevel(it, logLevel)
//            }else{
//                println it
//            }
//        }
        return true
    }

    boolean setupConsoleLoggerColorPattern(String color){
        String pattern = "%${color}(%msg) %n"
        return setupConsoleLogger(null, pattern)
    }

    boolean setupBeforeConsoleLoggerPattern(){
        if (logPatternStackTraceList.size() <= 1)
            return false

        int nowIndex = logPatternStackTraceList.size() - 1
        int beforeIndex = nowIndex - 1
        String beforePattern = logPatternStackTraceList[beforeIndex]
        logPatternStackTraceList.remove(nowIndex)
        logPatternStackTraceList.remove(beforeIndex)
        return setupConsoleLogger(null, beforePattern)
    }

    boolean setupFileLogger(String jobName, String logLevel, String logDir, String logFileName){
        if (!logLevel)
            return false
        // -File Appender
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        Level loggerLevelCode = Level.valueOf(logLevel)

        //Remove Appender & Stop
        [simpleFileAppenderName, choiceFileAppenderName].each{
            FileAppender fileAppender = rootLogger.getAppender(it)
            if (fileAppender){
                fileAppender.stop()
                rootLogger.detachAppender(fileAppender)
            }
        }

        //Add Default File Log
        if ([Level.INFO, Level.DEBUG, Level.TRACE].contains(loggerLevelCode))
            rootLogger.addAppender( generateFileAppender(simpleFileAppenderName, 'info', "%msg %n", logDir, logFileName, logFileExtension) )

        //Add Optional File Log
        if ([Level.ERROR, Level.WARN, Level.DEBUG, Level.TRACE].contains(loggerLevelCode))
            rootLogger.addAppender( generateFileAppender(choiceFileAppenderName, logLevel, "[%d] [%-5level] %logger[%method:%line] - %msg %n", logDir, "${logFileName}_${logLevel}", logFileExtension) )

        // OPTIONAL: print logback internal status messages
//        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()
//        StatusPrinter.print(loggerContext)

        return true
    }


    /*************************
     *
     * Log Color One Time
     *
     *************************/
    void logColorOneTimeConsoleLogger(def instance, String color, String message){
        logColorOneTimeConsoleLogger(instance.getClass(), color, message)
    }

    void logColorOneTimeConsoleLogger(Class clazz, String color, String message){
        org.slf4j.Logger templogger = LoggerFactory.getLogger(clazz)
        setupConsoleLoggerColorPattern(color)
        templogger.info(message)
        setupBeforeConsoleLoggerPattern()
    }



    /*************************
     *
     * Change
     *
     *************************/
    void changeAppenderThresholdFilterLevel(Appender appender, String thresholdFilterLevel){
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()
        //Remove Filter & Stop
        appender.stop()
        appender.clearAllFilters()

        //Add Filter
        ThresholdFilter thresholdFilter = new ThresholdFilter()
        thresholdFilter.context = loggerContext
        thresholdFilter.level = thresholdFilterLevel
        thresholdFilter.start()
        appender.addFilter(thresholdFilter)

        //Start
        appender.start()
    }

    void changeAppenderEncoder(Appender appender, String pattern){
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()
        //Stop
        appender.stop()

        //Set Log Pattern
        PatternLayoutEncoder encoder
        if (appender instanceof ConsoleAppender){
            encoder = ((ConsoleAppender)appender).encoder
        }else if (appender instanceof FileAppender){
            encoder = ((FileAppender)appender).encoder
        }else if (appender instanceof RollingFileAppender){
            encoder = ((RollingFileAppender)appender).encoder
        }
        encoder.stop()
        encoder.pattern = pattern
        encoder.start()

        //Start
        appender.start()
    }



    /*************************
     *
     * Generate
     *
     *************************/
    ConsoleAppender generateConsoleAppender(String appenderName, String thresholdFilterLevel, String logPattern){
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()

        //Add Filter
        ConsoleAppender consoleAppender = new ConsoleAppender()
        PatternLayoutEncoder encoder = new PatternLayoutEncoder()
        ThresholdFilter thresholdFilter = new ThresholdFilter()

        consoleAppender.name = appenderName

        encoder.context = loggerContext
        encoder.pattern = logPattern
        encoder.start()
        consoleAppender.encoder     = encoder

        thresholdFilter.context = loggerContext
        thresholdFilter.level = thresholdFilterLevel
        thresholdFilter.start()
        consoleAppender.addFilter(thresholdFilter)
        consoleAppender.start()

        return consoleAppender
    }

    FileAppender<ILoggingEvent> generateFileAppender(String appenderName, String thresholdFilterLevel, String logPattern, String fileDir, String fileName, String fileExtension){
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()

        ThresholdFilter thresholdFilter = new ThresholdFilter()
        PatternLayoutEncoder encoder = new PatternLayoutEncoder()
        FileAppender<ILoggingEvent> fileAppender = new FileAppender()

        fileAppender.context     = loggerContext
        fileAppender.name        = appenderName
        fileAppender.file        = "$fileDir/${fileName}.$fileExtension"

        encoder.context = loggerContext
        encoder.pattern = logPattern
        encoder.start()
        fileAppender.encoder     = encoder

        thresholdFilter.context = loggerContext
        thresholdFilter.level = thresholdFilterLevel
        thresholdFilter.start()
        fileAppender.addFilter(thresholdFilter)
        fileAppender.start()

        return fileAppender
    }

    RollingFileAppender<ILoggingEvent> generateRollingFileAppender(String appenderName, String thresholdFilterLevel, String logPattern, String fileDir, String fileName, String fileExtension){
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()
        ThresholdFilter thresholdFilter = new ThresholdFilter()
        PatternLayoutEncoder encoder = new PatternLayoutEncoder()
        SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy = new SizeAndTimeBasedFNATP<ILoggingEvent>()
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>()
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender()

        rollingFileAppender.context     = loggerContext
        rollingFileAppender.name        = appenderName
        rollingFileAppender.file        = "$fileDir/$fileName.$fileExtension"

        rollingPolicy.context = loggerContext
        rollingPolicy.fileNamePattern = "$fileDir/$fileName.%d{yyyyMMdd_HHmm}.%i.$fileExtension"
        rollingPolicy.maxHistory = 30
        rollingPolicy.parent = rollingFileAppender
        rollingPolicy.start()

        triggeringPolicy.context = loggerContext
        triggeringPolicy.maxFileSize = '50MB'
        triggeringPolicy.timeBasedRollingPolicy = rollingPolicy
        triggeringPolicy.start()

        rollingPolicy.timeBasedFileNamingAndTriggeringPolicy = triggeringPolicy
        rollingPolicy.start()
        rollingFileAppender.rollingPolicy = rollingPolicy
        rollingFileAppender.append      = true

        encoder.context = loggerContext
        encoder.pattern = logPattern
        encoder.start()
        rollingFileAppender.encoder     = encoder

        thresholdFilter.context = loggerContext
        thresholdFilter.level = thresholdFilterLevel
        thresholdFilter.start()
        rollingFileAppender.addFilter(thresholdFilter)
        rollingFileAppender.start()
        return rollingFileAppender
    }




    /*************************
     *
     * Print Info
     *
     *************************/
    void logVersion(PropMan propman){
        String thisVersion  = propman.get('lib.version')
        String thisBuildDate = propman.get('lib.build.date')
        logger.info ""
        logger.info "Installer Version ${thisVersion}"
        logger.info "${propman.get('lib.compiler')} compiled on ${thisBuildDate}"
        logger.info "https://github.com/avaj-java/installer-maker"
        logger.info ""
    }

    void logSystem(PropMan propman){
        String thisVersion  = propman.get('lib.version')
        String thisPath     = propman.get('lib.path')
        String osName       = propman.get('os.name')
        String osVersion    = propman.get('os.version')
        String userName     = propman.get('user.name')
        String javaVersion  = propman.get('java.version')
        String javaHome     = propman.get('java.home')
        String homePath     = propman.get('user.home')
        String nowPath      = propman.get('user.dir')
        logger.info ""
        logger.info "Check your system "
        logger.info " - OS                      : ${osName}, ${osVersion}"
        logger.info " - USER                    : ${userName}"
        logger.info " - JAVA Version            : ${javaVersion} (${javaHome})"
        logger.info " - HOME Path               : ${homePath}"
        logger.info " - YOUR Path               : ${nowPath}"
        logger.info " - INSTALLER-MAKER Path    : ${thisPath}"
        logger.info ""
    }

    void logFinished(){
        logFinished('')
    }

    void logFinished(String message){
        logger.info ""
        logger.info "   <<< Finish >>> ${message?:''}"
        logger.info ""
    }


}
