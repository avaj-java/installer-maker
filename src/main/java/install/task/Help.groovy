package install.task

import install.configuration.annotation.Alias
import install.configuration.annotation.method.Command
import install.configuration.annotation.type.Task
import install.util.TaskUtil

/**
 * Created by sujkim on 2017-06-26.
 */
@Alias('h')
@Task
class Help extends TaskUtil{


    @Override
    Integer run(){

        println 'help !!! '

        //Command
        config.methodCommandNameMap.each{ commandName, info ->
            println commandName
        }

        println "-----"

        //Task
        List<Object> taskInstances = config.findAllInstances(Task)
        taskInstances.each{
            println it.getClass().getSimpleName().toLowerCase()
        }
        

        return STATUS_TASK_DONE
    }

}
