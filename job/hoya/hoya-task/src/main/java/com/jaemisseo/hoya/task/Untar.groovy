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
class Untar extends TaskHelper{

    @Value(name='from', filter='getFilePath', required=true)
    String filePath

    @Value(name='to', filter='getFilePath', required=true)
    String destPath

    @Value
    FileSetup fileSetup



    @Override
    Integer run(){
        FileMan.untar(filePath, destPath, fileSetup)
        return STATUS_TASK_DONE
    }

}
