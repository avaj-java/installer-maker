package install.job

import install.task.TaskQuestionFindFile
import jaemisseo.man.FileMan
import install.task.TaskDecrypt
import install.task.TaskEncrypt
import install.task.TaskExec
import install.task.TaskFileCopy
import install.task.TaskFileJar
import install.task.TaskFileMkdir
import install.task.TaskFileReplace
import install.task.TaskFileTar
import install.task.TaskFileUnjar
import install.task.TaskFileUntar
import install.task.TaskFileUnzip
import install.task.TaskFileZip
import install.task.TaskMergeProperties
import install.task.TaskNotice
import install.task.TaskQuestion
import install.task.TaskQuestionChoice
import install.task.TaskQuestionYN
import install.task.TaskSet
import install.task.TaskSql
import install.task.TaskTestEMail
import install.task.TaskTestGroovyRange
import install.task.TaskTestJDBC
import install.task.TaskTestPort
import install.task.TaskTestREST
import install.task.TaskTestSocket
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-04-07.
 */
class JobUtil extends TaskUtil{

    public static final String JOB_BUILDER = "BUILDER"
    public static final String JOB_RECEPTIONIST = "RECEPTIONIST"
    public static final String JOB_INSTALLER = "INSTALLER"

    String levelNamesProperty = ''
    String executorNamePrefix = ''
    String propertiesFileName = ''
    List validTaskList = []
    List invalidTaskList = []
    def gOpt

    Integer taskResultStatus
    List<String> undoableList = [TASK_Q, TASK_Q_CHOICE, TASK_Q_YN, TASK_Q_FIND_FILE, TASK_SET, TASK_NOTICE]
    List<String> undoMoreList = [TASK_SET, TASK_NOTICE]


    //level by level For Task
    protected void eachLevelForTask(Closure closure){
        //1. Try to get levels from level property
        List<String> levelList = getSpecificLevelList(levelNamesProperty) ?: geLinetOrderedLevelList(propertiesFileName, executorNamePrefix)
        List<String> taskList = levelList.collect{ getTaskName("${executorNamePrefix}.${it}.") }
        List<String> prefixList = levelList.collect{ "${executorNamePrefix}.${it}." }

        //2. Do Each Tasks
        commit()
        for (int i=0; i<levelList.size(); i++){
            String levelName = levelList[i]
            String propertyPrefix = "${executorNamePrefix}.${levelName}."
            //- Do Task
            taskResultStatus = closure(propertyPrefix)
            //- Check Status
            if (taskResultStatus == TaskUtil.STATUS_UNDO_QUESTION)
                i = undo(taskList, prefixList, i)
            else if (taskResultStatus == TaskUtil.STATUS_REDO_QUESTION)
                i = redo(taskList, prefixList, i)
            else
                commit()
        }
    }

    //level by level
    protected void eachLevel(Closure closure){
        //1. Try to get levels from level property
        List<String> levelList = getSpecificLevelList(levelNamesProperty) ?: geLinetOrderedLevelList(propertiesFileName, executorNamePrefix)

        //2. Do Each Tasks
        for (int i=0; i<levelList.size(); i++){
            String levelName = levelList[i]
            String propertyPrefix = "${executorNamePrefix}.${levelName}."
            //- Do Task
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
    int undo(List taskList, List prefixList, int i){
        i -= 1
        if (undoableList.contains(taskList[i])){
            propman.undo()
            while (i > 0 && (undoMoreList.contains(taskList[i]) || !checkCondition(prefixList[i])) ){
                i -= 1
                propman.undo()
            }
            i -= 1
            if (i <= -1){
                i = -1
                propman.checkout(0)
                //First is undoMore, then auto-redo
                if (undoMoreList.contains(taskList[0])){
                    i += 1
                    propman.redo()
                    while ( propman.isNotHeadLast() && undoableList.contains(taskList[i+1]) && (undoMoreList.contains(taskList[i+1]) || !checkCondition(prefixList[i+1])) ){
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
    int redo(List taskList, List prefixList, int i){
        if (propman.isNotHeadLast()){
            propman.redo()
            while ( propman.isNotHeadLast() && undoableList.contains(taskList[i+1]) && (undoMoreList.contains(taskList[i+1]) || !checkCondition(prefixList[i+1])) ){
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



    String getTaskName(String propertyPrefix){
        return getString("${propertyPrefix}task")?.trim()?.toUpperCase()
    }

    Integer runTaskByPrefix(String propertyPrefix) {
        String taskName = getTaskName(propertyPrefix)
        return runTask(taskName, propertyPrefix)
    }

    Integer runTask(String taskName){
        return runTask(taskName, '')
    }

    Integer runTask(String taskName, String propertyPrefix){
        //Check Valid Task
        if (!taskName)
            throw new Exception(" 'No Task Name. ${propertyPrefix}task=???. Please Check Task.' ")
        if ( (validTaskList && !validTaskList.contains(taskName)) || (invalidTaskList && invalidTaskList.contains(taskName)) )
            throw new Exception(" 'Sorry, This is Not my task, [${taskName}]. I Can Not do this.' ")

        //Run Task
        switch (taskName){
            case TaskUtil.TASK_NOTICE:
                return new TaskNotice().setPropman(propman).start(propertyPrefix)
                break
            case TaskUtil.TASK_Q:
                return new TaskQuestion().setPropman(propman).setRememberAnswerLineList(rememberAnswerLineList).start(propertyPrefix)
                break
            case TaskUtil.TASK_Q_CHOICE:
                return new TaskQuestionChoice().setPropman(propman).setRememberAnswerLineList(rememberAnswerLineList).start(propertyPrefix)
                break
            case TaskUtil.TASK_Q_YN:
                return new TaskQuestionYN().setPropman(propman).setRememberAnswerLineList(rememberAnswerLineList).start(propertyPrefix)
                break
            case TaskUtil.TASK_Q_FIND_FILE:
                return new TaskQuestionFindFile().setPropman(propman).setRememberAnswerLineList(rememberAnswerLineList).start(propertyPrefix)
                break
            case TaskUtil.TASK_SET:
                return new TaskSet().setPropman(propman).start(propertyPrefix)
                break

            case TaskUtil.TASK_TAR:
                return new TaskFileTar().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_ZIP:
                return new TaskFileZip().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_JAR:
                return new TaskFileJar().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_UNTAR:
                return new TaskFileUntar().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_UNZIP:
                return new TaskFileUnzip().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_UNJAR:
                return new TaskFileUnjar().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_REPLACE:
                return new TaskFileReplace().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_COPY:
                return new TaskFileCopy().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_MKDIR:
                return new TaskFileMkdir().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_EXEC:
                return new TaskExec().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_SQL:
                return new TaskSql().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_MERGE_ROPERTIES:
                return new TaskMergeProperties().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_EMAIL://Not Supported Yet
                return new TaskTestEMail().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_SOCKET:
                return new TaskTestSocket().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_REST:
                return new TaskTestREST().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_JDBC:
                return new TaskTestJDBC().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_PORT:
                return new TaskTestPort().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_GROOVYRANGE:
                return new TaskTestGroovyRange().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_ENCRYPT:
                return new TaskEncrypt().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_DECRYPT:
                return new TaskDecrypt().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            default :
                break
        }
        return TaskUtil.STATUS_TASK_RUN_FAILED
    }

}
