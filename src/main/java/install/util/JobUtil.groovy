package install.util

import install.bean.LogSetup
import install.bean.ReportSetup
import install.bean.TaskSetup
import install.configuration.Config
import install.configuration.annotation.Inject
import install.configuration.annotation.method.After
import install.configuration.annotation.method.Before
import install.data.PropertyProvider
import install.task.*
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.VariableMan
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by sujkim on 2017-04-07.
 */
class JobUtil extends TaskUtil{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    String jobName = this.getClass().simpleName.toLowerCase()
    String levelNamesProperty = "${jobName}.level"
    String executorNamePrefix = jobName
    String propertiesFileName = jobName
    String propertiesFileExtension = ''
    File propertiesFile
    String propertyPrefix = ''

    List<Class> validTaskList = []
    List<Class> invalidTaskList = []
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

    VariableMan setupVariableMan(PropMan propman, String executorNamePrefix){
        VariableMan varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, executorNamePrefix)
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
    protected void eachLevelForTask(Closure closure){
        //1. Try to get levels from level property
        List<String> levelList = getSpecificLevelList(levelNamesProperty) ?: getLineOrderedLevelList(propertiesFileName, propertiesFileExtension, executorNamePrefix)
        List<String> prefixList = levelList.collect{ "${executorNamePrefix}.${it}." }
        List<Class> taskClassList = prefixList.collect{ getTaskClass(getTaskName(it)) }

        //2. Do Each Tasks
        commit()
        for (int i=0; i<levelList.size(); i++){
            String levelName = levelList[i]
            String propertyPrefix = "${executorNamePrefix}.${levelName}."

            //- Do Task
            taskResultStatus = closure(propertyPrefix)
            //- Check Status
            if (taskResultStatus == TaskUtil.STATUS_UNDO_QUESTION)
                i = undo(taskClassList, prefixList, i)
            else if (taskResultStatus == TaskUtil.STATUS_REDO_QUESTION)
                i = redo(taskClassList, prefixList, i)
            else
                commit()
        }
    }

    //level by level
    protected void eachLevel(Closure closure){
        //1. Try to get levels from level property
        List<String> levelList = getSpecificLevelList(levelNamesProperty) ?: getLineOrderedLevelList(propertiesFileName, propertiesFileExtension, executorNamePrefix)

        //2. Do Each Tasks
        levelList.eachWithIndex{ levelName, i ->
            String propertyPrefix = "${executorNamePrefix}.${levelName}."
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

    protected List<String> getLineOrderedLevelList(String fileName, String fileExtension, String executorName){
        Map levelNameMap = [:]
        String userSetPropertiesDir = propman.get('properties.dir')
        File scriptFile = (userSetPropertiesDir) ? new File("${userSetPropertiesDir}/${fileName}.${fileExtension}") : FileMan.getFileFromResource("${fileName}.${fileExtension}")

        //-YML or YAML
        if (fileExtension == 'yml' || fileExtension == 'yaml'){
            Map scriptMap = generatePropertiesMap(scriptFile)
            scriptMap.each{ String propertyName, String value ->
                List<String> propElementList = propertyName.split('[.]').toList()
                if (propElementList && propElementList.size() > 2){
                    String executorElementName = propElementList[0]
                    String levelElementName = propElementList[1]
                    if (executorElementName.equals(executorName)
                            && !propertyName.equals(levelNamesProperty)
                            && !levelNameMap[levelElementName]){
                        levelNameMap[levelElementName] = true
                    }
                }
            }
        //-PROPERTIES
        }else{
            scriptFile.text.eachLine{ String line ->
                String propertyName = line.split('[=]')[0]
                List<String> propElementList = propertyName.split('[.]').toList()
                if (propElementList && propElementList.size() > 2){
                    String executorElementName = propElementList[0]
                    String levelElementName = propElementList[1]
                    if (executorElementName.equals(executorName)
                            && !propertyName.equals(levelNamesProperty)
                            && !levelNameMap[levelElementName]){
                        levelNameMap[levelElementName] = true
                    }
                }
            }
        }
        return levelNameMap.keySet().toList()
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
                logger.error "It Can not undo"
            }
        }else{
            if (i == -1)
                logger.error "No more undo"
            else
                logger.error "It Can not undo"
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

    Integer runTask(String taskType, String propertyPrefix){
        provider.shift( jobName, propertyPrefix )
        List<String> propertyStructureList = propertyPrefix ? propertyPrefix.split('[.]').toList() : []
        TaskSetup task = config.injectValue(new TaskSetup(
                jobName: jobName,
                taskName: (propertyStructureList.size() >= 2) ? propertyStructureList[1] : '',
                taskTypeName: taskType,
                propertyPrefix: propertyPrefix
        ))
        task.taskClazz = getTaskClass(task.taskTypeName)

        //Validation
        //Check Valid Task
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
        Class taskClazz = validTaskList.find{ it.getSimpleName().equalsIgnoreCase(taskName) }
        if (!taskClazz)
            throw new Exception("${taskName} Does not exists task. or You Can't")
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
