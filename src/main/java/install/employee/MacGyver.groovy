package install.employee

import install.bean.ReportSetup
import install.configuration.annotation.Alias
import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Employee
import install.configuration.annotation.type.Task
import install.configuration.reflection.ReflectInfomation
import install.data.PropertyProvider
import install.util.EmployeeUtil
import install.util.TaskUtil
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-02-17.
 */
@Employee
class MacGyver extends EmployeeUtil {

    @Init
    void init(){
        levelNamesProperty = 'macgyver.level'
        executorNamePrefix = 'm'
        propertiesFileName = 'macgyver.properties'
        validTaskList = Util.findAllClasses('install', [Task])

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
        //Run Task
        //- Get Task Annotated Instance List from Singleton Pool
        List taskInstanceList = config.findAllInstances([Task])
        taskInstanceList.each{ instance ->
            String taskName = instance.getClass().getSimpleName().toLowerCase()
            if (propman.get(taskName))
                runTask(taskName)
        }

        //Run Task with Alias
        Map<Class, ReflectInfomation> aliasTaskReflectionMap = config.reflectionMap.findAll{ clazz, info ->
            info.alias && propman.get(info.alias)
        }
        aliasTaskReflectionMap.each{ clazz, info ->
            String taskName = info.instance.getClass().getSimpleName().toLowerCase()
            runTask(taskName)
        }

        return TaskUtil.STATUS_TASK_DONE
    }



    @Alias('m')
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



    @Alias('h')
    @Command('help')
    void help(){
        runTask('help')
    }



    @Command('test')
    void test(){
        config.command('clean')
        config.command('build')
        config.command('run')
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
