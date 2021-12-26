package com.jaemisseo.install.job

import com.jaemisseo.hoya.bean.GlobalOptionForInstaller
import com.jaemisseo.hoya.bean.ReportSetup
import jaemisseo.man.configuration.exception.WantToRestartException
import com.jaemisseo.hoya.job.JobHelper
import com.jaemisseo.hoya.task.TaskHelper
import com.jaemisseo.hoya.task.config.TaskSetup
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.configuration.config.CommanderConfig
import jaemisseo.man.configuration.annotation.HelpIgnore
import jaemisseo.man.configuration.annotation.method.Command
import jaemisseo.man.configuration.annotation.method.Init
import jaemisseo.man.configuration.annotation.type.Document
import jaemisseo.man.configuration.annotation.type.Job
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.data.PropertyProvider
import jaemisseo.man.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

/**
 * Created by sujkim on 2017-02-17.
 */
@Job
public class Installer extends JobHelper{

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    int jobCallingCount = 0

    Installer(){
        propertiesFileName = 'installer'
        jobName = 'installer'
    }

    void logo(){
        logger.info Util.multiTrim("""
        88                                          88 88                        
        ''                         ,d               88 88                        
                                   88               88 88                        
        88 8b,dPPYba,  ,adPPYba, MM88MMM ,adPPYYba, 88 88  ,adPPYba, 8b,dPPYba,  
        88 88P'   ''8a I8[    ''   88    ''     'Y8 88 88 a8P_____88 88P'   'Y8  
        88 88       88  ''Y8ba,    88    ,adPPPPP88 88 88 8PP''''''' 88
        88 88       88 aa    ]8I   88,   88,    ,88 88 88 '8b,   ,aa 88
        88 88       88 ''YbbdP''   'Y888 ''8bbdP'Y8 88 88  ''Ybbd8'' 88
        """)
    }

    @Init(lately=true)
    void init(){
        //Parse Global Property's variable
        this.propman = setupPropMan(provider)
        this.varman = setupVariableMan(propman)

        //Inject default value to GlobalOption
        provider.shift(jobName)
        this.gOpt = config.injectValue(new GlobalOptionForInstaller())

        //Make Virtual Command
        this.virtualPropman = new PropMan()
        cacheAllCommitTaskListOnAllCommand()

        //First Commit
        commit()
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForInstaller = provider.propGen.get('installer')
        PropMan propmanDefault = provider.propGen.getDefaultProperties()
        PropMan propmanProgram = provider.propGen.getProgramProperties()
        PropMan propmanExternal = provider.propGen.getExternalProperties()

        if (propmanDefault.getBoolean('mode.build.form')){
            //Mode Build Form
            PropMan propmanForInstallerMaker = provider.propGen.get('installer-maker')

            //- Try to get from User's FileSystem
            String propertiesDir = propmanExternal.get('properties.dir') ?: propmanDefault.get('user.dir')
            if (propertiesDir)
                propertiesFile = FileMan.find(propertiesDir, propertiesFileName, ["yml", "yaml", "properties"])

            //- Make Property Manager
            if (propertiesFile && propertiesFile.exists()){
                propertiesFileExtension = FileMan.getExtension(propertiesFile)
                Map propertiesMap = generateMapFromPropertiesFile(propertiesFile)
                propmanForInstaller.merge(propertiesMap)
                                    .mergeOnlyNew(propmanForInstallerMaker)
                                    .mergeOnlyNew(propmanProgram)
            }

        }else{
            //Normally
            String libtohomeRelPath = FileMan.getFileFromResourceWithoutException('.libtohome')?.text.replaceAll('\\s*', '') ?: '../'
            String installerHome = FileMan.getFullPath(propmanDefault.get('lib.dir'), libtohomeRelPath)

            propertiesFile = FileMan.findResource(null, propertiesFileName, ["yml", "yaml", "properties"])
            propertiesFileExtension = FileMan.getExtension(propertiesFile)
            if (propertiesFile && propertiesFile.exists()){
                Map propertiesMap = generateMapFromPropertiesFile(propertiesFile)
                propmanForInstaller.merge(propertiesMap)
                                    .merge(propmanExternal)
                                    .mergeOnlyNew(propmanDefault)
                                    .mergeOnlyNew(propmanProgram)
                                    .merge(['installer.home': installerHome])
            }else{
            }
        }

        return propmanForInstaller
    }



