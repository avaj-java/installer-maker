package install.job

import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Job
import install.data.PropertyProvider
import install.task.*
import install.util.JobUtil
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.util.FileSetup

/**
 * Created by sujkim on 2017-02-17.
 */
@Job
class Receptionist extends JobUtil{

    @Init(lately=true)
    void init(){
        levelNamesProperty = 'r.level'
        executorNamePrefix = 'r'
        propertiesFileName = 'receptionist.properties'
        validTaskList = [Notice, Question, QuestionChoice, QuestionYN, QuestionFindFile, Set]

        this.propman = setupPropMan(provider)
        this.varman = setupVariableMan(propman, executorNamePrefix)
        provider.shift(jobName)
        this.gOpt = provider.getReceptionistGlobalOption()
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForReceptionist = provider.propGen.get('receptionist')
        PropMan propmanDefault = provider.propGen.getDefaultProperties()
        PropMan propmanExternal = provider.propGen.getExternalProperties()

        if (propmanDefault.getBoolean('mode.build.form')){
            //Mode Build Form
            PropMan propmanForBuilder = provider.propGen.get('builder')
            String propertiesDir = propmanExternal.get('properties.dir') ?: propmanDefault.get('user.dir')
            propmanForReceptionist.merge("${propertiesDir}/receptionist.properties").mergeNew(propmanForBuilder)

        }else{
            //Normally
            String libtohomeRelPath = FileMan.getFileFromResource('.libtohome')?.text.replaceAll('\\s*', '') ?: '../'
            String installerHome = FileMan.getFullPath(propmanDefault.get('lib.dir'), libtohomeRelPath)
            //From User's FileSystem or Resource
            String userSetPropertiesDir = propmanExternal['properties.dir']
            if (userSetPropertiesDir)
                propmanForReceptionist.merge("${userSetPropertiesDir}/receptionist.properties")
            else
                propmanForReceptionist.mergeResource("receptionist.properties")
            propmanForReceptionist.merge(propmanExternal).mergeNew(propmanDefault).merge(['installer.home': installerHome])
        }
        
        return propmanForReceptionist
    }


    @Command('ask')
    void ask(){
        //0. Check Response File
        if (checkResponseFile()){
            propman.merge(getResponsePropMan())
            propman.set('answer.repeat.limit', 0)
            logBigTitle('Add Response File Answer')
        }
        //1. READ REMEMBERED ANSWER
        readRememberAnswer()
        //2. Each level by level
        eachLevelForTask{ String propertyPrefix ->
            return runTaskByPrefix("${propertyPrefix}")
        }
        //3. WRITE REMEMBERED ANSWER
        writeRememberAnswer()
    }

    boolean checkResponseFile(){
        String responseFilePath = gOpt.responseFilePath
        if (responseFilePath){
            if (new File(responseFilePath).exists()){
                return true
            }else{
                println " < Failed > Load Response File, Does not exists file - ${responseFilePath}"
                System.exit(0)
            }
        }
        return false
    }

    PropMan getResponsePropMan(){
        String responseFilePath = gOpt.responseFilePath
        return new PropMan(responseFilePath)
    }



    /*************************
     * Read Remeber File
     *************************/
    private void readRememberAnswer(){
        Boolean modeRemember = gOpt.modeRemember
        String rememberFilePath = gOpt.rememberFilePath

        if (modeRemember){
            logBigTitle('LOAD Remembered Your Answer ')
            try{
                PropMan rememberAnswerPropman = new PropMan().readFile(rememberFilePath).properties
                propman.merge(rememberAnswerPropman)
            }catch(Exception e){
                println "No Remember File"
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
            logBigTitle('SAVE Your Answer')
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


    /*************************
     * BUILD FORM
     *************************/
    /**
     * Generate Response File
     * 1. Load receptionist.properties
     * 2. Gen install.rsp
     */
    @Command('form')
    void buildForm(){
        List<String> allLineList = []

        logBigTitle('AUTO CREATE RESPONSE FILE')

        //Each level by level
        eachLevel{ String propertyPrefix ->
            String taskName = getTaskName(propertyPrefix)
            Class taskClazz  = getTaskClass(taskName)

            //Check Valid Task
            if (!taskName || !taskClazz)
                throw new Exception(" 'No Task Name. ${propertyPrefix}task=???. Please Check Task.' ")
            if ( (validTaskList && !validTaskList.contains(taskClazz)) || (invalidTaskList && invalidTaskList.contains(taskClazz)) )
                throw new Exception(" 'Sorry, This is Not my task, [${taskName}]. I Can Not do this.' ")

            //(Task)
            TaskUtil taskInstance = config.findInstance(taskClazz)

            //(Task) Inject Value
            provider.shift( jobName, propertyPrefix )
            config.injectValue(taskInstance)
//            taskInstance.provider = provider
            taskInstance.propertyPrefix = propertyPrefix
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

}
