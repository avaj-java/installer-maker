package install.task

import install.configuration.annotation.type.TerminalValueProtocol
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
@Document("""

""")
@TerminalValueProtocol(['file', 'replace.all'])
class Replace extends TaskUtil{

    @Value(property='file', method='getFilePathList')
    List<String> filePathList

    @Value(method='genMergedFileSetup')
    FileSetup fileSetup

    @Value(property='replace.all', method='getMap')
    Map replaceMap

    @Value(property='replace.line', method='getMap')
    Map replaceLineMap

    @Value(property='replace.property', method='getMap')
    Map replacePropertyMap



    @Override
    Integer run(){
        println "<REPLACE>"
        filePathList.each{ String filePath ->
            new FileMan(filePath)
                        .set( fileSetup )
                        .backup()
                        .read()
                        .replace( replaceMap )
                        .replaceLine( replaceLineMap )
                        .replaceProperty( replacePropertyMap )
                        .write( fileSetup.clone([modeAutoOverWrite:true]) )
                        .report()
        }

        return STATUS_TASK_DONE
    }

}

