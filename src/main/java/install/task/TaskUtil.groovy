package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.QuestionMan
import com.jaemisseo.man.SqlMan
import com.jaemisseo.man.VariableMan
import com.jaemisseo.man.util.FileSetup
import com.jaemisseo.man.util.SqlSetup
import com.jaemisseo.man.util.QuestionSetup

/**
 * Created by sujkim on 2017-03-11.
 */
class TaskUtil {

    public static final String JOB_BUILDER = "BUILDER"
    public static final String JOB_RECEPTIONIST = "RECEPTIONIST"
    public static final String JOB_INSTALLER = "INSTALLER"
    public static final String MACGYVER = "MACGYVER"

    public static final String TASK_SQL = "SQL"
    public static final String TASK_TAR = "TAR"
    public static final String TASK_ZIP = "ZIP"
    public static final String TASK_UNTAR = "UNTAR"
    public static final String TASK_UNZIP = "UNZIP"
    public static final String TASK_UNJAR = "UNJAR"
    public static final String TASK_MKDIR = "MKDIR"
    public static final String TASK_COPY = "COPY"
    public static final String TASK_REPLACE = "REPLACE"

    public static final String TASK_NOTICE = "NOTICE"
    public static final String TASK_Q = "Q"
    public static final String TASK_Q_CHOICE = "Q_CHOICE"
    public static final String TASK_Q_YN = "Q_YN"

    public static final String TASK_JDBC = "JDBC"
    public static final String TASK_REST = "REST"
    public static final String TASK_SOCKET = "SOCKET"
    public static final String TASK_EMAIL = "EMAIL"
    public static final String TASK_PORT = "PORT"
    public static final String TASK_MERGE_ROPERTIES = "MERGE_PROPERTIES"

    PropMan propman
    VariableMan varman
    SqlMan sqlman
    FileMan fileman
    QuestionMan qman

    String levelNamesProperty = ''
    List validTaskList = []
    List invalidTaskList = []
    def gOpt

    List beforeReportList = []
    List afterReportMapList = []
    List rememberAnswerLineList = []



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

    void run(){
        run('')
    }

    void run(String propertyPrefix){
        println "It is Empty Task. Implement This method."
    }

    void runTask(String taskName){
        runTask(taskName, '')
    }

    void runTask(String taskName, String propertyPrefix){
        //Check Valid Task
        if ( !taskName || (validTaskList && !validTaskList.contains(taskName)) || (invalidTaskList && invalidTaskList.contains(taskName)) ){
            throw new Exception(" 'Sorry, This is Not my task, [${taskName}]. I Can Not do this.' ")
            return
        }

        //Run Task
        switch (taskName){
            case TASK_NOTICE:
                new TaskWelcome(propman).run(propertyPrefix)
                break
            case TASK_Q:
                new TaskQuestion(propman, rememberAnswerLineList).run(propertyPrefix)
                break
            case TASK_Q_CHOICE:
                new TaskQuestionChoice(propman, rememberAnswerLineList).run(propertyPrefix)
                break
            case TASK_Q_YN:
                new TaskQuestionYN(propman, rememberAnswerLineList).run(propertyPrefix)
                break

            case TASK_TAR://Not Supported Yet
                new TaskFileTar(propman).run(propertyPrefix)
                break
            case TASK_ZIP:
                new TaskFileZip(propman).run(propertyPrefix)
                break
            case TASK_UNTAR:
                new TaskFileUntar(propman).run(propertyPrefix)
                break
            case TASK_UNZIP:
                new TaskFileUnzip(propman).run(propertyPrefix)
                break
            case TASK_UNJAR:
                new TaskFileUnjar(propman).run(propertyPrefix)
                break
            case TASK_REPLACE:
                new TaskFileReplace(propman).run(propertyPrefix)
                break
            case TASK_COPY:
                new TaskFileCopy(propman).run(propertyPrefix)
                break
            case TASK_MKDIR:
                new TaskFileMkdir(propman).run(propertyPrefix)
                break

            case TASK_SQL:
                new TaskSql(sqlman, propman, gOpt).setBeforeReporter(beforeReportList).setAfterReporter(afterReportMapList).run(propertyPrefix)
                break
            case TASK_MERGE_ROPERTIES:
                new TaskQuestion(propman).run(propertyPrefix)
                break

            case TASK_EMAIL://Not Supported Yet
                new TaskTestEMail(propman).run(propertyPrefix)
                break
            case TASK_SOCKET:
                new TaskTestSocket(propman).run(propertyPrefix)
                break
            case TASK_REST:
                new TaskTestREST(propman).run(propertyPrefix)
                break
            case TASK_JDBC:
                new TaskTestJDBC(propman).run(propertyPrefix)
                break
            case TASK_PORT:
                new TaskTestPort(propman).run(propertyPrefix)
                break

            default :
                break
        }
    }



