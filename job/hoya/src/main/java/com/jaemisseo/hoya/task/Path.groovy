package com.jaemisseo.hoya.task

import com.jaemisseo.hoya.bean.FileSetup
import jaemisseo.man.FileMan
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
@TerminalValueProtocol(['filePath'])
class Path extends TaskHelper{

    @Value(name='filePath', filter='findAllFilePaths', required=true)
    List<String> sourcePathList

    @Value
    FileSetup fileSetup



    @Override
    Integer run(){

        sourcePathList.each{ String sourceFilePath ->

//            FileMan.copy(sourceFilePath, destFilePath, fileSetup)
            println sourceFilePath

        }

        return STATUS_TASK_DONE
    }

}
