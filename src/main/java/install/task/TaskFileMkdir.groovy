package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
class TaskFileMkdir extends TaskUtil{

    TaskFileMkdir(PropMan propman){
        this.propman = propman
    }



    /**
     * RUN
     */
    void run(String propertyPrefix){

        //Ready
        String destPath = getFilePath(propertyPrefix, 'dest.path')
        Map buildStructureMap = getMap(propertyPrefix, 'structure')
        FileSetup fileSetup = genMergedFileSetup(propertyPrefix)

        //DO
        println "<MKDIR>"
        FileMan.mkdirs(destPath, buildStructureMap)

    }

}
