package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import jaemisseo.man.configuration.annotation.type.Document
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.FileMan
import com.jaemisseo.hoya.bean.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
@Document("""
    Replace some content on plain text
""")
@TerminalValueProtocol(['file', 'replace.all'])
class Replace extends TaskHelper{

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

