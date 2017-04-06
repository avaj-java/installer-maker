package install.bean

import com.jaemisseo.man.util.FileSetup
import com.jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-02-19.
 */
class ReceptionistGlobalOption extends Option{

    Boolean modeRemember
    String rememberFilePath

    FileSetup rememberFileSetup
    FileSetup fileSetup

}
