package install.util

import install.configuration.annotation.method.Before
import install.configuration.annotation.Inject
import install.bean.ReportSetup
import install.configuration.Config
import install.data.PropertyProvider
import install.task.*
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.VariableMan

/**
 * Created by sujkim on 2017-04-07.
 */
class JobUtil extends TaskUtil{

    String jobName = this.getClass().simpleName.toLowerCase()
    String levelNamesProperty = ''
    String executorNamePrefix = ''
    String propertiesFileName = ''

    List<Class> validTaskList = []
    List<Class> invalidTaskList = []
    List<Class> undoableList = [Question, QuestionChoice, QuestionYN, QuestionFindFile, Set, Notice]
    List<Class> undoMoreList = [Set, Notice]

    def gOpt
    Integer taskResultStatus

    Config config
    PropertyProvider provider

    @Inject void setConfig(Config config){ this.config = config }
    @Inject void setProvider(PropertyProvider provider){ this.provider = provider }



    @Before
    void before(){
        provider.shift( jobName )
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

    PropMan parsePropMan(PropMan propmanToDI, VariableMan varman){
        return parsePropMan(propmanToDI, varman, null)
    }

    PropMan parsePropMan(PropMan propmanToParse, VariableMan varman, String excludeStartsWith){
        //Parse ${Variable} Exclude Levels
        (1..5).each{
            Map map = propmanToParse.properties
            varman.putVariables(map)
            map.each{ String key, def value ->
                if (value && value instanceof String){
                    if (!excludeStartsWith || !key.startsWith(excludeStartsWith)){
                        propmanToParse.set(key, varman.parse(value))
                    }
                }
            }
        }
        return propmanToParse
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
    protected void eachLevelForTask(Closure closure){
        //1. Try to get levels from level property
        List<String> levelList = getSpecificLevelList(levelNamesProperty) ?: geLinetOrderedLevelList(propertiesFileName, executorNamePrefix)
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
        List<String> levelList = getSpecificLevelList(levelNamesProperty) ?: geLinetOrderedLevelList(propertiesFileName, executorNamePrefix)

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

    protected List<String> geLinetOrderedLevelList(String fileName, String executorName){
        Map levelNameMap = [:]
        String userSetPropertiesDir = propman.get('properties.dir')
        File scriptFile = (userSetPropertiesDir) ? new File("${userSetPropertiesDir}/${fileName}") : FileMan.getFileFromResource(fileName)
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
        return levelNameMap.keySet().toList()
    }



    /**
     * UNDO
     */
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
                println "It Can not undo"
            }
        }else{
            if (i == -1)
                println "No more undo"
            else
                println "It Can not undo"
        }
        return i
    }

    /**
     * REDO
     */
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
            println "It Can not redo more"
        }
        return i
    }

    /**
     * COMMIT
     */
    void commit(){
        propman.commit()
    }



    /*************************
     * 1. RUN TASK
     *************************/
    Integer runTaskByPrefix(String propertyPrefix) {
        String taskName = getTaskName(propertyPrefix)
        return runTask(taskName, propertyPrefix)
    }

    Integer runTask(String taskName){
        return runTask(taskName, '')
    }

    Integer runTask(String taskName, String propertyPrefix){
        //Check Valid Task
        Class taskClazz = getTaskClass(taskName)

        //Validation
        if (!taskName)
            throw new Exception(" 'No Task Name. ${propertyPrefix}task=???. Please Check Task.' ")
        if ( (validTaskList && !validTaskList.contains(taskClazz)) || (invalidTaskList && invalidTaskList.contains(taskClazz)) )
            throw new Exception(" 'Sorry, This is Not my task, [${taskName}]. I Can Not do this.' ")

        //(Task) Start
        return start(propertyPrefix, taskClazz)
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
    Integer start(String propertyPrefix, Class taskClazz){
        status = STATUS_NOTHING

        //Check Condition
        if ( !checkCondition(propertyPrefix) )
            return

        //Description
        descript(propertyPrefix)

        //Get Task Instance
        // - Find Task
        TaskUtil taskInstance = config.findInstance(taskClazz)
        // - Inject Value
        provider.shift( jobName, propertyPrefix )
        config.injectValue(taskInstance)
        taskInstance.provider = provider
        taskInstance.propertyPrefix = propertyPrefix
        taskInstance.rememberAnswerLineList = rememberAnswerLineList
        taskInstance.reportMapList = reportMapList

        try{
            //Start Task
            status = taskInstance.run()

        }catch(e){
            throw e
        }finally{
            if (status != STATUS_UNDO_QUESTION)
                report(taskInstance)
        }

        return status
    }

    protected boolean checkCondition(String propertyPrefix){
        return provider.checkCondition(propertyPrefix)
    }

    protected void descript(String propertyPrefix){
        String descriptionString = provider.get("desc")
        descriptionString = descriptionString ?: propertyPrefix
        if (descriptionString)
            logBigTitle(descriptionString)
    }

    /*************************
     * 3. REPORT
     *************************/
    void report(TaskUtil taskInstance){
        ReportSetup reportSetup = provider.genMergedReportSetup()

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
