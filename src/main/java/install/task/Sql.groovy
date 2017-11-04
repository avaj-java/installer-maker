package install.task

import ch.qos.logback.classic.Level
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import jaemisseo.man.FileMan
import jaemisseo.man.SqlAnalMan
import jaemisseo.man.SqlMan
import install.bean.SqlSetup
import install.bean.ReportSetup
import install.bean.ReportSql

import java.sql.SQLException

/**
 * Created by sujkim on 2017-02-17.
 */
@Document("""
    It can run sql file with JDBC 
""")
@Task
@TerminalValueProtocol(['file'])
class Sql extends TaskUtil{

    @Value(name='file', filter='getFilePathList', required=true)
    List<String> filePathList

    @Value
    SqlSetup sqlSetup

    @Value
    ReportSetup reportSetup

    List sqlObjectListList = []



    @Override
    Integer run(){

        //1. Default Setup
        sqlman = new SqlMan()
        sqlObjectListList = []

        // -Mode No Progress Bar
        if ([Level.INFO, Level.WARN, Level.ERROR, Level.OFF].contains(config.logGen.getConsoleLogLevel()))
            sqlSetup.modeSqlProgressBar = false

        //2. Execute All SQL
        filePathList.each{ String filePath ->

            String originFileName = new File(filePath).getName()

            //2. Generate Query Replaced With New Object Name
            sqlman.init()
                .queryFromFile(filePath)
                .command([SqlMan.ALL])
                .replace(sqlSetup)

            //3. Report Checking Before
            if (sqlSetup.modeSqlCheckBefore){
                try {
                    sqlman.checkBefore(sqlSetup)

                }catch(e){
                    println "<ERROR> Checking Before Execution"
                    throw new SQLException('Error, Checking Before Execution.')
                }
            }

            //- Generate SQL File
            if (sqlSetup.modeSqlFileGenerate){
                println "Creating SQL File..."
                FileMan.write("./replaced_${originFileName}", sqlman.getReplacedQueryList(), reportSetup.fileSetup)
            }

            //4. Execute
            if (sqlSetup.modeSqlExecute){
                sqlman.run(sqlSetup)
            }

            sqlObjectListList << sqlman.getAnalysisResultList()

        }

        return STATUS_TASK_DONE
    }

    @Override
    void reportWithConsole(ReportSetup reportSetup, List reportMapList){
        sqlman.reportResult()
    }

    @Override
    void reportWithText(ReportSetup reportSetup, List reportMapList){

    }

    @Override
    void reportWithExcel(ReportSetup reportSetup, List reportMapList){
//        Map resultMap = sqlman.getResultReportMap()
        sqlObjectListList.each{ List<SqlAnalMan.SqlObject> sqlObjectList ->

            sqlObjectList.each{ SqlAnalMan.SqlObject sqlObj ->
                reportMapList.add(new ReportSql(
                        sqlFileName: sqlObj.sqlFileName,
                        seq: sqlObj.seq,
                        query: sqlObj.query,
//                    isExistOnDB     : sqlObj.isExistOnDB?'Y':'N',
                        isOk: (sqlObj.isOk == null) ? '' : (sqlObj.isOk) ? 'Complete' : 'Failed',
                        warnningMessage: sqlObj.warnningMessage,
                        error: sqlObj.error?.toString(),
                ))
            }

        }
    }



}
