package install.task

import install.util.TaskUtil
import jaemisseo.man.configuration.annotation.HelpIgnore
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.type.Undomore

@Undomore
@Undoable
@Task
class Sleep extends TaskUtil{

    @Value('second')
    Integer second

    @Override
    Integer run(){
        Long ms = second *1000
        sleep(ms)
        return STATUS_TASK_DONE
    }

}
