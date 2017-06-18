package install.job

import install.util.JobUtil
import install.annotation.Command
import install.annotation.Init
import install.annotation.Job
import install.bean.InstallerGlobalOption
import install.bean.ReportSetup
import install.configuration.InstallerPropertiesGenerator
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-02-17.
 */
@Job
class Installer extends JobUtil{

    @Init(lately=true)
    void init(){
        levelNamesProperty = 'i.level'
        executorNamePrefix = 'i'
        propertiesFileName = 'installer.properties'
        validTaskList = Util.findAllClasses(packageNameForTask)

        this.propman = setupPropMan(propGen)
        this.varman = setupVariableMan(propman, executorNamePrefix)
        parsePropMan(propman, varman, executorNamePrefix)
        setBeforeGetProp(propman, varman)
        this.gOpt = new InstallerGlobalOption().merge(new InstallerGlobalOption(
                fileSetup                  : genGlobalFileSetup(),
                reportSetup                : genGlobalReportSetup(),
        ))
    }

    PropMan setupPropMan(InstallerPropertiesGenerator propGen){
        PropMan propmanForInstaller = propGen.get('installer')
        PropMan propmanForReceptionist = propGen.get('receptionist')
        PropMan propmanDefault = propGen.getDefaultProperties()
        PropMan propmanExternal = propGen.getExternalProperties()

        String libtohomeRelPath = FileMan.getFileFromResource('.libtohome')?.text.replaceAll('\\s*', '') ?: '../'
        String installerHome = FileMan.getFullPath(propmanDefault.get('lib.dir'), libtohomeRelPath)

        //From User's FileSystem or Resource
        String userSetPropertiesDir = propmanExternal['properties.dir']
        if (userSetPropertiesDir)
            propmanForInstaller.merge("${userSetPropertiesDir}/installer.properties")
        else
            propmanForInstaller.mergeResource("installer.properties")

        propmanForInstaller.merge(propmanForReceptionist)
                            .mergeNew(propmanDefault)
                            .merge(['installer.home': installerHome])

        return propmanForInstaller
    }



    /*************************
     * INSTALL
     *************************/
    @Command('install')
    void install(){
        ReportSetup reportSetup = gOpt.reportSetup
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



    /*************************
     * WRITE Report
     *************************/
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
                logBigTitle("SAVE Excel Report")
                println "Creating Excel Report File..."
                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", reportMapList, 'sqlFileName')
            }
        }
    }



}
