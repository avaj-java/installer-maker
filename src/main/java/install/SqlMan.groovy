package install

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
    def results = []
    def report


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
    SqlMan connect(def t){
        // DataSource
        String vendor = t.vendor ? t.vendor : dataSource.vendor
        String ip = t.ip ? t.ip : dataSource.ip
        String port = t.port ? t.port : dataSource.port
        String db = t.db ? t.db : dataSource.db
        String user = t.user ? t.user : dataSource.user
        String password = t.password ? t.password : dataSource.password
        String url = (t.url) ? t.url : getUrl(vendor, ip, port, db)
        url = (url) ? url : dataSource.url
        String driver = (t.driver) ? t.driver : getDriver(vendor)
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
    SqlMan option(def t){
        def replace = t.replace ? t.replace : option.replace
        def replaceTable = t.replaceTable ? t.replaceTable : option.replaceTable
        def replaceIndex = t.replaceIndex ? t.replaceIndex : option.replaceIndex
        def replaceView = t.replaceView ? t.replaceView : option.replaceView
        def replaceSequence = t.replaceSequence ? t.replaceSequence : option.replaceSequence
        def replaceTablespace = t.replaceTablespace ? t.replaceTablespace : option.replaceTablespace
        def replaceUser = t.replaceUser ? t.replaceUser : option.replaceUser
        def replaceDatafile = t.replaceDatafile ? t.replaceDatafile : option.replaceDatafile
        def replacePassword = t.replacePassword ? t.replacePassword : option.replacePassword
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
        this.results = []
        this.report = [:]
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
        results = getAnalysisResult(m)
        return this
    }

    SqlMan checkBefore(def tempDataSource){
        def existObjectList
        def existTablespaceList
        def existUserList
        def resultsForTablespace = results.findAll{ it.commandType.equalsIgnoreCase("CREATE") && it.objectType.equalsIgnoreCase("TABLESPACE") }
        def resultsForUser = results.findAll{ it.commandType.equalsIgnoreCase("CREATE") && it.objectType.equalsIgnoreCase("USER") }
        // Second Analysis
        try {
            connect(tempDataSource)
            existObjectList = sql.rows("SELECT OBJECT_NAME, OBJECT_TYPE, OWNER AS SCHEME FROM ALL_OBJECTS")
            results.each {
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
        def createTablespaceList = results.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('TABLESPACE') }
        def createUserList = results.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('USER') }
        def createTableList = results.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('TABLE') }
        def createIndexList = results.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('INDEX') }
        def createViewList = results.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('VIEW') }
        def createSequenceList = results.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('SEQUENCE') }
        def createFunctionList = results.findAll { it.commandType.equalsIgnoreCase('CREATE') && it.objectType.equalsIgnoreCase('FUNCTION') }
        def insertList = results.findAll { it.commandType.equalsIgnoreCase('INSERT') }
        def updateList = results.findAll { it.commandType.equalsIgnoreCase('UPDATE') }
//        println "//////////////////////////////////////////////////"
        warningAlreadyExist("create tablespace warning:", createTablespaceList)
        warningAlreadyExist("create user warning:", createUserList)
        warningAlreadyExist("create table warning:", createTableList)
        warningAlreadyExist("create index warning:", createIndexList)
        warningAlreadyExist("create view warning:", createViewList)
        warningAlreadyExist("create sequence warning:", createSequenceList)
        warningAlreadyExist("create function warning:", createFunctionList)
        warningNotExist("insert warning:", insertList)
        warningNotExist("update warning:", updateList)
        // check before run SQL
//        if (checkType == SqlMan.CHECK_BEFORE_AND_STOP
//        && (createTableWarningCnt || createIndexWarningCnt || createViewWarningCnt || createSequenceWarningCnt || createFunctionWarningCnt || insertWarningCnt || updateWarningCnt)){
        return this
    }

    SqlMan printQuerys(){
        println ""
        println ""
        println "///// QUERYS"
        results.eachWithIndex{ SqlObject sqlObj, int idx ->
            println ""
            println "--${idx+1}"
            println "${sqlObj.query}"
        }
        return this
    }


    SqlMan run() {
        return run([:])
    }
    SqlMan run(def tempDataSource) {
        // SQL
        runSql(tempDataSource, results)

        // create report
        createReport(results)
        return this
    }





    def getAnalysisResult(Matcher m){
        def results = []
        // First Analysis
        m.each { String query ->
            results << getReplacedObject(getAnalysisObject(query), this.connectedOption)
        }
        return results
    }


    void runSql(def tempDataSource, def results){
        connect(tempDataSource)
        sql.withTransaction{
            results.eachWithIndex{ def result, int idx ->
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




    void createReport(def results){
        this.report = [
                database:connectedDataSource,
                pattern:patternToGetQuery,
                matchedCnt:results.size(),
                succeededCnt:results.findAll{ it.isOk }.size(),
                failedCnt:results.findAll{ !it.isOk }.size(),
                summary:getSummary(results),
//                results:results
        ]
    }

    def report(){
        println ""
        println ""
        println "///// REPORT"
        report.each{
            println ""
            println it
        }
        println ""
        println ""
        println ""
        return
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

    def warningAlreadyExist(String msg, def list){
        int existCnt = list.findAll{ it.isExistOnDB }.size()
        if (existCnt){
            println  "[${msg}] Already Exists Object :   ${existCnt} / ${list.size()}"
        }
    }
    def warningNotExist(String msg, def list){
        int notExistCnt = list.findAll{ !it.isExistOnDB }.size()
        if (notExistCnt){
            println  "[${msg}] Not Exsist Object :   ${notExistCnt} / ${list.size()}"
        }
    }


}

