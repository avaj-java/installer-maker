package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
class TaskFileUnjar extends TaskUtil{

    TaskFileUnjar(PropMan propman){
        this.propman = propman
    }



    /**
     * RUN
     */
    void run(String propertyPrefix){

        //Ready
        String filePath = getFilePath(propertyPrefix, 'file.path')
        String destPath = getFilePath(propertyPrefix, 'dest.path')
        FileSetup fileSetup = genFileSetup(propertyPrefix)

        //DO
        println "<Extract JAR File>"
        println "- Source Path: ${filePath}"
        println "- Dest Path: ${destPath}"
        FileMan.unjar(filePath, destPath, fileSetup.modeAutoMkdir)

    }

}
