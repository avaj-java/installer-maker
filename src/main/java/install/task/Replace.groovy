package install.task

import install.configuration.annotation.type.TerminalValueProtocol
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.FileMan
import install.bean.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
@Document("""
    Replace some content on plain text
""")
@TerminalValueProtocol(['file', 'replace.all'])
class Replace extends TaskUtil{

    @Value(name='file', filter='getFilePathList', required=true)
    List<String> filePathList

    @Value
    FileSetup fileSetup

    @Value('replace.all')
    Map replaceMap

    @Value('replace.line')
    Map replaceLineMap

    @Value('replace.properties')
    Map replacePropertiesMap

    @Value('replace.yml')
    Map replaceYamlMap



    @Override
    Integer run(){

        logger.debug "[Replacement]"
        logger.debug replaceMap ? replaceMap.toMapString() : ''
        logger.debug replaceLineMap ? replaceLineMap.toMapString() : ''
        logger.debug replacePropertiesMap ? replacePropertiesMap.toMapString() : ''
        logger.debug replaceYamlMap ? replaceYamlMap.toMapString() : ''

        filePathList.each{ String filePath ->
            new FileMan(filePath)
                        .set( fileSetup )
                        .backup()
                        .read()
                        .replace( replaceMap )
                        .replaceLine( replaceLineMap )
                        .replaceProperties( replacePropertiesMap )
                        .replaceYaml( replaceYamlMap )
                        .write( fileSetup.clone([modeAutoOverWrite:true]) )
                        .report()
        }

        return STATUS_TASK_DONE
    }

}

