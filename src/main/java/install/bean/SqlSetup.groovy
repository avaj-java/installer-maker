package install.bean

import install.configuration.annotation.Value
import install.configuration.annotation.type.Bean

/**
 * Created by sujkim on 2017-03-17.
 */
@Bean
class SqlSetup extends jaemisseo.man.bean.SqlSetup {

    //-DataSource
    @Value('sql.vendor')
    String vendor
    @Value('sql.ip')
    String ip
    @Value('sql.port')
    String port
    @Value('sql.db')
    String db
    @Value('sql.user')
    String user
    @Value('sql.password')
    String password
    @Value('sql.url')
    String url
    @Value('sql.driver')
    String driver

    //-Replacement
    @Value(name='sql.replace.all', filter='parse')
    def replaceAll
    @Value(name='sql.replace.table', filter='parse')
    def replaceTable
    @Value(name='sql.replace.index', filter='parse')
    def replaceIndex
    @Value(name='sql.replace.sequence', filter='parse')
    def replaceSequence
    @Value(name='sql.replace.view', filter='parse')
    def replaceView
    @Value(name='sql.replace.function', filter='parse')
    def replaceFunction
    @Value(name='sql.replace.tablespace', filter='parse')
    def replaceTablespace
    @Value(name='sql.replace.user', filter='parse')
    def replaceUser
    @Value(name='sql.replace.datafile', filter='parse')
    def replaceDatafile
    @Value(name='sql.replace.password', filter='parse')
    def replacePassword

    //-Validation
    @Value(name='sql.command.that.object.must.exist', filter='parse')
    List<String> commnadListThatObjectMustExist = ['INSERT', 'UPDATE', 'DELETE', 'DROP']
    @Value(name='sql.command.that.object.must.not.exist', filter='parse')
    List<String> commnadListThatObjectMustNotExist = ['CREATE']

    //-Mode
    @Value('mode.sql.execute')
    Boolean modeSqlExecute
    @Value('mode.sql.check.before')
    Boolean modeSqlCheckBefore
    @Value('mode.sql.file.generate')
    Boolean modeSqlFileGenerate
    @Value('mode.sql.ignore.error.execute')
    Boolean modeSqlIgnoreErrorExecute
    @Value('mode.sql.ignore.error.check.before')
    Boolean modeSqlIgnoreErrorCheckBefore
    @Value('mode.sql.ignore.error.already.exist')
    Boolean modeSqlIgnoreErrorAlreadyExist
    @Value('mode.sql.progress.bar')
    Boolean modeSqlProgressBar = true

}
