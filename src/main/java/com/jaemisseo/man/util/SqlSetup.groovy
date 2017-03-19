package com.jaemisseo.man.util

/**
 * Created by sujkim on 2017-03-17.
 */
class SqlSetup extends Option{

    //-DataSource
    String vendor
    String ip
    String port
    String db
    String user
    String password
    String url
    String driver

    //-Replacement
    def replace
    def replaceTable
    def replaceIndex
    def replaceSequence
    def replaceView
    def replaceFunction
    def replaceTablespace
    def replaceUser
    def replaceDatafile
    def replacePassword

}
