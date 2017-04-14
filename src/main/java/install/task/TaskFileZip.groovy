package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-22.
 */
class TaskFileZip extends TaskUtil{

    @Override
    Integer run(){

        //Ready
        String filePath = getFilePath('file.path')
        String destPath = getFilePath('dest.path')
        FileSetup fileSetup = genMergedFileSetup()

        //DO
        println "<ZIP>"
        FileMan.zip(filePath, destPath, fileSetup)

        return STATUS_TASK_DONE
    }

}
