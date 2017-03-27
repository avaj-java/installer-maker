package install.job

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.VariableMan
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-02-17.
 */
class MacGyver extends TaskUtil {

    MacGyver(PropMan propman){
        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman)
    }



    /**
     * RUN
     */
    void run(){

        Map taskMap = [
                'tar': TASK_TAR,
                'zip': TASK_ZIP,
                'jar': TASK_JAR,
                'untar': TASK_UNTAR,
                'unzip': TASK_UNZIP,
                'unjar': TASK_UNJAR,
                'mkdir': TASK_MKDIR,
                'copy': TASK_COPY,
                'replace': TASK_REPLACE,
                'sql': TASK_SQL,
                'notice': TASK_NOTICE,
                'q': TASK_Q,
                'q-choice': TASK_Q_CHOICE,
                'q-yn': TASK_Q_YN,
                'test-db':TASK_JDBC,
                'test-rest':TASK_REST,
                'test-socket':TASK_SOCKET,
                'test-email': TASK_EMAIL,
                'test-port':TASK_PORT,
                'merge-properties':TASK_MERGE_ROPERTIES,
                'init':TASK_GEN_SAMPLE_PROPERTIES,
        ]

        taskMap.each{ String command, String taskCode ->
            if (propman.get(command))
                runTask(taskCode)
        }

    }

}
