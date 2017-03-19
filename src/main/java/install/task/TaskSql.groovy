package install.task

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.SqlMan
import com.jaemisseo.man.util.SqlSetup
import install.bean.InstallerGlobalOption

/**
 * Created by sujkim on 2017-02-17.
 */
class TaskSql extends TaskUtil{

    TaskSql(SqlMan sqlman, PropMan propman, InstallerGlobalOption gOpt) {
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
    InstallerGlobalOption gOpt

    List beforeReportList
    List afterReportMapList



    /**
     * RUN
     */
    void run(String propertyPrefix){

        //1. Default Setup
        SqlSetup globalSqlSetup = genSqlSetup()
        SqlSetup mergedSqlSetup = genSqlSetup(propertyPrefix)
        sqlman.set(globalSqlSetup)

        List<String> filePathList = getFilePathList(propertyPrefix, 'file.path', 'sql')

        //2. Execute All SQL
        filePathList.each{ String filePath ->
            String originFileName = new File(filePath).getName()

            //2. Generate Query Replaced With New Object Name
            sqlman.init()
                    .queryFromFile("${filePath}")
                    .command([SqlMan.ALL])
                    .replace(mergedSqlSetup)

            //3. Report Checking Before
            if (!gOpt.modeExcludeCheckBefore){
                sqlman.checkBefore(mergedSqlSetup)
                if (!gOpt.modeExcludeReport) {
                    if (!gOpt.modeExcludeReportConsole)
                        sqlman.reportAnalysis()
                    if (gOpt.modeGenerateReportText || gOpt.modeGenerateReportExcel)
                        beforeReportList.addAll(sqlman.getAnalysisResultList())
                }
            }

            //- Generate SQL File
            if (gOpt.modeGenerateReportSql){
                new FileMan().createNewFile('./', "replaced_${originFileName}", sqlman.getReplacedQueryList(), gOpt.reportFileSetup)
            }

            //4. Execute
            if (!gOpt.modeExcludeExecuteSql){
                sqlman.run(mergedSqlSetup)

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


}
