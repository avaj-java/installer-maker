package install.util

import install.bean.LogSetup
import install.bean.ReportSetup
import install.bean.TaskSetup
import install.configuration.Config
import install.configuration.annotation.Inject
import install.configuration.annotation.method.After
import install.configuration.annotation.method.Before
import install.configuration.annotation.type.Task
import install.data.PropertyProvider
import install.task.*
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.VariableMan
import jaemisseo.man.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by sujkim on 2017-04-07.
 */
class JobUtil extends TaskUtil{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    String jobName = this.getClass().simpleName.toLowerCase()
    String commandName = null
    String propertiesFileName = jobName
    String propertiesFileExtension = ''
    File propertiesFile
    String propertyPrefix = ''

    List<Class> validCommandList = []
    List<Class> validTaskList = []
    List<Class> invalidTaskList = []
    List<Class> allTaskList = Util.findAllClasses('install', [Task])
    List<Class> undoableList = [Question, QuestionChoice, QuestionYN, QuestionFindFile, Set, Notice]
    List<Class> undoMoreList = [Set, Notice]

    def gOpt
    Integer taskResultStatus



    /*************************
     * - Config: System Controller
     * - PropertyProvider: Data Controller
     *************************/
    Config config
    PropertyProvider provider

    @Inject void setConfig(Config config){ this.config = config }
    @Inject void setProvider(PropertyProvider provider){ this.provider = provider }



    /*************************
     * A thing to do, Before running command.
     *************************/
    @Before
    void before(){
        provider.shift( jobName, propertyPrefix )
    }



    /*************************
     * A thing to do After running command.
     *************************/
    @After
    void after(){
        this.propertyPrefix = ''
    }



    PropMan setupPropMan(PropertyProvider provider){
        String poolName = this.getClass().simpleName.toLowerCase()
        PropMan propman = provider.propGen.get(poolName)
        return propman
    }

    VariableMan setupVariableMan(PropMan propman){
        VariableMan varman = new VariableMan(propman.properties)

        //Analisys Exclusion List (task property)
        List<String> excludeStartsWithList = []
        propman.properties.keySet().each{ String propertyName ->
            if (propertyName.endsWith('.task')){
                List<String> propElementList = propertyName.split('[.]').toList()
                if (propElementList.size() == 3  && getTaskClass(propman[propertyName])){
                    excludeStartsWithList << (propElementList[0..1].join('.') + '.')
                }
            }
        }

        logger.trace( 'PROPERTIES SIZE: ' +propman.properties.size() )
        logger.trace( 'FIRST PARSING EXCLUSION LIST: ' +excludeStartsWithList )

        parsePropMan(propman, varman, excludeStartsWithList)
        setBeforeGetProp(propman, varman)
        return varman
    }

    PropMan setBeforeGetProp(PropMan propmanToSet, VariableMan varman){
        propmanToSet.setBeforeGetProp({ String propertyName, def value ->
            if (value && value instanceof String) {
                String parsedValue = varman.putVariables(propmanToSet.properties).parse(value)
                propmanToSet.set(propertyName, parsedValue)
            }
        })
        return propmanToSet
    }

    Map generatePropertiesMap(File propertiesFile){
        Map prop
        if (propertiesFile.name.endsWith('.yml') || propertiesFile.name.endsWith('.yaml'))
            prop = YamlUtil.generatePropertiesMap(propertiesFile)
        else
            prop = new PropMan(propertiesFile).properties
        return prop
    }



    //level by level For Task
    protected void eachTaskWithCommit(String commandName, Closure closure){
        //1. Try to get task order from property
        String taskOrderProperty = "${commandName}.order".toString()
        List<String> taskOrderList = getSpecificLevelList(taskOrderProperty) ?: getTaskLineOrderList(propertiesFileName, propertiesFileExtension, commandName, taskOrderProperty)
        List<String> prefixList = taskOrderList.collect{ "${commandName}.${it}.".toString() }
        List<Class> taskTypeList = prefixList.collect{ getTaskClass(getTaskName(it)) }

        //2. Do Each Tasks
        commit()
        for (int i=0; i<taskOrderList.size(); i++){
            String taskName = taskOrderList[i]
            String propertyPrefix = "${commandName}.${taskName}."

            //- Do Task
            taskResultStatus = closure(propertyPrefix)
            
            //- Check Status
            if (taskResultStatus == TaskUtil.STATUS_UNDO_QUESTION)
                i = undo(taskTypeList, prefixList, i)
            else if (taskResultStatus == TaskUtil.STATUS_REDO_QUESTION)
                i = redo(taskTypeList, prefixList, i)
            else
                commit()
        }
    }

