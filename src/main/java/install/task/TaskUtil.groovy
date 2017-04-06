package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.QuestionMan
import com.jaemisseo.man.SqlMan
import com.jaemisseo.man.VariableMan
import com.jaemisseo.man.util.FileSetup
import com.jaemisseo.man.util.SqlSetup
import com.jaemisseo.man.util.QuestionSetup
import install.bean.ReportSetup

/**
 * Created by sujkim on 2017-03-11.
 */
class TaskUtil {

    public static final String EMP_MACGYVER = "MACGYVER"

    public static final String JOB_BUILDER = "BUILDER"
    public static final String JOB_RECEPTIONIST = "RECEPTIONIST"
    public static final String JOB_INSTALLER = "INSTALLER"

    public static final String TASK_TAR = "TAR"
    public static final String TASK_ZIP = "ZIP"
    public static final String TASK_JAR = "JAR"
    public static final String TASK_UNTAR = "UNTAR"
    public static final String TASK_UNZIP = "UNZIP"
    public static final String TASK_UNJAR = "UNJAR"
    public static final String TASK_MKDIR = "MKDIR"
    public static final String TASK_COPY = "COPY"
    public static final String TASK_REPLACE = "REPLACE"
    public static final String TASK_SQL = "SQL"
    public static final String TASK_EXEC = "EXEC"

    public static final String TASK_NOTICE = "NOTICE"
    public static final String TASK_Q = "Q"
    public static final String TASK_Q_CHOICE = "Q-CHOICE"
    public static final String TASK_Q_YN = "Q-YN"

    public static final String TASK_JDBC = "JDBC"
    public static final String TASK_REST = "REST"
    public static final String TASK_SOCKET = "SOCKET"
    public static final String TASK_EMAIL = "EMAIL"
    public static final String TASK_PORT = "PORT"
    public static final String TASK_MERGE_ROPERTIES = "MERGE-PROPERTIES"

    PropMan propman
    VariableMan varman
    SqlMan sqlman
    FileMan fileman
    QuestionMan qman

    String levelNamesProperty = ''
    List validTaskList = []
    List invalidTaskList = []
    def gOpt

    List reportMapList = []
    List rememberAnswerLineList = []


