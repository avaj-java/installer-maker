package com.jaemisseo.man.util

import groovy.sql.Sql

import java.sql.Connection

/**
 * Created by sujkim on 2017-02-19.
 */
class ConnectionGenerator {

    ConnectionGenerator(){}

    ConnectionGenerator(Map<String, String> map){
        setDatasource(map)
    }

    static final String ORACLE = "oracle"
    static final String TIBERO = "tibero"
    String vendor, id, pw, ip, port, db, url, driver



    ConnectionGenerator init() {
        vendor = null; id = null; pw = null; ip = null; port = null; db = null; url = null; driver = null;
        return this
    }

    ConnectionGenerator setDatasource(Map map) {
        vendor  = map['vendor']
        id      = map['id']
        pw      = map['pw']
        ip      = map['ip']
        port    = map['port']
        db      = map['db']
        url     = map['url']
        driver  = map['driver']
        return this
    }

    Map<String, String> generateDataSourceMap() {
        return [
                vendor: vendor ?: 'oracle',
                id    : id,
                pw    : pw,
                ip    : ip ?: "127.0.0.1",
                port  : port ?: "1521",
                db    : db ?: "orcl",
                url   : url ?: "${getURLProtocol(vendor)}@${ip}:${port}:${db}",
                driver: driver ?: getDriverName(vendor)
        ]
    }

    Connection generate(Map map) {
        setDatasource(map)
        return generate()
    }

    Connection generate() {
        Sql sql
        Map<String, String> m = generateDataSourceMap()
        sql = Sql.newInstance(m.url, m.id, m.pw, m.driver)
        return sql.getConnection()
    }

    String getDriverName(String vendor) {
        vendor = (vendor) ?: 'oracle'
        String driver = ''
        //Get By Vendor
        if (vendor.equals('oracle')) driver = 'oracle.jdbc.driver.OracleDriver'
        else if (vendor.equals('tibero')) driver = 'com.tmax.tibero.jdbc.TbDriver'
        return driver
    }

    String getURLProtocol(String vendor) {
        vendor = (vendor) ?: 'oracle'
        String URLProtocol = ''
        //Get By Vendor
        if (vendor.equals('oracle')) URLProtocol = 'jdbc:oracle:thin:'
        else if (vendor.equals('tibero')) URLProtocol = 'jdbc:tibero:thin:'
        return URLProtocol
    }

}
