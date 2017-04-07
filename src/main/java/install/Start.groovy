package install

import com.jaemisseo.man.FileMan
import install.job.JobReceptionist
import install.job.JobBuilder
import install.employee.MacGyver
import install.job.JobInstaller
import com.jaemisseo.man.PropMan

class Start {

    /**
     * START INSTALL
     * @param args
     * @throws Exception
     */
    static void main(String[] args) throws Exception{
        Map propMap = new InstallerPropertiesGenerator().genPropertiesValueMap(args)
        //Start With Properties Value Map
        new Start().start(propMap)
    }



    InstallerPropertiesGenerator propGen = new InstallerPropertiesGenerator()
    InstallerLogGenerator logGen = new InstallerLogGenerator()

    /**
     * START
     * @param prop
     */
    void start(Map prop){

        /////Create Main Bean
        PropMan propmanDefault = propGen.genSystemDefaultProperties()
        PropMan propmanForBuilder = propGen.genBuilderDefaultProperties()
        PropMan propmanForReceptionist = propGen.genReceptionistDefaultProperties()
        PropMan propmanForInstaller = propGen.genInstallerDefaultProperties()

        String nowPath = propmanDefault.get('user.dir')
        String libDir = propmanDefault.get('lib.dir')
        String propertiesDir = prop['properties.dir'] ?: nowPath
        //builder
        String builderHome = FileMan.getFullPath(libDir, '../')
        //installer
        String libtohomeRelPath = FileMan.getFileFromResource('.libtohome')?.text.replaceAll('\\s*', '') ?: '../'
        String installerHome = FileMan.getFullPath(libDir, libtohomeRelPath)

        /////LOG
        logGen.logStart(propmanDefault)

        /**
         * BUILDER
         */
        ///// version
        if (prop['version'] || prop['v']){
            logGen.logVersion(propmanDefault)
            System.exit(0)
        }

        ///// init
        if (prop['init']){
            propmanForBuilder.merge(prop)
            new JobBuilder(propmanForBuilder).init()
        }

        ///// clean
        if (prop['clean']){
            propmanForBuilder.merge("${propertiesDir}/builder.properties")
                             .merge(prop)
                             .merge(['builder.home': builderHome])
                             .mergeNew(propmanDefault)
            new JobBuilder(propmanForBuilder).clean()
        }

        ///// build
        if (prop['build'] || prop['b']){
            propmanForBuilder.merge("${propertiesDir}/builder.properties")
                             .merge(prop)
                             .merge(['builder.home': builderHome])
                             .mergeNew(propmanDefault)
            new JobBuilder(propmanForBuilder).build()
        }

        /**
         * RECEPTIONIST
         */
        ///// ask
        if (prop['ask']){
            String userSetPropertiesDir = prop['properties.dir']
            if (userSetPropertiesDir)
                propmanForReceptionist.merge("${userSetPropertiesDir}/receptionist.properties")
            else
                propmanForReceptionist.mergeResource("receptionist.properties")
            propmanForReceptionist.merge(prop)
                                  .merge(['installer.home': installerHome])
                                  .mergeNew(propmanDefault)

            new JobReceptionist(propmanForReceptionist).ask()
        }

        /**
         * INSTALLER
         */
        ///// install
        if (prop['install'] || prop['i']){
            String userSetPropertiesDir = prop['properties.dir']
            if (userSetPropertiesDir)
                propmanForInstaller.merge("${userSetPropertiesDir}/installer.properties")
            else
                propmanForInstaller.mergeResource("installer.properties")
            propmanForInstaller.merge(propmanForReceptionist)
                               .merge(['installer.home': installerHome])
                               .mergeNew(propmanDefault)
            new JobInstaller(propmanForInstaller).install()
        }

        /**
         * MACGYVER
         */
        ///// Doing Other Task
        if (true){
            PropMan propman = new PropMan(prop)
            new MacGyver(propman).run('')
        }

        /////LOG
        logGen.logFinished()
    }



}

