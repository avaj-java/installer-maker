package com.jaemisseo.man

import groovy.sql.Sql

import java.util.regex.Matcher

/**
 * Created by sujung on 2016-09-24.
 */
class SqlMan extends SqlAnalMan{

    Sql sql
    def dataSource = [:]
    def option = [:]
    def connectedDataSource = [:]
    def connectedOption = [:]

    String sqlContent
    String patternToGetQuery
    List<SqlObject> analysisResultList = []
    Map resultReportMap


    public static final String ORACLE = "ORACLE"
    public static final String TIBERO = "TIBERO"

    public static final int ALL = 0
    public static final int CREATE = 10
    public static final int CREATE_TABLE = 11
    public static final int CREATE_INDEX = 12
    public static final int CREATE_VIEW = 13
    public static final int CREATE_SEQUENCE = 14
    public static final int CREATE_FUNCTION = 15
    public static final int CREATE_TABLESPACE = 16
    public static final int CREATE_USER = 17
    public static final int ALTER = 20
    public static final int ALTER_TABLE = 21
    public static final int ALTER_USER = 22
    public static final int INSERT = 30
    public static final int UPDATE = 40
    public static final int COMMENT = 50
    public static final int GRANT = 60

    public static final int IGNORE_CHECK = 1
    public static final int CHECK_BEFORE_AND_STOP = 2
    public static final int CHECK_RUNTIME_AND_STOP = 3

    SqlMan(){
    }



    List<SqlObject> getAnalysisResultList(){
        return analysisResultList
    }

    Map getResultReportMap(){
        return resultReportMap
    }

    List<String> getReplacedQueryList(){
        return analysisResultList.collect{ it.query }
    }



    List<String> getAnalysisStringResultList(){
        return getAnalysisStringResultList(analysisResultList)
    }

    List<String> getAnalysisStringResultList(List<SqlObject> analysisList){
        List<String> warningList = getWarningList(analysisList)
        List<String> analysisStringList = analysisList.collect{
            """
            Already Exist: ${it.isExistOnDB}        
            ${it.query}
            """
        }
        return (warningList + analysisStringList)
    }

    List<String> getResultList(){
        return getResultList([resultReportMap])
    }

    List<String> getResultList(List<Map> resultReportMapList){
        List<String> resultList = resultReportMapList.collect{
            """
            ${it}
            """
        }
        return resultList
    }



    SqlMan set(def obj){
        this.dataSource.vendor = obj.vendor
        this.dataSource.ip = obj.ip
        this.dataSource.port = obj.port
        this.dataSource.db = obj.db
        this.dataSource.user = obj.user
        this.dataSource.password = obj.password
        this.dataSource.url = (obj.url) ? obj.url : getUrl(obj.vendor, obj.ip, obj.port, obj.db)
        this.dataSource.driver = (obj.driver) ? obj.driver : getDriver(obj.vendor)
        this.option.replace = obj.replace
        this.option.replaceTable = obj.replaceTable
        this.option.replaceIndex = obj.replaceIndex
        this.option.replaceSequence = obj.replaceSequence
        this.option.replaceView = obj.replaceView
        this.option.replaceFunction = obj.replaceFunction
        this.option.replaceTablespace = obj.replaceTablespace
        this.option.replaceUser = obj.replaceUser
        this.option.replaceDatafile = obj.replaceDatafile
        this.option.replacePassword = obj.replacePassword
        return this
    }
    SqlMan connect(){
        close()
        return connect(this.dataSource)
    }
    SqlMan connect(Map dataSourceMap){
        Map l = dataSourceMap
        // DataSource
        String vendor = l.vendor ? l.vendor : dataSource.vendor
        String ip = l.ip ? l.ip : dataSource.ip
        String port = l.port ? l.port : dataSource.port
        String db = l.db ? l.db : dataSource.db
        String user = l.user ? l.user : dataSource.user
        String password = l.password ? l.password : dataSource.password
        String url = (l.url) ? l.url : getUrl(vendor, ip, port, db)
        url = (url) ? url : dataSource.url
        String driver = (l.driver) ? l.driver : getDriver(vendor)
        driver = (driver) ? driver : dataSource.driver
        this.sql = Sql.newInstance(url, user, password, driver)
        this.connectedDataSource = [
            user:user,
            password:password,
            url:url,
            driver:driver
        ]
        return this
    }
    SqlMan option(){
        return option(this.option)
    }
    SqlMan option(Map replacementMap){
        Map l = replacementMap
        def replace = l.replace ? l.replace : option.replace
        def replaceTable = l.replaceTable ? l.replaceTable : option.replaceTable
        def replaceIndex = l.replaceIndex ? l.replaceIndex : option.replaceIndex
        def replaceView = l.replaceView ? l.replaceView : option.replaceView
        def replaceSequence = l.replaceSequence ? l.replaceSequence : option.replaceSequence
        def replaceTablespace = l.replaceTablespace ? l.replaceTablespace : option.replaceTablespace
        def replaceUser = l.replaceUser ? l.replaceUser : option.replaceUser
        def replaceDatafile = l.replaceDatafile ? l.replaceDatafile : option.replaceDatafile
        def replacePassword = l.replacePassword ? l.replacePassword : option.replacePassword
        this.connectedOption = [
            replace :replace,
            replaceTable : replaceTable,
            replaceIndex :replaceIndex,
            replaceView :replaceView,
            replaceSequence :replaceSequence,
            replaceTablespace :replaceTablespace,
            replaceUser :replaceUser,
            replaceDatafile :replaceDatafile,
            replacePassword :replacePassword
        ]
        return this
    }

