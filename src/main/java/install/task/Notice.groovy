package install.task

import install.TaskUtil

/**
 * Created by sujkim on 2017-03-18.
 */
class Notice extends TaskUtil{

    @Override
    Integer run(){

        //Get Message
        String msg = get("msg")

        //Show You Welcome Message
        println msg

        return STATUS_TASK_DONE
    }
}
