package install.job

import com.jaemisseo.man.PropMan
import com.jaemisseo.man.ReportMan
import com.jaemisseo.man.VariableMan
import install.bean.InstallerGlobalOption
import install.bean.ReportSetup

/**
 * Created by sujkim on 2017-02-17.
 */
class JobInstaller extends JobUtil{

    JobInstaller(PropMan propman) {
        //Job Setup
        levelNamesProperty = 'i.level'
        levelNamePrefix = 'i'

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, levelNamePrefix)
        setBeforeGetProp(propman, varman)
        this.gOpt = new InstallerGlobalOption().merge(new InstallerGlobalOption(
                fileSetup                  : genGlobalFileSetup(),
                reportSetup                : genGlobalReportSetup(),
        ))
    }



    /**
     * INSTALL
     */
    void install(){

        ReportSetup reportSetup = gOpt.reportSetup

        //Each level by level
        eachLevel(levelNamesProperty, levelNamePrefix, 'installer.properties'){ String levelName ->
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
