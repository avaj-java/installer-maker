package com.jaemisseo.hoya.application;

import jaemisseo.man.configuration.context.CommanderConfig;
import jaemisseo.man.configuration.context.Environment;
import jaemisseo.man.configuration.context.SelfAware;

public abstract class AbstractHoyaCliApplication extends AbstractHoyaApplication implements Application {

    protected Environment lookAround(CommanderConfig config){
        return lookAround(new Environment(), config);
    }

    protected Environment lookAround(Environment environment, CommanderConfig config){
        environment.init(config);
        return environment;
    }

    protected SelfAware awareSelf(CommanderConfig config){
        SelfAware selfAware = new SelfAware();
        selfAware.init(config);
        return selfAware;
    }

    protected void exitAsNormal(){
        System.exit(0);
    }

    protected void exitAsAbnormal(){
        System.exit(-1);
    }

}
