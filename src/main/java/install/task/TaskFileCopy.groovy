package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.util.FileSetup
import install.bean.ReportSetup

/**
 * Created by sujkim on 2017-02-22.
 */
class TaskFileCopy extends TaskUtil{

    TaskFileCopy(PropMan propman){
        this.propman = propman
    }



    /**
     * RUN
     */
    void run(String propertyPrefix){

        //Ready
        String filePath = getFilePath(propertyPrefix, 'file.path')
        String destPath = getFilePath(propertyPrefix, 'dest.path')
        FileSetup fileSetup = genMergedFileSetup(propertyPrefix)
        ReportSetup reportSetup = genGlobalReportSetup()

        //DO
        println "<COPY>"
        try{
            FileMan.copy(filePath, destPath, fileSetup)
        }catch(e){
            throw e
        }finally{
            //- Add Reoprt
            addReport(reportSetup)
        }

    }

    /**
     * REPORT
     */
    void addReport(ReportSetup reportSetup){
        if (reportSetup.modeReport){
            if (reportSetup.modeReportConsole)
                sqlman.reportResult()
            if (reportSetup.modeReportText || reportSetup.modeReportExcel){
//                sqlman.getAnalysisResultList().each{ SqlAnalMan.SqlObject sqlObj ->
//                    reportMapList.add(new ReportSql(
//                            sqlFileName: sqlObj.sqlFileName,
//                            seq: sqlObj.seq,
//                            query: sqlObj.query,
////                                    isExistOnDB     : sqlObj.isExistOnDB?'Y':'N',
//                            isOk: sqlObj.isOk ? 'Y' : 'N',
//                            warnningMessage: sqlObj.warnningMessage,
//                            error: sqlObj.error?.toString(),
//                    ))
//                }
            }
        }
    }

}
