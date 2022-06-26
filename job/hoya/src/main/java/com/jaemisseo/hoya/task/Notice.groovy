package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalIgnore
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Undomore

/**
 * Created by sujkim on 2017-03-18.
 */
@TerminalIgnore
@Undoable
@Undomore
@Task
class Notice extends TaskHelper{

    @Value(name='msg', required=true)
    String msg



    @Override
    Integer run(){

        //Show You Welcome Message
        logger.info msg
        logger.info ''

        return STATUS_TASK_DONE

    }
}
