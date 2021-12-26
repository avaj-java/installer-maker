package com.jaemisseo.hoya.task


import groovy.sql.Sql
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import jaemisseo.man.util.ConnectionGenerator
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-01-25.
 */
@Task
@TerminalValueProtocol(['vendor', 'ip', 'port', 'db', 'user', 'password'])
class TestJDBC extends TaskHelper{

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

    @Value('try.second')
    Integer second

    @Value('mode.progress.bar')
    Boolean modeProgressBar

    @Override
    Integer run(){
        Map previewMap
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

        /** LOG **/
        logger.debug '<REQUEST> - CHECK'
        logInfo(previewMap)

        /** RUN **/
        logger.debug "<JDBC>"
        Long startDate = new Date().getTime()

        if (!second){
            try{
                list = runSql(connGen, previewMap.query)
            }catch(e){
                logger.error "Could not connect"
                throw e
            }

        }else{
            Integer elapsedSecond = Util.whileWithTimeProgressBar(second, 30, (modeProgressBar==null)?true:modeProgressBar){
                try{
                    list = runSql(connGen, previewMap.query, false)
                    return true
                }catch(e){
                    sleep(700)
                }
            }
            if (list != null){
                logger.info "Connected in ${elapsedSecond} seconds."
            }else{
                throw new Exception("The connection failed within ${elapsedSecond} seconds.")
            }
        }

        /** LOG RETURN DATA **/
        logger.debug "<RETURNED DATA(Count:${list.size()})> - CHECK"
        list.each{
            logger.debug it.toString()
        }

        /** FINISH **/
        logMiddleTitle 'FINISHED TESTJDBC'
        return STATUS_TASK_DONE
    }



    List runSql(ConnectionGenerator connGen, String query){
        runSql(connGen, query, true)
    }

    List runSql(ConnectionGenerator connGen, String query, boolean modeLog){
        Sql sql
        List resultList
        try{
            if (modeLog) logger.info 'Connect to DB'
            sql = new Sql(connGen.generate())
            if (modeLog) logger.info 'OK\n'

            if (modeLog) logger.info 'Run SQL'
            resultList = sql.rows(query)
            if (modeLog) logger.info 'OK\n'

        }catch(Exception e){
            if (modeLog) logger.error '!!! FAILED\n'
            throw e

        }finally{
            if (modeLog) logger.info 'Disconnect from DB'
            if (sql){
                sql.close()
                if (modeLog) logger.info 'OK\n'
            }else{
                if (modeLog) logger.error "!! Tester is not connected !!\n"
            }
        }
        return resultList
    }

    void logInfo(Map previewMap){
        logger.debug " - DRIVER  : ${previewMap.driver}"
        logger.debug " - URL     : ${previewMap.url}"
        logger.debug " - USER    : ${previewMap.user}"
        logger.debug " - PASSWORD: ${previewMap.password}"
        logger.debug " - QUERY   : ${previewMap.query} \n"
    }

}
