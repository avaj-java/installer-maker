package install.employee

import install.bean.GlobalOptionForMacgyver
import install.bean.ReportSetup
import install.bean.TaskSetup
import install.exception.WantToRestartException
import jaemisseo.man.configuration.annotation.Alias
import jaemisseo.man.configuration.annotation.HelpIgnore
import jaemisseo.man.configuration.annotation.method.Command
import jaemisseo.man.configuration.annotation.method.Init
import jaemisseo.man.configuration.annotation.type.Document
import jaemisseo.man.configuration.annotation.type.Employee
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.data.PropertyProvider
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
        commit()
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForMacgyver = provider.propGen.get('macgyver')
        PropMan propmanDefault = provider.propGen.getDefaultProperties()
        PropMan propmanProgram = provider.propGen.getProgramProperties()
        PropMan propmanExternal = provider.propGen.getExternalProperties()

        //- Try to get from User's FileSystem
        String propertiesDir = propmanExternal['properties.dir'] ?: propmanDefault.get('user.dir')
        if (propertiesDir)
            propertiesFile = FileMan.find(propertiesDir, propertiesFileName, ["yml", "yaml", "properties"])

        //- Try to get from resource
        if (!propertiesFile)
            propertiesFile = FileMan.findResource(null, propertiesFileName, ["yml", "yaml", "properties"])

        //- Make Property Manager
        if (propertiesFile && propertiesFile.exists()){
            propertiesFileExtension = FileMan.getExtension(propertiesFile)
            Map propertiesMap = generateMapFromPropertiesFile(propertiesFile)
            propmanForMacgyver.merge(propertiesMap)
                            .merge(propmanExternal)
                            .mergeNew(propmanDefault)
                            .mergeNew(propmanProgram)
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
        eachTaskWithCommit(commandName){ TaskSetup task ->
            try{
                return runTask(task)
            }catch(WantToRestartException wtre){
                throw wtre
            }catch(Exception e){
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
        eachTaskWithCommit('macgyver'){ TaskSetup task ->
            try{
                return runTask(task)
            }catch(WantToRestartException wtre){
                throw wtre
            }catch(Exception e){
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
         
      installer-maker test -rsp=<File>  
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
