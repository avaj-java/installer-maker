package install.task

import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
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
        FileMan.mkdirs(destPath, buildStructureMap)
        return STATUS_TASK_DONE
    }

}
