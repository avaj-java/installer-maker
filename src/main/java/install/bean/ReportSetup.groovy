package install.bean

import com.jaemisseo.man.util.FileSetup
import com.jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-04-04.
 */
class ReportSetup extends Option{

    Boolean modeReport
    Boolean modeReportText
    Boolean modeReportExcel
    Boolean modeReportConsole

    FileSetup fileSetup

}
