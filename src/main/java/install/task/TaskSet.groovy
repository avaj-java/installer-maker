package install.task
/**
 * Created by sujkim on 2017-03-18.
 */
class TaskSet extends TaskUtil{

    @Override
    Integer run(){
        //Set Some Property
        setPropValue()

        return STATUS_TASK_DONE
    }

}
