package install.util

import install.bean.LogSetup
import install.bean.ReportSetup
import install.bean.TaskSetup
import install.exception.WantToRestartException
import jaemisseo.man.configuration.Config
import jaemisseo.man.configuration.annotation.Inject
import jaemisseo.man.configuration.annotation.method.After
import jaemisseo.man.configuration.annotation.method.Before
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.Undoable
import jaemisseo.man.configuration.annotation.type.Undomore
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
    List<Class> undoMoreTaskList = Util.findAllClasses('install', [Undomore])

    def gOpt
    Integer taskResultStatus
    Map<String, List<TaskSetup>> commandNameTaskListMap = [:]
    Map<String, List<TaskSetup>> virtualCommandNameTaskListMap = [:]



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





    /*************************
     *
     * Each Task with Commit
     *
     *************************/
    protected void eachTaskWithCommit(String commandName, Closure closure){
        eachTaskWithCommit(commandName, 0, closure)
    }

    protected void eachTaskWithCommit(TaskSetup task, Closure closure){
        eachTaskWithCommit(task.commandName, task.propertyPrefix, closure)
    }

    protected void eachTaskWithCommit(String commandName, String propertyPrefix, Closure closure){
        List<TaskSetup> taskList = setupTaskListFromFileByCommandName(commandName)
        int startIndex = taskList.findIndexOf{ it.propertyPrefix == propertyPrefix }
        eachTaskWithCommit(commandName, startIndex, closure)
    }

    protected void eachTaskWithCommit(String startCommandName, int startIndex, Closure closure){
        String commandName = startCommandName
        Integer commandDepth = 0
        Integer commandStep = 0
        TaskSetup latestCommitTask
        List<TaskSetup> commitTaskList
        TaskSetup workingTask

        while(true){
            commitTaskList = setupTaskListFromFileByCommandName(commandName)
            if (!commitTaskList)
                break
            Integer i = startIndex

            /** Run Each Tasks **/
            while(i < commitTaskList.size()){
                workingTask = commitTaskList[i]
                latestCommitTask = (propman.isHeadLast()) ? workingTask : latestCommitTask
                logger.trace "[${propman.headIndex}:${commandDepth}-${commandStep}] ${workingTask.propertyPrefix}  (${propman.getCommitId()})"

                //- Do Task
                taskResultStatus = closure(workingTask)

                //- Check Status
                if (taskResultStatus == TaskUtil.STATUS_UNDO_QUESTION){
                    TaskSetup mvTask = undo(workingTask, latestCommitTask)
                    break

                }else if (taskResultStatus == TaskUtil.STATUS_REDO_QUESTION){
                    TaskSetup mvTask = redo(workingTask, latestCommitTask)
                    break

                }else if (taskResultStatus == TaskUtil.STATUS_BACK){
                    String destPrefix =  provider.getString('to') + '.'
                    TaskSetup mvTask = undoForce(workingTask, latestCommitTask, destPrefix)
                    break

                }else if (taskResultStatus == TaskUtil.STATUS_GOTO_COMMAND) {
                    List<String> commandList = provider.getList('command').collect { it.split('\\s+') }.flatten()
                    commit(workingTask, commandDepth, commandStep)
                    propman.getCommit().customData['commandList'] = commandList
                    break

                }else if (taskResultStatus == TaskUtil.STATUS_EXIT) {
                    break

                }else if (taskResultStatus == TaskUtil.STATUS_RESET){
                    provider.propman.checkout(0)
                    throw new WantToRestartException()

                }else{
                    commit(workingTask, commandDepth, commandStep)
                    i++
                }
            }

            /** EXIT **/
            if (taskResultStatus == TaskUtil.STATUS_EXIT) {
                return

            /** CHANGE **/
            }else if ([TaskUtil.STATUS_UNDO_QUESTION, TaskUtil.STATUS_REDO_QUESTION, TaskUtil.STATUS_BACK, TaskUtil.STATUS_GOTO_COMMAND].contains(taskResultStatus)) {
                TaskSetup checkCommitTask = propman.getCommit().customData['task']
                if (!checkCommitTask){
                    commandDepth = 0
                    commandStep = 0
                    commandName = startCommandName
                    startIndex = 0
                    continue
                }else if (checkCommitTask.taskClazz == Command){
                    List<String> commandList = propman.getCommit().customData['commandList']
                    commandDepth++
                    commandStep = 0
                    commandName = commandList[commandStep]
                    startIndex = 0
                    continue
                }else{
                    commandDepth = propman.getCommit()?.customData?.commandDepth
                    commandStep = propman.getCommit()?.customData?.commandStep
                    commandName = checkCommitTask?.commandName
                    i = getTaskIndexOnThisCommand(commandName, checkCommitTask?.propertyPrefix)
                    startIndex = i + 1
                    if (startIndex < setupTaskListFromFileByCommandName(commandName).size())
                        continue
                }
            }

            /** NEXT **/
            //Finish Command All
            if (commandDepth == 0 && (commitTaskList.size() -1) <= i){
                return
            //Finish This Command Depth
            }else{
                int nowCommandDepth = propman.getCommit().customData['commandDepth']
                int nowCommandStep = propman.getCommit().customData['commandStep']
                int beforeDepthLastCommitIndex = propman.commitStackList[0..propman.headIndex].findLastIndexOf { it.customData['commandDepth'] == (nowCommandDepth -1) }
                CommitObject commitTemp = propman.getCommit(beforeDepthLastCommitIndex)
                TaskSetup beforeDepthCommandTask = commitTemp.customData['task']

                if (beforeDepthCommandTask.taskClazz == Command){
                    List<String> commandList = commitTemp.customData['commandList']
                    if (commandList.size() > (nowCommandStep +1)){
                        commandDepth = commandDepth
                        commandStep++
                        commandName = commandList[commandStep]
                        startIndex = 0
                    }else{
                        commandDepth--
                        commandStep = 0
                        commandName = beforeDepthCommandTask.commandName
                        startIndex = getTaskIndexOnThisCommand(commandName, commitTemp.id) +1
                    }
                }
            }

        }
    }

    /*************************
     *
     * Each Task
     *
     *************************/
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

    /*************************
     *
     * Cache Task
     *
     *************************/
    protected void cacheAllCommitTaskListOnAllCommand(){
        //Cache All Command
        getAllCommandList().each{ String commandName ->
            setupTaskListFromFileByCommandName(commandName)
        }
    }

    protected List<String> getAllCommandList(){
        //Extract All Command
        Map<String, Object> extractedTaskProperty = propman.properties.findAll{ String key, value ->
            String[] keyItems = key.split('[.]')
            return keyItems.size() == 3 && keyItems[2].equals('task')
        }
        List<String> allCommandList = extractedTaskProperty.collect{ it.key.split('[.]')[0] }.unique()
        return allCommandList
    }

    protected List<TaskSetup> setupTaskListFromFileByCommandName(String commandName){
        propman.setModeIgnoreBeforeGetClosure(true)
        if (!commandNameTaskListMap.containsKey(commandName)){
            //1. Try to get task order from property
            String taskOrderProperty = "${commandName}.order".toString()
            List<String> taskOrderList = getSpecificLevelList(taskOrderProperty) ?: getTaskLineOrderList(propertiesFile, propertiesFileExtension, commandName, taskOrderProperty)
            // Add Task Setup List
            if (taskOrderList){
                List<TaskSetup> taskSetupList = taskOrderList.collect{ generateTaskSetup('', "${commandName}.${it}.".toString()) }
                commandNameTaskListMap[commandName] = taskSetupList
                /** Make Vitual Command **/
                makeVirtualCommand(commandName)
            }
        }
        propman.setModeIgnoreBeforeGetClosure(false)
        return (commandNameTaskListMap[commandName] ?: virtualCommandNameTaskListMap[commandName])
    }

    /*************************
     * Make Vitual Command
     * - mode.variable.question.before.task
     * - mode.variable.question.before.command
     *************************/
    protected void makeVirtualCommand(String commandName){
        //Find Task to need to add virtual command
        List<TaskSetup> taskSetupList = commandNameTaskListMap[commandName]
        List<String> allVariableNameList = []

        /** (modeVariableQuestionBeforeTask) **/
        for (int i=0; i<taskSetupList.size(); i++){
            TaskSetup task = taskSetupList[i]
            if (task.modeVariableQuestionBeforeTask || task.modeVariableQuestionBeforeCommand){
                String tempVirtualCommandName = "cmd_tmp_${new Date().getTime()}"
                String tempVirtualCommandsTaskTypeName = 'question'

                //Extract Variable Name
                List<String> variableNameList
                List<String> tempPropertyList = propman.getPropertyList().findAll{ it.startsWith(task.propertyPrefix) }
                variableNameList = tempPropertyList.collect{ String property ->
                    def value = propman.getRaw(property)
                    if (value instanceof List){
                        return value.collect{ String v -> new VariableMan().parsedDataList(v).collect{ it.originalCode } }.flatten()
                    }else{
                        return new VariableMan().parsedDataList(value).collect{ it.originalCode }
                    }
                }.flatten().unique() - [""]

                if (variableNameList){
                    //(modeVariableQuestionBeforeCommand) Collecting
                    if (task.modeVariableQuestionBeforeCommand){
                        allVariableNameList << variableNameList
                    }

                    //(modeVariableQuestionBeforeTask)
                    if (task.modeVariableQuestionBeforeTask){
                        //Generate Task (Command to Virtual Command)
                        String commandTaskPropertyPrefix = "${commandName}.${task.taskName}_${tempVirtualCommandName}."
                        String conditionString = propman.getRaw("${task.propertyPrefix}if") ?: ''
                        virtualPropman["${commandTaskPropertyPrefix}if"] = conditionString
                        virtualPropman["${commandTaskPropertyPrefix}task"] = 'command'
                        virtualPropman["${commandTaskPropertyPrefix}command"] = tempVirtualCommandName
                        propman["${commandTaskPropertyPrefix}if"] = conditionString
                        propman["${commandTaskPropertyPrefix}task"] = 'command'
                        propman["${commandTaskPropertyPrefix}command"] = tempVirtualCommandName
                        //Add Task (Command to Virtual Command)
                        TaskSetup virtualCommandTask = generateTaskSetup('', commandTaskPropertyPrefix)
                        taskSetupList.add(i, virtualCommandTask)
                        i++
                        //Generate Command (Virtual Command to Questions)
                        List<TaskSetup> tempTaskSetupList = []
                        variableNameList.eachWithIndex{ String variableName, int varIndex ->
                            String taskName = varIndex
                            String tempPropertyPrefix = "${tempVirtualCommandName}.${taskName}."
                            virtualPropman["${tempPropertyPrefix}task"] = tempVirtualCommandsTaskTypeName
                            virtualPropman["${tempPropertyPrefix}desc"] = variableName
                            virtualPropman["${tempPropertyPrefix}answer.default"] = ('${' +"${variableName}"+ '}')
                            virtualPropman["${tempPropertyPrefix}property"] = variableName
                            propman["${tempPropertyPrefix}task"] = tempVirtualCommandsTaskTypeName
                            propman["${tempPropertyPrefix}desc"] = variableName
                            propman["${tempPropertyPrefix}answer.default"] = ('${' +"${variableName}"+ '}')
                            propman["${tempPropertyPrefix}property"] = variableName
                            tempTaskSetupList << generateTaskSetup('', tempPropertyPrefix)
                        }
                        //Add Command (Virtual Command to Questions)
                        virtualCommandNameTaskListMap[tempVirtualCommandName] = tempTaskSetupList
                    }
                }
            }
        }

        /** (modeVariableQuestionBeforeCommand) **/
        if (allVariableNameList){
            String tempVirtualCommandName = "cmd_tmp_${new Date().getTime()}"
            String tempVirtualCommandsTaskTypeName = 'question'
            //Extract Variable Name
            allVariableNameList = allVariableNameList.flatten().unique() - [""]
            //Generate Task(Command to Virtual Command)
            String commandTaskPropertyPrefix = "${commandName}.question_before_command_${tempVirtualCommandName}."
            virtualPropman["${commandTaskPropertyPrefix}task"] = 'command'
            virtualPropman["${commandTaskPropertyPrefix}command"] = tempVirtualCommandName
            propman["${commandTaskPropertyPrefix}task"] = 'command'
            propman["${commandTaskPropertyPrefix}command"] = tempVirtualCommandName
            //Add Task(Command to Virtual Command)
            TaskSetup virtualCommandTask = generateTaskSetup('', commandTaskPropertyPrefix)
            taskSetupList.add(0, virtualCommandTask)
            //Generate Command (Virtual Command to Questions)
            List<TaskSetup> tempTaskSetupList = []
            allVariableNameList.eachWithIndex{ String variableName, int varIndex ->
                String taskName = varIndex
                String tempPropertyPrefix = "${tempVirtualCommandName}.${taskName}."
                virtualPropman["${tempPropertyPrefix}task"] = tempVirtualCommandsTaskTypeName
                virtualPropman["${tempPropertyPrefix}desc"] = variableName
                virtualPropman["${tempPropertyPrefix}answer.default"] = ('${' +"${variableName}"+ '}')
                virtualPropman["${tempPropertyPrefix}property"] = variableName
                propman["${tempPropertyPrefix}task"] = tempVirtualCommandsTaskTypeName
                propman["${tempPropertyPrefix}desc"] = variableName
                propman["${tempPropertyPrefix}answer.default"] = ('${' +"${variableName}"+ '}')
                propman["${tempPropertyPrefix}property"] = variableName
                tempTaskSetupList << generateTaskSetup('', tempPropertyPrefix)
            }
            //Add Command (Virtual Command to Questions)
            virtualCommandNameTaskListMap[tempVirtualCommandName] = tempTaskSetupList
        }
    }


    
    protected int getTaskIndexOnThisCommand(List<TaskSetup> taskList){
        String headCommitId = propman.getCommitId()
        int taskIndex = taskList.findIndexOf{ it.propertyPrefix == headCommitId }
        return taskIndex
    }

    protected int getTaskIndexOnThisCommand(String commandName, String propertyPrefix){
        List<TaskSetup> taskList = setupTaskListFromFileByCommandName(commandName)
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
                    if (commandName.equals(executorName) && !propertyName.equals(taskOrderProperty) && !taskNameMap[taskName]){
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
                    if (commandName.equals(executorName) && !propertyName.equals(taskOrderProperty) && !taskNameMap[taskName]){
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
            if (!commitTask && isUndoMoreTask(mvTask)){
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
     * FORCE UNDO TO
     *****/
    TaskSetup undoForce(TaskSetup nowTask, TaskSetup latestTask, String destPrefix){
        //Find specific commit index in commit history
        int destCommitIndex = propman.commitStackList[0..propman.headIndex].findLastIndexOf{ CommitObject commitObj ->
            TaskSetup task = commitObj.customData?.task
            return task && task.propertyPrefix == destPrefix
        }
        return undoForce(nowTask, latestTask, destCommitIndex)
    }

    TaskSetup undoForce(TaskSetup nowTask, TaskSetup latestTask, int destCommitIndex){
        //Get Previous Task
        TaskSetup mvTask = nowTask.clone()
        TaskSetup commitTask = propman.getCommit()?.customData?.task

        if (destCommitIndex != -1){
            propman.checkout(destCommitIndex-1)
            commitTask = propman.getCommit()?.customData?.task
            mvTask = propman.getNextCommit()?.customData?.task
        }else{
            throw new Exception("Nothing to back [${destPrefix}]")
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

    Integer runTaskByCommitTask(TaskSetup commitTask){
        return runTask(commitTask.taskTypeName, commitTask.propertyPrefix)
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

    protected boolean checkPortCondition(String propertyPrefix){
        boolean isTrue
        def conditionIfObj = propman.parse("${propertyPrefix}ifport")
        if (conditionIfObj){
            logger.info("!Checking Port")
            Map optionMap = [:]
            conditionIfObj.each{ String optionName, def value ->
                if (optionName){
                    optionMap[optionName] = new TestPort().testPort(optionName)
                }else{
                    logger.warn "Empty port value"
                }
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
//        String description = task.desc ? "$task.jobName:$task.desc" : "$task.jobName:$task.taskName:$task.taskTypeName"
        String description = task.desc ?: "$task.jobName:$task.taskName:$task.taskTypeName"
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
            if ( !(task.jobName.equalsIgnoreCase('hoya') && [Version, System, Help].contains(task.taskClazz)) ){
                if ( ![Question, QuestionYN, QuestionChoice, QuestionFindFile, Set, Notice].contains(task.taskClazz) )
                    descript(task)
            }

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

    protected void setupLog(LogSetup logOpt){
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
