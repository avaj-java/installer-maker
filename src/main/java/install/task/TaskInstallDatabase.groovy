package install.task

import install.bean.InstallMode
import com.jaemisseo.man.FileMan
import com.jaemisseo.man.util.FileSetup
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.ReportMan
import com.jaemisseo.man.SqlMan

/**
 * Created by sujkim on 2017-02-17.
 */
class TaskInstallDatabase {

    PropMan propman
    SqlMan sqlman
    InstallMode mode



    TaskInstallDatabase(){}

    TaskInstallDatabase(SqlMan sqlman, PropMan propman) {
        this.sqlman = sqlman
        this.propman = propman
        this.mode = new InstallMode().merge(new InstallMode(
            modeGenerateReportText     : propman.get('report.text'),
            modeGenerateReportExcel    : propman.get('report.excel'),
            modeGenerateReportSql      : propman.get('report.sql'),
            modeExcludeExecuteSql      : propman.get('x.execute.sql'),
            modeExcludeCheckBefore     : propman.get('x.check.before'),
            modeExcludeReport          : propman.get('x.report'),
            modeExcludeReportConsole   : propman.get('x.report.console'),
            reportFileEncoding         : propman.get('report.file.encoding'),
            reportFileLineBreak        : propman.get('report.file.linebreak'),
            reportFileLastLineBreak    : propman.get('report.file.last.linebreak'),
        ))
        mode.each{
            println "${it}  :   ${mode[it]}"
        }
    }

    /**
     * RUN
     */
    void run(){

        List analysisReportList = []
        List resultReportMapList = []
        FileSetup fileSetup = generateFileSetup()

        //Execute All SQL
        eachLevel{ List<String> filePathList, Map dataSourceMap, Map replacementMap ->

            filePathList.each{ String filePath ->
                String originFileName = new File(filePath).getName()

                //1. Log Start
                logTitle(filePath)

                //2. Generate Query Replaced With New Object Name
                sqlman.init().queryFromFile("${filePath}").command([SqlMan.ALL]).replace(replacementMap)

                //3. Report Checking Before
                if (!mode.modeExcludeCheckBefore){
                    sqlman.checkBefore(dataSourceMap)
                    if (!mode.modeExcludeReport) {
                        if (!mode.modeExcludeReportConsole)
                            sqlman.reportAnalysis()
                        if (mode.modeGenerateReportText || mode.modeGenerateReportExcel)
                            analysisReportList.addAll(sqlman.getAnalysisResultList())
                    }
                }

                //- Generate SQL File
                if (mode.modeGenerateReportSql){
                    new FileMan().createNewFile('./', "replaced_${originFileName}", sqlman.getReplacedQueryList(), fileSetup)
                }

                //4. Execute
                if (!mode.modeExcludeExecuteSql){
                    sqlman.run(dataSourceMap)

                    //5. Report Result Reoprt
                    if (!mode.modeExcludeReport){
                        if (!mode.modeExcludeReportConsole)
                            sqlman.reportResult()
                        if (mode.modeGenerateReportText || mode.modeGenerateReportExcel)
                            resultReportMapList.add(sqlman.getResultReportMap())
                    }
                }

            }

        }


        //6. Generate File Report
        String fileNamePrefix
        String date = new Date().format('yyyyMMdd_HHmmss')
        if (analysisReportList){
            fileNamePrefix = 'report_analysis'
            if (mode.modeGenerateReportText) {
                List stringList = sqlman.getAnalysisStringResultList(analysisReportList)
                new FileMan().createNewFile('./', "${fileNamePrefix}_${date}.txt", stringList, fileSetup)
            }
            if (mode.modeGenerateReportExcel)
                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", analysisReportList, fileSetup)
        }

        if (resultReportMapList){
            fileNamePrefix = 'report_result'
            if (mode.modeGenerateReportText){
                List stringList = sqlman.getResultList(resultReportMapList)
                new FileMan().createNewFile('./', "${fileNamePrefix}_${date}.txt", stringList, fileSetup)
            }
            if (mode.modeGenerateReportExcel)
                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", resultReportMapList, fileSetup)
        }


    }



    boolean eachLevel(Closure closure){
        // 1. Default Setup(Global)
        //- DataSource & Replacement
        setDefaultOption()
        // 2. Execute level by level
        getInstallLevelList().each{ String levelName ->
            //2-1) - Get DataSource By Level(Local)
            Map dataSourceMap = getDataSourceMapByLevelName(levelName)
            //2-2) - Get Replacement By Level(Local)
            Map replacementMap = getReplacementMapByLevelName(levelName)
            //2-3) - Get Files (.sql) By Level(Local)
            List<String> filePathList = getFilePathListByLevelName(levelName)
            //2-4) ! Execute Some Code
            closure(filePathList, dataSourceMap, replacementMap)
        }
        return true
    }



