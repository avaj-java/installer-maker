package install.task

import install.util.TaskUtil

/**
 * Created by sujkim on 2017-03-18.
 */
class Set extends TaskUtil{

    @Override
    Integer run(){
        //Set Some Property
        setPropValue()

        return STATUS_TASK_DONE
    }

}
