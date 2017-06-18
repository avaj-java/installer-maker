package install

import install.configuration.InstallerLogGenerator
import install.configuration.InstallerPropertiesGenerator
import install.task.*
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.VariableMan

/**
 * Created by sujkim on 2017-04-07.
 */
class JobUtil extends TaskUtil{

    String levelNamesProperty = ''
    String executorNamePrefix = ''
    String propertiesFileName = ''
    List<Class> validTaskList = []
    List<Class> invalidTaskList = []
    def gOpt

    Integer taskResultStatus
    List<Class> undoableList = [Question, QuestionChoice, QuestionYN, QuestionFindFile, Set, Notice]
    List<Class> undoMoreList = [Set, Notice]

    InstallerPropertiesGenerator propGen = new InstallerPropertiesGenerator()
    InstallerLogGenerator logGen = new InstallerLogGenerator()



    PropMan setupPropMan(InstallerPropertiesGenerator propGen){
        String poolName = this.getClass().simpleName.toLowerCase()
        PropMan propman = propGen.get(poolName)
        return propman
    }

    VariableMan setupVariableMan(PropMan propman, String executorNamePrefix){
        VariableMan varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, executorNamePrefix)
        setBeforeGetProp(propman, varman)
        return varman
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



    /**
     * RUN TASK
     */
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

        //Run Task
        TaskUtil taskInctance = newTaskInstance(taskClazz.getSimpleName())
        return taskInctance
                .setPropman(propman)
                .setRememberAnswerLineList(rememberAnswerLineList)
                .setReporter(reportMapList)
                .start(propertyPrefix)

        return TaskUtil.STATUS_TASK_RUN_FAILED
    }



    String getTaskName(String propertyPrefix){
        String taskName = getString("${propertyPrefix}task")?.trim()?.toUpperCase()
        return taskName
    }

    Class getTaskClass(String taskName){
        Class taskClazz = validTaskList.find{ it.getSimpleName().equalsIgnoreCase(taskName) }
        if (!taskClazz)
            throw new Exception('Does not exists task.')
        return taskClazz
    }

}