    //level by level
    protected void eachTask(String commandName, Closure closure){
        //1. Try to get levels from level property
        String taskOrderProperty = "${commandName}.order".toString()
        List<String> taskOrderList = getSpecificLevelList(taskOrderProperty) ?: getTaskLineOrderList(propertiesFileName, propertiesFileExtension, commandName, taskOrderProperty)

        //2. Do Each Tasks
        taskOrderList.eachWithIndex{ taskName, i ->
            String propertyPrefix = "${commandName}.${taskName}.".toString()
            closure(propertyPrefix)
        }
    }

    protected List<String> getSpecificLevelList(String levelNamesProperty){
        List<String> resultList = []
        String levelNames = propman.get(levelNamesProperty)
        List<String> list = levelNames?.split("\\s*,\\s*")
        //Each Specific Levels
        list.each{ String levelName ->
            if (levelName.contains('-')) {
                resultList += getListWithDashRange(levelName as String)
            }else if (levelName.contains('..')){
                resultList += getListWithDotDotRange(levelName as String)
            }else{
                resultList << levelName
            }
        }
        return resultList
    }

    protected List<String> getTaskLineOrderList(String fileName, String fileExtension, String executorName, String taskOrderProperty){
        Map taskNameMap = [:]
        String userSetPropertiesDir = propman.get('properties.dir')
        File scriptFile = (userSetPropertiesDir) ? new File("${userSetPropertiesDir}/${fileName}.${fileExtension}") : FileMan.getFileFromResource("${fileName}.${fileExtension}")

        //-YML or YAML
        if (fileExtension == 'yml' || fileExtension == 'yaml'){
            Map scriptMap = generatePropertiesMap(scriptFile)
            scriptMap.each{ String propertyName, String value ->
                List<String> propElementList = propertyName.split('[.]').toList()
                if (propElementList && propElementList.size() > 2){
                    String commandName = propElementList[0]
                    String taskName = propElementList[1]
                    if (commandName.equals(executorName)
                            && !propertyName.equals(taskOrderProperty)
                            && !taskNameMap[taskName]){
                        taskNameMap[taskName] = true
                    }
                }
            }
        //-PROPERTIES
        }else{
            scriptFile.text.eachLine{ String line ->
                String propertyName = line.split('[=]')[0]
                List<String> propElementList = propertyName.split('[.]').toList()
                if (propElementList && propElementList.size() > 2){
                    String commandName = propElementList[0]
                    String taskName = propElementList[1]
                    if (commandName.equals(executorName)
                            && !propertyName.equals(taskOrderProperty)
                            && !taskNameMap[taskName]){
                        taskNameMap[taskName] = true
                    }
                }
            }
        }
        return taskNameMap.keySet().toList()
    }



    /*****
     * UNDO
     *****/
    int undo(List<Class> taskClassList, List<String> prefixList, int i){
        i -= 1
        if (undoableList.contains(taskClassList[i])){
            propman.undo()
            while (i > 0 && (undoMoreList.contains(taskClassList[i]) || !checkCondition(prefixList[i])) ){
                i -= 1
                propman.undo()
            }
            i -= 1
            if (i <= -1){
                i = -1
                propman.checkout(0)
                //First is undoMore, then auto-redo
                if (undoMoreList.contains(taskClassList[0])){
                    i += 1
                    propman.redo()
                    while ( propman.isNotHeadLast() && undoableList.contains(taskClassList[i+1]) && (undoMoreList.contains(taskClassList[i+1]) || !checkCondition(prefixList[i+1])) ){
                        i += 1
                        propman.redo()
                    }
                }
                logger.error "It can not undo"
            }
        }else{
            if (i == -1)
                logger.error "No more undo"
            else
                logger.error "It can not undo"
        }
        return i
    }

    /*****
     * REDO
     *****/
    int redo(List<Class> taskClassList, List<String> prefixList, int i){
        if (propman.isNotHeadLast()){
            propman.redo()
            while ( propman.isNotHeadLast() && undoableList.contains(taskClassList[i+1]) && (undoMoreList.contains(taskClassList[i+1]) || !checkCondition(prefixList[i+1])) ){
                i += 1
                propman.redo()
            }
        }else{
            i -= 1
            propman.rollback()
            logger.error "It Can not redo more"
        }
        return i
    }

    /*****
     * COMMIT
     *****/
    void commit(){
        propman.commit()
    }



    /*************************
     * 1. RUN TASK
     *************************/
    Integer runTaskByPrefix(String propertyPrefix) {
        return runTask('', propertyPrefix)
    }

    Integer runTaskByType(String taskType){
        return runTask(taskType, '')
    }

    Integer runTaskByType(Class clazz){
        return runTask(clazz.getSimpleName(), '')
    }