    TaskUtil setReporter(List reportMapList){
        this.reportMapList = reportMapList
        return this
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
                new TaskNotice(propman).run(propertyPrefix)
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

            case TASK_TAR:
                new TaskFileTar(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_ZIP:
                new TaskFileZip(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_JAR:
                new TaskFileJar(propman).setReporter(reportMapList).run(propertyPrefix)
                break

            case TASK_UNTAR:
                new TaskFileUntar(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_UNZIP:
                new TaskFileUnzip(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_UNJAR:
                new TaskFileUnjar(propman).setReporter(reportMapList).run(propertyPrefix)
                break

            case TASK_REPLACE:
                new TaskFileReplace(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_COPY:
                new TaskFileCopy(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_MKDIR:
                new TaskFileMkdir(propman).setReporter(reportMapList).run(propertyPrefix)
                break

            case TASK_EXEC:
                new TaskExec(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_SQL:
                new TaskSql(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_MERGE_ROPERTIES:
                new TaskMergeProperties(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_EMAIL://Not Supported Yet
                new TaskTestEMail(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_SOCKET:
                new TaskTestSocket(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_REST:
                new TaskTestREST(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_JDBC:
                new TaskTestJDBC(propman).setReporter(reportMapList).run(propertyPrefix)
                break
            case TASK_PORT:
                new TaskTestPort(propman).setReporter(reportMapList).run(propertyPrefix)
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

    protected String getFilePath(String propertyName){
        return getFilePath('', propertyName)
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

    /**
     * FileSetup
     */
    protected FileSetup genFileSetup(String propertyPrefix){
        return new FileSetup(
                encoding            : propman.get("${propertyPrefix}file.encoding"),
                backupPath          : propman.get("${propertyPrefix}file.backup.path"),
                lineBreak           : propman.get("${propertyPrefix}file.linebreak"),
                lastLineBreak       : propman.get("${propertyPrefix}file.last.linebreak"),
                modeAutoLineBreak   : propman.get("${propertyPrefix}mode.auto.linebreak"),
                modeAutoBackup      : propman.get("${propertyPrefix}mode.auto.backup"),
                modeAutoMkdir       : propman.get("${propertyPrefix}mode.auto.mkdir"),
                modeAutoOverWrite   : propman.get("${propertyPrefix}mode.auto.overwrite"),
        )
    }

    protected FileSetup genGlobalFileSetup(){
        FileSetup defaultOpt = new FileSetup()
        FileSetup globalOpt = genFileSetup('')
        return defaultOpt.merge(globalOpt)
    }

    protected FileSetup genMergedFileSetup(String propertyPrefix){
        FileSetup defaultOpt = new FileSetup()
        FileSetup globalOpt = genFileSetup('')
        FileSetup localOpt = genFileSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt).merge(localOpt)
    }

    protected FileSetup genOtherFileSetup(String propertyPrefix){
        FileSetup defaultOpt = new FileSetup()
        FileSetup globalOpt = genFileSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt)
    }

    /**
     * SqlSetup
     */
    protected SqlSetup genSqlSetup(String propertyPrefix){
        return new SqlSetup(
                //-DataSource
                vendor      : propman.get("${propertyPrefix}sql.vendor"),
                ip          : propman.get("${propertyPrefix}sql.ip"),
                port        : propman.get("${propertyPrefix}sql.port"),
                db          : propman.get("${propertyPrefix}sql.db"),
                user        : propman.get("${propertyPrefix}sql.user"),
                password    : propman.get("${propertyPrefix}sql.password"),
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
                replacePassword     : propman.parse("${propertyPrefix}sql.replace.password"),
                modeSqlExecute                  : propman.get("${propertyPrefix}mode.sql.execute"),
                modeSqlCheckBefore              : propman.get("${propertyPrefix}mode.sql.check.before"),
                modeSqlFileGenerate             : propman.get("${propertyPrefix}mode.sql.file.generate"),
                modeSqlIgnoreErrorExecute       : propman.get("${propertyPrefix}mode.sql.ignore.error.execute"),
                modeSqlIgnoreErrorCheckBefore   : propman.get("${propertyPrefix}mode.sql.ignore.error.check.before"),

        )
    }

    protected SqlSetup genGlobalSqlSetup(){
        SqlSetup defaultOpt = new SqlSetup()
        SqlSetup globalOpt = genSqlSetup('')
        return defaultOpt.merge(globalOpt)
    }

    protected SqlSetup genMergedSqlSetup(String propertyPrefix){
        SqlSetup defaultOpt = new SqlSetup()
        SqlSetup globalOpt = genSqlSetup('')
        SqlSetup localOpt = genSqlSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt).merge(localOpt)
    }

    protected SqlSetup genOtherSqlSetup(String propertyPrefix){
        SqlSetup defaultOpt = new SqlSetup()
        SqlSetup globalOpt = genSqlSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt)
    }

    /**
     * QuestionSetup
     */
    protected QuestionSetup genQuestionSetup(String propertyPrefix){
        return new QuestionSetup(
            question            : propman.get("${propertyPrefix}question"),
            recommandAnswer     : propman.get("${propertyPrefix}answer"),
            descriptionMap      : propman.parse("${propertyPrefix}answer.description.map"),
            valueMap            : propman.parse("${propertyPrefix}answer.value.map"),
            validation          : propman.parse("${propertyPrefix}answer.validation"),
        )
    }

    /**
     * ReportSetup
     */
    protected ReportSetup genReportSetup(String propertyPrefix){
       return new ReportSetup(
            modeReport         : propman.get("${propertyPrefix}mode.report"),
            modeReportText     : propman.get("${propertyPrefix}mode.report.text"),
            modeReportExcel    : propman.get("${propertyPrefix}mode.report.excel"),
            modeReportConsole  : propman.get("${propertyPrefix}mode.report.console"),
            fileSetup          : genOtherFileSetup("${propertyPrefix}report."),
       )
    }

    protected ReportSetup genGlobalReportSetup(){
        ReportSetup defaultOpt = new ReportSetup()
        ReportSetup globalOpt = genReportSetup('')
        return defaultOpt.merge(globalOpt)
    }

}
