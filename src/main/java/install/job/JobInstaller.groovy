package install.job

import com.jaemisseo.man.*
import com.jaemisseo.man.util.FileSetup
import install.bean.InstallerGlobalOption
import install.bean.ReportSetup
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
        this.gOpt = new InstallerGlobalOption().merge(new InstallerGlobalOption(
                fileSetup                  : genGlobalFileSetup(),
                reportSetup                : genGlobalReportSetup(),
        ))
    }



    /**
     * RUN
     */
    void run(){
    }

    /**
     * INSTALL
     */
    void install(){

        ReportSetup reportSetup = gOpt.reportSetup

        //Each level by level
        eachLevel(levelNamesProperty){ String levelName ->
            try{
                String propertyPrefix = "${levelNamesProperty}.${levelName}."
                String taskName = getString(propertyPrefix, 'task')?.trim()?.toUpperCase()
                logBigTitle("${levelName}")
                runTask(taskName, propertyPrefix)
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
        String fileNamePrefix
        String date = new Date().format('yyyyMMdd_HHmmss')
        if (reportMapList){
            fileNamePrefix = 'report_analysis'
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
