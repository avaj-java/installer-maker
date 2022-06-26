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

    @Value(name='from', filter='findAllFilePaths', required=true)
//    @Value(name='from', filter='getFilePathList', required=true)
    List<String> sourcePaths

    @Value(name='to', filter='findAllFilePaths', required=true)
//    @Value(name='to', filter='getFilePathList', required=true)
    List<String> destPaths

    @Value
    FileSetup fileSetup



    @Override
    Integer run(){

        /** Log - START **/
        logger.info " <Task:Copy>"
        logger.info "  - From      : $sourcePaths"
        logger.info "  - To        : $destPaths"
        logger.info ""

        /** Log - START **/
        sourcePaths.each{ String sourceFilePath ->

            destPaths.each{ String destFilePath ->

                FileMan.copy(sourceFilePath, destFilePath, fileSetup)

            }

        }

        return STATUS_TASK_DONE
    }

}
