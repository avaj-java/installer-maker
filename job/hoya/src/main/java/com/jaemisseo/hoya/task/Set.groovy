package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalIgnore
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.PropMan
import jaemisseo.man.configuration.annotation.type.Undomore

/**
 * Created by sujkim on 2017-03-18.
 */
@TerminalIgnore
@Undoable
@Undomore
@Task
class Set extends TaskHelper{

    @Value(name='properties.file', filter="getFilePathList")
    List<String> propertiesFilePathList

    @Value('target')
    List<String> targetProperties

    @Value(name='type', caseIgnoreValidList=['parse', 'raw', 'list-from-file', 'raw-from-file'])
    String type

    @Value(name='rsp.file', filter="getFilePath")
    String rspFilePath



    @Override
    Integer run(){
        //- Set Some Properties from Response File(.rsp)
        if (rspFilePath){
            PropMan runtimeLoadedPropMan = generatePropMan(rspFilePath, 'ask')
            provider.propman.merge([
                'mode.load.rsp': true,
                'answer.repeat.limit': 0
            ]).merge(runtimeLoadedPropMan)
        }

        //- Set Some Properties from Properties File
        if (propertiesFilePathList){
            propertiesFilePathList.each{ String propertiesFilepath ->
                Map propertiesMap = generateMapFromPropertiesFile(new File(propertiesFilepath), targetProperties)
                provider.propman.merge(propertiesMap)
            }
        }

        //Set Some Property
        setPropValue()
        return STATUS_TASK_DONE
    }

}
