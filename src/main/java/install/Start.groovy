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
        PropMan propmanForMacgyver = propGen.genMacgyverDefaultProperties()

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
            System.exit(0)
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
            //0. New Instance
            // - Builder
            propmanForBuilder.merge("${propertiesDir}/builder.properties")
                             .merge(prop)
                             .merge(['builder.home': builderHome])
                             .mergeNew(propmanDefault)
            JobBuilder builder = new JobBuilder(propmanForBuilder)
            // - Receptionist
            propmanForReceptionist.merge("${propertiesDir}/receptionist.properties")
                                  .mergeNew(propmanForBuilder)
            JobReceptionist receptionist = new JobReceptionist(propmanForReceptionist)
            //1. Build
            builder.build()
            //2. Make a Response Form
            receptionist.buildForm()
            //3. Zip
            builder.zip()
        }

        /**
         * RECEPTIONIST
         */
        ///// ask
        if (prop['ask']){
            //From User's FileSystem or Resource
            String userSetPropertiesDir = prop['properties.dir']
            if (userSetPropertiesDir)
                propmanForReceptionist.merge("${userSetPropertiesDir}/receptionist.properties")
            else
                propmanForReceptionist.mergeResource("receptionist.properties")
            //ASK
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
            //From User's FileSystem or Resource
            String userSetPropertiesDir = prop['properties.dir']
            if (userSetPropertiesDir)
                propmanForInstaller.merge("${userSetPropertiesDir}/installer.properties")
            else
                propmanForInstaller.mergeResource("installer.properties")
            //INSTALL
            propmanForInstaller.merge(propmanForReceptionist)
                               .merge(['installer.home': installerHome])
                               .mergeNew(propmanDefault)
            new JobInstaller(propmanForInstaller).install()
        }

        /**
         * MACGYVER
         */
        ///// macgyver
        if (prop['macgyver'] || prop['m']){
            //From User's FileSystem or Resource
            String userSetPropertiesDir = prop['properties.dir']
            if (userSetPropertiesDir)
                propmanForMacgyver.merge("${userSetPropertiesDir}/macgyver.properties")
            else
                propmanForMacgyver.mergeResource("macgyver.properties")
            //Macgyver Do Something
            propmanForMacgyver.merge(prop)
                              .mergeNew(propmanDefault)
            new MacGyver(propmanForMacgyver).doSomething()
        }

        ///// Doing Other Task with Command Line Options
        if (true){
            PropMan propman = new PropMan(prop)
            new MacGyver(propman).run()
        }

        /////LOG
        logGen.logFinished()
    }



}

