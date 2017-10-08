package install.task

import install.configuration.annotation.type.TerminalValueProtocol
import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.FileMan
import install.bean.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
@TerminalValueProtocol(['from', 'to'])
class Jar extends TaskUtil{

    @Value(name='from', filter='getFilePath')
    String filePath

    @Value(name='to', filter='getFilePath')
    String destPath

    @Value
    FileSetup fileSetup



    @Override
    Integer run(){
        println "<JAR>"
        FileMan.jar(filePath, destPath, fileSetup)

        return STATUS_TASK_DONE
    }

}
