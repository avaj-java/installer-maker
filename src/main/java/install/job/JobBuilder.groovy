package install.job

import com.jaemisseo.man.*
import com.jaemisseo.man.util.FileSetup
import install.bean.BuilderGlobalOption
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-02-17.
 */
class JobBuilder extends TaskUtil {

    JobBuilder(PropMan propman){
        //Job Setup
        levelNamesProperty = 'build.level'
        invalidTaskList = [TASK_SQL]

        this.propman = propman
        this.varman = new VariableMan(propman.properties)
        parsePropMan(propman, varman, levelNamesProperty)
        setBeforeGetProp(propman, varman)
        this.gOpt = new BuilderGlobalOption().merge(new BuilderGlobalOption(
            fileSetup           : genFileSetup(),
            installerName            : getValue('installer.name') ?: 'installer',
            installerHomeToLibRelPath: getValue('installer.home.to.lib.relpath') ?: './lib',
            installerHomeToBinRelPath: getValue('installer.home.to.bin.relpath') ?: './bin',
            buildDir            : getFilePath('build.dir'),
            buildTempHome       : getFilePath('build.temp.dir'),
            buildInstallerHome  : getFilePath('build.installer.home'),
            propertiesDir       : getValue('properties.dir') ?: './',
        ))
    }



    /**
     * RUN
     */
    void run(){

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
        String destPath = getFilePath('dest.path') ?: FileMan.getFullPath('./')
        //DO
        println "<Init File>"
        println "- Dest Path: ${destPath}"
        new FileMan().readResource('sampleProperties/builder.sample.properties').write("${destPath}/builder.properties", fileSetup)
        new FileMan().readResource('sampleProperties/receptionist.sample.properties').write("${destPath}/receptionist.properties", fileSetup)
        new FileMan().readResource('sampleProperties/installer.sample.properties').write("${destPath}/installer.properties", fileSetup)
    }

    /**
     * CLEAN
     * Clean Build Directory
     */
    void clean(){
        try{
            FileMan.delete(gOpt.buildInstallerHome)
            FileMan.delete(gOpt.buildTempHome)
        }catch(e){
        }
    }

    /**
     * BUILD
     */
    void build(){
        //1. Gen Starter
        setLibAndBin()
        //2. Each level by level
        eachLevel(levelNamesProperty){ String levelName ->
            String propertyPrefix = "${levelNamesProperty}.${levelName}."
            String taskName = getString(propertyPrefix, 'task')?.trim()?.toUpperCase()
            logBigTitle("${levelName}")
            runTask(taskName, propertyPrefix)
        }
    }



    /**
     * Generate Install Starter (Lib, Bin)
     * ${Installer.home}/lib
     * ${Installer.home}/bin
     *
     *  1. Create Dir
     *  2. Generate Lib
     *  3. Generate Bin
     */
    private void setLibAndBin(){

        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        String buildTempHome = gOpt.buildTempHome
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
        String tempNowDir = "${buildTempHome}/temp_${new Date().format('yyyyMMdd_HHmmssSSS')}"
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
        new FileMan(libSourcePath).set(fileSetup).copy(libDestPath, new FileSetup(modeAutoMkdir:true))

        //2. Convert Init Script to Script Editd By User
        FileMan.unjar(libPath, tempNowDir, true)
        FileMan.copy("*.properties", tempNowDir)
        FileMan.write("${tempNowDir}/.libtohome", libToHomeRelPath, fileSetup)
        FileMan.jar("${tempNowDir}/*", "${libDestPath}/${thisFileName}")

        println """<Builder> Generate Bin, install:
            SH  : ${binInstallShDestPath}
            BAT : ${binInstallBatDestPath}
        """

        //3. Gen bin/install(sh)
        new FileMan()
        .set(fileSetup)
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
        .set(fileSetup)
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

}
