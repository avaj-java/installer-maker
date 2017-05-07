package install.bean

import jaemisseo.man.util.FileSetup
import jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-02-19.
 */
class BuilderGlobalOption extends Option{

    String installerName
    String installerHomeToLibRelPath
    String installerHomeToBinRelPath
    String installerHomeToRspRelPath
    String buildDir
    String buildTempDir
    String buildDistDir
    String buildInstallerHome
    String modeAutoRsp
    String modeAutoZip
    String modeAutoTar

    String propertiesDir

    FileSetup fileSetup
    ReportSetup reportSetup

}
