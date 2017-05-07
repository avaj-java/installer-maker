package install.task

import jaemisseo.man.FileMan
import jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
class TaskFileMkdir extends TaskUtil{

    @Override
    Integer run(){

        //Ready
        String destPath = getFilePath('dest.path')
        Map buildStructureMap = getMap('structure')
        FileSetup fileSetup = genMergedFileSetup()

        //DO
        println "<MKDIR>"
        FileMan.mkdirs(destPath, buildStructureMap)

        return STATUS_TASK_DONE
    }

}
