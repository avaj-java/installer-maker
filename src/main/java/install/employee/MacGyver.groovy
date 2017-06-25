package install.employee

import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Employee
import install.configuration.annotation.type.Task
import install.bean.ReportSetup
import install.configuration.PropertyProvider
import install.util.JobUtil
import install.util.TaskUtil
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan

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

        this.propman = setupPropMan(provider)
        this.varman = setupVariableMan(propman, executorNamePrefix)
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForMacgyver = provider.propGen.get('macgyver')
        PropMan propmanDefault = provider.propGen.getDefaultProperties()
        PropMan propmanExternal = provider.propGen.getExternalProperties()

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
        //Get Task Annotated Instance List from Singleton Pool
        List instanceList = config.findAllInstances([Task])
        instanceList.each{ instance ->
            String taskName = instance.getClass()
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

        ReportSetup reportSetup = provider.genGlobalReportSetup()

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
