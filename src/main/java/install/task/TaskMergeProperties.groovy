package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-27.
 */
class TaskMergeProperties extends TaskUtil{

    @Override
    void run(Map prop) {
        //Ready
        PropMan propman = new PropMan(prop)
        String sourceFilePath               = propman.get('merge-into')
        String specificPropertiesFilePath   = propman.get('get-value-from')
        String fileEncoding                 = propman.get('file.encoding')
        FileSetup opt = new FileSetup(modeAutoBackup: true)
        if (fileEncoding)
            opt.encoding = fileEncoding

        logMiddleTitle('START MERGE PROPERTIES')

        // - Read Properties
        FileMan fileman = new FileMan(sourceFilePath).set(opt).read()
        PropMan propmanSource = new PropMan().readFile(sourceFilePath)
        PropMan propmanSpecific = new PropMan().readFile(specificPropertiesFilePath)

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
            println "=> NO MERGE\n"

        logMiddleTitle('FINISHED MERGE PROPERTIES')
    }

}
