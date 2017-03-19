package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan

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

        //DO
        println "<MKDIR>"
        println "- Structure: ${buildStructureMap}"
        println "- Dest Path: ${destPath}"

        //MKDIR
        FileMan.mkdirs(destPath, buildStructureMap)

    }

}
