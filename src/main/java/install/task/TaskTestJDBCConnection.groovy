package install.task

import groovy.sql.Sql
import com.jaemisseo.man.util.ConnectionGenerator

/**
 * Created by sujkim on 2017-01-25.
 */
class TaskTestJDBCConnection implements InstallerExternalTask{

    TaskTestJDBCConnection(){}

    @Override
    void run(Map prop){
        ConnectionGenerator connGen
        Map previewMap
        Sql sql
        List list

        //START
        connGen = new ConnectionGenerator(prop)
        previewMap = connGen.generateDataSourceMap()
        String query = prop['-query'] ?: "select * from META_OBJECT where OBJECT_ID = 1"

        //START LOG
        logTitle('START CHECK DB')

        //SYSTEM CHECK
        println "0. System Info - CHECK"
        println "   JAVA VERSION: ${System.getProperty("java.version")}"
        println ""

        //CHECK INFO
        println "1. Your Input Info - CHECK"
        println "   DRIVER: ${previewMap.driver}"
        println "   URL: ${previewMap.url}"
        println "   ID: ${previewMap.id}"
        println "   PW: ${previewMap.pw}"
        println "   QUERY: ${query} \n"

        try{
            //CONNECT
            print '2. Connect to DB - '
            sql = new Sql(connGen.generate())
            println 'OK\n'

            //RUN SQL
            print '3. Run SQL - '
            list = sql.rows(query)
            println 'OK\n'

        }catch(Exception e){
            println '!!! FAILED\n'
            e.printStackTrace()
            throw e
        }finally{
            //DISCONNECT
            print '4. Disconnect from DB - '
            if (sql){
                sql.close()
                println 'OK\n'
            }else{
                println '!!! Tester is not connected\n'
            }
        }

        // CHECK DATA
        println "5. Returned Data(Count:${list.size()}) - CHECK"
        list.each{ println it }

        //FINISH LOG
        logTitle('FINISHED CHECK DB')
    }

    void logTitle(String title){
        println '\n=================================================='
        println " - ${title} -"
        println '=================================================='
    }



}
