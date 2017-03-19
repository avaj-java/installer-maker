package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
class TaskFileReplace extends TaskUtil{

    TaskFileReplace(PropMan propman){
        this.propman = propman
    }



    /**
     * RUN
     */
    void run(String propertyPrefix){

        //Ready
        List<String> filePathList = getFilePathList(propertyPrefix, 'file.path')
        FileSetup globalOption = genFileSetup()
        FileSetup localOption = genFileSetup(propertyPrefix)
        Map replaceMap = getMap(propertyPrefix, 'file.replace')
        Map replaceLineMap = getMap(propertyPrefix, 'file.replace.line')
        Map replacePropertyMap = getMap(propertyPrefix, 'file.replace.property')

        //Do
        filePathList.each{ String filePath ->
            new FileMan(filePath)
                        .set( globalOption )
                        //BACKUP
                        .backup( localOption )
                        //READ
                        .read()
                        //REPLACE
                        .replace( replaceMap )
                        .replaceLine( replaceLineMap )
                        .replaceProperty( replacePropertyMap )
                        //WRITE
                        .write( localOption )
                        //REPORT
                        .report()
        }

    }

}