    private void logTitle(String title){
        println ""
        println ""
        println ""
        println "///////////////////////////////////////////////////////////////////////////"
        println "///// ${title}"
        println "///////////////////////////////////////////////////////////////////////////"
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
                replace             : propman.parse("sql.file.replace"),
                replaceTable        : propman.parse("sql.file.replace.table"),
                replaceIndex        : propman.parse("sql.file.replace.index"),
                replaceSequence     : propman.parse("sql.file.replace.sequence"),
                replaceView         : propman.parse("sql.file.replace.view"),
                replaceFunction     : propman.parse("sql.file.replace.function"),
                replaceTablespace   : propman.parse("sql.file.replace.tablespace"),
                replaceUser         : propman.parse("sql.file.replace.user"),
                replaceDatafile     : propman.parse("sql.file.replace.datafile"),
                replacePassword     : propman.parse("sql.file.replace.password")
        ])
    }

    private List<String> getInstallLevelList(){
        List<String> installLevels = propman.get('install.level').split("\\s*,\\s*").collect{ it.trim() }
        return installLevels
    }

    private Map getDataSourceMapByLevelName(String levelName){
        return [
                vendor      : propman.get("install.level.${levelName}.sql.vendor"),
                ip          : propman.get("install.level.${levelName}.sql.ip"),
                port        : propman.get("install.level.${levelName}.sql.port"),
                db          : propman.get("install.level.${levelName}.sql.db"),
                user        : propman.get("install.level.${levelName}.sql.user"),
                password    : propman.get("install.level.${levelName}.sql.password")
        ]
    }

    private Map getReplacementMapByLevelName(String levelName){
        return [
                replace             : propman.parse("install.level.${levelName}.sql.file.replace"),
                replaceTable        : propman.parse("install.level.${levelName}.sql.file.replace.table"),
                replaceIndex        : propman.parse("install.level.${levelName}.sql.file.replace.index"),
                replaceSequence     : propman.parse("install.level.${levelName}.sql.file.replace.sequence"),
                replaceView         : propman.parse("install.level.${levelName}.sql.file.replace.view"),
                replaceFunction     : propman.parse("install.level.${levelName}.sql.file.replace.function"),
                replaceUser         : propman.parse("install.level.${levelName}.sql.file.replace.user"),
                replacePassword     : propman.parse("install.level.${levelName}.sql.file.replace.password"),
                replaceTablespace   : propman.parse("install.level.${levelName}.sql.file.replace.tablespace"),
                replaceDatafile     : propman.parse("install.level.${levelName}.sql.file.replace.datafile")
        ]
    }

    private FileSetup generateFileSetup(){
        FileSetup fileSetup = new FileSetup()
        if (mode.reportFileEncoding)
            fileSetup.encoding = mode.reportFileEncoding
        if (mode.reportFileLineBreak)
            fileSetup.lineBreak = mode.reportFileLineBreak
        if (mode.reportFileLastLineBreak)
            fileSetup.lastLineBreak = mode.reportFileLastLineBreak
        return fileSetup
    }

    private List<String> getFilePathListByLevelName(String levelName){
        //4-2) Get Files (.sql)
        String fileDirectory = propman.get("install.level.${levelName}.sql.file.directory")
        String fileName = propman.get("install.level.${levelName}.sql.file.name")
        List<String> fileList = getFilePathList(fileDirectory, fileName, 'sql')
        return fileList
    }

    private def getFilePathList(String fileDirectory, String fileName, String extension){
        def filePathList = []
        // check files
        if (fileName){
            filePathList = fileName.split(",").collect{ return it.trim() }
            if (filePathList.size() == 1)
                filePathList = filePathList[0].split(" ").collect{ return it.trim() }
            filePathList = filePathList.collect{ return new File("${fileDirectory}/${it}").path }
        }else{
            new File(fileDirectory).listFiles().each{ File file ->
                filePathList << file.path
            }
        }
        // check extension
        if (extension){
            filePathList = filePathList.findAll{
                int lastDotIdx = it.lastIndexOf('.')
                String itExtension = it.substring(lastDotIdx+1).toUpperCase()
                String acceptExtension = extension.toUpperCase()
                return ( itExtension.equals(acceptExtension) )
            }
        }
        return filePathList
    }

}
