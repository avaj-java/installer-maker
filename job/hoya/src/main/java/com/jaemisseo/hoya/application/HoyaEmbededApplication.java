package com.jaemisseo.hoya.application;

import jaemisseo.man.configuration.context.Command;
import jaemisseo.man.configuration.context.CommanderConfig;
import jaemisseo.man.configuration.context.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoyaEmbededApplication extends AbstractHoyaEmbededApplication {

    final Logger logger = LoggerFactory.getLogger(getClass());

    public void run(Command command){
        logger.debug("[Hoya] Application Setup");
        Environment environment = new Environment();
        environment.setApplicationName("hoya");

        try{
            super.run(command, environment);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void makePropMan(CommanderConfig config){
        super.makePropMan(config);
    }

}
