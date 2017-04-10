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
class TaskUtil{

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
    public static final String TASK_SET = "SET"

    public static final String TASK_GROOVYRANGE = "GROOVYRANGE"
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

    List reportMapList = []
    List rememberAnswerLineList = []


    TaskUtil setReporter(List reportMapList){
        this.reportMapList = reportMapList
        return this
    }

    TaskUtil setRememberAnswerLineList(List rememberAnswerLineList){
        this.rememberAnswerLineList = rememberAnswerLineList
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



    /**
     * 1. START
     */
    void start(String propertyPrefix){

        if ( !isTrueCondition(propertyPrefix) )
            return

        descript(propertyPrefix)

        try{
            run(propertyPrefix)
        }catch(e){
            throw e
        }finally{
            report(propertyPrefix)
        }

    }

    /**
     * 2. RUN
     */
    void run(String propertyPrefix){
        //TODO: Override And Implement
        println "It is Empty Task. Implement This method."
    }

    /**
     * 3. REPORT
     */
    void report(String propertyPrefix){
        ReportSetup reportSetup = genMergedReportSetup()
        if (reportSetup.modeReport){

            if (reportSetup.modeReportConsole)
                reportWithConsole(reportSetup, reportMapList)

            if (reportSetup.modeReportText)
                reportWithText(reportSetup, reportMapList)

            if (reportSetup.modeReportExcel)
                reportWithExcel(reportSetup, reportMapList)

        }
    }

    void reportWithConsole(ReportSetup reportSetup, List reportMapList){
        //TODO: Override And Implement
    }

    void reportWithText(ReportSetup reportSetup, List reportMapList){
        //TODO: Override And Implement
    }

    void reportWithExcel(ReportSetup reportSetup, List reportMapList){
        //TODO: Override And Implement
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

    protected void setPropValue(String propertyPrefix){
        //Set Some Property
        def property = propman.parse("${propertyPrefix}property")
        if (property instanceof String){
            def value = propman.get("${propertyPrefix}value")
            propman.set(property, value)
        }else if (property instanceof Map){
            (property as Map).each{ String propName, def propValue ->
                propman.set(propName, propValue)
            }
        }
    }

    protected boolean isTrueCondition(String propertyPrefix){
        def conditionIfObj = propman.parse("${propertyPrefix}if")
        boolean isTrue = propman.match(conditionIfObj)
        return isTrue
    }

    protected void descript(String propertyPrefix){
        String descriptString = propman.get("${propertyPrefix}descript")
        descriptString = descriptString ?: propertyPrefix
        logBigTitle(descriptString)
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
                modeAutoLineBreak   : propman.getBoolean("${propertyPrefix}mode.auto.linebreak"),
                modeAutoBackup      : propman.getBoolean("${propertyPrefix}mode.auto.backup"),
                modeAutoMkdir       : propman.getBoolean("${propertyPrefix}mode.auto.mkdir"),
                modeAutoOverWrite   : propman.getBoolean("${propertyPrefix}mode.auto.overwrite"),
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
                modeSqlExecute                  : propman.getBoolean("${propertyPrefix}mode.sql.execute"),
                modeSqlCheckBefore              : propman.getBoolean("${propertyPrefix}mode.sql.check.before"),
                modeSqlFileGenerate             : propman.getBoolean("${propertyPrefix}mode.sql.file.generate"),
                modeSqlIgnoreErrorExecute       : propman.getBoolean("${propertyPrefix}mode.sql.ignore.error.execute"),
                modeSqlIgnoreErrorCheckBefore   : propman.getBoolean("${propertyPrefix}mode.sql.ignore.error.check.before"),

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
            recommandAnswer     : propman.get("${propertyPrefix}answer.default"),
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
            modeReport         : propman.getBoolean("${propertyPrefix}mode.report"),
            modeReportText     : propman.getBoolean("${propertyPrefix}mode.report.text"),
            modeReportExcel    : propman.getBoolean("${propertyPrefix}mode.report.excel"),
            modeReportConsole  : propman.getBoolean("${propertyPrefix}mode.report.console"),
            fileSetup          : genOtherFileSetup("${propertyPrefix}report."),
       )
    }

    protected ReportSetup genGlobalReportSetup(){
        ReportSetup defaultOpt = new ReportSetup()
        ReportSetup globalOpt = genReportSetup('')
        return defaultOpt.merge(globalOpt)
    }

    protected ReportSetup genMergedReportSetup(String propertyPrefix){
        ReportSetup defaultOpt = new ReportSetup()
        ReportSetup globalOpt = genReportSetup('')
        ReportSetup localOpt = genReportSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt).merge(localOpt)
    }

}
                                                                                                