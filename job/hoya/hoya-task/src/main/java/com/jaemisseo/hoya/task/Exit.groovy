package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.type.Undomore


@Undoable
@Undomore
@Task
class Exit extends TaskHelper{

    @Override
    Integer run(){
        return STATUS_EXIT
    }

}
