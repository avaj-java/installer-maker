package install.external

import groovy.sql.Sql

/**
 * Created by sujkim on 2017-01-25.
 */
class TestDB implements ExternalFunction{

    TestDB(){}

    @Override
    boolean run(Map exProp){
        List list
        Sql sql

        // START
        String vendor = exProp['-vendor']?:'oracle'
        String id = exProp['-id']
        String pw = exProp['-pw']
        String ip = exProp['-ip'] ?: "127.0.0.1"
        String port = exProp['-port'] ?: "1521"
        String db = exProp['-db'] ?: "orcl"
        String url = exProp['-url'] ?: "${getURLProtocol(vendor)}@${ip}:${port}:${db}"
        String driver = exProp['-driver'] ?: getDriverName(vendor)
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

        // FINISH
        println '\n=================================================='
        println ' - FINISHED CHECK DB -'
        println '==================================================\n'
    }





    /**
     * Get Driver Name
     * @param vendor
     * @return
     */
    String getDriverName(String vendor){
        vendor = (vendor) ?: 'oracle'
        String driver = ''
        //Get By Vendor
        if (vendor.equals('oracle')) driver = 'oracle.jdbc.driver.OracleDriver'
        else if (vendor.equals('tibero')) driver = 'com.tmax.tibero.jdbc.TbDriver'
        return driver
    }

    /**
     * Get URLProtocol
     * @param vendor
     * @return
     */
    String getURLProtocol(String vendor){
        vendor = (vendor) ?: 'oracle'
        String URLProtocol = ''
        //Get By Vendor
        if (vendor.equals('oracle')) URLProtocol = 'jdbc:oracle:thin:'
        else if (vendor.equals('tibero')) URLProtocol = 'jdbc:tibero:thin:'
        return URLProtocol
    }
}
