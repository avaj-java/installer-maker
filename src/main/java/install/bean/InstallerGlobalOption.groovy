package install.bean

import com.jaemisseo.man.util.FileSetup
import com.jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-02-19.
 */
class InstallerGlobalOption extends Option{

    Boolean modeGenerateReportText = false
    Boolean modeGenerateReportExcel = false
    Boolean modeGenerateReportSql = false
    Boolean modeExcludeExecuteSql
    Boolean modeExcludeCheckBefore
    Boolean modeExcludeReport
    Boolean modeExcludeReportConsole

    FileSetup reportFileSetup
    FileSetup fileSetup

}
