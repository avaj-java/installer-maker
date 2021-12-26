package com.jaemisseo.hoya.application;

import ch.qos.logback.classic.Level;
import jaemisseo.man.FileMan;
import jaemisseo.man.PropMan;
import jaemisseo.man.configuration.context.Command;
import jaemisseo.man.configuration.context.Environment;
import jaemisseo.man.configuration.context.SelfAware;
import jaemisseo.man.configuration.handler.AbstractAfterCommandMessenger;
import jaemisseo.man.configuration.handler.AbstractBeforeCommandMessenger;
import jaemisseo.man.configuration.handler.AbstractCommandMessenger;
import jaemisseo.man.configuration.handler.CommandMessegerRunner;
import jaemisseo.man.configuration.context.CommanderConfig;
import jaemisseo.man.configuration.annotation.type.AfterCommandMessenger;
import jaemisseo.man.configuration.annotation.type.BeforeCommandMessenger;
import jaemisseo.man.configuration.annotation.type.CommandMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractHoyaApplication implements Application {

    final Logger logger = LoggerFactory.getLogger(getClass());

    public void run(Command command){
        run(command, null);
    }

    public void run(Command command, Environment environment){
        logger.debug("[Installer-Maker] Application Setup");
        String[] args = command.getArgs();

        CommanderConfig config = new CommanderConfig();
        config.setup("com.jaemisseo", args);

        environment = (environment != null) ? lookAround(environment, config) : lookAround(config);
        SelfAware selfAware = awareSelf(config);

        //- Try to get from User's FileSystem
        makePropMan(config);

        config.makeDependency(environment);
        config.makeDependency(selfAware);
        config.injectDependenciesToBean();
        config.makeBeanInit();

        takeOrder(environment, selfAware);
    }

    protected void makePropMan(CommanderConfig config){
        generatePropMan(config, "hoya");
    }

    protected void generatePropMan(CommanderConfig config, String fileName){
        PropMan propmanExternal = config.getPropGen().getExternalProperties();
        PropMan propmanDefault = config.getPropGen().getDefaultProperties();

        File propertiesFile = null;
        String propertiesDir = propmanExternal.has("properties.dir") ? propmanExternal.getString("properties.dir") : propmanDefault.getString("user.dir");

        if (propertiesDir != null){
            propertiesFile = FileMan.find(propertiesDir, fileName + ".default", Arrays.asList("properties"));
        }

        if (propertiesFile != null){
            config.getPropGen().genSingletonPropManFromFileSystem(fileName, propertiesFile.getPath());
        }else{
            config.getPropGen().genSingletonPropManFromResource(fileName, "defaultProperties/" +fileName+ ".default.properties");
        }

    }

    abstract protected Environment lookAround(CommanderConfig config);

    abstract protected Environment lookAround(Environment environment, CommanderConfig config);

    abstract protected SelfAware awareSelf(CommanderConfig config);

    abstract protected void exitAsNormal();

    abstract protected void exitAsAbnormal();



    private void takeOrder(Environment environment, SelfAware selfAware){
        CommanderConfig config = environment.getConfig();
        String applicationName = environment.getApplicationName();

        try{
            List<AbstractBeforeCommandMessenger> beforeHandlers = (List<AbstractBeforeCommandMessenger>) config.findAllInstances(BeforeCommandMessenger.class);
            List<AbstractCommandMessenger> handlers = (List<AbstractCommandMessenger>) config.findAllInstances(CommandMessenger.class);
            List<AbstractAfterCommandMessenger> afterHandlers = (List<AbstractAfterCommandMessenger>) config.findAllInstances(AfterCommandMessenger.class);

            beforeHandlers = (List<AbstractBeforeCommandMessenger>) filterHandlers(beforeHandlers, applicationName);
            handlers = (List<AbstractCommandMessenger>) filterHandlers(handlers, applicationName);
            afterHandlers = (List<AbstractAfterCommandMessenger>) filterHandlers(afterHandlers, applicationName);

            CommandMessegerRunner runner = new CommandMessegerRunner(environment, beforeHandlers, handlers, afterHandlers);
            runner.run(environment, selfAware);

        }catch(Exception e){
            logError(e, config);
            this.exitAsAbnormal();
        }

        /** [Finish] INSTALLER-MAKER **/
        config.getLogGen().logFinished();

        this.exitAsNormal();
    }

    private List<?> filterHandlers(List<?> handlers, String applicationName){
        List<?> filteredHandlers = handlers.stream().filter( o -> {
            CommandMessenger ant = o.getClass().getAnnotation(CommandMessenger.class);
            String targetApplication = ant.value();
            return targetApplication.equals(applicationName);
        }).collect(Collectors.toList());
        return filteredHandlers;
    }

    private void logError(Exception e, CommanderConfig config){
        //Start Color Log Pattern
        config.getLogGen().setupConsoleLoggerColorPattern("red");

        Throwable cause = e.getCause();
        String indent = "\t- ";
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        String errorClass = e.toString();
        String errorMessage = e.getMessage();

        rootLogger.error("<< Error >>");
        if (errorMessage != null && !errorMessage.isEmpty())
            rootLogger.error( indent + (errorMessage.trim()) );

        if (cause != null){
            String errorCauseMessage = cause.getMessage();
            if (errorCauseMessage != null && !errorCauseMessage.isEmpty())
                rootLogger.error( indent + errorCauseMessage );
            while((cause = cause.getCause()) != null){
                errorCauseMessage = cause.getMessage();
                if (errorCauseMessage != null && !errorCauseMessage.isEmpty())
                    rootLogger.error( indent + errorCauseMessage );
            }
        }

        if (Arrays.asList(Level.DEBUG, Level.TRACE).contains( config.getLogGen().getConsoleLogLevel() )){
            rootLogger.error("Error", e);
            //Finish Color Log Pattern
            config.getLogGen().setupBeforeConsoleLoggerPattern();
        }else{
            rootLogger.detachAppender("CONSOLE");
            rootLogger.debug("Error", e);
        }
    }



}
