package install.task

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.QuestionMan
import com.jaemisseo.man.util.QuestionSetup

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