    @Command
    void customCommand(){
        if (!jobCallingCount++)
            logo()

        //Setup Log
        setupLog(gOpt.logSetup)

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



    @Command('ask')
    @HelpIgnore
    @Document('''
    No User's Command 
    ''')
    void ask(){
        if (!jobCallingCount++)
            logo()

        //Setup Log
        setupLog(gOpt.logSetup)

        logTaskDescription('ask')

        if (!propertiesFile)
            throw new Exception('Does not exists script file [ installer.yml ]')

        //- Ready event
        InstallerBeforeAsk beforeAsk = new InstallerBeforeAsk(propman, config, provider, environment, selfAware)
        InstallerAfterAsk afterAsk = new InstallerAfterAsk(propman, config, provider, environment, selfAware)

        //0. Check Response File

        if (beforeAsk.checkResponseFile(gOpt.responseFilePath)){
            PropMan propmanResponse = new PropMan(gOpt.responseFilePath as String).merge(provider.propGen.getExternalProperties())
            propmanResponse = generatePropMan(propmanResponse, ['ask'])

            propman.merge(propmanResponse)
            propman.set('mode.load.rsp', true)
            propman.set('answer.repeat.limit', 0)
            logTaskDescription('added response file answer')
        }

        //1. READ REMEMBERED ANSWER
        beforeAsk.readRememberAnswer(gOpt)

        //2. Each level by level
        validTaskList = CommanderConfig.findAllClasses('com.jaemisseo', [Task])
        eachTaskWithCommit('ask'){ TaskSetup commitTask ->
            return runTaskByCommitTask(commitTask)
        }

        //3. WRITE REMEMBERED ANSWER
        afterAsk.writeRememberAnswer(gOpt)
    }

    @Command('install')
    @HelpIgnore
    @Document('''
    No User's Command 
    ''')
    void install(){
        if (!jobCallingCount++)
            logo()

        //Setup Log
        setupLog(gOpt.logSetup)

        logTaskDescription('install')

        if (!propertiesFile)
            throw new Exception('Does not exists script file [ installer.yml ]')

        ReportSetup reportSetup = gOpt.reportSetup

        //Each level by level
        validTaskList = CommanderConfig.findAllClasses('com.jaemisseo', [Task])
        eachTaskWithCommit('install'){ TaskSetup commitTask ->
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

    @Command('form')
    @HelpIgnore
    @Document('''
    No User's Command
    Generate Response File
        1. Load installer.properties
        2. Analysis 'ask' commands tasks list
        3. Gen install.rsp 
    ''')
    void buildForm(){
        if (!propertiesFile)
            throw new Exception('Does not exists script file [ installer.yml ]')

        List<String> allLineList = []

        logTaskDescription('auto create response file')

        //Each level by level
        validTaskList = undoableTaskList
        eachTask('ask'){ String propertyPrefix ->
            String taskName = getTaskName(propertyPrefix)
            Class taskClazz = getTaskClass(taskName)

            //Check Valid Task
            if (!taskName || !taskClazz)
                throw new Exception(" 'No Task Name. ${propertyPrefix}task=???. Please Check Task.' ")
            if ( (validTaskList && !validTaskList.contains(taskClazz)) || (invalidTaskList && invalidTaskList.contains(taskClazz)) )
                throw new Exception(" 'Sorry, This is not my task, [${taskName}]. I Can Not do this.' ")

            //(Task)
            TaskHelper taskInstance = config.findInstance(taskClazz)

            //(Task) Inject Value
            provider.shift( jobName, propertyPrefix )
            config.injectValue(taskInstance)
//            taskInstance.provider = provider
//            taskInstance.propertyPrefix = propertyPrefix
            taskInstance.rememberAnswerLineList = rememberAnswerLineList
            taskInstance.reportMapList = reportMapList

            //(Task) Build Form
            List<String> lineList = taskInstance.buildForm(propertyPrefix)

            //Add One Question into Form
            if (lineList){
                List editingList = lineList.collect{ "# $it" }
                editingList.add(0, "#########################")
                editingList << "#########################"
                editingList << "${propertyPrefix}answer="
                editingList << ""
                allLineList.addAll(editingList)
            }
        }

        //Make a Response File
        if (allLineList){
            String buildInstallerHome = provider.getString('build.installer.home')
            String homeToRspRelPath = provider.getString('installer.home.to.rsp.relpath')
            String rspDestPath = FileMan.getFullPath(buildInstallerHome, homeToRspRelPath)
            String rspInstallRspDestPath = "${rspDestPath}/install.rsp"
            FileMan.write(rspInstallRspDestPath, allLineList, true)
        }
    }






    /*************************
     * WRITE Report
     *************************/
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

            if (reportSetup.modeReportExcel) {
//                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", reportMapList, 'sqlFileName')
//                new ReportMan("${fileNamePrefix}_${date}.xlsx").write('sqlFileName', reportMapList)
                new ReportMan("${filePath}.xlsx").write(reportMapList, 'sqlFileName')
            }
        }
    }



}
