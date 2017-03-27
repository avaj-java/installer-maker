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
            fileSetup   : genFileSetup()
        ))
    }



    /**
     * RUN
     */
    void run(){

        //Gen Starter
        setLibAndBin()

        //Each level by level
        eachLevel(levelNamesProperty){ String levelName ->
            String propertyPrefix = "${levelNamesProperty}.${levelName}."
            String taskName = getString(propertyPrefix, 'task')?.trim()?.toUpperCase()
            logBigTitle("${levelName}")
            runTask(taskName, propertyPrefix)
        }

    }



    /**
     * Install Starter (Lib, Bin) - Create Dir & Copy Lib & Generate Bin
     * ${Installer.home}/lib
     * ${Installer.home}/bin
     */
    void setLibAndBin(){

        //Ready
        FileSetup gFileSetup = gOpt.fileSetup
        String installerHome = getFilePath('installer.home')
        String homeToLibRelPath = getValue('installer.lib.relpath') ?: './lib'
        String homeToBinRelPath = getValue('installer.bin.relpath') ?: './bin'
        String binDestPath = FileMan.getFullPath(installerHome, homeToBinRelPath)
        String binToHomeRelPath = FileMan.getRelativePath(binDestPath, installerHome)
        String binToHomeRelPathForWin = binToHomeRelPath.replace('/','\\')
        String homeToLibRelPathForWin = homeToLibRelPath.replace('/','\\')
        String libDir = getFilePath('this.dir')
        String thisPath = getFilePath('this.path')
        String thisFileName = FileMan.getLastFileName(thisPath)
        String builderHome = "${libDir}/.."
        String tempDir = "${builderHome}/temp"
        String libSourcePath = "${libDir}/*.*"
        String libDestPath = FileMan.getFullPath(installerHome, homeToLibRelPath)
        String binShSourcePath = 'binForInstaller/install'
        String binBatSourcePath ='binForInstaller/install.bat'
        String binShDestPath = "${binDestPath}/install"
        String binBatDestPath = "${binDestPath}/install.bat"
        String tempNowDir = "${tempDir}/temp_${new Date().format('yyyyMMddHHmmssSSS')}"

        //1. Copy Libs
        println """<Builder> Copy And Generate Installer Library
         - Installer Home: ${installerHome}"
         - Copy Installer Lib: 
            FROM : ${libSourcePath}
            TO   : ${libDestPath}
        """
        new FileMan(libSourcePath)
            .set(gFileSetup)
            .copy(libDestPath)

        //2. Convert Init Script to Script Editd By User
        FileMan.unjar("${thisPath}", tempNowDir, true)
        FileMan.copy("*.properties", tempNowDir)
        FileMan.jar("${tempNowDir}/*", "${libDestPath}/${thisFileName}")

        //3. Gen Bins
        println """<Builder> Generate Bin:
            SH  : ${binShDestPath}
            BAT : ${binBatDestPath}
        """
        new FileMan()
            .set(gFileSetup)
            .readResource(binBatSourcePath)
            .replaceLine([
                'set REL_PATH_BIN_TO_HOME=' : "set REL_PATH_BIN_TO_HOME=${binToHomeRelPathForWin}",
                'set REL_PATH_HOME_TO_LIB=' : "set REL_PATH_HOME_TO_LIB=${homeToLibRelPathForWin}"
            ])
            .write(binBatDestPath)

        new FileMan()
            .set(gFileSetup)
            .readResource(binShSourcePath)
            .replaceLine([
                'REL_PATH_BIN_TO_HOME=' : "REL_PATH_BIN_TO_HOME=${binToHomeRelPath}",
                'REL_PATH_HOME_TO_LIB=' : "REL_PATH_HOME_TO_LIB=${homeToLibRelPath}"
            ])
            .write(binShDestPath)
    }

}
