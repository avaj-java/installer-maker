package install.bean

import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Bean
import jaemisseo.man.util.Option

/**
 * Created by sujkim on 2017-04-04.
 */
@Bean
class ReportSetup extends Option<ReportSetup> {

    @Value('mode.report')
    Boolean modeReport
    @Value('mode.report.text')
    Boolean modeReportText
    @Value('mode.report.excel')
    Boolean modeReportExcel
    @Value('mode.report.console')
    Boolean modeReportConsole

    @Value(prefix='report.')
    FileSetup fileSetup


}
