package install.job

import install.configuration.annotation.HelpIgnore
import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Job
import install.configuration.annotation.type.Task
import install.bean.ReportSetup
import install.data.PropertyProvider
import install.util.JobUtil
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.util.FileSetup
import jaemisseo.man.util.Util

/**
 * Created by sujkim on 2017-02-17.
 */
@Job
class Builder extends JobUtil{

    Builder(){
        propertiesFileName = 'builder'
        executorNamePrefix = 'builder'
        levelNamesProperty = 'builder.level'
    }

    @Init(lately=true)
    void init(){
        validTaskList = Util.findAllClasses('install', [Task])

        this.propman = setupPropMan(provider)
        this.varman = setupVariableMan(propman, executorNamePrefix)
        provider.shift(jobName)
        this.gOpt = provider.getBuilderGlobalOption()
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForBuilder = provider.propGen.get('builder')
        PropMan propmanDefault = provider.propGen.getDefaultProperties()
        PropMan propmanExternal = provider.propGen.getExternalProperties()

        //From User's FileSystem
        String propertiesDir = propmanExternal.get('properties.dir') ?: propmanDefault.get('user.dir')
        propertiesFile = FileMan.find(propertiesDir, propertiesFileName, ["yml", "yaml", "properties"])
        propertiesFileExtension = FileMan.getExtension(propertiesFile)
        Map propertiesMap = generatePropertiesMap(propertiesFile)

        propmanForBuilder.merge(propertiesMap)
                        .merge(propmanExternal)
                        .mergeNew(propmanDefault)
                        .merge(['builder.home': FileMan.getFullPath(propmanDefault.get('lib.dir'), '../')])

        println propmanForBuilder.get('startup.meta')
        println propmanForBuilder.get('startup.meta') == true
        println propmanForBuilder.get('startup.meta') == 'true'

        return propmanForBuilder
    }



    @Document("""
      INIT
      Generate Sample Properties Files
      1. builder.properties
      2. receptionist.properties
      3. installer.properties        
    """)
    @Command('init')
    void initCommand(){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        fileSetup.modeAutoOverWrite = false
        String propertiesDir = provider.getFilePath('properties.dir') ?: FileMan.getFullPath('./')
        String filePath
        String destPath

        //DO
        println "<Init File>"
        println "- Dest Path: ${propertiesDir}"

        try{
            filePath = "sampleProperties/builder.sample.properties"
            destPath = "${propertiesDir}/builder.properties"
            new FileMan().readResource(filePath).write(destPath, fileSetup)
        }catch(e){
            println "File Aready Exists. ${destPath}\n"
        }

        try{
            filePath = "sampleProperties/receptionist.sample.properties"
            destPath = "${propertiesDir}/receptionist.properties"
            new FileMan().readResource(filePath).write(destPath, fileSetup)
        }catch(e){
            println "File Aready Exists. ${destPath}\n"
        }

        try{
            filePath = "sampleProperties/installer.sample.properties"
            destPath = "${propertiesDir}/installer.properties"
            new FileMan().readResource(filePath).write(destPath, fileSetup)
        }catch(e){
            println "File Aready Exists. ${destPath}\n"
        }
    }

    @Document("""
    CLEAN
    Clean Build Directory        
    """)
    @Command('clean')
    void clean(){
        try{
            FileMan.delete(gOpt.buildInstallerHome)
            FileMan.delete(gOpt.buildDistDir)
            FileMan.delete(gOpt.buildTempDir)
        }catch(e){
        }
    }

    @Document("""
    BUILD        
    """)
    @Command('build')
    void build(){
        try{
            ReportSetup reportSetup = gOpt.reportSetup
            //1. Gen Starter and Response File
            String binPath = genLibAndBin()
            //- set bin path on builded installer
            provider.setRaw('build.installer.bin.path', binPath)

            //2. Each level by level
            eachLevelForTask{ String propertyPrefix ->
                try{
                    return runTaskByPrefix("${propertyPrefix}")
                }catch(e){
                    //Write Report
                    writeReport(reportMapList, reportSetup)
                    throw e
                }
            }
            //Write Report
            writeReport(reportMapList, reportSetup)

            // - 1) Make a Response Form
            if (propman.getBoolean('mode.auto.rsp'))
                buildForm()

            // - 2) Zip
            if (propman.getBoolean('mode.auto.zip'))
                zip()

            // - 3) Tar
            if (propman.getBoolean('mode.auto.tar'))
                tar()

        }catch(e){
            e.printStackTrace()
            throw e
        }
    }