    SqlMan close(){
        if (sql) sql.close()
        return this
    }


    SqlMan init(){
        this.sqlContent = ''
        this.patternToGetQuery = ''
        this.analysisResultList = []
        this.resultReportMap = [:]
        return this
    }

    SqlMan query(String query){
        sqlContent = query
        return this
    }

    SqlMan queryFromFile(String url){
        return query(new File(url).text)
    }

    SqlMan command(def targetList){
        this.patternToGetQuery = getSqlPattern(targetList)
        return this
    }

    SqlMan replace(def tempOption){
        option(tempOption)
        // analysis
        Matcher m = getMatchedList(this.sqlContent, this.patternToGetQuery)
        analysisResultList = getAnalysisResultList(m)
        return this
    }

    SqlMan checkBefore(Map dataSourceMap){
        def existObjectList
        def existTablespaceList
        def existUserList
        def resultsForTablespace = analysisResultList.findAll{ it.commandType.equalsIgnoreCase("CREATE") && it.objectType.equalsIgnoreCase("TABLESPACE") }
        def resultsForUser = analysisResultList.findAll{ it.commandType.equalsIgnoreCase("CREATE") && it.objectType.equalsIgnoreCase("USER") }
        // Second Analysis
        try {
            connect(dataSourceMap)
            existObjectList = sql.rows("SELECT OBJECT_NAME, OBJECT_TYPE, OWNER AS SCHEME FROM ALL_OBJECTS")
            analysisResultList.each {
                it.isExistOnDB = isExistOnSchemeOnDB(it, existObjectList)
            }
            if (resultsForTablespace) {
                existTablespaceList = sql.rows("SELECT TABLESPACE_NAME AS OBJECT_NAME, 'TABLESPACE' AS OBJECT_TYPE FROM USER_TABLESPACES")
                resultsForTablespace.each {
                    it.isExistOnDB = isExistOnDB(it, existTablespaceList)
                }
            }
            if (resultsForUser){
                existUserList = sql.rows("SELECT USERNAME AS OBJECT_NAME, 'USER' AS OBJECT_TYPE FROM ALL_USERS")
                resultsForUser.each {
                    it.isExistOnDB = isExistOnDB(it, existUserList)
                }
            }
        }catch(Exception){
        }finally{
            close()
        }
        return this
    }




    SqlMan run() {
        return run([:])
    }
    SqlMan run(Map dataSourceMap) {
        // SQL
        runSql(dataSourceMap, analysisResultList)

        // create report
        createReport(analysisResultList)
        return this
    }





    def getAnalysisResultList(Matcher m){
        def results = []
        // First Analysis
        m.each { String query ->
            results << getReplacedObject(getAnalysisObject(query), this.connectedOption)
        }
        return results
    }


