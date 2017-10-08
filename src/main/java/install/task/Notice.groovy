package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.type.TerminalIgnore
import install.configuration.annotation.type.Undoable
import install.configuration.annotation.Value
import install.util.TaskUtil

/**
 * Created by sujkim on 2017-03-18.
 */
@Undoable(modeMore=true)
@Task
@TerminalIgnore
class Notice extends TaskUtil{

    @Value(name='msg', required=true)
    String msg



    @Override
    Integer run(){
        //Show You Welcome Message
        println msg

        return STATUS_TASK_DONE
    }
}