    @HelpIgnore
    @Command('run')
    void runCommand(){
        String binPath = provider.get('build.installer.bin.path') ?: FileMan.getFullPath(gOpt.buildInstallerHome, gOpt.installerHomeToBinRelPath)
        String argsExceptCommand = provider.get('args.except.command')
        String argsModeExec = '-mode.exec.self=true'
        String installBinPathForWIn = "${binPath}/install.bat".replaceAll(/[\/\\]+/, "\\$File.separator")
        String installBinPathForLin = "${binPath}/install".replaceAll(/[\/\\]+/, "/")
        provider.setRaw('exec.command.win', "${installBinPathForWIn} ${argsExceptCommand} ${argsModeExec}")
        provider.setRaw('exec.command.lin', "${installBinPathForLin} ${argsExceptCommand} ${argsModeExec}")
        runTask('exec')
        provider.setRaw('exec.command.win', "")
        provider.setRaw('exec.command.lin', "")
    }

    void buildForm(){
        provider.propGen.getDefaultProperties().set('mode.build.form', true)
        config.command('form')
//        Receptionist receptionist = new Receptionist()
//        receptionist.propGen = propGen
//        receptionist.init()
//        receptionist.buildForm()
    }

    /*************************
     * WRITE Report
     *************************/
    private void writeReport(List reportMapList, ReportSetup reportSetup){

        //Generate File Report
        if (reportMapList){
            String date = new Date().format('yyyyMMdd_HHmmss')
            String fileNamePrefix = 'report_analysis'

            if (reportSetup.modeReportText) {
//                List<String> stringList = sqlman.getAnalysisStringResultList(reportMapList)
//                FileMan.write("${fileNamePrefix}_${date}.txt", stringList, opt)
            }

            if (reportSetup.modeReportExcel){
                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", reportMapList, 'sqlFileName')
            }

        }

    }