    protected void logBigTitle(String title){
        println ""
        println ""
        println ""
        println "///////////////////////////////////////////////////////////////////////////"
        println "///// ${title}"
        println "///////////////////////////////////////////////////////////////////////////"
    }

    protected void logMiddleTitle(String title){
        println '\n=================================================='
        println " - ${title} -"
        println '=================================================='
    }

    //Do level by level
    protected void eachLevel(String levelNamesProperty, Closure closure){
        getLevelList(levelNamesProperty).each{ String levelName ->
            closure(levelName)
        }
    }

    protected List<String> getLevelList(String levelNamesProperty){
        List<String> resultList = []
        List<String> list = propman.get(levelNamesProperty).split("\\s*,\\s*")
        list.each{ String levelName ->
            if (levelName.contains('-')){
                resultList += getListWithDashRange(levelName as String)
            }else{
                resultList << levelName
            }
        }
        return resultList
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

    protected def getValue(String propertyName){
        return getValue("", propertyName)
    }

    protected def getValue(String propertyPrefix, String propertyName){
        return propman.get("${propertyPrefix}${propertyName}") ?: ''
    }

    protected String getString(String propertyName){
        return getString("", propertyName)
    }

    protected String getString(String propertyPrefix, String propertyName){
        return propman.get("${propertyPrefix}${propertyName}") ?: ''
    }

    protected List<String> getFilePathList(String propertyPrefix, String propertyName){
        return getFilePathList(propertyPrefix, propertyName, '')
    }

    protected List<String> getFilePathList(String propertyPrefix, String propertyName, String extention){
        String filePath = propman.get("${propertyPrefix}${propertyName}")
        return FileMan.getFilePathList(filePath, extention)
    }

    protected String getFilePath(String propertyPrefix, String propertyName){
        String filePath = propman.get("${propertyPrefix}${propertyName}")
        return FileMan.getFullPath(filePath)
    }

    protected Map getMap(String propertyName){
        return getMap("", propertyName)
    }

    protected Map getMap(String propertyPrefix, String propertyName){
        Map map = propman.parse("${propertyPrefix}${propertyName}")
        return map
    }

    protected FileSetup genFileSetup(){
        return genFileSetup('')
    }

    protected FileSetup genFileSetup(String propertyPrefix){
        FileSetup defaultOpt = new FileSetup()
        FileSetup globalOpt = defaultOpt.clone().merge(new FileSetup(
                modeAutoLineBreak   : propman.get("mode.auto.linebreak"),
                modeAutoBackup      : propman.get("mode.auto.backup"),
                modeAutoMkdir       : propman.get("mode.auto.mkdir"),
                encoding            : propman.get("file.encoding"),
                backupPath          : propman.get("file.backup.path"),
                lineBreak           : propman.get("file.linebreak"),
                lastLineBreak       : propman.get("file.last.linebreak")
        ))
        FileSetup localOpt = globalOpt.clone().merge(new FileSetup(
                modeAutoLineBreak   : propman.get("${propertyPrefix}mode.auto.linebreak"),
                modeAutoBackup      : propman.get("${propertyPrefix}mode.auto.backup"),
                modeAutoMkdir       : propman.get("${propertyPrefix}mode.auto.mkdir"),
                encoding            : propman.get("${propertyPrefix}file.encoding"),
                backupPath          : propman.get("${propertyPrefix}file.backup.path"),
                lineBreak           : propman.get("${propertyPrefix}file.linebreak"),
                lastLineBreak       : propman.get("${propertyPrefix}file.last.linebreak")
        ))
        return localOpt
    }

    protected SqlSetup genSqlSetup(){
        return genSqlSetup('')
    }

    protected SqlSetup genSqlSetup(String propertyPrefix){
        SqlSetup defaultOpt = new SqlSetup()
        SqlSetup globalOpt = defaultOpt.clone().merge(new SqlSetup(
                //-DataSource
                vendor      : propman.get("sql.vendor"),
                ip          : propman.get("sql.ip"),
                port        : propman.get("sql.port"),
                db          : propman.get("sql.db"),
                user        : propman.get("sql.user"),
                password    : propman.get("sql.password"),
                //-Replacement
                replace             : propman.parse("sql.replace"),
                replaceTable        : propman.parse("sql.replace.table"),
                replaceIndex        : propman.parse("sql.replace.index"),
                replaceSequence     : propman.parse("sql.replace.sequence"),
                replaceView         : propman.parse("sql.replace.view"),
                replaceFunction     : propman.parse("sql.replace.function"),
                replaceTablespace   : propman.parse("sql.replace.tablespace"),
                replaceUser         : propman.parse("sql.replace.user"),
                replaceDatafile     : propman.parse("sql.replace.datafile"),
                replacePassword     : propman.parse("sql.replace.password")
        ))
        SqlSetup localOpt = globalOpt.clone().merge(new SqlSetup(
                //-DataSource
                vendor      : propman.get("${propertyPrefix}sql.vendor"),
                ip          : propman.get("${propertyPrefix}sql.ip"),
                port        : propman.get("${propertyPrefix}sql.port"),
                db          : propman.get("${propertyPrefix}sql.db"),
                user        : propman.get("${propertyPrefix}sql.user"),
                password    : propman.get("${propertyPrefix}sql.password"),
                driver      : propman.get("${propertyPrefix}sql.driver"),
                url         : propman.get("${propertyPrefix}sql.url"),
                //-Replacement
                replace             : propman.parse("${propertyPrefix}sql.replace"),
                replaceTable        : propman.parse("${propertyPrefix}sql.replace.table"),
                replaceIndex        : propman.parse("${propertyPrefix}sql.replace.index"),
                replaceSequence     : propman.parse("${propertyPrefix}sql.replace.sequence"),
                replaceView         : propman.parse("${propertyPrefix}sql.replace.view"),
                replaceFunction     : propman.parse("${propertyPrefix}sql.replace.function"),
                replaceTablespace   : propman.parse("${propertyPrefix}sql.replace.tablespace"),
                replaceUser         : propman.parse("${propertyPrefix}sql.replace.user"),
                replaceDatafile     : propman.parse("${propertyPrefix}sql.replace.datafile"),
                replacePassword     : propman.parse("${propertyPrefix}sql.replace.password")
        ))
        return localOpt
    }

    protected QuestionSetup genQuestionSetup(){
        return genQuestionSetup('')
    }

    protected QuestionSetup genQuestionSetup(String propertyPrefix){
        return new QuestionSetup(
            question            : propman.get("${propertyPrefix}question"),
            recommandAnswer     : propman.get("${propertyPrefix}answer"),
            descriptionMap      : propman.parse("${propertyPrefix}answer.description.map"),
            valueMap            : propman.parse("${propertyPrefix}answer.value.map"),
            validation          : propman.parse("${propertyPrefix}answer.validation"),
        )
    }

}
