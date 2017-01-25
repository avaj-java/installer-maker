package install

import groovy.sql.Sql
import install.external.TestDB

class Start {

    PropMan prop
    SqlMan sql

    Start(){
    }


    /**
     * START
     * @param exProp
     */
    void start(def exProp){
        ///// other function
        runOtherFunc(exProp)

        ///// Properties Manager
        prop = getPropertiesFile(exProp)

        ///// SQL Manager
        sql = new SqlMan()

        // Set Default(Global) Info
        sql.set([
                vendor:prop["sql.vendor"],
                ip:prop["sql.ip"],
                port:prop["sql.port"],
                db:prop["sql.db"],
                user:prop["sql.user"],
                password:prop["sql.password"],
                replace : prop.parse("sql.file.replace"),
                replaceTable : prop.parse("sql.file.replace.table"),
                replaceIndex : prop.parse("sql.file.replace.index"),
                replaceSequence : prop.parse("sql.file.replace.sequence"),
                replaceView : prop.parse("sql.file.replace.view"),
                replaceFunction : prop.parse("sql.file.replace.function"),
                replaceTablespace : prop.parse("sql.file.replace.tablespace"),
                replaceUser : prop.parse("sql.file.replace.user"),
                replaceDatafile : prop.parse("sql.file.replace.datafile"),
                replacePassword : prop.parse("sql.file.replace.password")
        ])


        ///// Get Install Level
        List<String> installLevels = prop['install.level'].split(',').collect{ it.trim() }

        ///// INSTALL level by level
        installLevels.each{ String levelName ->
            def tempDataSource = [
                    vendor : prop["install.level.${levelName}.sql.vendor"],
                    ip : prop["install.level.${levelName}.sql.ip"],
                    port : prop["install.level.${levelName}.sql.port"],
                    db : prop["install.level.${levelName}.sql.db"],
                    user : prop["install.level.${levelName}.sql.user"],
                    password : prop["install.level.${levelName}.sql.password"]
            ]
            def tempOptionReplace = [
                    replace : prop.parse("install.level.${levelName}.sql.file.replace"),
                    replaceTable : prop.parse("install.level.${levelName}.sql.file.replace.tablespace"),
                    replaceIndex : prop.parse("install.level.${levelName}.sql.file.replace.tablespace"),
                    replaceSequence : prop.parse("install.level.${levelName}.sql.file.replace.tablespace"),
                    replaceView : prop.parse("install.level.${levelName}.sql.file.replace.tablespace"),
                    replaceFunction : prop.parse("install.level.${levelName}.sql.file.replace.tablespace"),
                    replaceTablespace : prop.parse("install.level.${levelName}.sql.file.replace.tablespace"),
                    replaceUser : prop.parse("install.level.${levelName}.sql.file.replace.tablespace"),
                    replaceDatafile : prop.parse("install.level.${levelName}.sql.file.replace.datafile"),
                    replacePassword : prop.parse("install.level.${levelName}.sql.file.replace.password"),
            ]
            def tempOptionFile = [
                    fileDirectory : prop["install.level.${levelName}.sql.file.directory"],
                    fileName : prop["install.level.${levelName}.sql.file.name"]
            ]

            def fileList = getFileList(tempOptionFile.fileDirectory, tempOptionFile.fileName, 'sql')

            fileList.each{ String filePath ->
                    println ""
                    println ""
                    println ""
                    println "///////////////////////////////////////////////////////////////////////////"
                    println "///// ${filePath}"
                    println "///////////////////////////////////////////////////////////////////////////"
                    sql.init()
                            .queryFromFile("${filePath}")
                            .command([SqlMan.ALL])
                            .replace(tempOptionReplace)
                            .checkBefore(tempDataSource)
                            .printQuerys()
                            .run(tempDataSource)
                            .report()
            }
        }
    }


    /**
     *
     * @param fileDirectory
     * @param fileName
     * @param extension
     * @return
     */
    def getFileList(String fileDirectory, String fileName, String extension){
        def fileList = []
        // check files
        if (fileName){
            fileList = fileName.split(",").collect{ return it.trim() }
            if (fileList.size() == 1)
                fileList = fileList[0].split(" ").collect{ return it.trim() }
            fileList = fileList.collect{ return new File("${fileDirectory}/${it}").path }
        }else{
            new File(fileDirectory).listFiles().each{ File file ->
                fileList << file.path
            }
        }
        // check extension
        if (extension){
            fileList = fileList.findAll{
                int lastDotIdx = it.lastIndexOf('.')
                String itExtension = it.substring(lastDotIdx+1).toUpperCase()
                String acceptExtension = extension.toUpperCase()
                return ( itExtension.equals(acceptExtension) )
            }
        }
        return fileList
    }

    /**
     *
     * @param exProp
     * @return
     */
    PropMan getPropertiesFile(def exProp){
        String thisPath = new File(this.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent()
        PropMan prop = new PropMan(thisPath)
        prop.getFIle(exProp['prop.path'], exProp['prop.filename'], exProp)
                .validate(['sql.user', 'sql.password'])
        return prop
    }


    /**
     *
     * @param exProp
     */
    void runOtherFunc(def exProp){

        if (exProp['-getPath']){
            println new PropMan().getFullPath(exProp['-getPath'])
            System.exit(0)
        }
        if (exProp['-testdb'] as boolean){
            new TestDB().run(exProp)
            System.exit(0)
        }
    }



    static void main(String[] args) throws Exception{
        // basic properties
        def prop = [
            'prop.path': ['/', './', './conf', '../conf'],
            'prop.filename': 'installer.properties'
        ]
        // OverWrite with external properties
        if (args){
            args.each{
                int indexEqualMark = it.indexOf('=');
                String beforeEqualMark
                def afterEqualMark
                if (indexEqualMark != -1){
                    beforeEqualMark = it.substring(0, indexEqualMark)
                    afterEqualMark = it.substring(indexEqualMark + 1)
                    prop[beforeEqualMark] = (afterEqualMark) ?: ''
                }
            }
        }
        // start
        new Start().start(prop)
    }

}

