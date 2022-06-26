package com.jaemisseo.install.job

import com.jaemisseo.hoya.bean.FileSetup
import com.jaemisseo.hoya.bean.GlobalOptionForInstallerMaker
import com.jaemisseo.hoya.bean.ReportSetup
import com.jaemisseo.install.helper.InstallerMakerAfterBuild
import com.jaemisseo.install.helper.InstallerMakerBeforeBuild
import jaemisseo.man.configuration.exception.ScriptNotFoundException
import jaemisseo.man.configuration.exception.WantToRestartException
import com.jaemisseo.hoya.job.JobHelper
import com.jaemisseo.hoya.task.config.TaskSetup
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.configuration.context.CommanderConfig
import jaemisseo.man.configuration.annotation.HelpIgnore
import jaemisseo.man.configuration.annotation.method.Command
import jaemisseo.man.configuration.annotation.method.Init
import jaemisseo.man.configuration.annotation.type.Document
import jaemisseo.man.configuration.annotation.type.Job
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.data.PropertyProvider
import jaemisseo.man.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

/**
 * Created by sujkim on 2017-02-17.
 */
@Job
public class InstallerMaker extends JobHelper {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    int jobCallingCount = 0

    InstallerMaker(){
        propertiesFileName = 'installer-maker'
        jobName = 'installer-maker'
    }

    void logo(){
        logger.info Util.multiTrim("""
        88                                          88 88                                       
        ''                         ,d               88 88                                       
                                   88               88 88                                       
        88 8b,dPPYba,  ,adPPYba, MM88MMM ,adPPYYba, 88 88  ,adPPYba, 8b,dPPYba,                 
        88 88P'   ''8a I8[    ''   88    ''     'Y8 88 88 a8P_____88 88P'   'Y8                 
        88 88       88  ''Y8ba,    88    ,adPPPPP88 88 88 8PP''''''' 88                         
        88 88       88 aa    ]8I   88,   88,    ,88 88 88 '8b,   ,aa 88                         
        88 88       88 ''YbbdP''   'Y888 ''8bbdP'Y8 88 88  ''Ybbd8'' 88                         
                                                                                                
                                      88                                                        
                                      88                                                        
                                      88                                                        
        88,dPYba,,adPYba,  ,adPPYYba, 88   ,d8  ,adPPYba, 8b,dPPYba,                            
        88P'   '88'    '8a ''     'Y8 88 ,a8'  a8P_____88 88P'   'Y8                            
        88      88      88 ,adPPPPP88 8888[    8PP''''''' 88                                    
        88      88      88 88,    ,88 88''Yba, '8b,   ,aa 88                                    
        88      88      88 ''8bbdP'Y8 88   'Y8a ''Ybbd8'' 88                                    
        """)
    }

    @Init(lately=true)
    void init(){
        //Parse Global Property's variable
        this.propman = setupPropMan(provider)
        this.varman = setupVariableMan(propman)

        //Inject default value to GlobalOption
        provider.shift(jobName)
        this.gOpt = config.injectValue(new GlobalOptionForInstallerMaker())

        //Make Virtual Command
        this.virtualPropman = new PropMan()
        cacheAllCommitTaskListOnAllCommand()

        //First Commit
        commit()
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForInstallerMaker = provider.propGen.get('installer-maker')
        PropMan propmanDefault = provider.propGen.getDefaultProperties()
        PropMan propmanProgram = provider.propGen.getProgramProperties()
        PropMan propmanExternal = provider.propGen.getExternalProperties()

        //- Try to get from User's FileSystem
        String propertiesDir = propmanExternal.get('properties.dir') ?: propmanDefault.get('user.dir')
        if (propertiesDir)
            propertiesFile = FileMan.find(propertiesDir, propertiesFileName, ["yml", "yaml", "properties"])

        //- Make Property Manager
        if (propertiesFile && propertiesFile.exists()){
            propertiesFileExtension = FileMan.getExtension(propertiesFile)
            Map propertiesMap = generateMapFromPropertiesFile(propertiesFile)
            propmanForInstallerMaker.merge(propertiesMap)
                            .merge(propmanExternal)
                            .mergeOnlyNew(propmanDefault)
                            .mergeOnlyNew(propmanProgram)
                            .merge(['builder.home': FileMan.getFullPath(propmanDefault.get('lib.dir'), '../')])
        }

        return propmanForInstallerMaker
    }



