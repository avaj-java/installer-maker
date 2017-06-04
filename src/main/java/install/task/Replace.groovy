package install.task

import install.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
class Replace extends TaskUtil{

    @Override
    Integer run(){

        //Ready
        List<String> filePathList = getFilePathList('file.path')
        FileSetup fileSetup = genMergedFileSetup()
        Map replaceMap = getMap('file.replace')
        Map replaceLineMap = getMap('file.replace.line')
        Map replacePropertyMap = getMap('file.replace.property')

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

