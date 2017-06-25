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
class Jar extends TaskUtil{

    @Value(property='file.path', method='getFilePath')
    String filePath

    @Value(property='dest.path', method='getFilePath')
    String destPath

    @Value(method='genMergedFileSetup')
    FileSetup fileSetup



    @Override
    Integer run(){
        println "<JAR>"
        FileMan.jar(filePath, destPath, fileSetup)

        return STATUS_TASK_DONE
    }

}
