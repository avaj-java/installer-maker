package install.bean

import com.jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-02-19.
 */
class InstallGlobalOption extends Option{

    Boolean modeGenerateReportText = false
    Boolean modeGenerateReportExcel = false
    Boolean modeGenerateReportSql = false
    Boolean modeExcludeExecuteSql
    Boolean modeExcludeCheckBefore
    Boolean modeExcludeReport
    Boolean modeExcludeReportConsole

    String reportFileEncoding
    String reportFileLineBreak = '\r\n\r\n'
    String reportFileLastLineBreak

}
