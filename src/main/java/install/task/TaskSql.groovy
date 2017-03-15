package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.SqlMan
import com.jaemisseo.man.util.FileSetup
import install.bean.InstallGlobalOption

/**
 * Created by sujkim on 2017-02-17.
 */
class TaskSql extends TaskUtil{

    TaskSql(SqlMan sqlman, PropMan propman, InstallGlobalOption gOpt) {
        this.sqlman = sqlman
        this.propman = propman
        this.gOpt = gOpt
    }

    TaskSql setBeforeReporter(List beforeReportList){
        this.beforeReportList = beforeReportList
        return this
    }

    TaskSql setAfterReporter(List afterReportMapList){
        this.afterReportMapList = afterReportMapList
        return this
    }

    SqlMan sqlman
    InstallGlobalOption gOpt

    List beforeReportList
    List afterReportMapList



    /**
     * RUN
     */
    void run(String propertyPrefix){

        //Ready For Report
        FileSetup fileSetup = generateFileSetup()

        //1. Default Setup
        setDefaultOption()
        Map dataSourceMap = getDataSourceMapByLevelName(propertyPrefix)
        Map replacementMap = getReplacementMapByLevelName(propertyPrefix)
        List<String> filePathList = getFilePathList(propertyPrefix, 'file.path', 'sql')

        //2. Execute All SQL
        filePathList.each{ String filePath ->
            String originFileName = new File(filePath).getName()

            //2. Generate Query Replaced With New Object Name
            sqlman.init().queryFromFile("${filePath}").command([SqlMan.ALL]).replace(replacementMap)

            //3. Report Checking Before
            if (!gOpt.modeExcludeCheckBefore){
                sqlman.checkBefore(dataSourceMap)
                if (!gOpt.modeExcludeReport) {
                    if (!gOpt.modeExcludeReportConsole)
                        sqlman.reportAnalysis()
                    if (gOpt.modeGenerateReportText || gOpt.modeGenerateReportExcel)
                        beforeReportList.addAll(sqlman.getAnalysisResultList())
                }
            }

            //- Generate SQL File
            if (gOpt.modeGenerateReportSql){
                new FileMan().createNewFile('./', "replaced_${originFileName}", sqlman.getReplacedQueryList(), fileSetup)
            }

            //4. Execute
            if (!gOpt.modeExcludeExecuteSql){
                sqlman.run(dataSourceMap)

                //5. Report Result Reoprt
                if (!gOpt.modeExcludeReport){
                    if (!gOpt.modeExcludeReportConsole)
                        sqlman.reportResult()
                    if (gOpt.modeGenerateReportText || gOpt.modeGenerateReportExcel)
                        afterReportMapList.add(sqlman.getResultReportMap())
                }
            }

        }

    }



    private void setDefaultOption(){
        sqlman.set([
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
        ])
    }

    private Map getDataSourceMapByLevelName(String propertyPrefix){
        return [
                vendor      : propman.get("${propertyPrefix}sql.vendor"),
                ip          : propman.get("${propertyPrefix}sql.ip"),
                port        : propman.get("${propertyPrefix}sql.port"),
                db          : propman.get("${propertyPrefix}sql.db"),
                user        : propman.get("${propertyPrefix}sql.user"),
                password    : propman.get("${propertyPrefix}sql.password")
        ]
    }

    private Map getReplacementMapByLevelName(String propertyPrefix){
        return [
                replace             : propman.parse("${propertyPrefix}sql.replace"),
                replaceTable        : propman.parse("${propertyPrefix}sql.replace.table"),
                replaceIndex        : propman.parse("${propertyPrefix}sql.replace.index"),
                replaceSequence     : propman.parse("${propertyPrefix}sql.replace.sequence"),
                replaceView         : propman.parse("${propertyPrefix}sql.replace.view"),
                replaceFunction     : propman.parse("${propertyPrefix}sql.replace.function"),
                replaceUser         : propman.parse("${propertyPrefix}sql.replace.user"),
                replacePassword     : propman.parse("${propertyPrefix}sql.replace.password"),
                replaceTablespace   : propman.parse("${propertyPrefix}sql.replace.tablespace"),
                replaceDatafile     : propman.parse("${propertyPrefix}sql.replace.datafile")
        ]
    }

    private FileSetup generateFileSetup(){
        FileSetup fileSetup = new FileSetup()
        if (gOpt.reportFileEncoding)
            fileSetup.encoding = gOpt.reportFileEncoding
        if (gOpt.reportFileLineBreak)
            fileSetup.lineBreak = gOpt.reportFileLineBreak
        if (gOpt.reportFileLastLineBreak)
            fileSetup.lastLineBreak = gOpt.reportFileLastLineBreak
        return fileSetup
    }

}
