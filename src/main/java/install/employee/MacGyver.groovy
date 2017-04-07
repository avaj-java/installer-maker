package install.employee

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.VariableMan
import install.job.JobUtil
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-02-17.
 */
class MacGyver extends JobUtil {

    MacGyver(PropMan propman){
        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman)
    }



    /**
     * RUN
     */
    void run(String propertyPrefix){
        List taskMap = [
                TASK_TAR,
                TASK_ZIP,
                TASK_JAR,
                TASK_UNTAR,
                TASK_UNZIP,
                TASK_UNJAR,
                TASK_MKDIR,
                TASK_COPY,
                TASK_JDBC,
                TASK_REST,
                TASK_SOCKET,
                TASK_EMAIL,
                TASK_PORT,
                TASK_MERGE_ROPERTIES,

                TASK_REPLACE,
                TASK_SQL,
                TASK_NOTICE,
                TASK_Q,
                TASK_Q_CHOICE,
                TASK_Q_YN,
        ]


        taskMap.each{ String taskCode ->
            if (propman.get(taskCode.toLowerCase()))
                runTask(taskCode)
        }

    }

}
