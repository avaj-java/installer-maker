package install.bean

import jaemisseo.man.util.FileSetup
import jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-04-04.
 */
class ReportSetup extends Option<ReportSetup> {

    Boolean modeReport
    Boolean modeReportText
    Boolean modeReportExcel
    Boolean modeReportConsole

    FileSetup fileSetup

}