    void runSql(Map dataSourceMap, List<SqlObject> analysisResultList){
        connect(dataSourceMap)
        sql.withTransaction{
            analysisResultList.eachWithIndex{ SqlObject result, int idx ->
                try{
                    String query = result.query
                    sql.execute(removeLastSemicoln(removeLastSlash(query)))
                    result.isOk = true
                }catch(Exception e){
                    result.isOk = false
                    result.error = e
                }
            }
        }
        close()
    }




    void createReport(List<SqlObject> results){
        this.resultReportMap = [
                database    :connectedDataSource,
                pattern     :patternToGetQuery,
                matchedCnt  :results.size(),
                succeededCnt:results.findAll{ it.isOk }.size(),
                failedCnt   :results.findAll{ !it.isOk }.size(),
                summary     :getSummary(results),
//                analysisResultList:analysisResultList
        ]
    }



    /**
     * Report 'Before Check' With Console
     */
    SqlMan reportAnalysis(){
        List<String> warningList = getWarningList()
        warningList.each{
            println it
        }
        reportGeneratedQuerys()
        return this
    }

    SqlMan reportGeneratedQuerys(){
        println ""
        println ""
        println "///// QUERYS"
        analysisResultList.eachWithIndex{ SqlObject sqlObj, int idx ->
            println ""
            println "--${idx+1}"
            println "${sqlObj.query}"
        }
        return this
    }

    /**
     * Report 'SQL Result' With Console
     */
    SqlMan reportResult(){
        println ""
        println ""
        println "///// REPORT"
        resultReportMap.each{
            println ""
            println it
        }
        println ""
        println ""
        println ""
        return this
    }






    String getUrl(String vendor, String ip, String port, String db){
        String url
        switch (vendor.toUpperCase()) {
            case SqlMan.ORACLE:
                url = "jdbc:oracle:thin:@${ip}:${port}:${db}"
                break
            default:
                url = "jdbc:oracle:thin:@${ip}:${port}:${db}"
                break
        }
        return url
    }

    String getDriver(String vendor){
        String driver
        switch (vendor.toUpperCase()) {
            case SqlMan.ORACLE:
                driver = "oracle.jdbc.driver.OracleDriver"
                break
            default:
                driver = "oracle.jdbc.driver.OracleDriver"
                break
        }
        return driver
    }





