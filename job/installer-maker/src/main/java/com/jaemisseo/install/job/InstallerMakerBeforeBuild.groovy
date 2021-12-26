package com.jaemisseo.install.job

import com.jaemisseo.hoya.bean.FileSetup
import com.jaemisseo.hoya.bean.GlobalOptionForInstallerMaker
import com.jaemisseo.hoya.job.Hoya
import com.jaemisseo.hoya.job.JobHelper
import com.jaemisseo.install.exception.FailedToCopyDistributionDirException
import com.jaemisseo.install.exception.FailedToDownloadDistributionException
import com.jaemisseo.install.exception.FailedToExtractDistributionException
import com.jaemisseo.install.exception.OverRetryLimit
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.configuration.context.CommanderConfig
import jaemisseo.man.configuration.context.Environment
import jaemisseo.man.configuration.context.SelfAware
import jaemisseo.man.configuration.data.PropertyProvider
import org.apache.commons.io.FileUtils

import java.text.SimpleDateFormat

class InstallerMakerBeforeBuild extends JobHelper {

    InstallerMakerBeforeBuild(PropMan propman, CommanderConfig config, PropertyProvider provider, Environment environment, SelfAware selfAware){
        this.propman = propman
        this.config = config
        this.provider = provider
        this.environment = environment
        this.selfAware = selfAware
    }


    /*************************
     * Generate Install Starter (Lib, Bin)
     * ${build.installer.home}/lib
     * ${build.installer.home}/bin
     *
     *  1. Clone library for builder to installer
     *  2. Remake Jar(Installer Core)
     *  3. Remake Executable Bin
     *************************/
    public String remakeLibAndBin(GlobalOptionForInstallerMaker gOpt){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        FileSetup fileSetupForLin = fileSetup.clone([chmod:'755', lineBreak:'\n'])
        String buildTempDir = gOpt.buildTempDir
        String buildDistDir = gOpt.buildDistDir
        String buildInstallerHome = gOpt.buildInstallerHome
        String homeToBinRelPath = gOpt.installerHomeToBinRelPath
        String homeToLibRelPath = gOpt.installerHomeToLibRelPath

        String binDestPath = FileMan.getFullPath(buildInstallerHome, homeToBinRelPath)
        String tempDir = "${buildTempDir}/temp_${new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())}"

        /** 1. Clone library for builder to installer **/
        cloneLibFiles(gOpt, tempDir)

        /** 2. Remake Jar(Installer Core) **/
        remakeThisLibFile(gOpt, tempDir)

        /** 3. Remake Executable Bin **/
        remakeBinFiles(gOpt)

        return binDestPath
    }


    /**
     * 1. Clone library
     * @param gOpt
     */
    private void cloneLibFiles(GlobalOptionForInstallerMaker gOpt, String tempDir){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        FileSetup fileSetupForLin = fileSetup.clone([chmod:'755', lineBreak:'\n'])
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

//        String tempDir = "${buildTempDir}/temp_${new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date())}"

        String libSourcePath = "${libDir}/*.*"
        String libDestPath = FileMan.getFullPath(buildInstallerHome, homeToLibRelPath)
        String libToHomeRelPath = FileMan.getRelativePath(libDestPath, buildInstallerHome)

        //4. Copy to Installer lib dir
        //TODO: How to check environment cli or gradle
        boolean modeNeedToDownload = environment.getModeNeedToDownloadForRemaking()
        if (modeNeedToDownload){
            libSourcePath = 'https://github.com/avaj-java/installer-maker/releases/download/0.7.3/installer-maker-0.7.3.zip';
            logger.debug """<Installer-Maker> Copy And Generate Installer Library
             - Installer Home: ${buildInstallerHome}"
             - Copy Installer Lib: 
                FROM : ${libSourcePath}
                TO   : ${libDestPath}
            """
            String storedDirPath = ".gradle/installer-maker";
            downloadAndStoreDistribution(libSourcePath, storedDirPath, gOpt, 0)
            if (new File(storedDirPath).exists()){
                try {
                    String storedLibPath = new File(storedDirPath, "installer-maker-0.7.3/lib/*.*").getPath()
                    copyLibDirToLibDir(storedLibPath, libDestPath, gOpt);
//                    copyLibToLibDirWithURLClassLoader(libDestPath, gOpt)
                }catch(FailedToCopyDistributionDirException ftcdde){
                    throw ftcdde
                }
            }

        }else{
            libSourcePath = "${libDir}/*.*"
            logger.debug """<Installer-Maker> Copy And Generate Installer Library
             - Installer Home: ${buildInstallerHome}"
             - Copy Installer Lib: 
                FROM : ${libSourcePath}
                TO   : ${libDestPath}
            """
            try {
                copyLibDirToLibDir(libSourcePath, libDestPath, gOpt)
//            copyLibToLibDirWithURLClassLoader(libDestPath, gOpt)
            }catch(FailedToCopyDistributionDirException ftcdde){
                throw ftcdde
            }

        }

        System.out.println( "!Complete - Clone libs ");
    }



