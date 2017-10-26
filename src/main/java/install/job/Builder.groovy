package install.job

import install.bean.GlobalOptionForBuilder
import install.bean.ReportSetup
import install.configuration.annotation.HelpIgnore
import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Job
import install.configuration.annotation.type.Task
import install.data.PropertyProvider
import install.util.JobUtil
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import install.bean.FileSetup
import jaemisseo.man.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by sujkim on 2017-02-17.
 */
@Job
class Builder extends JobUtil{

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    int buildCallCount = 0

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
        this.gOpt = config.injectValue(new GlobalOptionForBuilder())
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForBuilder = provider.propGen.get('builder')
        PropMan propmanDefault = provider.propGen.getDefaultProperties()
        PropMan propmanExternal = provider.propGen.getExternalProperties()

        //From User's FileSystem
        String propertiesDir = propmanExternal.get('properties.dir') ?: propmanDefault.get('user.dir')
        propertiesFile = FileMan.find(propertiesDir, propertiesFileName, ["yml", "yaml", "properties"])
        propertiesFileExtension = FileMan.getExtension(propertiesFile)
        if (propertiesFile && propertiesFile.exists()){
            Map propertiesMap = generatePropertiesMap(propertiesFile)
            propmanForBuilder.merge(propertiesMap)
                            .merge(propmanExternal)
                            .mergeNew(propmanDefault)
                            .merge(['builder.home': FileMan.getFullPath(propmanDefault.get('lib.dir'), '../')])
        }else{
        }