    Integer runTask(String taskTypeName, String propertyPrefix){
        provider.shift( jobName, propertyPrefix )
        List<String> propertyStructureList = propertyPrefix ? propertyPrefix.split('[.]').toList() : []
        TaskSetup task = config.injectValue(new TaskSetup(
                jobName: jobName,
                commandName: (propertyStructureList.size() >= 2) ? propertyStructureList[0] : '',
                taskName: (propertyStructureList.size() >= 2) ? propertyStructureList[1] : '',
                taskTypeName: taskTypeName,
                propertyPrefix: propertyPrefix
        ))
        task.taskClazz = getTaskClass(task.taskTypeName)

        //Validation
        //Check Valid Task
        if (!task.taskClazz)
            throw new Exception("${task.taskTypeName} Does not exists task. or You Can't")
        if (!task.taskTypeName)
            throw new Exception(" 'No Task Name. ${task.propertyPrefix}task=???. Please Check Task.' ")
        if ( (validTaskList && !validTaskList.contains(task.taskClazz)) || (invalidTaskList && invalidTaskList.contains(task.taskClazz)) )
            throw new Exception(" 'Sorry, This is Not my task, [${task.taskTypeName}]. I Can Not do this.' ")

        //(Task) Start
        return start(task)
    }



    String getTaskName(String propertyPrefix){
        String taskName = provider.getString("${propertyPrefix}task")?.trim()?.toUpperCase()
        return taskName
    }

    Class getTaskClass(String taskName){
        Class taskClazz = allTaskList.find{ it.getSimpleName().equalsIgnoreCase(taskName) }
        return taskClazz
    }

    /*************************
     * 2. START
     *************************/
    Integer start(TaskSetup task){
        status = STATUS_NOTHING

        //Check Condition
        if ( !checkCondition(task.propertyPrefix) )
            return

        //Get Task Instance
        // - Find Task
        TaskUtil taskInstance = config.findInstance(task.taskClazz)
        // - Inject Value
        provider.shift( task.jobName, task.propertyPrefix )
        config.cleanValue(taskInstance)
        config.injectValue(taskInstance)
        taskInstance.rememberAnswerLineList = rememberAnswerLineList
        taskInstance.reportMapList = reportMapList

        try{
            if (task.color)
                config.logGen.setupConsoleLoggerColorPattern(task.color)

            //Description
            if ( !task.jobName.equalsIgnoreCase('macgyver') && !task.jobName.equalsIgnoreCase('receptionist') )
                descript(task)

            //Start Task
            status = taskInstance.run()

        }catch(e){
            throw e
        }finally{
            if (task.color)
                config.logGen.setupBeforeConsoleLoggerPattern()

            if (status != STATUS_UNDO_QUESTION)
                report(taskInstance)
        }

        return status
    }

    protected boolean checkCondition(String propertyPrefix){
        return provider.checkCondition(propertyPrefix)
    }

    protected void descript(TaskSetup task){
        String description = task.desc ? "$task.jobName:$task.desc" : "$task.jobName:$task.taskName:$task.taskTypeName"
        if (description){
            if (task.descColor)
                config.logGen.setupConsoleLoggerColorPattern(task.descColor)
            logTaskDescription(description)
            if (task.descColor)
                config.logGen.setupBeforeConsoleLoggerPattern()
        }
    }

    /*************************
     * 3. REPORT
     *************************/
    void report(TaskUtil taskInstance){
        ReportSetup reportSetup = config.injectValue(new ReportSetup())

        if (reportSetup.modeReport){
            if (reportSetup.modeReportConsole)
                taskInstance.reportWithConsole(reportSetup, reportMapList)

            if (reportSetup.modeReportText)
                taskInstance.reportWithText(reportSetup, reportMapList)

            if (reportSetup.modeReportExcel)
                taskInstance.reportWithExcel(reportSetup, reportMapList)
        }
    }



    /*************************
     * Setup Log
     *************************/
    protected void setuptLog(LogSetup logOpt){
        config.logGen.setupConsoleLogger(logOpt.logLevelConsole)
        config.logGen.setupFileLogger(jobName, logOpt.logLevelFile, logOpt.logDir, logOpt.logFileName)
    }





    protected List<String> getListWithDashRange(String dashRnage){
        List<String> resultList = []
        List<String> split = dashRnage.split('[-]')
        String rangeStart = split[0]
        String rangeEnd = split[1]
        if (rangeStart.isNumber() && rangeEnd.isNumber())
            (Integer.parseInt(rangeStart)..Integer.parseInt(rangeEnd)).each{
                resultList << it.toString()
            }
        else{
            (rangeStart..rangeEnd).each{
                resultList << it.toString()
            }
        }
        return resultList
    }

    protected List<String> getListWithDotDotRange(String dashRnage){
        List<String> resultList = []
        List<String> split = dashRnage.split('[.][.]')
        String rangeStart = split[0]
        String rangeEnd = split[1]
        if (rangeStart.isNumber() && rangeEnd.isNumber())
            (Integer.parseInt(rangeStart)..Integer.parseInt(rangeEnd)).each{
                resultList << it.toString()
            }
        else{
            (rangeStart..rangeEnd).each{
                resultList << it.toString()
            }
        }
        return resultList
    }

}