    /**
     * 2. Remake this lib file
     * @param gOpt
     */
    private void remakeThisLibFile(GlobalOptionForInstallerMaker gOpt, String tempDir){
        //Ready
        FileSetup fileSetup = gOpt.fileSetup
        FileSetup fileSetupForLin = fileSetup.clone([chmod:'755', lineBreak:'\n'])
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

        String libSourcePath = "${libDir}/*.*"
        String libDestPath = FileMan.getFullPath(buildInstallerHome, homeToLibRelPath)
        String libToHomeRelPath = FileMan.getRelativePath(libDestPath, buildInstallerHome)

        //- Unjar libs
        FileSetup opt = new FileSetup(modeAutoMkdir:true, modeAutoOverWrite:true)
        FileMan.unjar(libPath, tempDir, opt)

        generatePropertiesFile(tempDir, opt)
        generateInfoFile(tempDir, opt, libToHomeRelPath)

        //- jar libs
        FileMan.jar("${tempDir}/*", "${libDestPath}/${thisFileName}", opt)
    }

    private void generatePropertiesFile(String tempDir, FileSetup opt){
        //- Copy Scripts to Installer
        PropMan propmanExternal = provider.propGen.getExternalProperties()
        String userSetPropertiesDir = propman['properties.dir']

        //InstallerMaker
        InstallerMaker installerMaker = config.findInstance(InstallerMaker)
        File builderPropertiesFile = FileMan.find(userSetPropertiesDir, installerMaker.propertiesFileName, ["yml", "yaml", "properties"])
        if (builderPropertiesFile)
            FileMan.copy(builderPropertiesFile.path, tempDir, opt)
        else
            throw new Exception("Does not exist script file(${installerMaker.propertiesFileName})")

        //Installer
        Installer installer = config.findInstance(Installer)
        File installerPropertiesFile = FileMan.find(userSetPropertiesDir, installer.propertiesFileName, ["yml", "yaml", "properties"])
        if (installerPropertiesFile)
            FileMan.copy(installerPropertiesFile.path, tempDir, opt)
        else
            throw new Exception("Does not exist script file(${installer.propertiesFileName})")

        //Hoya
        Hoya hoya = config.findInstance(Hoya)
        File hoyaPropertiesFile = FileMan.find(userSetPropertiesDir, hoya.propertiesFileName, ["yml", "yaml", "properties"])
        if (hoyaPropertiesFile)
            FileMan.copy(hoyaPropertiesFile.path, tempDir, opt)
    }

    private void generateInfoFile(String tempDir, FileSetup opt, String libToHomeRelPath){
        String productVersion = propman['product.version']
        String productName = propman['product.name']

        //- Write 'Relative Path from [lib dir] to [Installer Home dir]'
        FileMan.write("${tempDir}/.libtohome", libToHomeRelPath, opt)
        //- Write 'Product Name'
        FileMan.write("${tempDir}/.productname", productName, opt)
        //- Write 'Product Version'
        FileMan.write("${tempDir}/.productversion", productVersion, opt)
    }