    /*************************
     * Generate Install Starter (Lib, Bin)
     * ${build.installer.home}/lib
     * ${build.installer.home}/bin
     *
     *  1. Create Dir
     *  2. Generate Lib
     *  3. Generate Bin
     *************************/
    private String genLibAndBin(){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        FileSetup fileSetupForLin = fileSetup.clone([lineBreak:'\n'])
        String buildTempDir = gOpt.buildTempDir
        String buildDistDir = gOpt.buildDistDir
        String buildInstallerHome = gOpt.buildInstallerHome
        String homeToBinRelPath = gOpt.installerHomeToBinRelPath
        String homeToLibRelPath = gOpt.installerHomeToLibRelPath

        String binDestPath = FileMan.getFullPath(buildInstallerHome, homeToBinRelPath)
        String binToHomeRelPath = FileMan.getRelativePath(binDestPath, buildInstallerHome)
        String binToHomeRelPathForWin = binToHomeRelPath.replace('/','\\')
        String homeToLibRelPathForWin = homeToLibRelPath.replace('/','\\')
        String libDir = provider.getFilePath('lib.dir')
        String libPath = provider.getFilePath('lib.path')
        String thisFileName = FileMan.getLastFileName(libPath)
        String tempNowDir = "${buildTempDir}/temp_${new Date().format('yyyyMMdd_HHmmssSSS')}"
        String libSourcePath = "${libDir}/*.*"
        String libDestPath = FileMan.getFullPath(buildInstallerHome, homeToLibRelPath)
        String libToHomeRelPath = FileMan.getRelativePath(libDestPath, buildInstallerHome)

        String binInstallShSourcePath = 'binForInstaller/install'
        String binInstallBatSourcePath = 'binForInstaller/install.bat'
        String binInstallShDestPath = "${binDestPath}/install"
        String binInstallBatDestPath = "${binDestPath}/install.bat"

        String binInstallerShSourcePath = 'binForInstaller/installer'
        String binInstallerBatSourcePath = 'binForInstaller/installer.bat'
        String binInstallerShDestPath = "${binDestPath}/installer"
        String binInstallerBatDestPath = "${binDestPath}/installer.bat"

        println """<Builder> Copy And Generate Installer Library
         - Installer Home: ${buildInstallerHome}"
         - Copy Installer Lib: 
            FROM : ${libSourcePath}
            TO   : ${libDestPath}
        """

        //1. Copy Libs
        FileSetup opt = new FileSetup(modeAutoMkdir:true, modeAutoOverWrite:true)
        new FileMan(libSourcePath).set(fileSetup).copy(libDestPath, opt)

        //2. Convert Init Script to Script Editd By User
        FileMan.unjar(libPath, tempNowDir, opt)

        //Search scriptfiles from User's FileSystem
        PropMan propmanExternal = provider.propGen.getExternalProperties()
        String userSetPropertiesDir = propmanExternal['properties.dir']
        Builder builder = config.findInstance(Builder)
        Receptionist receptionist = config.findInstance(Receptionist)
        Installer installer = config.findInstance(Installer)
        File builderPropertiesFile = FileMan.find(userSetPropertiesDir, builder.propertiesFileName, ["yml", "yaml", "properties"])
        File receptionistPropertiesFile = FileMan.find(userSetPropertiesDir, receptionist.propertiesFileName, ["yml", "yaml", "properties"])
        File installerPropertiesFile = FileMan.find(userSetPropertiesDir, installer.propertiesFileName, ["yml", "yaml", "properties"])

        FileMan.copy(builderPropertiesFile.path, tempNowDir, opt)
        FileMan.copy(receptionistPropertiesFile.path, tempNowDir, opt)
        FileMan.copy(installerPropertiesFile.path, tempNowDir, opt)
        FileMan.write("${tempNowDir}/.libtohome", libToHomeRelPath, opt)
        FileMan.jar("${tempNowDir}/*", "${libDestPath}/${thisFileName}", opt)

        println """<Builder> Generate Bin, install:
            SH  : ${binInstallShDestPath}
            BAT : ${binInstallBatDestPath}
        """

        //3. Gen bin/install(sh)
        new FileMan()
        .set(fileSetupForLin)
        .readResource(binInstallShSourcePath)
        .replaceLine([
            'REL_PATH_BIN_TO_HOME=' : "REL_PATH_BIN_TO_HOME=${binToHomeRelPath}",
            'REL_PATH_HOME_TO_LIB=' : "REL_PATH_HOME_TO_LIB=${homeToLibRelPath}"
        ])
        .write(binInstallShDestPath)

        //4. Gen bin/install.bat
        new FileMan()
        .set(fileSetup)
        .readResource(binInstallBatSourcePath)
        .replaceLine([
            'set REL_PATH_BIN_TO_HOME=' : "set REL_PATH_BIN_TO_HOME=${binToHomeRelPathForWin}",
            'set REL_PATH_HOME_TO_LIB=' : "set REL_PATH_HOME_TO_LIB=${homeToLibRelPathForWin}"
        ])
        .write(binInstallBatDestPath)

        println """<Builder> Generate Bin, installer:
            SH  : ${binInstallerShDestPath}
            BAT : ${binInstallerBatDestPath}
        """

        //5. Gen bin/installer(sh)
        new FileMan()
        .set(fileSetupForLin)
        .readResource(binInstallerShSourcePath)
        .replaceLine([
            'REL_PATH_BIN_TO_HOME=' : "REL_PATH_BIN_TO_HOME=${binToHomeRelPath}",
            'REL_PATH_HOME_TO_LIB=' : "REL_PATH_HOME_TO_LIB=${homeToLibRelPath}"
        ])
        .write(binInstallerShDestPath)

        //6. Gen bin/installer.bat
        new FileMan()
        .set(fileSetup)
        .readResource(binInstallerBatSourcePath)
        .replaceLine([
            'set REL_PATH_BIN_TO_HOME=' : "set REL_PATH_BIN_TO_HOME=${binToHomeRelPathForWin}",
            'set REL_PATH_HOME_TO_LIB=' : "set REL_PATH_HOME_TO_LIB=${homeToLibRelPathForWin}"
        ])
        .write(binInstallerBatDestPath)
        return binDestPath
    }

    /*************************
     * Distribute ZIP
     * From: ${build.installer.home}
     *   To: ${build.dist.dir}
     *************************/
    void zip(){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        String installerName = gOpt.installerName
        String buildTempDir = gOpt.buildTempDir
        String buildDistDir = gOpt.buildDistDir
        String buildInstallerHome = gOpt.buildInstallerHome

        //Log
        logBigTitle('AUTO ZIP')

        //Zip
        FileMan.zip(buildInstallerHome, "${buildDistDir}/${installerName}.zip", fileSetup)
    }

    /*************************
     * Distribute TAR
     * From: ${build.installer.home}
     *   To: ${build.dist.dir}
     *************************/
    void tar(){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        String installerName = gOpt.installerName
        String buildTempDir = gOpt.buildTempDir
        String buildDistDir = gOpt.buildDistDir
        String buildInstallerHome = gOpt.buildInstallerHome

        //Log
        logBigTitle('AUTO TAR')

        //Zip
        FileMan.tar(buildInstallerHome, "${buildDistDir}/${installerName}.tar", fileSetup)
    }



}
