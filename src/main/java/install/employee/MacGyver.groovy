package install.employee

import install.bean.ReportSetup
import install.configuration.annotation.Alias
import install.configuration.annotation.HelpIgnore
import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Employee
import install.configuration.annotation.type.Task
import install.configuration.reflection.ReflectInfomation
import install.data.PropertyProvider
import install.util.EmployeeUtil
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-02-17.
 */
@Employee
class MacGyver extends EmployeeUtil {

    MacGyver(){
        propertiesFileName = 'macgyver'
        executorNamePrefix = 'macgyver'
        levelNamesProperty = 'macgyver.level'
    }

    @Init
    void init(){
        validTaskList = Util.findAllClasses('install', [Task])
        this.propman = setupPropMan(provider)
        this.varman = setupVariableMan(propman, executorNamePrefix)
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForMacgyver = provider.propGen.get('macgyver')
        PropMan propmanDefault = provider.propGen.getDefaultProperties()
        PropMan propmanExternal = provider.propGen.getExternalProperties()

        //From User's FileSystem or Resource
//        String userSetPropertiesDir = propmanExternal['properties.dir']
//        if (userSetPropertiesDir){
//            propertiesFile = FileMan.find(userSetPropertiesDir, propertiesFileName, ["yml", "yaml", "properties"])
//        }else{
            propertiesFile = FileMan.findResource(null, propertiesFileName, ["yml", "yaml", "properties"])
//        }
        propertiesFileExtension = FileMan.getExtension(propertiesFile)
        if (propertiesFile && propertiesFile.exists()){
            Map propertiesMap = generatePropertiesMap(propertiesFile)
            propmanForMacgyver.merge(propertiesMap)
                            .merge(propmanExternal)
                            .mergeNew(propmanDefault)
        }

        return propmanForMacgyver
    }



    @Command('doSomething')
    @HelpIgnore
    @Document("""
    No User's Command       
    """)
    Integer doSomething(){
        boolean modeHelp = propman.getBoolean('help')

        /** Help - Command **/
        // -Collect Command
        PropMan propmanExternal = config.propGen.getExternalProperties()
        List installerCommandCalledByUserList = propmanExternal.get('') ?: []

        // -Print Help Command
        if (modeHelp && installerCommandCalledByUserList){
            installerCommandCalledByUserList.each{ commandNameCalledByUser ->
                propman.set('help.task.name', '')
                propman.set('help.command.name', commandNameCalledByUser)
                runTask('help')
            }
            return TaskUtil.STATUS_TASK_DONE
        }

        /** Help - Task **/
        // -Collect Task
        //- Get Task Annotated Instance List from Singleton Pool
        List<Object> taskInstanceList = config.findAllInstances([Task])
        List<String> taskNameCalledByUserList = []
        taskInstanceList.each{ instance ->
            String taskName = instance.getClass().getSimpleName().toLowerCase()
            if (propman.get(taskName))
                taskNameCalledByUserList << taskName
        }
        
        // -Collect Task Alias
        if (!(taskNameCalledByUserList - ['help'])){
            Map<Class, ReflectInfomation> aliasTaskReflectionMap = config.reflectionMap.findAll{ clazz, info ->
                info.alias && propman.get(info.alias)
            }
            aliasTaskReflectionMap.each{ clazz, info ->
                taskNameCalledByUserList << info.instance.getClass().getSimpleName().toLowerCase()
            }
        }

        /** Run Task **/
        for (String taskNameCalledByUser : taskNameCalledByUserList){
            if (taskNameCalledByUserList.size() > 1 && taskNameCalledByUserList.contains('help')){
                if (modeHelp && taskNameCalledByUser != 'help'){
                    propman.set('help.command.name', '')
                    propman.set('help.task.name', taskNameCalledByUser)
                    runTask('help')
                }
            }else{
                propman.set('help.command.name', '')
                propman.set('help.task.name', '')
                runTask(taskNameCalledByUser)
                break
            }
        }

        return TaskUtil.STATUS_TASK_DONE
    }



    @Alias('m')
    @Command('macgyver')
    @Document("""
    You can use 'macgyver' to use a task on Terminal       
    """)
    void macgyver(){

        ReportSetup reportSetup = config.injectValue(new ReportSetup())

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
    @Document("""
    You can know How to use Command or Task on Terminal      
    """)
    void help(){
        runTask('help')
    }

    @Command('test')
    @Document("""
    - Test Command do 'clean' 'build' 'run'.

    - You can use 'test' command to test or build CI Environment.
    
    - Response File(.rsp) can help your test.
         
      installer-maker test -response.file.path=<File>  
    """)
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
