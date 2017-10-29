package install.job

import install.bean.GlobalOptionForReceptionist
import install.configuration.annotation.HelpIgnore
import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Job
import install.data.PropertyProvider
import install.task.*
import install.util.JobUtil
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import install.bean.FileSetup
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by sujkim on 2017-02-17.
 */
@Job
class Receptionist extends JobUtil{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    Receptionist() {
        propertiesFileName = 'receptionist'
        jobName = 'receptionist'
    }

    @Init(lately=true)
    void init(){
        validTaskList = [Notice, Question, QuestionChoice, QuestionYN, QuestionFindFile, Set]
        validCommandList = ['ask']
        this.propman = setupPropMan(provider)
        this.varman = setupVariableMan(propman, validCommandList)
        provider.shift(jobName)
        this.gOpt = config.injectValue(new GlobalOptionForReceptionist())
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForReceptionist = provider.propGen.get('receptionist')
        PropMan propmanDefault = provider.propGen.getDefaultProperties()
        PropMan propmanExternal = provider.propGen.getExternalProperties()

        if (propmanDefault.getBoolean('mode.build.form')){
            //Mode Build Form
            PropMan propmanForBuilder = provider.propGen.get('builder')

            //From User's FileSystem
            String propertiesDir = propmanExternal.get('properties.dir') ?: propmanDefault.get('user.dir')
            if (propertiesDir){
                propertiesFile = FileMan.find(propertiesDir, propertiesFileName, ["yml", "yaml", "properties"])
            }
            propertiesFileExtension = FileMan.getExtension(propertiesFile)
            if (propertiesFile && propertiesFile.exists()){
                Map propertiesMap = generatePropertiesMap(propertiesFile)
                propmanForReceptionist.merge(propertiesMap)
                                    .mergeNew(propmanForBuilder)
            }else{
            }

        }else{
            //Normally
            String libtohomeRelPath = FileMan.getFileFromResource('.libtohome')?.text.replaceAll('\\s*', '') ?: '../'
            String installerHome = FileMan.getFullPath(propmanDefault.get('lib.dir'), libtohomeRelPath)

            //From User's FileSystem or Resource
//            String userSetPropertiesDir = propmanExternal['properties.dir']
//            if (userSetPropertiesDir){
//                propertiesFile = FileMan.find(userSetPropertiesDir, propertiesFileName, ["yml", "yaml", "properties"])
//            }else{
                propertiesFile = FileMan.findResource(null, propertiesFileName, ["yml", "yaml", "properties"])
//            }
            propertiesFileExtension = FileMan.getExtension(propertiesFile)
            if (propertiesFile && propertiesFile.exists()){
                Map propertiesMap = generatePropertiesMap(propertiesFile)
                propmanForReceptionist.merge(propertiesMap)
                                    .merge(propmanExternal)
                                    .mergeNew(propmanDefault)
                                    .merge(['installer.home': installerHome])
            }else{
            }
        }

        return propmanForReceptionist
    }

    @Command('ask')
    @HelpIgnore
    @Document('''
    No User's Command 
    ''')
    void ask(){
        //Setup Log
        setuptLog(gOpt.logSetup)

        logBigTitle "Receptionist"

        logTaskDescription('ask')

        if (!propertiesFile)
            throw Exception('Does not exists script file [ receptionist.yml ]')

        //0. Check Response File
        if (checkResponseFile(gOpt.responseFilePath)){
            PropMan responsePropMan = generatePropMan(gOpt.responseFilePath, 'ask')
            propman.merge(responsePropMan)
            propman.set('answer.repeat.limit', 0)
            logTaskDescription('added response file answer')
        }
        //1. READ REMEMBERED ANSWER
        readRememberAnswer()
        //2. Each level by level
        eachLevelForTask('ask'){ String propertyPrefix ->
            return runTaskByPrefix("${propertyPrefix}")
        }
        //3. WRITE REMEMBERED ANSWER
        writeRememberAnswer()
    }

    @Command('form')
    @HelpIgnore
    @Document('''
    No User's Command
    Generate Response File
        1. Load receptionist.properties
        2. Gen install.rsp 
    ''')
    void buildForm(){
        if (!propertiesFile)
            throw Exception('Does not exists script file [ receptionist.yml ]')

        List<String> allLineList = []

        logTaskDescription('auto create response file')

        //Each level by level
        eachLevel('ask'){ String propertyPrefix ->
            String taskName = getTaskName(propertyPrefix)
            Class taskClazz = getTaskClass(taskName)

            //Check Valid Task
            if (!taskName || !taskClazz)
                throw new Exception(" 'No Task Name. ${propertyPrefix}task=???. Please Check Task.' ")
            if ( (validTaskList && !validTaskList.contains(taskClazz)) || (invalidTaskList && invalidTaskList.contains(taskClazz)) )
                throw new Exception(" 'Sorry, This is not my task, [${taskName}]. I Can Not do this.' ")

            //(Task)
            TaskUtil taskInstance = config.findInstance(taskClazz)

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

    boolean checkResponseFile(String responseFilePath){
        //Try To Load Response File
        if (responseFilePath){
            if (new File(responseFilePath).exists()){
                return true
            }else{
                logger.error " < Failed > Load Response File, Does not exists file - ${responseFilePath}"
                System.exit(0)
            }
        }
        return false
    }




    /*************************
     * Read Remeber File
     *************************/
    private void readRememberAnswer(){
        Boolean modeRemember = gOpt.modeRemember
        String rememberFilePath = gOpt.rememberFilePath

        if (modeRemember){
            logTaskDescription('load remembered your answer')
            try{
                PropMan rememberAnswerPropman = new PropMan().readFile(rememberFilePath).properties
                propman.merge(rememberAnswerPropman)
            }catch(Exception e){
                logger.error "No Remember File!!!"
            }
        }
    }



    /*************************
     * Backup & Write Remeber File
     *************************/
    private void writeRememberAnswer(){
        Boolean modeRemember = gOpt.modeRemember
        String rememberFilePath = gOpt.rememberFilePath
        FileSetup fileSetup = gOpt.rememberFileSetup

        if (modeRemember){
            logTaskDescription('save your answer')
            FileMan fileman = new FileMan(rememberFilePath).set(fileSetup)
            try{
                if (fileman.exists())
                    fileman.backup()
            }catch(Exception e){
                e.printStackTrace()
            }
            try{
                fileman.read(rememberAnswerLineList).write()
            }catch(Exception e){
                e.printStackTrace()
            }
        }
    }

}