        return propmanForBuilder
    }


    @Command('init')
    @Document("""
    Init 3 Installer-Maker script files

        You can generate Sample Properties Files to build installer 
    
        1. builder.yml
        2. receptionist.yml
        3. installer.yml        
    """)
    void initCommand(){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        fileSetup.modeAutoOverWrite = false
        String propertiesDir = provider.getFilePath('properties.dir') ?: FileMan.getFullPath('./')
        String fileFrom
        String fileTo

        //DO
        logger.info "<Init File>"
        logger.info "- Dest Path: ${propertiesDir}"

        try{
            fileFrom = "sampleProperties/builder.sample.yml"
            fileTo = "${propertiesDir}/builder.yml"
            new FileMan().readResource(fileFrom).write(fileTo, fileSetup)
        }catch(e){
            logger.error "File Aready Exists. ${fileTo}\n"
        }

        try{
            fileFrom = "sampleProperties/receptionist.sample.yml"
            fileTo = "${propertiesDir}/receptionist.yml"
            new FileMan().readResource(fileFrom).write(fileTo, fileSetup)
        }catch(e){
            logger.error "File Aready Exists. ${fileTo}\n"
        }

        try{
            fileFrom = "sampleProperties/installer.sample.yml"
            fileTo = "${propertiesDir}/installer.yml"
            new FileMan().readResource(fileFrom).write(fileTo, fileSetup)
        }catch(e){
            logger.error "File Aready Exists. ${fileTo}\n"
        }
    }

    @Command('clean')
    @Document('''
    Clean Build Directory
    
        You need Builder Script file(builder.yml) 
    
    - Options
        1. You can change your build directory on Builder Script  
            [default value list]
                installer.name=installer_myproject                        
                build.dir=./build
                build.temp.dir=${build.dir}/installer_temp
                build.dist.dir=${build.dir}/installer_dist
                build.installer.home=${build.dir}/${installer.name}
                
        2. You can specify script files path on your workspace.
            [default value list]
                properties.dir=./
            [example]
                installer-maker clean build -properties.dir=./installer-data/                     
    ''')
    void clean(){
        //Setup Log
        setuptLog(gOpt.logSetup)

        if (!buildCallCount++)
            logBigTitle "Builder"

        logTaskDescription('clean')

        if (!propertiesFile)
            throw Exception('Does not exists script file [ builder.yml ]')

        try{
            FileMan.delete(gOpt.buildInstallerHome)
        }catch(e){
        }

        try{
            FileMan.delete(gOpt.buildDistDir)
        }catch(e){
        }

        try{
            FileMan.delete(gOpt.buildTempDir)
        }catch(e){
        }
    }

    @Command('build')
    @Document('''
    Build Your Installer
                                                     
          You need 3 Script files(builder.yml, receptionist.yml, installer.yml)

    - Options
        1. You can change your build directory on Builder Script(builder.yml)  
            [default value list]
                installer.name=installer_myproject                        
                build.dir=./build
                build.temp.dir=${build.dir}/installer_temp
                build.dist.dir=${build.dir}/installer_dist
                build.installer.home=${build.dir}/${installer.name}
                
        2. You can specify script files path on your workspace.
            [default value list]
                properties.dir=./
            [example]
                installer-maker clean build -properties.dir=./installer-data/        
    ''')
    void build(){
        //Setup Log
        setuptLog(gOpt.logSetup)

        if (!buildCallCount++)
            logBigTitle "Builder"

        logTaskDescription('build')

        if (!propertiesFile)
            throw Exception('Does not exists script file [ builder.yml ]')

        try{
            ReportSetup reportSetup = gOpt.reportSetup

            //1. Gen Starter and Response File
            String binPath = genLibAndBin(gOpt)

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
                zip(gOpt)

            // - 3) Tar
            if (propman.getBoolean('mode.auto.tar'))
                tar(gOpt)

        }catch(e){
            throw e
        }
    }

    @Command('run')
    @HelpIgnore
    @Document("""
    No User's Command        
    """)
    void runCommand(){
        logTaskDescription('RUN')

        if (!propertiesFile)
            throw Exception('Does not exists script file [ builder.yml ]')


        String binPath = provider.get('build.installer.bin.path') ?: FileMan.getFullPath(gOpt.buildInstallerHome, gOpt.installerHomeToBinRelPath)
        String argsExceptCommand = provider.get('args.except.command')
        String argsModeExec = '-mode.exec.self=true'
        String installBinPathForWIn = "${binPath}/install.bat".replaceAll(/[\/\\]+/, "\\$File.separator")
        String installBinPathForLin = "${binPath}/install".replaceAll(/[\/\\]+/, "/")
        provider.setRaw('exec.command.win', "${installBinPathForWIn} ${argsExceptCommand} ${argsModeExec}")
        provider.setRaw('exec.command.lin', "${installBinPathForLin} ${argsExceptCommand} ${argsModeExec}")
        runTaskByName('exec')
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
    private String genLibAndBin(GlobalOptionForBuilder gOpt){
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

        /** 1. Convert library for builder to installer**/
        logger.debug """<Builder> Copy And Generate Installer Library
         - Installer Home: ${buildInstallerHome}"
         - Copy Installer Lib: 
            FROM : ${libSourcePath}
            TO   : ${libDestPath}
        """

        //- Copy Libs
        FileSetup opt = new FileSetup(modeAutoMkdir:true, modeAutoOverWrite:true)
        new FileMan(libSourcePath).set(fileSetup).copy(libDestPath, opt)

        //- Unjar libs
        FileMan.unjar(libPath, tempNowDir, opt)

        //- Copy Scripts to Installer
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

        //- Write Scripts to Installer
        FileMan.write("${tempNowDir}/.libtohome", libToHomeRelPath, opt)

        //- Make jar
        FileMan.jar("${tempNowDir}/*", "${libDestPath}/${thisFileName}", opt)

        /** 2. Gen bin/install **/
        String binInstallShSourcePath = 'binForInstaller/install'
        String binInstallBatSourcePath = 'binForInstaller/install.bat'
        String binInstallShDestPath = "${binDestPath}/install"
        String binInstallBatDestPath = "${binDestPath}/install.bat"
        logger.debug """<Builder> Generate Bin, install:
            SH  : ${binInstallShDestPath}
            BAT : ${binInstallBatDestPath}
        """

        //- Gen bin/install(sh)
        new FileMan()
        .set(fileSetupForLin)
        .readResource(binInstallShSourcePath)
        .replaceLine([
            'REL_PATH_BIN_TO_HOME=' : "REL_PATH_BIN_TO_HOME=${binToHomeRelPath}",
            'REL_PATH_HOME_TO_LIB=' : "REL_PATH_HOME_TO_LIB=${homeToLibRelPath}"
        ])
        .write(binInstallShDestPath)

        //- Gen bin/install.bat
        new FileMan()
        .set(fileSetup)
        .readResource(binInstallBatSourcePath)
        .replaceLine([
            'set REL_PATH_BIN_TO_HOME=' : "set REL_PATH_BIN_TO_HOME=${binToHomeRelPathForWin}",
            'set REL_PATH_HOME_TO_LIB=' : "set REL_PATH_HOME_TO_LIB=${homeToLibRelPathForWin}"
        ])
        .write(binInstallBatDestPath)

        /** 3. Gen bin/installer-maker **/
//        String binInstallerShSourcePath = 'binForInstaller/installer-maker'
//        String binInstallerBatSourcePath = 'binForInstaller/installer-maker.bat'
//        String binInstallerShDestPath = "${binDestPath}/installer-maker"
//        String binInstallerBatDestPath = "${binDestPath}/installer-maker.bat"
//        logger.debug """<Builder> Generate Bin, installer:
//            SH  : ${binInstallerShDestPath}
//            BAT : ${binInstallerBatDestPath}
//        """
//
//        //- Gen bin/installer-maker(sh)
//        new FileMan()
//        .set(fileSetupForLin)
//        .readResource(binInstallerShSourcePath)
//        .replaceLine([
//            'REL_PATH_BIN_TO_HOME=' : "REL_PATH_BIN_TO_HOME=${binToHomeRelPath}",
//            'REL_PATH_HOME_TO_LIB=' : "REL_PATH_HOME_TO_LIB=${homeToLibRelPath}"
//        ])
//        .write(binInstallerShDestPath)
//
//        //- Gen bin/installer-maker.bat
//        new FileMan()
//        .set(fileSetup)
//        .readResource(binInstallerBatSourcePath)
//        .replaceLine([
//            'set REL_PATH_BIN_TO_HOME=' : "set REL_PATH_BIN_TO_HOME=${binToHomeRelPathForWin}",
//            'set REL_PATH_HOME_TO_LIB=' : "set REL_PATH_HOME_TO_LIB=${homeToLibRelPathForWin}"
//        ])
//        .write(binInstallerBatDestPath)

        /** 4. Gen bin/macgyver **/
        String binMacgyverShSourcePath = 'binForInstaller/macgyver'
        String binMacgyverBatSourcePath = 'binForInstaller/macgyver.bat'
        String binMacgyverShDestPath = "${binDestPath}/macgyver"
        String binMacgyverBatDestPath = "${binDestPath}/macgyver.bat"
        logger.debug """<Builder> Generate Bin, Macgyver:
            SH  : ${binMacgyverShDestPath}
            BAT : ${binMacgyverBatDestPath}
        """

        //- Gen bin/macgyver(sh)
        new FileMan()
        .set(fileSetupForLin)
        .readResource(binMacgyverShSourcePath)
        .replaceLine([
            'REL_PATH_BIN_TO_HOME=' : "REL_PATH_BIN_TO_HOME=${binToHomeRelPath}",
            'REL_PATH_HOME_TO_LIB=' : "REL_PATH_HOME_TO_LIB=${homeToLibRelPath}"
        ])
        .write(binMacgyverShDestPath)

        //- Gen bin/macgyver.bat
        new FileMan()
        .set(fileSetup)
        .readResource(binMacgyverBatSourcePath)
        .replaceLine([
            'set REL_PATH_BIN_TO_HOME=' : "set REL_PATH_BIN_TO_HOME=${binToHomeRelPathForWin}",
            'set REL_PATH_HOME_TO_LIB=' : "set REL_PATH_HOME_TO_LIB=${homeToLibRelPathForWin}"
        ])
        .write(binMacgyverBatDestPath)
        return binDestPath
    }

    /*************************
     * Distribute ZIP
     * From: ${build.installer.home}
     *   To: ${build.dist.dir}
     *************************/
    void zip(GlobalOptionForBuilder gOpt){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        String installerName = gOpt.installerName
        String buildTempDir = gOpt.buildTempDir
        String buildDistDir = gOpt.buildDistDir
        String buildInstallerHome = gOpt.buildInstallerHome

        //Log
        logTaskDescription('auto zip')

        //Zip
        FileMan.zip(buildInstallerHome, "${buildDistDir}/${installerName}.zip", fileSetup)
    }

    /*************************
     * Distribute TAR
     * From: ${build.installer.home}
     *   To: ${build.dist.dir}
     *************************/
    void tar(GlobalOptionForBuilder gOpt){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        String installerName = gOpt.installerName
        String buildTempDir = gOpt.buildTempDir
        String buildDistDir = gOpt.buildDistDir
        String buildInstallerHome = gOpt.buildInstallerHome

        //Log
        logTaskDescription('auto tar')

        //Zip
        FileMan.tar(buildInstallerHome, "${buildDistDir}/${installerName}.tar", fileSetup)
    }



}
