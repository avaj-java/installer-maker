package com.jaemisseo.hoya.application;

import jaemisseo.man.configuration.context.Command;

public class HoyaCliApplication extends HoyaEmbededApplication {

    public static void main(String[] args) throws Exception{
        Command command = new Command(args);

        new HoyaCliApplication().run(command);
    }

}
