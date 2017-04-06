package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.SqlAnalMan
import com.jaemisseo.man.SqlMan
import com.jaemisseo.man.util.SqlSetup
import install.bean.ReportSetup
import install.bean.ReportSql

/**
 * Created by sujkim on 2017-02-17.
 */
class TaskSql extends TaskUtil{

    TaskSql(PropMan propman) {
        this.propman = propman
    }

    /**
     * RUN
     */
    void run(String propertyPrefix){

        //1. Default Setup
        sqlman = new SqlMan()
        SqlSetup sqlSetup = genMergedSqlSetup(propertyPrefix)
        List<String> filePathList = getFilePathList(propertyPrefix, 'file.path', 'sql')
        ReportSetup reportSetup = genGlobalReportSetup()


        //2. Execute All SQL
        println "<SQL>"
        filePathList.each{ String filePath ->
            String originFileName = new File(filePath).getName()

            //2. Generate Query Replaced With New Object Name
            sqlman.init()
                    .queryFromFile("${filePath}")
                    .command([SqlMan.ALL])
                    .replace(sqlSetup)

            //3. Report Checking Before
            if (sqlSetup.modeSqlCheckBefore){
                sqlman.checkBefore(sqlSetup)
                //- Add Reoprt
                addReportBefore(reportSetup)
            }

            //- Generate SQL File
            if (sqlSetup.modeSqlFileGenerate)
                FileMan.write("./replaced_${originFileName}", sqlman.getReplacedQueryList(), reportSetup.fileSetup)

            //4. Execute
            if (sqlSetup.modeSqlExecute){
                try{
                    sqlman.run(sqlSetup)
                }catch(e){
                    throw e
                }finally{
                    //- Add Reoprt
                    addReport(reportSetup)
                }
            }
        }

    }

    /**
     * REPORT
     */
    void addReportBefore(ReportSetup reportSetup){
        if (reportSetup.modeReport){
            if (reportSetup.modeReportConsole)
                sqlman.reportAnalysis()
            if (reportSetup.modeReportText || reportSetup.modeReportExcel){
                sqlman.getAnalysisResultList().each{ SqlAnalMan.SqlObject sqlObj ->
                    reportMapList.add(new ReportSql(
                            sqlFileName     : sqlObj.sqlFileName,
                            seq             : sqlObj.seq,
                            query           : sqlObj.query,
//                                isExistOnDB     : sqlObj.isExistOnDB?'Y':'N',
//                                isOk            : sqlObj.isOk?'Y':'N',
                            warnningMessage : sqlObj.warnningMessage,
//                                error           : sqlObj.error?.toString(),
                    ))
                }
            }
        }
    }

    void addReport(ReportSetup reportSetup){
        if (reportSetup.modeReport){
            if (reportSetup.modeReportConsole)
                sqlman.reportResult()
            if (reportSetup.modeReportText || reportSetup.modeReportExcel){
//                Map resultMap = sqlman.getResultReportMap()
                sqlman.getAnalysisResultList().each{ SqlAnalMan.SqlObject sqlObj ->
                    reportMapList.add(new ReportSql(
                            sqlFileName: sqlObj.sqlFileName,
                            seq: sqlObj.seq,
                            query: sqlObj.query,
//                                    isExistOnDB     : sqlObj.isExistOnDB?'Y':'N',
                            isOk: sqlObj.isOk ? 'Y' : 'N',
                            warnningMessage: sqlObj.warnningMessage,
                            error: sqlObj.error?.toString(),
                    ))
                }
            }
        }
    }



}
