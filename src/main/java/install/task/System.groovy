package install.task

import jaemisseo.man.configuration.annotation.Alias
import jaemisseo.man.configuration.annotation.type.Task
import install.util.TaskUtil

/**
 * Created by sujkim on 2017-02-22.
 */
@Alias('s')
@Task
class System extends TaskUtil{



    @Override
    Integer run(){

        provider.printSystem()

        java.lang.System.exit(0)

        return STATUS_TASK_DONE
    }

}