    def getSummary(def results){
        def tableList = results.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("TABLE") }
        def indexList = results.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("INDEX") }
        def viewList = results.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("VIEW") }
        def sequenceList = results.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("SEQUENCE") }
        def functionList = results.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("FUNCTION") }
        def tablespaceList = results.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("TABLESPACE") }
        def userList = results.findAll{ it.commandType.equalsIgnoreCase('CREATE') && it.objectType .equalsIgnoreCase("USER") }
        def commentList = results.findAll{ it.commandType.equalsIgnoreCase('COMMENT') }
        def grantList = results.findAll{ it.commandType.equalsIgnoreCase('GRANT') }
        def insertList = results.findAll{ it.commandType.equalsIgnoreCase('INSERT') }
        def updateList = results.findAll{ it.commandType.equalsIgnoreCase('UPDATE') }
        def summary = [
                table:[
                        all: tableList.size(),
                        o: tableList.findAll{ it.isOk }.size(),
                        x: tableList.findAll{ !it.isOk }.size()
                ],
                index:[
                        all: indexList.size(),
                        o: indexList.findAll{ it.isOk }.size(),
                        x: indexList.findAll{ !it.isOk }.size()

                ],
                view:[
                        all: viewList.size(),
                        o: viewList.findAll{ it.isOk }.size(),
                        x: viewList.findAll{ !it.isOk }.size()

                ],
                sequence:[
                        all: sequenceList.size(),
                        o: sequenceList.findAll{ it.isOk }.size(),
                        x: sequenceList.findAll{ !it.isOk }.size()
                ],
                function:[
                        all: functionList.size(),
                        o: functionList.findAll{ it.isOk }.size(),
                        x: functionList.findAll{ !it.isOk }.size()
                ],
                tablespace:[
                        all: tablespaceList.size(),
                        o: tablespaceList.findAll{ it.isOk }.size(),
                        x: tablespaceList.findAll{ !it.isOk }.size()
                ],
                user:[
                        all: userList.size(),
                        o: userList.findAll{ it.isOk }.size(),
                        x: userList.findAll{ !it.isOk }.size()
                ],
                comment:[
                        all: commentList.size(),
                        o: commentList.findAll{ it.isOk }.size(),
                        x: commentList.findAll{ !it.isOk }.size()
                ],
                grant:[
                        all: grantList.size(),
                        o: grantList.findAll{ it.isOk }.size(),
                        x: grantList.findAll{ !it.isOk }.size()
                ],
                insert:[
                        all: insertList.size(),
                        o: insertList.findAll{ it.isOk }.size(),
                        x: insertList.findAll{ !it.isOk }.size()
                ],
                update:[
                        all: updateList.size(),
                        o: updateList.findAll{ it.isOk }.size(),
                        x: updateList.findAll{ !it.isOk }.size()
                ]
        ]
        return summary
    }





    boolean isExistOnDB(def result, def objectList){
        boolean isExist
        def equalList = objectList.findAll{ Map<String, String> row ->
            return row["OBJECT_NAME"].equalsIgnoreCase(result.objectName) && row["OBJECT_TYPE"].equalsIgnoreCase(result.objectType)
        }
        isExist = (equalList) ? true : false
        return isExist
    }

    boolean isExistOnSchemeOnDB(def result, def objectList){
        boolean isExist
        String objectName = result.objectName
        int idx = objectName.indexOf(".")
        objectName = (idx == -1) ? objectName : objectName.substring(idx+1)
        def equalList = objectList.findAll{ Map<String, String> row ->
            return row["OBJECT_NAME"].equalsIgnoreCase(objectName) && row["OBJECT_TYPE"].equalsIgnoreCase(result.objectType) && row["SCHEME"].equalsIgnoreCase(result.schemeName)
        }
        isExist = (equalList) ? true : false
        return isExist
    }

    List<String> getWarningAlreadyExist(String msg, List<SqlObject> list){
        List<String> result = []
        int existCnt = list.findAll{ it.isExistOnDB }.size()
        if (existCnt)
            result << "[${msg}] Already Exists Object :   ${existCnt} / ${list.size()}"
        return result
    }
    List<String> getWarningNotExist(String msg, List<SqlObject> list){
        List<String> result = []
        int notExistCnt = list.findAll{ !it.isExistOnDB }.size()
        if (notExistCnt)
            result << "[${msg}] Not Exsist Object :   ${notExistCnt} / ${list.size()}"
        return result
    }

    List<String> getWarningList(){
        return getWarningList(analysisResultList)
    }

    List<String> getWarningList(List<SqlObject> analysisResultList){
        List<String> warningList = []
        def createTablespaceList    = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('TABLESPACE') }
        def createUserList          = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('USER') }
        def createTableList         = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('TABLE') }
        def createIndexList         = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('INDEX') }
        def createViewList          = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('VIEW') }
        def createSequenceList      = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('SEQUENCE') }
        def createFunctionList      = analysisResultList.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('FUNCTION') }
        def insertList              = analysisResultList.findAll { it.commandType.equalsIgnoreCase('INSERT') }
        def updateList              = analysisResultList.findAll { it.commandType.equalsIgnoreCase('UPDATE') }
        warningList += getWarningAlreadyExist("create tablespace warning:", createTablespaceList)
        warningList += getWarningAlreadyExist("create user warning:", createUserList)
        warningList += getWarningAlreadyExist("create table warning:", createTableList)
        warningList += getWarningAlreadyExist("create index warning:", createIndexList)
        warningList += getWarningAlreadyExist("create view warning:", createViewList)
        warningList += getWarningAlreadyExist("create sequence warning:", createSequenceList)
        warningList += getWarningAlreadyExist("create function warning:", createFunctionList)
        warningList += getWarningNotExist("insert warning:", insertList)
        warningList += getWarningNotExist("update warning:", updateList)
        return warningList
        // check before run SQL
//        if (checkType == SqlMan.CHECK_BEFORE_AND_STOP
//        && (createTableWarningCnt || createIndexWarningCnt || createViewWarningCnt || createSequenceWarningCnt || createFunctionWarningCnt || insertWarningCnt || updateWarningCnt)){
    }


}

