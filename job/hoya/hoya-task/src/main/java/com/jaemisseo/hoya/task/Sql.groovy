package com.jaemisseo.hoya.task

import ch.qos.logback.classic.Level
import jaemisseo.man.configuration.annotation.type.Document
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import jaemisseo.man.FileMan
import jaemisseo.man.SqlAnalMan
import jaemisseo.man.SqlMan
import com.jaemisseo.hoya.bean.SqlSetup
import com.jaemisseo.hoya.bean.ReportSetup
import com.jaemisseo.hoya.bean.ReportSql

import java.sql.SQLException

/**
 * Created by sujkim on 2017-02-17.
 */
@Document("""
    It can run sql file with JDBC 
""")
@Task
@TerminalValueProtocol(['file'])
class Sql extends TaskHelper{

    @Value(name='file', filter='getFilePathList', required=true)
    List<String> filePathList

    @Value
    SqlSetup sqlSetup

    @Value
    ReportSetup reportSetup

    List<List<SqlAnalMan.SqlObject>> sqlObjectListList = []


    @Override
    Integer run(){

        //1. Default Setup
        SqlMan sqlman = new SqlMan()
        sqlObjectListList = []

        // -Mode No Progress Bar
        if ([Level.WARN, Level.ERROR, Level.OFF].contains(config.logGen.getConsoleLogLevel()))
            sqlSetup.modeProgressBar = false

        //2. Execute All SQL
        filePathList.each{ String filePath ->
            try{
                String originFileName = new File(filePath).getName()
                logger.info(" <<< SQL: ${originFileName} >>> ")

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
                        throw new SQLException('Error, Checking Before Execution.', e)
                    }
                }

                //- Generate SQL File
                if (sqlSetup.modeSqlFileGenerate){
                    logger.info("Creating SQL File...")
                    FileMan.write("./replaced_${originFileName}", sqlman.getReplacedQueryList(), reportSetup.fileSetup)
                }

                //4. Execute
                if (sqlSetup.modeSqlExecute){
                    sqlman.run(sqlSetup)
                }

            }catch(e){
                throw e
            }finally{
                //Add Report
                sqlObjectListList << sqlman.getAnalysisResultList()
                //Report to console
                sqlman.reportResult()
                logger.info("")
            }

        }

        return STATUS_TASK_DONE
    }

    @Override
    void reportWithConsole(ReportSetup reportSetup, List reportMapList){
        
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
                        isOk: (sqlObj.isOk == null) ? '' : (sqlObj.isOk) ? 'Complete' : 'Failed',
                        query: sqlObj.query,
                        commandType: sqlObj.commandType,
                        objectType: sqlObj.objectType,
                        schemeName: sqlObj.schemeName,
                        objectName: sqlObj.objectName,
                        warnningMessage: sqlObj.warnningMessage,
                        error: sqlObj.error?.toString(),
                ))
            }

        }
    }



}
