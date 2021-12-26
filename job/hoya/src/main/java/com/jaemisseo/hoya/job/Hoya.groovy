package com.jaemisseo.hoya.job

import com.jaemisseo.hoya.bean.GlobalOptionForHoya
import com.jaemisseo.hoya.bean.ReportSetup
import com.jaemisseo.hoya.task.config.TaskSetup
import jaemisseo.man.configuration.exception.WantToRestartException
import jaemisseo.man.configuration.context.CommanderConfig
import jaemisseo.man.configuration.annotation.Alias
import jaemisseo.man.configuration.annotation.HelpIgnore
import jaemisseo.man.configuration.annotation.method.Command
import jaemisseo.man.configuration.annotation.method.Init
import jaemisseo.man.configuration.annotation.type.Document
import jaemisseo.man.configuration.annotation.type.Job
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.data.PropertyProvider

import com.jaemisseo.hoya.task.TaskHelper
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

@Job
public class Hoya extends JobHelper {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    int jobCallingCount = 0

    Hoya(){
        propertiesFileName = 'hoya'
        jobName = 'hoya'
    }

    void logo(){
        logger.info Util.multiTrim("""
        88                                                     /^-^\\
        88                                                    / o o \\
        88                                                   /   Y   \\
        88,dPPYba,   ,adPPYba,  8b       d8 ,adPPYYba,       V \\ v / V
        88P'    "8a a8"     "8a `8b     d8' ""     `Y8         / - \\
        88       88 8b       d8  `8b   d8'  ,adPPPPP88        /    |
        88       88 "8a,   ,a8"   `8b,d8'   88,    ,88  (    /     |
        88       88  `"YbbdP"'      Y88'    `"8bbdP"Y8   ===/___) ||
                                    d8'                 
                                   d8'                  
        """)
    }

    @Init(lately=true)
    void init(){
        //Parse Global Property's variable
        this.propman = setupPropMan(provider)
        this.varman = setupVariableMan(propman)

        //Inject default value to GlobalOption
        provider.shift(jobName)
        this.gOpt = config.injectValue(new GlobalOptionForHoya())

        //Make Virtual Command
        this.virtualPropman = new PropMan()
        cacheAllCommitTaskListOnAllCommand()

        //First Commit
        commit()
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForHoya = provider.propGen.get('hoya')
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
            propmanForHoya.merge(propertiesMap)
                            .merge(propmanExternal)
                            .mergeOnlyNew(propmanDefault)
                            .mergeOnlyNew(propmanProgram)
        }

        return propmanForHoya
    }



    @Command
    void customCommand(){
        //Setup Log
        setupLog(gOpt.logSetup)

        if (!jobCallingCount++)
            logo()

        ReportSetup reportSetup = config.injectValue(new ReportSetup())

        //Each level by level
        validTaskList = CommanderConfig.findAllClasses('com.jaemisseo', [Task])
        eachTaskWithCommit(commandName){ TaskSetup commitTask ->
            try{
                return runTaskByCommitTask(commitTask)
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
        setupLog(gOpt.logSetup)

        validTaskList = CommanderConfig.findAllClasses('com.jaemisseo', [Task])
        boolean modeHelp = propman.getBoolean(['help', 'h'])
        String applicationName = propman.getString('application.name')
        // -Collect Command
        PropMan propmanExternal = config.propGen.getExternalProperties()
        List<String> commandCalledByUserList = propmanExternal.get('') ?: []
        List<String> taskCalledByUserList = config.taskCalledByUserList

        /** Run Help **/
        if (modeHelp){
            //Command
            if (helpCommand(commandCalledByUserList))
                return TaskHelper.STATUS_TASK_DONE

            //Task
            if (helpTask(taskCalledByUserList))
                return TaskHelper.STATUS_TASK_DONE

            //Main Help
            if (helpMain())
                return TaskHelper.STATUS_TASK_DONE
        }

        /** Run Task **/
        if (taskCalledByUserList){
            String taskName = taskCalledByUserList[0]
            propman.set('help.command.name', '')
            propman.set('help.task.name', '')
            runTaskByType(taskName)
        }
        return TaskHelper.STATUS_TASK_DONE
    }

    boolean helpCommand(List<String> commandCalledByUserList){
        // -Print Help Command
        if (commandCalledByUserList){
            commandCalledByUserList.each{ commandNameCalledByUser ->
                propman.set('help.task.name', '')
                propman.set('help.command.name', commandNameCalledByUser)
                runTaskByType('help')
            }
            return true
        }
        return false
    }

    boolean helpTask(List<String> taskNameCalledByUserList){
        if (taskNameCalledByUserList){
            for (String taskNameCalledByUser : taskNameCalledByUserList) {
                if (taskNameCalledByUser != 'help'){
                    propman.set('help.command.name', '')
                    propman.set('help.task.name', taskNameCalledByUser)
                    runTaskByType('help')
                }
            }
            return true
        }
        return false
    }

    boolean helpMain(){
        // -Print Help Command
        propman.set('help.task.name', '')
        propman.set('help.command.name', '')
        runTaskByType('help')
        return true
    }


    @Command('hoya')
    @HelpIgnore
    @Document("""
    You can use 'hoya' to use a task on Terminal       
    """)
    void hoya(){
        //Setup Log
        setupLog(gOpt.logSetup)

        if (!jobCallingCount++)
            logo()

        ReportSetup reportSetup = config.injectValue(new ReportSetup())

        //Each level by level
        CommanderConfig.findAllClasses('install', [Task])
        validTaskList = CommanderConfig.findAllClasses('com.jaemisseo', [Task])
        eachTaskWithCommit('hoya'){ TaskSetup commitTask ->
            try{
                return runTaskByCommitTask(commitTask)
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





    /**
     * WRITE Report
     */
    private void writeReport(List reportMapList, ReportSetup reportSetup){
        //Generate File Report
        if (reportMapList){
            String date = new SimpleDateFormat('yyyyMMdd_HHmmss').format(new Date())
            String fileNamePrefix = 'report_analysis'
            String filePath = reportSetup.fileSetup.path ?: "${fileNamePrefix}_${date}"

            if (reportSetup.modeReportText) {
//                List<String> stringList = sqlman.getAnalysisStringResultList(reportMapList)
//                FileMan.write("${reportSetup.path}.txt", stringList, opt)
            }

            if (reportSetup.modeReportExcel){
//                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", reportMapList, 'sqlFileName')
//                new ReportMan("${fileNamePrefix}_${date}.xlsx").write('sqlFileName', reportMapList)
                new ReportMan("${filePath}.xlsx").write(reportMapList, 'sqlFileName')
            }
        }

    }

}
