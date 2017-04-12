package install.job

import com.jaemisseo.man.FileMan
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
    String levelNamePrefix = ''
    List validTaskList = []
    List invalidTaskList = []
    def gOpt



    //Do level by level
    protected void eachLevel(String levelNamesProperty, String executorName, String fileName, Closure closure){
        //1. Try to get levels from level property
        List<String> levelList = getSpecificLevelList(levelNamesProperty) ?: geLinetOrderedLevelList(fileName, executorName)
        //3. Do Each Tasks
        levelList.each{ String levelName ->
            closure(levelName)
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





    void runTaskByPrefix(String propertyPrefix) {
        String taskName = getString("${propertyPrefix}task")?.trim()?.toUpperCase()
        runTask(taskName, propertyPrefix)
    }

    void runTask(String taskName){
        runTask(taskName, '')
    }

    void runTask(String taskName, String propertyPrefix){
        //Check Valid Task
        if (!taskName)
            throw new Exception(" 'No Task Name. ${propertyPrefix}task=???. Please Check Task.' ")
        if ( (validTaskList && !validTaskList.contains(taskName)) || (invalidTaskList && invalidTaskList.contains(taskName)) )
            throw new Exception(" 'Sorry, This is Not my task, [${taskName}]. I Can Not do this.' ")

        //Run Task
        switch (taskName){
            case TaskUtil.TASK_NOTICE:
                new TaskNotice().setPropman(propman).start(propertyPrefix)
                break
            case TaskUtil.TASK_Q:
                new TaskQuestion().setPropman(propman).setRememberAnswerLineList(rememberAnswerLineList).start(propertyPrefix)
                break
            case TaskUtil.TASK_Q_CHOICE:
                new TaskQuestionChoice().setPropman(propman).setRememberAnswerLineList(rememberAnswerLineList).start(propertyPrefix)
                break
            case TaskUtil.TASK_Q_YN:
                new TaskQuestionYN().setPropman(propman).setRememberAnswerLineList(rememberAnswerLineList).start(propertyPrefix)
                break
            case TaskUtil.TASK_SET:
                new TaskSet().setPropman(propman).start(propertyPrefix)
                break

            case TaskUtil.TASK_TAR:
                new TaskFileTar().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_ZIP:
                new TaskFileZip().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_JAR:
                new TaskFileJar().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_UNTAR:
                new TaskFileUntar().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_UNZIP:
                new TaskFileUnzip().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_UNJAR:
                new TaskFileUnjar().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_REPLACE:
                new TaskFileReplace().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_COPY:
                new TaskFileCopy().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_MKDIR:
                new TaskFileMkdir().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_EXEC:
                new TaskExec().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_SQL:
                new TaskSql().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_MERGE_ROPERTIES:
                new TaskMergeProperties().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_EMAIL://Not Supported Yet
                new TaskTestEMail().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_SOCKET:
                new TaskTestSocket().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_REST:
                new TaskTestREST().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_JDBC:
                new TaskTestJDBC().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_GROOVYRANGE:
                new TaskTestGroovyRange().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_PORT:
                new TaskTestPort().setPropman(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            default :
                break
        }
    }

}
