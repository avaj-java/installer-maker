package install.job

import install.bean.GlobalOptionForInstaller
import install.configuration.annotation.HelpIgnore
import install.configuration.annotation.method.Command
import install.configuration.annotation.method.Init
import install.configuration.annotation.type.Document
import install.configuration.annotation.type.Job
import install.configuration.annotation.type.Task
import install.bean.ReportSetup
import install.data.PropertyProvider
import install.util.JobUtil
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.ReportMan
import jaemisseo.man.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by sujkim on 2017-02-17.
 */
@Job
class Installer extends JobUtil{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    Installer(){
        propertiesFileName = 'installer'
        jobName = 'installer'
    }

    @Init(lately=true)
    void init(){
        validTaskList = Util.findAllClasses('install', [Task])
        validCommandList = ['install']

        this.propman = setupPropMan(provider)
        this.varman = setupVariableMan(propman, validCommandList)
        provider.shift(jobName)
        this.gOpt = config.injectValue(new GlobalOptionForInstaller())
    }

    PropMan setupPropMan(PropertyProvider provider){
        PropMan propmanForInstaller = provider.propGen.get('installer')
        PropMan propmanForReceptionist = provider.propGen.get('receptionist')
        PropMan propmanDefault = provider.propGen.getDefaultProperties()
        PropMan propmanExternal = provider.propGen.getExternalProperties()

        String libtohomeRelPath = FileMan.getFileFromResource('.libtohome')?.text.replaceAll('\\s*', '') ?: '../'
        String installerHome = FileMan.getFullPath(propmanDefault.get('lib.dir'), libtohomeRelPath)

        //From User's FileSystem or Resource
//        String userSetPropertiesDir = propmanExternal['properties.dir']
//        if (userSetPropertiesDir){
//            propertiesFile = FileMan.find(userSetPropertiesDir, propertiesFileName, ["yml", "yaml", "properties"])
//        }else{
            propertiesFile = FileMan.findResource(null, propertiesFileName, ["yml", "yaml", "properties"])
//        }
        propertiesFileExtension = FileMan.getExtension(propertiesFile)
        if (propertiesFile && propertiesFile.exists()){
            Map propertiesMap = generatePropertiesMap(propertiesFile)
            propmanForInstaller.merge(propertiesMap)
                                .merge(propmanForReceptionist)
                                .mergeNew(propmanDefault)
                                .merge(['installer.home': installerHome])
        }else{
        }

        return propmanForInstaller
    }



    @Command
    void customCommand(){
        //Setup Log
        setuptLog(gOpt.logSetup)

        ReportSetup reportSetup = config.injectValue(new ReportSetup())

        //Each level by level
        eachLevelForTask(commandName){ String propertyPrefix ->
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



    /*************************
     * INSTALL
     *************************/
    @Command('install')
    @HelpIgnore
    @Document('''
    No User's Command 
    ''')
    void install(){
        //Setup Log
        setuptLog(gOpt.logSetup)

        logBigTitle "Installer"

        logTaskDescription('install')

        if (!propertiesFile)
            throw Exception('Does not exists script file [ installer.yml ]')

        ReportSetup reportSetup = gOpt.reportSetup

        //Each level by level
        eachLevelForTask('install'){ String propertyPrefix ->
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
                logTaskDescription("save excel report")
                logger.debug "Creating Excel Report File..."
                new ReportMan().write("${fileNamePrefix}_${date}.xlsx", reportMapList, 'sqlFileName')
            }
        }
    }



}
