package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import jaemisseo.man.FileMan
import com.jaemisseo.hoya.bean.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
@TerminalValueProtocol(['to', 'structure'])
class Mkdir extends TaskHelper{

    @Value(name='to', filter='getFilePathList', required=true)
    List<String> destPathList

    @Value('structure')
    Map<String, Map<String, Object>> buildStructureMap

    @Value
    FileSetup fileSetup



    @Override
    Integer run(){

        destPathList.each{ String path ->

            FileMan.mkdirs(path, buildStructureMap, true)

        }

        return STATUS_TASK_DONE
    }

}
