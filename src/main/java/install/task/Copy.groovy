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
class Copy extends TaskUtil{

    @Value(name='from', filter='getFilePathList', required=true)
    List<String> sourcePathList

    @Value(name='to', filter='getFilePathList', required=true)
    List<String> destPathList

    @Value
    FileSetup fileSetup



    @Override
    Integer run(){
        sourcePathList.each{ String sourceFilePath ->
            destPathList.each{ String destFilePath ->
                FileMan.copy(sourceFilePath, destFilePath, fileSetup)
            }
        }
        return STATUS_TASK_DONE
    }

}
