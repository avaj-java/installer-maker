package install.job

import com.jaemisseo.man.*
import install.bean.BuilderGlobalOption
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-02-17.
 */
class JobBuilder extends TaskUtil {

    JobBuilder(PropMan propman){
        //Job Setup
        levelNamesProperty = 'build.level'
        invalidTaskList = [TASK_SQL]

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, levelNamesProperty)
        setBeforeGetProp(propman, varman)
        this.gOpt = new BuilderGlobalOption().merge(new BuilderGlobalOption(
            fileSetup   : genFileSetup()
        ))
    }



    /**
     * RUN
     */
    void run(){

        //Each level by level
        eachLevel(levelNamesProperty){ String levelName ->
            String propertyPrefix = "${levelNamesProperty}.${levelName}."
            String taskName = getString(propertyPrefix, 'task')?.trim()?.toUpperCase()
            logBigTitle("${levelName}")
            runTask(taskName, propertyPrefix)
        }

    }

}