    @Command
    void customCommand(){
        //Setup Log
        setupLog(gOpt.logSetup)

        if (!jobCallingCount++)
            logo()

        if (!propertiesFile)
            throw new ScriptNotFoundException("Does not exists script file [ installer-maker.yml ]")

        ReportSetup reportSetup = config.injectValue(new ReportSetup())

        //Each level by level
        validTaskList = CommanderConfig.findAllClasses('com.jaemisseo', [Task])
        eachTaskWithCommit(commandName){ TaskSetup commitTask ->
            try{
                return runTaskByCommitTask(commitTask)
            }catch(WantToRestartException wtre){
                throw wtre
            }catch(Exception e){
                //Write Report
                writeReport(reportMapList, reportSetup)
                throw e
            }
        }

        //Write Report
        writeReport(reportMapList, reportSetup)
    }



    @Command('init')
    @Document("""
    Init 2 Installer-Maker script files
    You can generate Sample Properties Files to build installer (installer-maker.yml, installer.yml)
    
        installer-maker init    
    
    You can generate Default Properties Files (installer-maker.default.properties, installer.default.properties) 
            
        installer-maker init --default
    
    And you can create custum task manager script (hoya.yml) 
        
        hoya init
            
    Default.. (hoya.default.properties)
    
        hoya init --default
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

        PropMan externalPropMan = config.propGen.getExternalProperties()
        List<String> dashDashOptionList = externalPropMan.get('--')

        /** Default Properties **/
        if (dashDashOptionList.contains('default')){
            if (dashDashOptionList.contains('hoya')) {
                try{
                    fileFrom = "defaultProperties/hoya.default.properties"
                    fileTo = "${propertiesDir}/hoya.default.properties"
                    new FileMan().readResource(fileFrom).write(fileTo, fileSetup)
                }catch(e){
                    logger.warn "File Aready Exists. ${fileTo}\n"
                }
            }else{
                try{
                    fileFrom = "defaultProperties/installer-maker.default.properties"
                    fileTo = "${propertiesDir}/installer-maker.default.properties"
                    new FileMan().readResource(fileFrom).write(fileTo, fileSetup)
                }catch(e){
                    logger.warn "File Aready Exists. ${fileTo}\n"
                }

                try{
                    fileFrom = "defaultProperties/installer.default.properties"
                    fileTo = "${propertiesDir}/installer.default.properties"
                    new FileMan().readResource(fileFrom).write(fileTo, fileSetup)
                }catch(e){
                    logger.warn "File Aready Exists. ${fileTo}\n"
                }
            }

        /** Properties **/
        }else{
            if (dashDashOptionList.contains('hoya')){
                try{
                    fileFrom = "hoya.yml"
                    fileTo = "${propertiesDir}/hoya.yml"
                    new FileMan().readResource(fileFrom).write(fileTo, fileSetup)
                }catch(e){
                    logger.warn "File Aready Exists. ${fileTo}\n"
                }

            }else{
                try{
                    fileFrom = "sampleProperties/installer-maker.sample.yml"
                    fileTo = "${propertiesDir}/installer-maker.yml"
                    new FileMan().readResource(fileFrom).write(fileTo, fileSetup)
                }catch(e){
                    logger.warn "File Aready Exists. ${fileTo}\n"
                }

                try{
                    fileFrom = "sampleProperties/installer.sample.yml"
                    fileTo = "${propertiesDir}/installer.yml"
                    new FileMan().readResource(fileFrom).write(fileTo, fileSetup)
                }catch(e){
                    logger.warn "File Aready Exists. ${fileTo}\n"
                }
            }
        }
    }

    @Command('clean')
    @Document('''
    Clean Build Directory
    
        You need installer-maker Script file(installer-maker.yml) 
    
    - Options
        1. You can change your build directory on Builder Script  
            [default value list]
                installer.name: installer_myproject                        
                build.dir: ./build
                build.temp.dir: ${build.dir}/installer_temp
                build.dist.dir: ${build.dir}/installer_dist
                build.installer.home: ${build.dir}/${installer.name}
                
        2. You can specify script files path on your workspace.
            [default value list]
                properties.dir: ./
            [example]
                installer-maker clean build -properties.dir=./installer-data/                     
    ''')
    void clean(){
        //Setup Log
        setupLog(gOpt.logSetup)

        if (!jobCallingCount++)
            logo()

        logTaskDescription('clean')

        if (!propertiesFile)
            throw new Exception('Does not exists script file [ installer-maker.yml ]')

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
                                                     
          You need 2 Script files(installer-maker.yml, installer.yml)

    - Options
        1. You can change your build directory on Builder Script(installer-maker.yml)  
            [default value list]
                installer.name: installer_myproject                        
                build.dir: ./build
                build.temp.dir: ${build.dir}/installer_temp
                build.dist.dir: ${build.dir}/installer_dist
                build.installer.home: ${build.dir}/${installer.name}
                
        2. You can specify script files path on your workspace.
            [default value list]
                properties.dir: ./
            [example]
                installer-maker clean build -properties.dir=./installer-data/        
    ''')
    void build(){
        //Setup Log
        setupLog(gOpt.logSetup)

        if (!jobCallingCount++)
            logo()

        logTaskDescription('build')

        if (!propertiesFile)
            throw new Exception('Does not exists script file [ installer-maker.yml ]')

        //- Ready event
        InstallerMakerBeforeBuild before = new InstallerMakerBeforeBuild(propman, config, provider, environment, selfAware)
        InstallerMakerAfterBuild after = new InstallerMakerAfterBuild(propman, config, provider, environment, selfAware)

        try{
            ReportSetup reportSetup = gOpt.reportSetup

            //1. Gen Starter and Response File
            String binPath = before.remakeLibAndBin(gOpt)

            //- set bin path on builded installer
            provider.setToRawProperty('build.installer.bin.path', binPath)

            //2. Each level by level
            validTaskList = CommanderConfig.findAllClasses('com.jaemisseo', [Task])
            eachTaskWithCommit('build'){ TaskSetup commitTask ->
                try{
                    return runTaskByCommitTask(commitTask)
                }catch(WantToRestartException wtre){
                    throw wtre
                }catch(Exception e){
                    //Write Report
                    writeReport(reportMapList, reportSetup)
                    throw e
                }
            }
            //Write Report
            writeReport(reportMapList, reportSetup)

            after.generateResponseFileAndCompressDistribution(gOpt)

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
            throw new Exception('Does not exists script file [ installer-maker.yml ]')


        String binPath = provider.get('build.installer.bin.path') ?: FileMan.getFullPath(gOpt.buildInstallerHome, gOpt.installerHomeToBinRelPath)
        String argsExceptCommand = provider.get('program.args.except.command')
        String argsModeExec = '-mode.exec.self=true'
        String installBinPathForWIn = "${binPath}/installer.bat".replaceAll(/[\/\\]+/, "\\$File.separator")
        String installBinPathForLin = FileMan.toSlash("${binPath}/installer")
        provider.setToRawProperty('command.win', "${installBinPathForWIn} ask install ${argsExceptCommand} ${argsModeExec}")
        provider.setToRawProperty('command.lin', "${installBinPathForLin} ask install ${argsExceptCommand} ${argsModeExec}")
        //run
        runTaskByType('exec')
        //clear
        provider.setToRawProperty('command.win', "")
        provider.setToRawProperty('command.lin', "")
    }

    @Command('test')
    @Document("""
    - Test Command do 'clean' 'build' 'run'.

    - You can use 'test' command to test or build CI Environment.
    
    - Response File(.rsp) can help your test.
         
      installer-maker test -rsp=<File>  
    """)
    void test(){
        config.command( 'clean')
        config.command('build')
        config.command('run')
    }





    /*************************
     * WRITE Report
     *************************/
    private void writeReport(List reportMapList, ReportSetup reportSetup){
        //Generate File Report
        if (reportMapList){
            String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
            String fileNamePrefix = 'report_analysis'
            String filePath = reportSetup.fileSetup.path ?: "${fileNamePrefix}_${date}"

            if (reportSetup.modeReportText) {
//                List<String> stringList = sqlman.getAnalysisStringResultList(reportMapList)
//                FileMan.write("${reportSetup.path}.txt", stringList, opt)
            }

            if (reportSetup.modeReportExcel) {
//                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", reportMapList, 'sqlFileName')
//                new ReportMan("${fileNamePrefix}_${date}.xlsx").write('sqlFileName', reportMapList)
                new ReportMan("${filePath}.xlsx").write(reportMapList, 'sqlFileName')
            }
        }

    }



}
