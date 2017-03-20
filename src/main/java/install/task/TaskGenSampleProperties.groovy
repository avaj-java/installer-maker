package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
class TaskGenSampleProperties extends TaskUtil{

    TaskGenSampleProperties(PropMan propman){
        this.propman = propman
    }



    /**
     * RUN
     */
    void run(String propertyPrefix){

        //Ready
        String destPath = getFilePath(propertyPrefix, 'dest.path') ?: FileMan.getFullPath('./')
        FileSetup fileSetup = genFileSetup(propertyPrefix)

        //DO
        println "<Copy File>"
        println "- Dest Path: ${destPath}"

        new FileMan().readResource('builder.properties').write("${destPath}/builder.properties", fileSetup)
        new FileMan().readResource('receptionist.properties').write("${destPath}/receptionist.properties", fileSetup)
        new FileMan().readResource('installer.properties').write("${destPath}/installer.properties", fileSetup)

    }

}
