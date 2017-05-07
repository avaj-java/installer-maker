package install.employee

import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.VariableMan
import install.bean.ReportSetup
import install.job.JobUtil
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-02-17.
 */
class MacGyver extends JobUtil {

    MacGyver(PropMan propman){
        levelNamesProperty = 'macgyver.level'
        executorNamePrefix = 'm'
        propertiesFileName = 'macgyver.properties'

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, executorNamePrefix)
        setBeforeGetProp(propman, varman)
    }



    /**
     * RUN
     */
    Integer run(){
        List taskMap = [
            TASK_TAR, TASK_ZIP, TASK_JAR, TASK_UNTAR, TASK_UNZIP, TASK_UNJAR,
            TASK_MKDIR, TASK_COPY, TASK_MERGE_ROPERTIES, TASK_REPLACE, TASK_SQL,
            TASK_JDBC, TASK_REST, TASK_SOCKET, TASK_EMAIL, TASK_PORT,
            TASK_NOTICE, TASK_Q, TASK_Q_CHOICE, TASK_Q_YN, TASK_SET,
            TASK_GROOVYRANGE, TASK_ENCRYPT, TASK_DECRYPT
        ]

        taskMap.each{ String taskCode ->
            if (propman.get(taskCode.toLowerCase()))
                runTask(taskCode)
        }

        return TaskUtil.STATUS_TASK_DONE

    }


    /**
     * DO SOMETHING
     */
    void doSomething(){

        ReportSetup reportSetup = genGlobalReportSetup()

        //Each level by level
        eachLevelForTask{ String propertyPrefix ->
            try{
                return runTaskByPrefix("${propertyPrefix}")
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
