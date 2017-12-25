package install.task

import install.util.TaskUtil
import jaemisseo.man.configuration.annotation.HelpIgnore
import jaemisseo.man.configuration.annotation.type.Task


@Task
class Restart extends TaskUtil{

    @Override
    Integer run(){
        return STATUS_RESET
    }

}
