package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import jaemisseo.man.FileMan
import install.bean.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
@TerminalValueProtocol(['to', 'structure'])
class Mkdir extends TaskUtil{

    @Value(name='to', filter='getFilePath', required=true)
    String destPath

    @Value('structure')
    Map buildStructureMap

    @Value
    FileSetup fileSetup



    @Override
    Integer run(){
        println "<MKDIR>"
        FileMan.mkdirs(destPath, buildStructureMap)

        return STATUS_TASK_DONE
    }

}
