package install.employee

import install.bean.GlobalOptionForMacgyver
import install.bean.ReportSetup
import install.configuration.annotation.Alias
import install.configuration.annotation.HelpIgnore
import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Employee
import install.configuration.annotation.type.Task
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
        jobName = 'macgyver'
    }

    @Init(lately=true)
    void init(){
        this.propman = setupPropMan(provider)
        this.varman = setupVariableMan(propman)
        provider.shift(jobName)
        this.gOpt = config.injectValue(new GlobalOptionForMacgyver())
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



    @Command
    void customCommand(){
        //Setup Log
        setuptLog(gOpt.logSetup)

        ReportSetup reportSetup = config.injectValue(new ReportSetup())

        //Each level by level
        validTaskList = Util.findAllClasses('install', [Task])
        eachTaskWithCommit(commandName){ String propertyPrefix ->
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



    @Command('doSomething')
    @HelpIgnore
    @Document("""
    No User's Command       
    """)
    Integer doSomething(){
        //Setup Log
        setuptLog(gOpt.logSetup)

        validTaskList = Util.findAllClasses('install', [Task])
        boolean modeHelp = propman.getBoolean(['help', 'h'])
        String applicationName = propman.getString('application.name')
        List<String> taskCalledByUserList = config.taskCalledByUserList

        /** Run Help - Command **/
        if (helpCommand(modeHelp))
            return TaskUtil.STATUS_TASK_DONE

        if (taskCalledByUserList){
            String taskName = taskCalledByUserList[0]
            /** Run Help - Task **/
            if (helpTask(modeHelp, taskCalledByUserList))
                return TaskUtil.STATUS_TASK_DONE
            /** Run Task **/
//            if (applicationName == Commander.APPLICATION_INSTALLER && taskTypeName != 'version')
//                return TaskUtil.STATUS_TASK_RUN_FAILED
            propman.set('help.command.name', '')
            propman.set('help.task.name', '')
            runTaskByType(taskName)
        }

        return TaskUtil.STATUS_TASK_DONE
    }

    boolean helpCommand(boolean modeHelp){
        // -Collect Command
        PropMan propmanExternal = config.propGen.getExternalProperties()
        List installerCommandCalledByUserList = propmanExternal.get('') ?: []

        // -Print Help Command
        if (modeHelp && installerCommandCalledByUserList){
            installerCommandCalledByUserList.each{ commandNameCalledByUser ->
                propman.set('help.task.name', '')
                propman.set('help.command.name', commandNameCalledByUser)
                runTaskByType('help')
            }
            return true
        }
        return false
    }

    boolean helpTask(boolean modeHelp, List<String> taskNameCalledByUserList){
        if (taskNameCalledByUserList && taskNameCalledByUserList.size() > 1 && taskNameCalledByUserList.contains('help')){
            for (String taskNameCalledByUser : taskNameCalledByUserList) {
                if (modeHelp && taskNameCalledByUser != 'help'){
                    propman.set('help.command.name', '')
                    propman.set('help.task.name', taskNameCalledByUser)
                    runTaskByType('help')
                }
            }
            return true
        }else{
            return false
        }
    }



    @Alias('m')
    @Command('macgyver')
    @Document("""
    You can use 'macgyver' to use a task on Terminal       
    """)
    void macgyver(){
        //Setup Log
        setuptLog(gOpt.logSetup)

        ReportSetup reportSetup = config.injectValue(new ReportSetup())

        //Each level by level
        validTaskList = Util.findAllClasses('install', [Task])
        eachTaskWithCommit('macgyver'){ String propertyPrefix ->
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
        runTaskByType('help')
    }

    @Command('test')
    @Document("""
    - Test Command do 'clean' 'build' 'run'.

    - You can use 'test' command to test or build CI Environment.
    
    - Response File(.rsp) can help your test.
         
      installer-maker test -response.file.path=<File>  
    """)
    void test(){
        config.command( 'clean')
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
