package install.task

import install.annotation.Task
import install.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
@Task
class Replace extends TaskUtil{

    //Ready
    @Value(property='file.path', method='getFilePathList')
    List<String> filePathList

    @Value(method='genMergedFileSetup')
    FileSetup fileSetup

    @Value(property='file.replace', method='getMap')
    Map replaceMap

    @Value(property='file.replace.line', method='getMap')
    Map replaceLineMap

    @Value(property='file.replace.property', method='getMap')
    Map replacePropertyMap



    @Override
    Integer run(){

        //Do
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

