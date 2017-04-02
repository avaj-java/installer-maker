package install.bean

import com.jaemisseo.man.util.FileSetup
import com.jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-02-19.
 */
class BuilderGlobalOption extends Option{

    String installerName
    String installerHomeToLibRelPath
    String installerHomeToBinRelPath
    String buildDir
    String buildTempHome
    String buildInstallerHome
    String propertiesDir

    FileSetup fileSetup

}
