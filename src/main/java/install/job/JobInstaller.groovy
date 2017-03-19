package install.job

import com.jaemisseo.man.*
import com.jaemisseo.man.util.FileSetup
import install.bean.ReportSql
import install.bean.InstallerGlobalOption
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-02-17.
 */
class JobInstaller extends TaskUtil{

    JobInstaller(PropMan propman) {
        //Job Setup
        levelNamesProperty = 'install.level'

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, levelNamesProperty)
        setBeforeGetProp(propman, varman)
        this.sqlman = new SqlMan()
        this.fileman = new FileMan()
        this.gOpt = new InstallerGlobalOption().merge(new InstallerGlobalOption(
            modeGenerateReportText     : propman.get('report.text'),
            modeGenerateReportExcel    : propman.get('report.excel'),
            modeGenerateReportSql      : propman.get('report.sql'),
            modeExcludeExecuteSql      : propman.get('x.execute.sql'),
            modeExcludeCheckBefore     : propman.get('x.check.before'),
            modeExcludeReport          : propman.get('x.report'),
            modeExcludeReportConsole   : propman.get('x.report.console'),
            fileSetup                  : genFileSetup(),
            reportFileSetup            : genFileSetup("report."),
        ))
    }



    /**
     * RUN
     */
    void run(){

        //Each level by level
        eachLevel(levelNamesProperty){ String levelName ->
            String propertyPrefix = "${levelNamesProperty}.${levelName}."
            String taskName = getString(propertyPrefix, 'task')?.trim()?.toUpperCase()
            logBigTitle("${levelName}")
            runTask(taskName, propertyPrefix)
        }

        //Write Report
        writeReport(beforeReportList, afterReportMapList, gOpt.reportFileSetup)

    }



    /**
     * WRITE
     */
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
