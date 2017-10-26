package install.task

import groovy.sql.Sql
import install.configuration.annotation.type.Task
import install.configuration.annotation.Value
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil
import jaemisseo.man.util.ConnectionGenerator

/**
 * Created by sujkim on 2017-01-25.
 */
@Task
@TerminalValueProtocol(['vendor', 'ip', 'port', 'db', 'user', 'password'])
class TestJDBC extends TaskUtil{

    @Value('user')
    String user

    @Value('password')
    String password

    @Value('db')
    String db

    @Value('ip')
    String ip

    @Value('port')
    String port

    @Value('vendor')
    String vendor

    @Value('url')
    String url

    @Value('driver')
    String driver

    @Value('query')
    String query



    @Override
    Integer run(){
        Map previewMap
        Sql sql
        List list

        //READY
        ConnectionGenerator connGen = new ConnectionGenerator([
            vendor  : vendor,
            user    : user,
            password: password,
            ip      : ip,
            port    : port,
            db      : db,
            url     : url,
            driver  : driver
        ])
        previewMap = connGen.generateDataBaseInfoMap()
        previewMap.query = query ?: "select 'Try To Check Your Query' as TEST from dual"

        //START
        logger.debug '<REQUEST> - CHECK'
        logInfo(previewMap)

        //RUN
        logger.debug "<JDBC>"
        try{
            printStep '1. Connect to DB'
            sql = new Sql(connGen.generate())
            printOk()

            printStep '2. Run SQL'
            list = sql.rows(previewMap.query)
            printOk()

        }catch(Exception e){
            printFailed()
            throw e

        }finally{
            printStep '3. Disconnect from DB'
            if (sql){
                sql.close()
                printOk()
            }else{
                printError 'Tester is not connected'
            }
        }

        //RETURN DATA
        logger.debug "<RETURNED DATA(Count:${list.size()})> - CHECK"
        list.each{ logger.debug it }

        //FINISH
        logMiddleTitle 'FINISHED TESTJDBC'
        return STATUS_TASK_DONE
    }



    void printStep(String title){
        print "${title} - "
    }

    void printError(String errorMessage){
        logger.error "!! ${errorMessage} !!\n"
    }

    void printOk(){
        logger.debug 'OK\n'
    }

    void printFailed(){
        logger.error '!!! FAILED\n'
    }

    void logInfo(Map previewMap){
        logger.debug " - DRIVER  : ${previewMap.driver}"
        logger.debug " - URL     : ${previewMap.url}"
        logger.debug " - USER    : ${previewMap.user}"
        logger.debug " - PASSWORD: ${previewMap.password}"
        logger.debug " - QUERY   : ${previewMap.query} \n"
    }

}
