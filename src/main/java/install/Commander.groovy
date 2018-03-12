package install

import install.util.CommanderUtil
import jaemisseo.man.FileMan
import jaemisseo.man.configuration.Config
import install.job.Hoya
import install.job.InstallerMaker
import install.job.Installer
import jaemisseo.man.PropMan
import jaemisseo.man.TimeMan
import jaemisseo.man.configuration.annotation.type.Bean
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Bean
class Commander extends CommanderUtil{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    Commander(){
    }

    Commander(Config config, TimeMan timeman){
        this.config = config
        this.timeman = timeman
        propmanExternal = config.propGen.getExternalProperties()
        propmanDefault = config.propGen.getDefaultProperties()
        dashDashOptionList = propmanExternal.get('--')
        init()
    }

    PropMan propmanExternal
    PropMan propmanDefault
    List<String> dashDashOptionList



    /*************************
     *
     * INIT
     *
     *************************/
    void init(){
        //- Try to get from User's FileSystem
        generatePropMan('installer-maker')
        generatePropMan('installer')
        generatePropMan('hoya')
    }

    void generatePropMan(String fileName){
        PropMan propmanExternal = config.propGen.getExternalProperties()
        PropMan propmanDefault = config.propGen.getDefaultProperties()
        File propertiesFile
        String propertiesDir = propmanExternal.get('properties.dir') ?: propmanDefault.get('user.dir')
        if (propertiesDir)
            propertiesFile = FileMan.find(propertiesDir, "${fileName}.default", ["properties"])
        if (propertiesFile)
            config.propGen.genSingletonPropManFromFileSystem(fileName, propertiesFile.path)
        else
            config.propGen.genSingletonPropManFromResource(fileName, "defaultProperties/${fileName}.default.properties")
    }



    /*************************
     *
     * RUN
     *
     *************************/
    void run(){
        //- CommandList & TaskList called by user
        List<String> commandCalledByUserList = config.commandCalledByUserList
        List<String> taskCalledByUserList = config.taskCalledByUserList

        /** [Setup] --OPTION **/
        String applicationName = setupApplicationName()
        setupLogOption()
        setupOtherOption()

        /** [Run] **/
        //- Check Mode Help
        boolean modeHelp = propmanExternal.getBoolean(['help', 'h'])
        boolean modeExecSelf = propmanExternal.getBoolean('mode.exec.self')
        boolean hasCommand = !!commandCalledByUserList
        boolean hasTask = !!taskCalledByUserList

        boolean modeDefaultCommand = !hasCommand && !hasTask && !modeHelp
        boolean modeCommand = hasCommand && !modeHelp
        boolean modeTask = !hasCommand && hasTask && !modeHelp

        switch (applicationName){
            /*************************
             * INSTALLER_MAKER
             *************************/
            case APPLICATION_INSTALLER_MAKER:
                if (modeHelp)
                    runCommand('doSomething')

                /** [Command] Auto Command **/
                if (modeDefaultCommand){
                    logExternalProperty(propmanExternal)
                    runCommand(['help'], InstallerMaker)

                /** [Command] **/
                }else if (modeCommand){
                    logExternalProperty(propmanExternal)
                    runCommand(commandCalledByUserList, InstallerMaker)
                    if (modeExecSelf)
                        return
                    finishCommand(commandCalledByUserList, timeman.stop().getTime())

                /** [Task / Help] **/
                }else if (modeTask){
                    runCommand('doSomething')

                }
                break

            /*************************
             * INSTALLER
             *************************/
            case APPLICATION_INSTALLER:
                if (modeHelp)
                    runCommand('doSomething')

                /** [Command] Auto Command **/
                if (modeDefaultCommand){
                    logExternalProperty(propmanExternal)
                    runCommand(['ask', 'install'], Installer)

                /** [Command] **/
                }else if (modeCommand){
                    logExternalProperty(propmanExternal)
                    runCommand(commandCalledByUserList, Installer)
                    if (modeExecSelf)
                        return
                    finishCommand(commandCalledByUserList, timeman.stop().getTime())

                /** [Task / Help] **/
                }else if (modeTask){
                    if (['version', 'system'].contains(taskCalledByUserList[0]))
                        runCommand('doSomething')

                }
                break

            /*************************
             * HOYA
             *************************/
            case APPLICATION_HOYA:
                if (modeHelp)
                    runCommand('doSomething')

                /** [Command] Auto Command **/
                if (modeDefaultCommand){
                    logExternalProperty(propmanExternal)
                    runCommand(['hoya'], Hoya)

                /** [Command] **/
                }else if (modeCommand){
                    logExternalProperty(propmanExternal)
                    runCommand(commandCalledByUserList, Hoya)
                    if (modeExecSelf)
                        return
                    finishCommand(commandCalledByUserList, timeman.stop().getTime())

                /** [Task / Help] **/
                }else if (modeTask){
                    runCommand('doSomething')

                }
                break

            default:
                break
        }

        /** [Finish] INSTALLER-MAKER **/
        config.logGen.logFinished()
    }



    /*************************
     *
     *  Setup from Dash Dash Option
     *
     *************************/
    void setupLogOption(){
        //- Set Log
        if (dashDashOptionList.contains('error')){
            propmanExternal.set('log.level.console', 'error')
            if (dashDashOptionList.contains('log.file'))
                propmanExternal.set('log.level.file', 'error')
        }else if (dashDashOptionList.contains('debug')){
            propmanExternal.set('log.level.console', 'debug')
            if (dashDashOptionList.contains('log.file'))
                propmanExternal.set('log.level.file', 'debug')
        }else if (dashDashOptionList.contains('trace')){
            propmanExternal.set('log.level.console', 'trace')
            if (dashDashOptionList.contains('log.file'))
                propmanExternal.set('log.level.file', 'trace')
        }
    }

    String setupApplicationName(){
        //- Set Application Identity
        String applicationName = getApplicationName(propmanExternal)
        propmanDefault.set('application.name', applicationName)
        return applicationName
    }

    void setupOtherOption(){
        //- Set Help Option
        if (dashDashOptionList.contains('help'))
            propmanExternal.set('help', true)

        //- Set Help Option
        if (dashDashOptionList.contains('gen'))
            propmanExternal.set('mode.generate', true)
    }

}
