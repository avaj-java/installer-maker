package install.employee

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.ReportMan
import com.jaemisseo.man.VariableMan
import install.bean.ReportSetup
import install.job.JobUtil

/**
 * Created by sujkim on 2017-02-17.
 */
class MacGyver extends JobUtil {

    MacGyver(PropMan propman){
        levelNamesProperty = 'macgyver.level'
        levelNamePrefix = 'm'

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, levelNamePrefix)
        setBeforeGetProp(propman, varman)
    }



    /**
     * RUN
     */
    void run(){
        List taskMap = [
            TASK_TAR, TASK_ZIP, TASK_JAR, TASK_UNTAR, TASK_UNZIP, TASK_UNJAR,
            TASK_MKDIR, TASK_COPY, TASK_GROOVYRANGE, TASK_MERGE_ROPERTIES, TASK_REPLACE, TASK_SQL,
            TASK_JDBC, TASK_REST, TASK_SOCKET, TASK_EMAIL, TASK_PORT,
            TASK_NOTICE, TASK_Q, TASK_Q_CHOICE, TASK_Q_YN, TASK_SET,
        ]

        taskMap.each{ String taskCode ->
            if (propman.get(taskCode.toLowerCase()))
                runTask(taskCode)
        }

    }


    /**
     * DO SOMETHING
     */
    void doSomething(){

        ReportSetup reportSetup = genMergedReportSetup('')

        //Each level by level
        eachLevel(levelNamesProperty, levelNamePrefix, 'macgyver.properties'){ String levelName ->
            try{
                runTaskByPrefix("${levelNamePrefix}.${levelName}.")
            }catch(e){
                //Write Report
                writeReport(reportMapList, reportSetup)
                throw e
            }
        }

        //Write Report
        writeReport(reportMapList, reportSetup)
    }

    /**
     * WRITE Report
     */
    private void writeReport(List reportMapList, ReportSetup reportSetup){

        //Generate File Report
        if (reportMapList){
            String date = new Date().format('yyyyMMdd_HHmmss')
            String fileNamePrefix = 'report_analysis'

            if (reportSetup.modeReportText) {
//                List<String> stringList = sqlman.getAnalysisStringResultList(reportMapList)
//                FileMan.write("${fileNamePrefix}_${date}.txt", stringList, opt)
            }

            if (reportSetup.modeReportExcel){
                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", reportMapList, 'sqlFileName')
            }

        }

    }

}
