package install.task

import com.jaemisseo.man.*
import com.jaemisseo.man.util.FileSetup
import install.bean.ReportSql
import install.bean.InstallGlobalOption

/**
 * Created by sujkim on 2017-02-17.
 */
class TaskInstall extends TaskUtil{

    TaskInstall(SqlMan sqlman, PropMan propman, FileMan fileman) {
        this.sqlman = sqlman
        this.fileman = fileman
        this.propman = propman
        this.gOpt = new InstallGlobalOption().merge(new InstallGlobalOption(
            modeGenerateReportText     : propman.get('report.text'),
            modeGenerateReportExcel    : propman.get('report.excel'),
            modeGenerateReportSql      : propman.get('report.sql'),
            modeExcludeExecuteSql      : propman.get('x.execute.sql'),
            modeExcludeCheckBefore     : propman.get('x.check.before'),
            modeExcludeReport          : propman.get('x.report'),
            modeExcludeReportConsole   : propman.get('x.report.console'),
            reportFileEncoding         : propman.get('report.file.encoding'),
            reportFileLineBreak        : propman.get('report.file.linebreak'),
            reportFileLastLineBreak    : propman.get('report.file.last.linebreak'),
        ))
    }

    InstallGlobalOption gOpt
    FileMan fileman
    SqlMan sqlman

    String levelNamesProperty = 'install.level'



    /**
     * RUN
     */
    void run(){
        FileSetup fileSetup = generateFileSetup()

        //Each level by level
        eachLevel(levelNamesProperty){ String levelName ->
            String propertyPrefix = "${levelNamesProperty}.${levelName}."
            String taskName = getString(propertyPrefix, 'task')?.trim()?.toUpperCase()
            logBigTitle("${levelName}")

            runTask(taskName, propertyPrefix)
        }

        writeReport(beforeReportList, afterReportMapList, fileSetup)
    }



    private FileSetup generateFileSetup(){
        FileSetup fileSetup = new FileSetup()
        if (gOpt.reportFileEncoding)
            fileSetup.encoding = gOpt.reportFileEncoding
        if (gOpt.reportFileLineBreak)
            fileSetup.lineBreak = gOpt.reportFileLineBreak
        if (gOpt.reportFileLastLineBreak)
            fileSetup.lastLineBreak = gOpt.reportFileLastLineBreak
        return fileSetup
    }

    void writeReport(List beforeReportList, List afterReportMapList, FileSetup fileSetup){
        //Generate File Report
        String fileNamePrefix
        String date = new Date().format('yyyyMMdd_HHmmss')
        if (beforeReportList){
            fileNamePrefix = 'report_analysis'
            if (gOpt.modeGenerateReportText) {
                List<String> stringList = sqlman.getAnalysisStringResultList(beforeReportList)
                new FileMan().createNewFile('./', "${fileNamePrefix}_${date}.txt", stringList, fileSetup)
            }
            if (gOpt.modeGenerateReportExcel){
                List<ReportSql> excelReportList = beforeReportList.collect{ SqlAnalMan.SqlObject sqlObj ->
                    new ReportSql(
                            sqlFileName     : sqlObj.sqlFileName,
                            seq             : sqlObj.seq,
                            query           : sqlObj.query,
//                        isExistOnDB     : sqlObj.isExistOnDB?'Y':'N',
//                        isOk            : sqlObj.isOk?'Y':'N',
                            warnningMessage : sqlObj.warnningMessage,
//                        error           : sqlObj.error?.toString(),
                    )
                }
                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", excelReportList, 'sqlFileName')
            }
        }

        if (afterReportMapList){
            fileNamePrefix = 'report_result'
            if (gOpt.modeGenerateReportText){
                List<String> stringList = sqlman.getResultList(afterReportMapList)
                new FileMan().createNewFile('./', "${fileNamePrefix}_${date}.txt", stringList, fileSetup)
            }
            if (gOpt.modeGenerateReportExcel){
                List<ReportSql> excelReportList = beforeReportList.collect{ SqlAnalMan.SqlObject sqlObj ->
                    new ReportSql(
                            sqlFileName     : sqlObj.sqlFileName,
                            seq             : sqlObj.seq,
                            query           : sqlObj.query,
//                        isExistOnDB     : sqlObj.isExistOnDB?'Y':'N',
                            isOk            : sqlObj.isOk?'Y':'N',
                            warnningMessage : sqlObj.warnningMessage,
                            error           : sqlObj.error?.toString(),
                    )
                }
                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", excelReportList, 'sqlFileName')
            }
        }
    }

}
