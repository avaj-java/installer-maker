package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.type.Undoable
import install.util.TaskUtil

/**
 * Created by sujkim on 2017-03-18.
 */
@Undoable(modeMore=true)
@Task
class Set extends TaskUtil{

    @Override
    Integer run(){
        //Set Some Property
        setPropValue()

        return STATUS_TASK_DONE
    }

}
