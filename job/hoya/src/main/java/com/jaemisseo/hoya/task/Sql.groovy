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

    @Value(name='file', filter='getFilePathList')
    List<String> files

    @Value(name='query', filter='getList')
    List<String> queries

    @Value(name='before-query', filter='getList')
    List<String> beforeQueries

    @Value(name='after-query', filter='getList')
    List<String> afterQueries

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
        //- Mode No Progress Bar
        if ([Level.WARN, Level.ERROR, Level.OFF].contains(config.logGen.getConsoleLogLevel()))
            sqlSetup.modeProgressBar = false

        //2. Execute All SQL

        //- Queries from File
        files?.each{ String filePath ->

            //- Check File
            File file = new File(filePath)
            String originFileName = file.getName()
            logger.info(" <<< SQL(file): ${originFileName} >>> ")

            //- Generate Query Replaced With New Object Name
            sqlman.init()
                    .beforeQuery(beforeQueries.toArray(new String[0]))
                    .query(file)
                    .afterQuery(afterQueries.toArray(new String[0]))
                    .command([SqlMan.ALL])
                    .replace(sqlSetup)

            //- Execute
            execute( sqlman, sqlSetup )

        }

        //- Queries
        queries?.each{ String query ->

            //- Check Query
            logger.info(" <<< SQL(query): ${query} >>> ")

            //- Generate Query Replaced With New Object Name
            sqlman.init()
                    .beforeQuery(beforeQueries.toArray(new String[0]))
                    .query(query)
                    .afterQuery(afterQueries.toArray(new String[0]))
                    .command([SqlMan.ALL])
                    .replace(sqlSetup)

            //- Execute
            execute( sqlman, sqlSetup )

        }

        return STATUS_TASK_DONE
    }

    private execute(SqlMan sqlman, SqlSetup sqlSetup){
        try{
            //1. Report Checking Before
            if (sqlSetup.modeSqlCheckBefore){
                try {
                    sqlman.checkBefore(sqlSetup)
                }catch(Exception e){
                    throw new SQLException('Error, Checking Before Execution.', e)
                }
            }

            //2. Generate SQL File
            if (sqlSetup.modeSqlFileGenerate){
                String originFileName = sqlman.sqlFileName ?: new UUID().toString();
                logger.info("Creating SQL File...")
                FileMan.write("./replaced_${originFileName}", sqlman.getReplacedQueryList(), reportSetup.fileSetup)
            }

            //3. Execute
            if (sqlSetup.modeSqlExecute){
                sqlman.run(sqlSetup)
            }

        }catch(e){
            throw e
        }finally{
            //- Add Report
            sqlObjectListList << sqlman.getAnalysisResultList()
            //- Report to console
            sqlman.reportResult()
            logger.info("")
        }
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
                        isOk: (sqlObj.isOk == null) ? "" : (sqlObj.isOk) ? "O" : "X",
                        query: sqlObj.query,
                        executor: sqlObj.executor,
                        commandType: sqlObj.commandType,
                        objectType: sqlObj.objectType,
                        schemeName: sqlObj.schemaName ?: sqlObj.schemaNameForObject,
                        objectName: sqlObj.objectName,
                        warnningMessage: sqlObj.warnningMessage,
                        error: sqlObj.error?.toString(),
                ))
            }

        }
    }



}
