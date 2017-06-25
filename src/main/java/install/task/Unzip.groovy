package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
class Unzip extends TaskUtil{

    @Value(property='file.path', method='getFilePath')
    String filePath

    @Value(property='dest.path', method='getFilePath')
    String destPath

    @Value(method='genMergedFileSetup')
    FileSetup fileSetup



    @Override
    Integer run(){
        println "<UNZIP>"
        FileMan.unzip(filePath, destPath, fileSetup)

        return STATUS_TASK_DONE
    }

}
