package install

import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.QuestionMan
import jaemisseo.man.SqlMan
import jaemisseo.man.VariableMan
import jaemisseo.man.util.FileSetup
import jaemisseo.man.util.SqlSetup
import jaemisseo.man.util.QuestionSetup
import install.bean.ReportSetup
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-03-11.
 */
class TaskUtil{

    public static final Integer STATUS_NOTHING = 0
    public static final Integer STATUS_TASK_DONE = 1
    public static final Integer STATUS_TASK_RUN_FAILED = 2
    public static final Integer STATUS_UNDO_QUESTION = 3
    public static final Integer STATUS_REDO_QUESTION = 4



    PropMan propman
    VariableMan varman
    SqlMan sqlman
    FileMan fileman
    QuestionMan qman

    String packageNameForTask = 'install.task'
    Integer status
    String propertyPrefix = ''
    List reportMapList = []
    List rememberAnswerLineList = []
    String undoSign = '<'
    String redoSign = '>'

    TaskUtil setPropman(PropMan propman){
        this.propman = propman
        return this
    }

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
    Integer start(String propertyPrefix){
        status = STATUS_NOTHING
        this.propertyPrefix = propertyPrefix

        if ( !checkCondition(propertyPrefix) )
            return

        descript(propertyPrefix)

        try{
            status = run()

        }catch(e){
            throw e
        }finally{
            if (status != STATUS_UNDO_QUESTION)
                report(propertyPrefix)
        }

        return status
    }

    /**
     * 2. RUN
     */
    Integer run(){
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

    List<String> buildForm(String propertyPrefix){
        //TODO: Override And Implement
        // To Build 'Question User Response Form'
        return []
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



    protected Object newTaskInstance(String taskName){
        return Util.newInstance("${packageNameForTask}.${taskName}")
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

    boolean checkUndoQuestion(String yourAnswer){
        //'Please Show Preview Question'
        return yourAnswer.equals(undoSign)
    }

    boolean checkRedoQuestion(String yourAnswer){
        //'Please Show Preview Question'
        return yourAnswer.equals(redoSign)
    }



    protected void set(String propertyName, def value){
        propman.set("${propertyPrefix}${propertyName}", value)
    }

    protected def get(String propertyName){
        return propman.get("${propertyPrefix}${propertyName}") ?: propman.get(propertyName)
    }

    protected def parse(String propertyName){
        return propman.parse("${propertyPrefix}${propertyName}")  ?: propman.parse(propertyName)
    }

    protected String getString(String propertyName){
        return propman.getString("${propertyPrefix}${propertyName}") ?: propman.getString(propertyName) ?: ''
    }

    protected Boolean getBoolean(String propertyName){
        return propman.getBoolean("${propertyPrefix}${propertyName}") ?: propman.getBoolean(propertyName)
    }

    protected List<String> getFilePathList(String propertyName){
        return getFilePathList(propertyName, '')
    }

    protected List<String> getFilePathList(String propertyName, String extention){
        String filePath = get(propertyName)
        return FileMan.getSubFilePathList(filePath, extention)
    }

    protected String getFilePath(String propertyName){
        String filePath = get(propertyName)
        return FileMan.getFullPath(filePath)
    }

    protected Map getMap(String propertyName){
        Map map = parse(propertyName)
        return map
    }

    protected void setPropValue(){
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

    protected boolean checkCondition(String propertyPrefix){
        def conditionIfObj = propman.parse("${propertyPrefix}if")
        boolean isTrue = propman.match(conditionIfObj)
        return isTrue
    }

    protected void descript(String propertyPrefix){
        String descriptionString = propman.get("${propertyPrefix}desc")
        descriptionString = descriptionString ?: propertyPrefix
        if (descriptionString)
            logBigTitle(descriptionString)
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

    protected FileSetup genMergedFileSetup(){
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

    protected SqlSetup genMergedSqlSetup(){
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
            answer              : propman.getString("${propertyPrefix}answer"),
            recommandAnswer     : propman.getString("${propertyPrefix}answer.default"),
            modeOnlyInteractive : propman.getBoolean("${propertyPrefix}mode.only.interactive"),
            repeatLimit         : propman.getInteger("${propertyPrefix}answer.repeat.limit"),
            validation          : propman.parse("${propertyPrefix}answer.validation"),
            descriptionMap      : propman.parse("${propertyPrefix}answer.description.map"),
            valueMap            : propman.parse("${propertyPrefix}answer.value.map"),
        )
    }

    protected QuestionSetup genGlobalQuestionSetup(){
        QuestionSetup defaultOpt = new QuestionSetup()
        QuestionSetup globalOpt = genQuestionSetup('')
        return defaultOpt.merge(globalOpt)
    }

    protected QuestionSetup genMergedQuestionSetup(){
        QuestionSetup defaultOpt = new QuestionSetup()
        QuestionSetup globalOpt = genQuestionSetup('')
        QuestionSetup localOpt = genQuestionSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt).merge(localOpt)
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

    protected ReportSetup genMergedReportSetup(){
        ReportSetup defaultOpt = new ReportSetup()
        ReportSetup globalOpt = genReportSetup('')
        ReportSetup localOpt = genReportSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt).merge(localOpt)
    }

}