    /**
     * 3. Remake bin
     * @param gOpt
     */
    private void remakeBinFiles(GlobalOptionForInstallerMaker gOpt){
        FileSetup fileSetup = gOpt.fileSetup
        FileSetup fileSetupForLin = fileSetup.clone([chmod:'755', lineBreak:'\n'])
        String buildTempDir = gOpt.buildTempDir
        String buildDistDir = gOpt.buildDistDir
        String buildInstallerHome = gOpt.buildInstallerHome
        String homeToBinRelPath = gOpt.installerHomeToBinRelPath
        String homeToLibRelPath = gOpt.installerHomeToLibRelPath
        String binDestPath = FileMan.getFullPath(buildInstallerHome, homeToBinRelPath)
        String binToHomeRelPath = FileMan.getRelativePath(binDestPath, buildInstallerHome)
        String binToHomeRelPathForWin = binToHomeRelPath.replace('/','\\')
        String homeToLibRelPathForWin = homeToLibRelPath.replace('/','\\')

        String binInstallShSourcePath = 'binForInstaller/install'
        String binInstallBatSourcePath = 'binForInstaller/install.bat'
        String binInstallShDestPath = "${binDestPath}/install"
        String binInstallBatDestPath = "${binDestPath}/install.bat"
        logger.debug """<Installer-Maker> Generate Bin, install:
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

        /** 4. Generate Runable Binary File (installer) **/
        String binInstallerShSourcePath = 'binForInstaller/installer'
        String binInstallerBatSourcePath = 'binForInstaller/installer.bat'
        String binInstallerShDestPath = "${binDestPath}/installer"
        String binInstallerBatDestPath = "${binDestPath}/installer.bat"
        logger.debug """<Installer-Maker> Generate Bin, installer:
            SH  : ${binInstallerShDestPath}
            BAT : ${binInstallerBatDestPath}
        """

        //- Gen bin/installer(sh)
        new FileMan()
                .set(fileSetupForLin)
                .readResource(binInstallerShSourcePath)
                .replaceLine([
                        'REL_PATH_BIN_TO_HOME=' : "REL_PATH_BIN_TO_HOME=${binToHomeRelPath}",
                        'REL_PATH_HOME_TO_LIB=' : "REL_PATH_HOME_TO_LIB=${homeToLibRelPath}"
                ])
                .write(binInstallerShDestPath)

        //- Gen bin/installer.bat
        new FileMan()
                .set(fileSetup)
                .readResource(binInstallerBatSourcePath)
                .replaceLine([
                        'set REL_PATH_BIN_TO_HOME=' : "set REL_PATH_BIN_TO_HOME=${binToHomeRelPathForWin}",
                        'set REL_PATH_HOME_TO_LIB=' : "set REL_PATH_HOME_TO_LIB=${homeToLibRelPathForWin}"
                ])
                .write(binInstallerBatDestPath)

        /** 5. Generate Runable Binary File (hoya) **/
        String binHoyaShSourcePath = 'binForHoya/hoya'
        String binHoyaBatSourcePath = 'binForHoya/hoya.bat'
        String binHoyaShDestPath = "${binDestPath}/hoya"
        String binHoyaBatDestPath = "${binDestPath}/hoya.bat"
        logger.debug """<Installer-Maker> Generate Bin, Hoya:
            SH  : ${binHoyaShDestPath}
            BAT : ${binHoyaBatDestPath}
        """

        //- Gen bin/hoya(sh)
        new FileMan()
                .set(fileSetupForLin)
                .readResource(binHoyaShSourcePath)
                .replaceLine([
                        'REL_PATH_BIN_TO_HOME=' : "REL_PATH_BIN_TO_HOME=${binToHomeRelPath}",
                        'REL_PATH_HOME_TO_LIB=' : "REL_PATH_HOME_TO_LIB=${homeToLibRelPath}"
                ])
                .write(binHoyaShDestPath)

        //- Gen bin/hoya.bat
        new FileMan()
                .set(fileSetup)
                .readResource(binHoyaBatSourcePath)
                .replaceLine([
                        'set REL_PATH_BIN_TO_HOME=' : "set REL_PATH_BIN_TO_HOME=${binToHomeRelPathForWin}",
                        'set REL_PATH_HOME_TO_LIB=' : "set REL_PATH_HOME_TO_LIB=${homeToLibRelPathForWin}"
                ])
                .write(binHoyaBatDestPath)

        /** 6. Generate Runable Binary File (check) **/
        String binCheckShSourcePath = 'binForHoya/check'
        String binCheckShDestPath = "${binDestPath}/check"
        logger.debug """<Installer-Maker> Generate Bin, check:
            SH  : ${binCheckShDestPath}
        """

        //- Gen bin/hoya(sh)
        new FileMan()
                .set(fileSetupForLin)
                .readResource(binCheckShSourcePath)
                .replaceLine([
                        'REL_PATH_BIN_TO_HOME=' : "REL_PATH_BIN_TO_HOME=${binToHomeRelPath}",
                        'REL_PATH_HOME_TO_LIB=' : "REL_PATH_HOME_TO_LIB=${homeToLibRelPath}"
                ])
                .write(binCheckShDestPath)
    }

    private void copyLibDirToLibDir(String from, String to, GlobalOptionForInstallerMaker gOpt){
        System.out.println( "  > Clone some libs ==> lib" );
        System.out.println( "    - from: ${from}" );
        System.out.println( "    -   to: ${to}" );
        FileSetup globalFileSetup = gOpt.fileSetup
        FileSetup opt = new FileSetup(modeAutoMkdir:true, modeAutoOverWrite:true)
        new FileMan(from).set(globalFileSetup).copy(to, opt)
    }

    private void copyLibToLibDirWithURLClassLoader(String to, GlobalOptionForInstallerMaker gOpt){
        System.out.println( "  > Clone URLClassLoader ==> lib" );
        System.out.println( "    - from: URLClassLoader" );
        System.out.println( "    -   to: ${to}" );
        FileSetup globalFileSetup = gOpt.fileSetup
        FileSetup opt = new FileSetup(modeAutoMkdir:true, modeAutoOverWrite:true)
        ClassLoader myCl = getClass().getClassLoader();
        URLClassLoader myUcl = (URLClassLoader) myCl;
        String from = null;
        for (URL url : myUcl.getURLs()) {
            from = url.toString().replace("file:", "")
            System.out.println( from );
            new FileMan(from).set(globalFileSetup).copy(to, opt)
        }
    }

    private void downloadAndStoreDistribution(String from, String storeDirPath, GlobalOptionForInstallerMaker gOpt, int retry){
        if (retry > 3){
            throw new OverRetryLimit(retry)
        }else if (retry != 0 && retry <= 3){
            System.out.println( "> Cloning distribution [retry:${retry}]");
        }else{
            System.out.println( "> Cloning distribution");
        }

        URL downloadUrl = new URL(from)
        File downloadFile = new File(downloadUrl.getFile())
        File downloadDestFile = new File("build/", downloadFile.getName());
        File storeDir = new File(storeDirPath);

        //1. Check already downloaded file
        //Check storeDir
        if (!storeDir.exists()){
            try {
                //2. Download file
                download(downloadUrl, downloadDestFile)

            }catch(FailedToDownloadDistributionException ftdde){
                System.out.println( "  >> retry ${retry +1}" );
                downloadAndStoreDistribution(from, storeDirPath, gOpt, retry +1)
                return;
            }

            try {
                //3. Extract downloaded file
                extract(downloadDestFile, storeDir);

            }catch(FailedToExtractDistributionException ftdde){
                System.out.println( "  >> retry ${retry +1}" );
                downloadAndStoreDistribution(from, storeDirPath, gOpt, retry +1)
                return;
            }
        }
    }

    private void download(URL downloadUrl, File downloadDestFile){
        //Check downloadedFile
        if (!downloadDestFile.exists()){
            System.out.println( "  > Download distribution");
            System.out.println( "    - from: ${downloadUrl.getPath()}" );
            System.out.println( "    -   to: ${downloadDestFile.getPath()}" );
            FileUtils.copyURLToFile(downloadUrl, downloadDestFile);
        }

        //Check downloadedFile
        if (!downloadDestFile.exists()){
            throw new FailedToDownloadDistributionException()
        }
    }

    private void extract(File downloadDestFile, File storeDir){
        System.out.println( "  > Unzip distribution");
        System.out.println( "    - from: ${downloadDestFile.getPath()}" );
        System.out.println( "    -   to: ${storeDir.getPath()}" );
        FileMan.unzip(downloadDestFile.getPath(), storeDir.getPath(), true);

        if (!storeDir.exists()){
            throw new FailedToExtractDistributionException()
        }
    }

}
