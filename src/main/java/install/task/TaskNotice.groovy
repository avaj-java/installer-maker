package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.VariableMan

/**
 * Created by sujkim on 2017-03-18.
 */
class TaskNotice extends TaskUtil{

    @Override
    Integer run(){

        //Get Message
        String msg = get("msg")

        //Show You Welcome Message
        println msg

        return STATUS_TASK_DONE
    }
}
