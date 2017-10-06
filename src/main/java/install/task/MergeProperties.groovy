package install.task

import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-27.
 */
@Task
@TerminalValueProtocol(['from', 'into'])
class MergeProperties extends TaskUtil{

    @Value('from')
    String specificPropertiesFilePath

    @Value('into')
    String sourceFilePath

    @Value(method='genMergedFileSetup')
    FileSetup opt



    @Override
    Integer run() {
        opt.put([modeAutoBackup:true])

        logMiddleTitle('START MERGE PROPERTIES')

        // - Read Properties
        FileMan fileman = new FileMan(sourceFilePath).set(opt).read()
        PropMan propmanSource = new PropMan(sourceFilePath)
        PropMan propmanSpecific = new PropMan(specificPropertiesFilePath)

        //Do
        Map differentValuePropMap = propmanSpecific.diffMap(propmanSource)
        Map matchingPropMap = propmanSource.getMatchingMap(propmanSpecific)
        Map notMatchingPropMap = propmanSource.getNotMatchingMap(propmanSpecific)
        Map notMatchingPropMap2 = propmanSpecific.getNotMatchingMap(propmanSource)

        //Log
        if (differentValuePropMap)
            differentValuePropMap.each{ println it }
        println "<GET-VALUE-FROM (${propmanSource.filePath})> - CHECK"
        println " - All Properties Size         : ${propmanSource.size()}"
        println " - Matching Properties Size    : ${matchingPropMap.size()} <<"
        println " - NotMatching Properties Size : ${notMatchingPropMap.size()}"
        println ""

        println "<MERGE-INTO (${propmanSpecific.filePath})> - CHECK"
        println " - All Properties Size         : ${propmanSpecific.size()}"
        println " - Matching Properties Size    : ${matchingPropMap.size()} <<"
        println " - NotMatching Properties Size : ${notMatchingPropMap2.size()}"
        println ""

        println "<MERGE> - CHECK"
        println " - Matching Properties Size                     : ${matchingPropMap.size()}"
        println " - Matching Properties And Different Value Size : ${differentValuePropMap.size()}"
        println ""

        //Merge DifferentValue into SourceProperties
        if (differentValuePropMap)
            fileman.backup().replaceProperty(differentValuePropMap).write()
        else
            println "=> NOTHING TO MERGE\n"

        logMiddleTitle('FINISHED MERGE PROPERTIES')

        return STATUS_TASK_DONE
    }

}
