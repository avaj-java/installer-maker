package com.jaemisseo.install.job

import com.jaemisseo.hoya.bean.FileSetup
import com.jaemisseo.hoya.bean.GlobalOptionForInstallerMaker
import com.jaemisseo.hoya.job.JobHelper
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.configuration.context.CommanderConfig
import jaemisseo.man.configuration.context.Environment
import jaemisseo.man.configuration.context.SelfAware
import jaemisseo.man.configuration.data.PropertyProvider

class InstallerMakerAfterBuild extends JobHelper {

    InstallerMakerAfterBuild(PropMan propman, CommanderConfig config, PropertyProvider provider, Environment environment, SelfAware selfAware){
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
     *  1. Make a Response Form
     *  2. Zip
     *  3. Tar
     *************************/
    public String generateResponseFileAndCompressDistribution(GlobalOptionForInstallerMaker gOpt){
        /** 1) Make a Response Form **/
        if (propman.getBoolean('mode.auto.rsp'))
            buildForm(gOpt, config, provider)

        /** - 2) Zip **/
        if (propman.getBoolean('mode.auto.zip'))
            zip(gOpt)

        /** - 3) Tar **/
        if (propman.getBoolean('mode.auto.tar'))
            tar(gOpt)
    }





    /*************************
     * Build Form
     *
     *
     *************************/
    private void buildForm(GlobalOptionForInstallerMaker gOpt, CommanderConfig config, PropertyProvider provider){
        provider.propGen.getDefaultProperties().set('mode.build.form', true)
        config.command('form')
    }

    /*************************
     * Distribute ZIP
     * From: ${build.installer.home}
     *   To: ${build.dist.dir}
     *************************/
    private void zip(GlobalOptionForInstallerMaker gOpt){
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
    private void tar(GlobalOptionForInstallerMaker gOpt){
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
