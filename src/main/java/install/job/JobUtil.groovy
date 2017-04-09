package install.job

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
                new TaskNotice(propman).start(propertyPrefix)
                break
            case TaskUtil.TASK_Q:
                new TaskQuestion(propman).setRememberAnswerLineList(rememberAnswerLineList).start(propertyPrefix)
                break
            case TaskUtil.TASK_Q_CHOICE:
                new TaskQuestionChoice(propman).setRememberAnswerLineList(rememberAnswerLineList).start(propertyPrefix)
                break
            case TaskUtil.TASK_Q_YN:
                new TaskQuestionYN(propman).setRememberAnswerLineList(rememberAnswerLineList).start(propertyPrefix)
                break
            case TaskUtil.TASK_SET:
                new TaskSet(propman).start(propertyPrefix)
                break

            case TaskUtil.TASK_TAR:
                new TaskFileTar(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_ZIP:
                new TaskFileZip(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_JAR:
                new TaskFileJar(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_UNTAR:
                new TaskFileUntar(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_UNZIP:
                new TaskFileUnzip(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_UNJAR:
                new TaskFileUnjar(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_REPLACE:
                new TaskFileReplace(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_COPY:
                new TaskFileCopy(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_MKDIR:
                new TaskFileMkdir(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            case TaskUtil.TASK_EXEC:
                new TaskExec(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_SQL:
                new TaskSql(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_MERGE_ROPERTIES:
                new TaskMergeProperties(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_EMAIL://Not Supported Yet
                new TaskTestEMail(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_SOCKET:
                new TaskTestSocket(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_REST:
                new TaskTestREST(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_JDBC:
                new TaskTestJDBC(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_GROOVYRANGE:
                new TaskTestGroovyRange(propman).setReporter(reportMapList).start(propertyPrefix)
                break
            case TaskUtil.TASK_PORT:
                new TaskTestPort(propman).setReporter(reportMapList).start(propertyPrefix)
                break

            default :
                break
        }
    }

}
