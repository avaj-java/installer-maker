package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.Alias
import jaemisseo.man.configuration.annotation.HelpIgnore
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task

/**
 * Created by sujkim on 2017-02-22.
 */
@Alias('v')
@Task
class Version extends TaskHelper{

    @HelpIgnore
    @Value('application.name')
    String applicationName = 'installer-maker'

    @Override
    Integer run(){

        provider.printVersion()

        java.lang.System.exit(0)

        return STATUS_TASK_DONE
    }

}
