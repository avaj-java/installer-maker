package install.util

import install.bean.LogSetup
import install.bean.ReportSetup
import install.bean.TaskSetup
import jaemisseo.man.configuration.Config
import jaemisseo.man.configuration.annotation.Inject
import jaemisseo.man.configuration.annotation.method.After
import jaemisseo.man.configuration.annotation.method.Before
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.data.PropertyProvider
import install.task.*
import jaemisseo.man.PropMan
import jaemisseo.man.VariableMan
import jaemisseo.man.util.CommitObject
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

    List<Class> validTaskList = []
    List<Class> invalidTaskList = []
    List<Class> allTaskList = Util.findAllClasses('install', [Task])
    List<Class> undoableTaskList = Util.findAllClasses('install', [Undoable])
    List<Class> undoMoreTaskList = [Set, Notice, Command]
//    List<Class> undoableTaskList = [Question, QuestionChoice, QuestionYN, QuestionFindFile, Set, Notice]
//    List<Class> undoMoreTaskList = [Set, Notice]

    def gOpt
    Integer taskResultStatus
    Map<String, List<TaskSetup>> commandNameTaskListMap = [:]



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




    //level by level For Task
    protected void eachTaskWithCommit(String commandName, Closure closure){
        eachTaskWithCommit(commandName, 0, closure)
    }

    protected void eachTaskWithCommit(TaskSetup task, Closure closure){
        eachTaskWithCommit(task.commandName, task.propertyPrefix, closure)
    }

    protected void eachTaskWithCommit(String commandName, String propertyPrefix, Closure closure){
        List<TaskSetup> taskList = getTaskListByCommandName(commandName)
        int startIndex = taskList.findIndexOf{ it.propertyPrefix == propertyPrefix }
        eachTaskWithCommit(commandName, startIndex, closure)
    }

    protected void eachTaskWithCommit(String commandName, int startIndex, Closure closure){
        Integer commandDepth = 0
        Integer commandStep = 0
        TaskSetup latestTask
        List<TaskSetup> taskList
        TaskSetup task

        while(true){
            taskList = getTaskListByCommandName(commandName)
            Integer i = startIndex

            /** Run Each Tasks **/
            while(i < taskList.size()){
                task = taskList[i]
                latestTask = (propman.isHeadLast()) ? task : latestTask
                logger.trace "[${propman.headIndex}:${commandDepth}-${commandStep}] ${task.propertyPrefix}  (${propman.getCommitId()})"

                //- Do Task
                taskResultStatus = closure(task)

                //- Check Status
                if (taskResultStatus == TaskUtil.STATUS_UNDO_QUESTION){
                    TaskSetup mvTask = undo(task, latestTask)
                    commandName = mvTask.commandName
                    int tempDepth = propman.getCommit()?.customData?.commandDepth ?: 0
                    commandDepth = (propman.getCommit()?.customData?.task?.taskClazz == Command) ? tempDepth +1 : tempDepth
                    startIndex = getTaskIndexOnThisCommand(commandName, mvTask.propertyPrefix)
                    break

                }else if (taskResultStatus == TaskUtil.STATUS_REDO_QUESTION){
                    TaskSetup mvTask = redo(task, latestTask)
                    commandName = mvTask.commandName
                    int tempDepth = propman.getCommit()?.customData?.commandDepth ?: 0
                    commandDepth = (propman.getCommit()?.customData?.task?.taskClazz == Command) ? tempDepth +1 : tempDepth
                    startIndex = getTaskIndexOnThisCommand(commandName, mvTask.propertyPrefix)
                    break

                }else if (taskResultStatus == TaskUtil.STATUS_GOTO_COMMAND){
                    List<String> commandList =  provider.getList('command').collect{ it.split('\\s+') }.flatten()
                    commit(task, commandDepth, commandStep)

                    propman.getCommit().customData['commandList'] = commandList
                    commandDepth++
                    commandStep = 0
                    commandName = commandList[0]
                    startIndex = 0
                    break

                }else{
                    commit(task, commandDepth, commandStep)
                    i++
                }
            }

            /** Control **/
            if (taskResultStatus == TaskUtil.STATUS_GOTO_COMMAND) {
                continue
            }else if (taskResultStatus == TaskUtil.STATUS_UNDO_QUESTION){
                continue
            }else if (taskResultStatus == TaskUtil.STATUS_REDO_QUESTION){
                continue
            }else{
                //Finish Command All
                if (commandDepth == 0 && (taskList.size() -1) <= i){
                    return
                //Finish This Command Depth
                }else{
                    int nowCommandDepth = propman.getCommit().customData['commandDepth']
                    int nowCommandStep = propman.getCommit().customData['commandStep']
                    int beforeDepthLastCommitIndex = propman.commitStackList.findLastIndexOf { it.customData['commandDepth'] == (nowCommandDepth -1) }
                    CommitObject commitTemp = propman.getCommit(beforeDepthLastCommitIndex)
                    task = commitTemp.customData['task']

                    if (task.taskClazz == Command){
                        List<String> commandList = commitTemp.customData['commandList']
                        if (commandList.size() > (nowCommandStep +1)){
                            commandDepth = commandDepth
                            commandStep++
                            commandName = commandList[commandStep]
                            startIndex = 0
                        }else{
                            commandDepth--
                            commandStep = 0
                            commandName = task.commandName
                            startIndex = getTaskIndexOnThisCommand(commandName, commitTemp.id)
                            startIndex++
                        }
                    }
                }
            }

        }
    }

    //level by level
    protected void eachTask(String commandName, Closure closure){
        //1. Try to get levels from level property
        String taskOrderProperty = "${commandName}.order".toString()
        List<String> taskOrderList = getSpecificLevelList(taskOrderProperty) ?: getTaskLineOrderList(propertiesFile, propertiesFileExtension, commandName, taskOrderProperty)

        //2. Do Each Tasks
        taskOrderList.eachWithIndex{ taskName, i ->
            String propertyPrefix = "${commandName}.${taskName}.".toString()
            closure(propertyPrefix)
        }
    }

    protected List<TaskSetup> getTaskListByCommandName(String commandName){
        if (!commandNameTaskListMap.containsKey(commandName)){
            //1. Try to get task order from property
            String taskOrderProperty = "${commandName}.order".toString()
            List<String> taskOrderList = getSpecificLevelList(taskOrderProperty) ?: getTaskLineOrderList(propertiesFile, propertiesFileExtension, commandName, taskOrderProperty)
            List<String> prefixList = taskOrderList.collect{ "${commandName}.${it}.".toString() }
            List<Class> taskTypeList = prefixList.collect{ getTaskClass(getTaskName(it)) }
            commandNameTaskListMap[commandName] = prefixList.collect{ generateTaskSetup('', it) }
        }
        return commandNameTaskListMap[commandName]
    }

    protected int getTaskIndexOnThisCommand(List<TaskSetup> taskList){
        String headCommitId = propman.getCommitId()
        int taskIndex = taskList.findIndexOf{ it.propertyPrefix == headCommitId }
        return taskIndex
    }

    protected int getTaskIndexOnThisCommand(String commandName, String propertyPrefix){
        List<TaskSetup> taskList = getTaskListByCommandName(commandName)
        int taskIndex = taskList.findIndexOf{ it.propertyPrefix == propertyPrefix }
        return taskIndex
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

    protected List<String> getTaskLineOrderList(File propertiesFile, String fileExtension, String executorName, String taskOrderProperty){
        Map taskNameMap = [:]
        File scriptFile = propertiesFile

        //-YML or YAML
        if (['yml', 'yaml'].contains(fileExtension)){
            Map scriptMap = generateMapFromPropertiesFile(scriptFile)
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
    TaskSetup undo(TaskSetup nowTask, TaskSetup latestTask){
        //Get Previous Task
        TaskSetup mvTask = nowTask.clone()
        TaskSetup commitTask = propman.getCommit()?.customData?.task

        //Check NowTask is undoable task?
        if (!propman.isHeadFirst()
        && nowTask && (isUndoableTask(nowTask) || !checkCondition(nowTask))
        && commitTask && (isUndoableTask(commitTask) || !checkCondition(commitTask))
        ){

            //Check the previous task
            while ( !propman.isHeadFirst() && (isUndoMoreTask(commitTask) || !checkCondition(commitTask)) ){
                propman.undo()
                commitTask = propman.getCommit().customData['task']
                mvTask = propman.getNextCommit().customData['task']
            }

            if (commitTask && (isUndoableTask(commitTask) || !checkCondition(commitTask))){
                propman.undo()
                commitTask = propman.getCommit().customData['task']
                mvTask = propman.getNextCommit().customData['task']
            }

            //Check It is Last Task?
            //If First Task is undoMore, then auto-redo
            if (!commitTask){
                mvTask = redo(mvTask, latestTask)
                logger.error "It can not undo"
            }

        }else{
            logger.error ((propman.isHeadFirst()) ?  "No more undo" : "It can not undo")
        }
        return mvTask
    }

    /*****
     * REDO
     *****/
    TaskSetup redo(TaskSetup nowTask, TaskSetup latestTask){
        //Get Previous Task
        TaskSetup mvTask = nowTask.clone()
        TaskSetup commitTask = propman.getCommit()?.customData?.task

        //Check NowTask is undoable task?
        if (propman.isNotHeadLast()
        && nowTask && (isUndoableTask(nowTask) || !checkCondition(nowTask))
        //&& commitTask && (isUndoableTask(commitTask) || !checkCondition(commitTask))
        ){
            propman.redo()
            commitTask = propman.getCommit()?.customData?.task
            mvTask = propman.getNextCommit()?.customData?.task

            //Check the next task
            while ( propman.isNotHeadLast() && (isUndoMoreTask(mvTask) || !checkCondition(mvTask)) ){
                propman.redo()
                commitTask = propman.getCommit()?.customData?.task
                mvTask = propman.getNextCommit()?.customData?.task
            }

            if (!mvTask)
                mvTask = latestTask.clone()

        }else{
            propman.rollback()
            logger.error "It Can not redo more"
        }
        return mvTask
    }

    /*****
     * COMMIT
     *****/
    void commit(){
        propman.commit()
    }

    void commit(String commitId){
        propman.commit(commitId)
    }

    void commit(TaskSetup task, int commandDepth, int commandStep){
        String commitId = task.propertyPrefix
        propman.commit(commitId)
        propman.getCommit().customData['task'] = task
        propman.getCommit().customData['commandDepth'] = commandDepth
        propman.getCommit().customData['commandStep'] = commandStep
    }

    boolean isUndoableTask(TaskSetup task){
        return isUndoableTask(task.taskClazz)
    }

    boolean isUndoableTask(Class task){
        return undoableTaskList.contains(task)
    }

    boolean isUndoMoreTask(TaskSetup task){
        return isUndoMoreTask(task.taskClazz)
    }

    boolean isUndoMoreTask(Class task){
        return undoMoreTaskList.contains(task)
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
        TaskSetup task = generateTaskSetup(taskTypeName, propertyPrefix)
        //(Task) Start
        return runTask(task)
    }

    Integer runTask(TaskSetup task){
        /** Validation **/
        if (!task.taskClazz)
            throw new Exception("${task.taskTypeName} Does not exists task. or You Can't")
        if (!task.taskTypeName)
            throw new Exception(" 'No Task Name. ${task.propertyPrefix}task=???. Please Check Task.' ")
        if ( (validTaskList && !validTaskList.contains(task.taskClazz)) || (invalidTaskList && invalidTaskList.contains(task.taskClazz)) )
            throw new Exception(" 'Sorry, This is Not my task, [${task.taskTypeName}]. I Can Not do this.' ")

        status = STATUS_NOTHING
        /** Check Condition **/
        if (checkCondition(task.propertyPrefix)){
            /** Start Task **/
            status = doTask(task)

        }else if (propman.getString("${task.propertyPrefix}else.task")){
            /** Start Else Task **/
            status = runTaskByPrefix("${task.propertyPrefix}else.")
        }
        return status
    }

    TaskSetup generateTaskSetup(String taskTypeName, String propertyPrefix){
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
        return task
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
     * CONDITION
     *************************/
    protected boolean checkCondition(TaskSetup task){
        return checkCondition(task.propertyPrefix)
    }

    protected boolean checkCondition(String propertyPrefix){
        return (provider.checkCondition(propertyPrefix) && provider.checkDashDashOption(propertyPrefix) && checkPortCondition(propertyPrefix))
    }

    protected boolean checkPortCondition(propertyPrefix){
        boolean isTrue
        def conditionIfObj = propman.parse("${propertyPrefix}ifport")
        if (conditionIfObj){
            logger.info("!Checking Port")
            Map optionMap = [:]
            conditionIfObj.each{ String optionName, def value ->
                optionMap[optionName] = new TestPort().testPort(optionName)
            }
            def foundItem = Util.find(optionMap, conditionIfObj)
            isTrue = !!foundItem
        }else{
            isTrue = true
        }
        if (!isTrue)
            logger.warn "The port conditions do not match."
        return isTrue
    }
    
    /*************************
     * DESCRIPT
     *************************/
    protected void descript(TaskSetup task){
        String description = task.desc ? "$task.jobName:$task.desc" : "$task.jobName:$task.taskName:$task.taskTypeName"
        if (description && !task.commandName.equalsIgnoreCase('ask')){
            if (task.descColor)
                config.logGen.setupConsoleLoggerColorPattern(task.descColor)
            logTaskDescription(description)
            if (task.descColor)
                config.logGen.setupBeforeConsoleLoggerPattern()
        }
    }



    /*************************
     * 3. DO TASK
     *************************/
    Integer doTask(TaskSetup task){
        Integer status = STATUS_NOTHING

        /** Find Task **/
        task.taskInstance = setupTaskInstance(task)

        try{
            //Start Color Log Pattern
            if (task.color)
                config.logGen.setupConsoleLoggerColorPattern(task.color)

            //Description
            if ( !(task.jobName.equalsIgnoreCase('macgyver') && [Version, System, Help].contains(task.taskClazz)) )
                descript(task)

            /** Start Task **/
            status = task.taskInstance.run()

        }catch(e){
            throw e
        }finally{
            //Finish Color Log Pattern
            if (task.color)
                config.logGen.setupBeforeConsoleLoggerPattern()

            //Save Report
            if (status != STATUS_UNDO_QUESTION)
                report(task.taskInstance)
        }
        return status
    }

    TaskUtil setupTaskInstance(TaskSetup task){
        TaskUtil taskInstance = config.findInstance(task.taskClazz)
        // - Inject Value
        provider.shift( task.jobName, task.propertyPrefix )
        varman.setVariableSign(task.variableSign)
        config.cleanValue(taskInstance)
        config.injectValue(taskInstance)
        taskInstance.rememberAnswerLineList = rememberAnswerLineList
        taskInstance.reportMapList = reportMapList
        return taskInstance
    }

    protected void setuptLog(LogSetup logOpt){
        config.logGen.setupConsoleLogger(logOpt.logLevelConsole)
        config.logGen.setupFileLogger(jobName, logOpt.logLevelFile, logOpt.logDir, logOpt.logFileName)
    }



    /*************************
     * 4. REPORT
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
