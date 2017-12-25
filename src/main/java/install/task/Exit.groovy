package install.task

import install.util.TaskUtil
import jaemisseo.man.configuration.annotation.HelpIgnore
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.type.Undomore

@HelpIgnore
@Undoable
@Undomore
@Task
class Exit extends TaskUtil{

    @Override
    Integer run(){
        return STATUS_EXIT
    }

}
