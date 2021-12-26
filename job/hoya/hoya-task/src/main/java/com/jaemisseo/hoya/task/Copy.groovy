package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.FileMan
import com.jaemisseo.hoya.bean.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
@TerminalValueProtocol(['from', 'to'])
class Copy extends TaskHelper{

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
