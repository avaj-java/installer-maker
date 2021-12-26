package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.type.Undomore

@Undomore
@Undoable
@Task
class Sleep extends TaskHelper{

    @Value('second')
    Integer second

    @Override
    Integer run(){
        Long ms = second *1000
        sleep(ms)
        return STATUS_TASK_DONE
    }

}
