package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan

/**
 * Created by sujkim on 2017-02-22.
 */
class TaskFileCopy extends TaskUtil{

    TaskFileCopy(PropMan propman){
        this.propman = propman
    }



    /**
     * RUN
     */
    void run(String propertyPrefix){

        //Ready
        String filePath = getFilePath(propertyPrefix, 'file.path')
        String destPath = getFilePath(propertyPrefix, 'dest.path')
//        Boolean autoMkdir = getValue(propertyPrefix, 'auto.mkdir')

        //DO
        println "<Copy File>"
        println "- Source Path: ${filePath}"
        println "- Dest Path: ${destPath}"

        //COPY
        FileMan.copy(filePath, destPath)

    }

}
