package install.task

import groovy.sql.Sql
import install.annotation.Task
import install.annotation.Value
import install.util.TaskUtil
import jaemisseo.man.util.ConnectionGenerator

/**
 * Created by sujkim on 2017-01-25.
 */
@Task
class TestJDBC extends TaskUtil{

    @Value('query')
    String query



    @Override
    Integer run(){
        //READY
        ConnectionGenerator connGen = new ConnectionGenerator(propman.properties)
        Map previewMap = connGen.generateDataSourceMap()
        previewMap.query = query ?: "select 'Try To Check Your Query' as TEST from dual"
        Sql sql
        List list

        //DO TEST
        logMiddleTitle 'START CHECK DB'
        println '<DATASOURCE AND SELECT QUERY> - CHECK'
        logInfo(previewMap)

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

        println "<RETURNED DATA(Count:${list.size()})> - CHECK"
        list.each{ println it }

        logMiddleTitle 'FINISHED CHECK DB'

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
        println " - ID      : ${previewMap.id}"
        println " - PW      : ${previewMap.pw}"
        println " - QUERY   : ${previewMap.query} \n"
    }

}
