package install.data

import install.configuration.InstallerLogGenerator
import install.configuration.InstallerPropertiesGenerator
import install.configuration.annotation.type.Data
import install.configuration.annotation.method.Init
import install.configuration.annotation.method.Method
import install.bean.BuilderGlobalOption
import install.bean.InstallerGlobalOption
import install.bean.ReceptionistGlobalOption
import install.bean.ReportSetup
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.util.FileSetup
import jaemisseo.man.util.QuestionSetup
import jaemisseo.man.util.SqlSetup

/**
 * Created by sujkim on 2017-06-20.
 */
@Data
class PropertyProvider {

    PropMan propman
    String propertyPrefix
    InstallerPropertiesGenerator propGen
    InstallerLogGenerator logGen

    @Init
    void init(){
        this.propman = propGen.getDefaultProperties()
    }

    void printVersion(){
        logGen.logVersion(propGen.getDefaultProperties())
    }

    void printSystem(){
        logGen.logSystem(propGen.getDefaultProperties())
    }
    
    void shift(String name){
        this.propman = propGen.get(name)
    }

    void shift(String name, String propertyPrefix){
        this.propman = propGen.get(name)
        this.propertyPrefix = propertyPrefix
    }


    boolean checkCondition(String propertyPrefix){
        def conditionIfObj = propman.parse("${propertyPrefix}if")
        boolean isTrue = propman.match(conditionIfObj)
        return isTrue
    }


    void set(String propertyName, def value){
        propman.set("${propertyPrefix}${propertyName}", value ?: '')
    }

    void setRaw(String propertyName, def value){
        propman.set(propertyName, value ?: '')
    }


    
    /*****
     *
     *****/
    @Method('get')
    def get(String propertyName){
        return propman.get("${propertyPrefix}${propertyName}") ?: propman.get(propertyName)
    }

    @Method('parse')
    def parse(String propertyName){
        return propman.parse("${propertyPrefix}${propertyName}")  ?: propman.parse(propertyName)
    }

    @Method('getString')
    String getString(String propertyName){
        return propman.getString("${propertyPrefix}${propertyName}") ?: propman.getString(propertyName) ?: ''
    }

    @Method('getBoolean')
    Boolean getBoolean(String propertyName){
        return propman.getBoolean("${propertyPrefix}${propertyName}") ?: propman.getBoolean(propertyName)
    }

    @Method('getFilePathList')
    List<String> getFilePathList(String propertyName){
        return getFilePathList(propertyName, '')
    }

    List<String> getFilePathList(String propertyName, String extention){
        String filePath = get(propertyName)
        return FileMan.getSubFilePathList(filePath, extention)
    }

    @Method('getFilePath')
    String getFilePath(String propertyName){
        String filePath = get(propertyName)
        return FileMan.getFullPath(filePath)
    }

    @Method('getMap')
    Map getMap(String propertyName){
        Map map = parse(propertyName)
        return map
    }



    /*************************
     * FileSetup
     *************************/
    FileSetup genFileSetup(String propertyPrefix){
        return new FileSetup(
                encoding            : propman.get("${propertyPrefix}file.encoding"),
                backupPath          : propman.get("${propertyPrefix}file.backup.path"),
                lineBreak           : propman.get("${propertyPrefix}file.linebreak"),
                lastLineBreak       : propman.get("${propertyPrefix}file.last.linebreak"),
                modeAutoBackup      : propman.getBoolean("${propertyPrefix}mode.auto.backup"),
                modeAutoMkdir       : propman.getBoolean("${propertyPrefix}mode.auto.mkdir"),
                modeAutoOverWrite   : propman.getBoolean("${propertyPrefix}mode.auto.overwrite"),
        )
    }

    @Method('genGlobalFileSetup')
    FileSetup genGlobalFileSetup(){
        FileSetup defaultOpt = new FileSetup()
        FileSetup globalOpt = genFileSetup('')
        return defaultOpt.merge(globalOpt)
    }

