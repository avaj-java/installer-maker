package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
class TaskFileJar extends TaskUtil{

    TaskFileJar(PropMan propman){
        this.propman = propman
    }



    /**
     * RUN
     */
    void run(String propertyPrefix){

        //Ready
        String filePath = getFilePath(propertyPrefix, 'file.path')
        String destPath = getFilePath(propertyPrefix, 'dest.path')
        FileSetup fileSetup = genMergedFileSetup(propertyPrefix)

        //DO
        println "<JAR>"
        FileMan.jar(filePath, destPath, fileSetup)

    }

}
