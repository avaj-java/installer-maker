package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import install.bean.FileSetup

/**
 * Created by sujkim on 2017-02-27.
 */
@Task
@TerminalValueProtocol(['from', 'into'])
class MergeProperties extends TaskUtil{

    @Value(name='from', required=true)
    String specificPropertiesFilePath

    @Value(name='into', required=true)
    String sourceFilePath

    @Value
    FileSetup fileSetup



    @Override
    Integer run() {
        fileSetup.put([modeAutoBackup:true])

        logMiddleTitle('START MERGE PROPERTIES')

        // - Read Properties
        FileMan fileman = new FileMan(sourceFilePath).set(fileSetup).read()
        PropMan propmanSource = new PropMan(sourceFilePath)
        PropMan propmanSpecific = new PropMan(specificPropertiesFilePath)

        //Do
        Map differentValuePropMap = propmanSpecific.diffMap(propmanSource)
        Map matchingPropMap = propmanSource.getMatchingMap(propmanSpecific)
        Map notMatchingPropMap = propmanSource.getNotMatchingMap(propmanSpecific)
        Map notMatchingPropMap2 = propmanSpecific.getNotMatchingMap(propmanSource)

        //Log
        if (differentValuePropMap)
            differentValuePropMap.each{ logger.debug it }
        logger.debug "<GET-VALUE-FROM (${propmanSource.filePath})> - CHECK"
        logger.debug " - All Properties Size         : ${propmanSource.size()}"
        logger.debug " - Matching Properties Size    : ${matchingPropMap.size()} <<"
        logger.debug " - NotMatching Properties Size : ${notMatchingPropMap.size()}"
        logger.debug ""

        logger.debug "<MERGE-INTO (${propmanSpecific.filePath})> - CHECK"
        logger.debug " - All Properties Size         : ${propmanSpecific.size()}"
        logger.debug " - Matching Properties Size    : ${matchingPropMap.size()} <<"
        logger.debug " - NotMatching Properties Size : ${notMatchingPropMap2.size()}"
        logger.debug ""

        logger.debug "<MERGE> - CHECK"
        logger.debug " - Matching Properties Size                     : ${matchingPropMap.size()}"
        logger.debug " - Matching Properties And Different Value Size : ${differentValuePropMap.size()}"
        logger.debug ""

        //Merge DifferentValue into SourceProperties
        if (differentValuePropMap)
            fileman.backup().replaceProperty(differentValuePropMap).write()
        else
            logger.error "=> NOTHING TO MERGE\n"

        return STATUS_TASK_DONE
    }

}