    @Method('genMergedFileSetup')
    FileSetup genMergedFileSetup(){
        FileSetup defaultOpt = new FileSetup()
        FileSetup globalOpt = genFileSetup('')
        FileSetup localOpt = genFileSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt).merge(localOpt)
    }

    FileSetup genOtherFileSetup(String propertyPrefix){
        FileSetup defaultOpt = new FileSetup()
        FileSetup globalOpt = genFileSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt)
    }

    /*************************
     * SqlSetup
     *************************/
    SqlSetup genSqlSetup(String propertyPrefix){
        return new SqlSetup(
                //-DataSource
                vendor      : propman.get("${propertyPrefix}sql.vendor"),
                ip          : propman.get("${propertyPrefix}sql.ip"),
                port        : propman.get("${propertyPrefix}sql.port"),
                db          : propman.get("${propertyPrefix}sql.db"),
                user        : propman.get("${propertyPrefix}sql.user"),
                password    : propman.get("${propertyPrefix}sql.password"),
                //-CheckBefore
                commnadListThatObjectMustExist: propman.parse("${propertyPrefix}sql.command.that.object.must.exist"),
                commnadListThatObjectMustNotExist: propman.parse("${propertyPrefix}sql.command.that.object.must.not.exist"),
                //-Replacement
                replace             : propman.parse("${propertyPrefix}sql.replace"),
                replaceTable        : propman.parse("${propertyPrefix}sql.replace.table"),
                replaceIndex        : propman.parse("${propertyPrefix}sql.replace.index"),
                replaceSequence     : propman.parse("${propertyPrefix}sql.replace.sequence"),
                replaceView         : propman.parse("${propertyPrefix}sql.replace.view"),
                replaceFunction     : propman.parse("${propertyPrefix}sql.replace.function"),
                replaceTablespace   : propman.parse("${propertyPrefix}sql.replace.tablespace"),
                replaceUser         : propman.parse("${propertyPrefix}sql.replace.user"),
                replaceDatafile     : propman.parse("${propertyPrefix}sql.replace.datafile"),
                replacePassword     : propman.parse("${propertyPrefix}sql.replace.password"),
                modeSqlExecute                  : propman.getBoolean("${propertyPrefix}mode.sql.execute"),
                modeSqlCheckBefore              : propman.getBoolean("${propertyPrefix}mode.sql.check.before"),
                modeSqlFileGenerate             : propman.getBoolean("${propertyPrefix}mode.sql.file.generate"),
                modeSqlIgnoreErrorExecute       : propman.getBoolean("${propertyPrefix}mode.sql.ignore.error.execute"),
                modeSqlIgnoreErrorCheckBefore   : propman.getBoolean("${propertyPrefix}mode.sql.ignore.error.check.before"),
                modeSqlProgressBar              : propman.getBoolean("${propertyPrefix}mode.sql.progress.bar"),

        )
    }

    @Method('genGlobalSqlSetup')
    SqlSetup genGlobalSqlSetup(){
        SqlSetup defaultOpt = new SqlSetup()
        SqlSetup globalOpt = genSqlSetup('')
        return defaultOpt.merge(globalOpt)
    }

    @Method('genMergedSqlSetup')
    SqlSetup genMergedSqlSetup(){
        SqlSetup defaultOpt = new SqlSetup()
        SqlSetup globalOpt = genSqlSetup('')
        SqlSetup localOpt = genSqlSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt).merge(localOpt)
    }

    SqlSetup genOtherSqlSetup(String propertyPrefix){
        SqlSetup defaultOpt = new SqlSetup()
        SqlSetup globalOpt = genSqlSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt)
    }

    /*************************
     * QuestionSetup
     *************************/
    QuestionSetup genQuestionSetup(String propertyPrefix){
        return new QuestionSetup(
                question            : propman.get("${propertyPrefix}question"),
                answer              : propman.getString("${propertyPrefix}answer"),
                recommandAnswer     : propman.getString("${propertyPrefix}answer.default"),
                modeOnlyInteractive : propman.getBoolean("${propertyPrefix}mode.only.interactive"),
                modeLoadResponseFile : propman.get("response.file.path") ? true : false,
                repeatLimit         : propman.getInteger("${propertyPrefix}answer.repeat.limit"),
                validation          : propman.parse("${propertyPrefix}answer.validation"),
                descriptionMap      : propman.parse("${propertyPrefix}answer.description.map"),
                valueMap            : propman.parse("${propertyPrefix}answer.value.map"),
        )
    }

    @Method('genGlobalQuestionSetup')
    QuestionSetup genGlobalQuestionSetup(){
        QuestionSetup defaultOpt = new QuestionSetup()
        QuestionSetup globalOpt = genQuestionSetup('')
        return defaultOpt.merge(globalOpt)
    }

    @Method('genMergedQuestionSetup')
    QuestionSetup genMergedQuestionSetup(){
        QuestionSetup defaultOpt = new QuestionSetup()
        QuestionSetup globalOpt = genQuestionSetup('')
        QuestionSetup localOpt = genQuestionSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt).merge(localOpt)
    }

    /*************************
     * ReportSetup
     *************************/
    ReportSetup genReportSetup(String propertyPrefix){
        return new ReportSetup(
                modeReport         : propman.getBoolean("${propertyPrefix}mode.report"),
                modeReportText     : propman.getBoolean("${propertyPrefix}mode.report.text"),
                modeReportExcel    : propman.getBoolean("${propertyPrefix}mode.report.excel"),
                modeReportConsole  : propman.getBoolean("${propertyPrefix}mode.report.console"),
                fileSetup          : genOtherFileSetup("${propertyPrefix}report."),
        )
    }

    @Method('genGlobalReportSetup')
    ReportSetup genGlobalReportSetup(){
        ReportSetup defaultOpt = new ReportSetup()
        ReportSetup globalOpt = genReportSetup('')
        return defaultOpt.merge(globalOpt)
    }

    @Method('genMergedReportSetup')
    ReportSetup genMergedReportSetup(){
        ReportSetup defaultOpt = new ReportSetup()
        ReportSetup globalOpt = genReportSetup('')
        ReportSetup localOpt = genReportSetup(propertyPrefix)
        return defaultOpt.merge(globalOpt).merge(localOpt)
    }





    @Method('getInstallerGlobalOption')
    InstallerGlobalOption getInstallerGlobalOption(){
        return new InstallerGlobalOption().merge(new InstallerGlobalOption(
                fileSetup                  : genGlobalFileSetup(),
                reportSetup                : genGlobalReportSetup(),
        ))
    }

    @Method('getReceptionistGlobalOption')
    ReceptionistGlobalOption getReceptionistGlobalOption(){
        return new ReceptionistGlobalOption().merge(new ReceptionistGlobalOption(
                modeRemember        : getBoolean("mode.remember.answer"),
                rememberFilePath    : getString("remember.answer.file.path"),
                rememberFileSetup   : genOtherFileSetup("remember.answer."),
                responseFilePath    : getString("response.file.path"),
        ))
    }

    @Method('getBuilderGlobalOption')
    BuilderGlobalOption getBuilderGlobalOption(){
        return new BuilderGlobalOption().merge(new BuilderGlobalOption(
                fileSetup           : genGlobalFileSetup(),
                reportSetup         : genReportSetup(),

                installerName            : getString('installer.name') ?: 'installer',
                installerHomeToLibRelPath: getString('installer.home.to.lib.relpath') ?: './lib',
                installerHomeToBinRelPath: getString('installer.home.to.bin.relpath') ?: './bin',
                installerHomeToRspRelPath: getString('installer.home.to.rsp.relpath') ?: './rsp',
                buildDir            : getFilePath('build.dir'),
                buildTempDir        : getFilePath('build.temp.dir'),
                buildDistDir        : getFilePath('build.dist.dir'),
                buildInstallerHome  : getFilePath('build.installer.home'),
                modeAutoRsp         : getFilePath('mode.auto.rsp'),
                modeAutoZip         : getFilePath('mode.auto.zip'),
                modeAutoTar         : getFilePath('mode.auto.tar'),
                propertiesDir       : getString('properties.dir') ?: './',
        ))
    }


}
