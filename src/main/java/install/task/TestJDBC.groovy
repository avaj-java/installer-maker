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
        logMiddleTitle 'START TESTJDBC'
        println '<REQUEST> - CHECK'
        logInfo(previewMap)

        //RUN
        println "<JDBC>"
        try{
            printStep '1. Connect to DB'
            sql = new Sql(connGen.generate())
            printOk()

            printStep '2. Run SQL'
            list = sql.rows(previewMap.query)
            printOk()

        }catch(Exception e){
            printFailed()
            e.printStackTrace()
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
        println "<RETURNED DATA(Count:${list.size()})> - CHECK"
        list.each{ println it }

        //FINISH
        logMiddleTitle 'FINISHED TESTJDBC'
        return STATUS_TASK_DONE
    }



    void printStep(String title){
        print "${title} - "
    }

    void printError(String errorMessage){
        println "!! ${errorMessage} !!\n"
    }

    void printOk(){
        println 'OK\n'
    }

    void printFailed(){
        println '!!! FAILED\n'
    }

    void logInfo(Map previewMap){
        println " - DRIVER  : ${previewMap.driver}"
        println " - URL     : ${previewMap.url}"
        println " - USER    : ${previewMap.user}"
        println " - PASSWORD: ${previewMap.password}"
        println " - QUERY   : ${previewMap.query} \n"
    }

}
