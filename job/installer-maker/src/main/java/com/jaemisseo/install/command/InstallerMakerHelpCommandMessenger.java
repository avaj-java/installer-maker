package com.jaemisseo.install.command;

import jaemisseo.man.configuration.context.Environment;
import jaemisseo.man.configuration.context.SelfAware;
import jaemisseo.man.configuration.annotation.Order;
import jaemisseo.man.configuration.handler.AbstractCommandMessenger;
import jaemisseo.man.PropMan;
import jaemisseo.man.configuration.context.CommanderConfig;
import jaemisseo.man.configuration.annotation.type.CommandMessenger;

import java.util.Arrays;
import java.util.List;

@Order(100)
@CommandMessenger("installer-maker")
public class InstallerMakerHelpCommandMessenger extends AbstractCommandMessenger {

    @Override
    public boolean check(Environment environment) {
        CommanderConfig config = environment.getConfig();
        PropMan propmanExternal = environment.getPropmanExternal();

        //- CommandList & TaskList called by user
        List<String> commandCalledByUserList = config.getCommandCalledByUserList();
        List<String> taskCalledByUserList = config.getTaskCalledByUserList();

        /** [Run] **/
        //- Check Mode Help
        boolean modeHelp = propmanExternal.getBoolean( Arrays.asList("help", "h") );
        boolean modeExecSelf = propmanExternal.getBoolean("mode.exec.self", false);
        boolean hasCommand = commandCalledByUserList != null && commandCalledByUserList.size() > 0;
        boolean hasTask = taskCalledByUserList != null && taskCalledByUserList.size() > 0;

        boolean modeDefaultCommand = !hasCommand && !hasTask && !modeHelp;
        boolean modeCommand = hasCommand && !modeHelp;
        boolean modeTask = !hasCommand && hasTask && !modeHelp;
        return modeHelp;
    }

    @Override
    public void run(Environment environment, SelfAware selfAware){
        environment.runCommand("doSomething");
    }

}
