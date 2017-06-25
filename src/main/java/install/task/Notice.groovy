package install.task

import install.annotation.Task
import install.annotation.Value
import install.util.TaskUtil

/**
 * Created by sujkim on 2017-03-18.
 */
@Task
class Notice extends TaskUtil{

    @Value('msg')
    String msg



    @Override
    Integer run(){
        //Show You Welcome Message
        println msg

        return STATUS_TASK_DONE
    }
}
