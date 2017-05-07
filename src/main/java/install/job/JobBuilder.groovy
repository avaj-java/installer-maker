package install.job

import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.VariableMan
import jaemisseo.man.util.FileSetup
import install.bean.BuilderGlobalOption
import install.bean.ReportSetup

/**
 * Created by sujkim on 2017-02-17.
 */
class JobBuilder extends JobUtil{

    JobBuilder(PropMan propman){
        //Job Setup
        levelNamesProperty = 'b.level'
        executorNamePrefix = 'b'
        propertiesFileName = 'builder.properties'

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, executorNamePrefix)
        setBeforeGetProp(propman, varman)
        this.gOpt = new BuilderGlobalOption().merge(new BuilderGlobalOption(
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



    /**
     * INIT
     * Generate Sample Properties Files
     * 1. builder.properties
     * 2. receptionist.properties
     * 3. installer.properties
     */
    void init(){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        fileSetup.modeAutoOverWrite = false
        String propertiesDir = getFilePath('properties.dir') ?: FileMan.getFullPath('./')
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

    /**
     * CLEAN
     * Clean Build Directory
     */
    void clean(){
        try{
            FileMan.delete(gOpt.buildInstallerHome)
            FileMan.delete(gOpt.buildDistDir)
            FileMan.delete(gOpt.buildTempDir)
        }catch(e){
        }
    }

    /**
     * BUILD
     */
    void build(){

        ReportSetup reportSetup = gOpt.reportSetup

        //1. Gen Starter and Response File
        genLibAndBin()

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
    }



    /**
     * WRITE Report
     */
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



    /**
     * Generate Install Starter (Lib, Bin)
     * ${build.installer.home}/lib
     * ${build.installer.home}/bin
     *
     *  1. Create Dir
     *  2. Generate Lib
     *  3. Generate Bin
     */
    private void genLibAndBin(){

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
        String libDir = getFilePath('lib.dir')
        String libPath = getFilePath('lib.path')
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
        FileMan.copy("builder.properties", tempNowDir, opt)
        FileMan.copy("receptionist.properties", tempNowDir, opt)
        FileMan.copy("installer.properties", tempNowDir, opt)
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
    }

    /**
     * Distribute ZIP
     * From: ${build.installer.home}
     *   To: ${build.dist.dir}
     */
    void zip(){
        if (!propman.getBoolean('mode.auto.zip'))
            return

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

    /**
     * Distribute TAR
     * From: ${build.installer.home}
     *   To: ${build.dist.dir}
     */
    void tar(){
        if (!propman.getBoolean('mode.auto.tar'))
            return

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
