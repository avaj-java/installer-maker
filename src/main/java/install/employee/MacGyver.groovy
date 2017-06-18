package install.employee

import install.util.JobUtil
import install.util.TaskUtil
import install.annotation.Command
import install.annotation.Employee
import install.annotation.Init
import install.bean.ReportSetup
import install.configuration.InstallerPropertiesGenerator
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-02-17.
 */
@Employee
class MacGyver extends JobUtil {

    @Init
    void init(){
        levelNamesProperty = 'macgyver.level'
        executorNamePrefix = 'm'
        propertiesFileName = 'macgyver.properties'

        this.propman = setupPropMan(propGen)
        this.varman = setupVariableMan(propman, executorNamePrefix)
    }

    PropMan setupPropMan(InstallerPropertiesGenerator propGen){
        PropMan propmanForMacgyver = propGen.get('macgyver')
        PropMan propmanDefault = propGen.getDefaultProperties()
        PropMan propmanExternal = propGen.getExternalProperties()

        //From User's FileSystem or Resource
        String userSetPropertiesDir = propmanExternal['properties.dir']
        if (userSetPropertiesDir)
            propmanForMacgyver.merge("${userSetPropertiesDir}/macgyver.properties")
        else
            propmanForMacgyver.mergeResource("macgyver.properties")

        propmanForMacgyver.merge(propmanExternal)
                            .mergeNew(propmanDefault)

        return propmanForMacgyver
    }

    

    @Command('doSomething')
    Integer doSomething(){
        List<Class> clazzList = Util.findAllClasses(packageNameForTask)

        clazzList.each{ clazz ->
            String taskName = clazz.getSimpleName()
            if (propman.get(taskName))
                runTask(taskName)
        }

        return TaskUtil.STATUS_TASK_DONE
    }


    /**
     * DO SOMETHING
     */
    @Command('macgyver')
    void macgyver(){

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
