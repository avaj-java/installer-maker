package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.util.FileSetup
import install.bean.ReportSetup

/**
 * Created by sujkim on 2017-02-22.
 */
class TaskFileCopy extends TaskUtil{

    @Override
    void run(){

        //Ready
        String filePath = getFilePath('file.path')
        String destPath = getFilePath('dest.path')
        FileSetup fileSetup = genMergedFileSetup()

        //DO
        println "<COPY>"
        FileMan.copy(filePath, destPath, fileSetup)

    }

}
