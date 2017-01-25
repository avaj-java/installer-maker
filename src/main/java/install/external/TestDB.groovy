package install.external

import groovy.sql.Sql

/**
 * Created by sujkim on 2017-01-25.
 */
class TestDB {

    TestDB(){}

    boolean run(def exProp){
        List list
        Sql sql

        // START
        String id = exProp['-id']
        String pw = exProp['-pw']
        String ip = exProp['-ip'] ?: "127.0.0.1"
        String port = exProp['-port'] ?: "1521"
        String db = exProp['-db'] ?: "orcl"
        String url = exProp['-url'] ?: "jdbc:oracle:thin:@${ip}:${port}:${db}"
        String driver = exProp['-driver'] ?: "oracle.jdbc.driver.OracleDriver"
        String query = exProp['-query'] ?: "select * from META_OBJECT where OBJECT_ID = 1"
        println '\n=================================================='
        println ' - START CHECK DB -'
        println '=================================================='
        //SYSTEM CHECK
        String javaVersion = System.getProperty("java.version");
        println "0. System Info - CHECK"
        println "   JAVA VERSION: ${javaVersion}"
        println ""
        // CHECK INFO
        println "1. Your Input Info - CHECK"
        println "   DRIVER: ${driver}"
        println "   URL: ${url}"
        println "   ID: ${id}"
        println "   PW: ${pw}"
        println "   QUERY: ${query} \n"

        try{
            // CONNECT
            print '2. Connect to DB - '
            sql = Sql.newInstance(url, id, pw, driver)
            println 'OK\n'

            // RUN SQL
            print '3. Run SQL - '
            list = sql.rows(query)
            println 'OK\n'

        }catch(Exception e){
            e.printStackTrace()
            throw e
        }finally{
            //DISCONNECT
            print '4. Disconnect from DB - '
            sql.close()
            println 'OK\n'
        }

        // CHECK DATA
        println "5. Returned Data(Count:${list.size()}) - CHECK"
        list.each{ println it }

        // FINISH
        println '\n=================================================='
        println ' - FINISHED CHECK DB -'
        println '==================================================\n'
    }
}
