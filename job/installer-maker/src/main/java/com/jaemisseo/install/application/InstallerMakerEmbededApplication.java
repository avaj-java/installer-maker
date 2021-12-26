package com.jaemisseo.install.application;

import com.jaemisseo.hoya.application.AbstractHoyaEmbededApplication;
import jaemisseo.man.configuration.context.Command;
import jaemisseo.man.configuration.context.CommanderConfig;
import jaemisseo.man.configuration.context.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallerMakerEmbededApplication extends AbstractHoyaEmbededApplication {

    final Logger logger = LoggerFactory.getLogger(getClass());

    public void run(Command command){
        logger.debug("[Installer-Maker] Application Setup");
        Environment environment = new Environment();
        environment.setApplicationName("installer-maker");

        try{
            super.run(command, environment);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void makePropMan(CommanderConfig config){
        super.makePropMan(config);
        generatePropMan(config, "installer-maker");
        generatePropMan(config, "installer");
    }

}
