package install.task

import install.exception.WantToRestartException
import install.util.TaskUtil
import jaemisseo.man.configuration.annotation.type.Task

@Task
class Restart extends TaskUtil{

    @Override
    Integer run(){
        provider.propman.checkout(0)
        throw new WantToRestartException()
        return STATUS_TASK_DONE
    }

}
