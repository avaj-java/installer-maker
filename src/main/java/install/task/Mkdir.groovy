package install.task

import install.annotation.Task
import install.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
class Mkdir extends TaskUtil{

    @Value(property='dest.path', method='getFilePath')
    String destPath

    @Value(property='structure', method='getMap')
    Map buildStructureMap

    @Value(method='genMergedFileSetup')
    FileSetup fileSetup



    @Override
    Integer run(){
        println "<MKDIR>"
        FileMan.mkdirs(destPath, buildStructureMap)

        return STATUS_TASK_DONE
    }

}
