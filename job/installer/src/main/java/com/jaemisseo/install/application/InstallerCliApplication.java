package com.jaemisseo.install.application;

import jaemisseo.man.configuration.context.Command;

public class InstallerCliApplication extends InstallerEmbededApplication {

    public static void main(String[] args) throws Exception{
        Command command = new Command(args);

        new InstallerCliApplication().run(command);
    }

}
