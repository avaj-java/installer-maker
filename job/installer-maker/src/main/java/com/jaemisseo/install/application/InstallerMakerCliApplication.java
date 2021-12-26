package com.jaemisseo.install.application;

import jaemisseo.man.configuration.context.Command;

public class InstallerMakerCliApplication extends InstallerMakerEmbededApplication {

    public static void main(String[] args) throws Exception{
        Command command = new Command(args);

        new InstallerMakerCliApplication().run(command);
    }

}
