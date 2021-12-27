package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.Alias
import jaemisseo.man.configuration.annotation.type.Task

/**
 * Created by sujkim on 2017-02-22.
 */
@Alias('s')
@Task
class System extends TaskHelper{



    @Override
    Integer run(){

        provider.printSystem()

        java.lang.System.exit(0)

        return STATUS_TASK_DONE
    }

}
