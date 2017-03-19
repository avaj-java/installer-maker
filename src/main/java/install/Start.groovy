package install

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.VariableMan
import install.job.JobReceptionist
import install.job.JobBuilder
import install.job.MacGyver
import install.task.TaskMergeProperties
import install.task.TaskTestPort
import install.job.JobInstaller
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.SqlMan
import install.task.TaskTestJDBC
import install.task.TaskTestREST
import install.task.TaskTestSocket

class Start {

    /**
     * START INSTALL
     * @param args
     * @throws Exception
     */
    static void main(String[] args) throws Exception{
        Map prop = [:]
        // 1. [prop] OverWrite with External Properties
        if (args){
            args.each{
                int indexEqualMark = it.indexOf('=')
                String beforeEqualMark
                def afterEqualMark
                if (indexEqualMark != -1){
                    //COMMAND: -PROP.PERTY=VALUE
                    //RESULT: prop['PROP.PERTY'] = VALUE
                    beforeEqualMark = (it.startsWith('-')) ? it.substring(1, indexEqualMark) : ''
                    afterEqualMark = it.substring(indexEqualMark + 1)
                    prop[beforeEqualMark] = (afterEqualMark) ?: ''
                }else{
                    //COMMAND: -PROP.PERTY
                    //RESULT: prop['PROP.PERTY'] = true
                    beforeEqualMark = (it.startsWith('-')) ? it.substring(1, it.length()) : ''
                    prop[beforeEqualMark] = true
                }
            }
        }
        // 2. Start With Properties
        new Start().start(prop)
    }





    Start(){}

    String osName
    String osVersion
    String userName
    String javaHome
    String javaVersion
    String thisPath
    String nowPath
    String homePath



    /**
     * START
     * @param prop
     */
    void start(Map prop){
        osName = System.getProperty('os.name')
        osVersion = System.getProperty('os.version')
        userName = System.getProperty('user.name')
        javaVersion = System.getProperty('java.version')
        javaHome = System.getProperty('java.home')
        thisPath = getThisAppPath()
        nowPath = System.getProperty('user.dir')
        homePath = System.getProperty('user.home')

        /////LOG
        logStart()

        /////Create Main Bean
        String propertiesDir = prop['properties.dir'] ?: thisPath
        PropMan propmanForBuilder
        PropMan propmanForReceptionist
        PropMan propmanForInstaller

        ///// -build
        if (prop['build']){
            propmanForBuilder = getProperties(propertiesDir, "builder.properties").merge(prop)
            new JobBuilder(propmanForBuilder).run()
        }

        ///// -ask
        if (prop['ask']){
            propmanForReceptionist = getProperties(propertiesDir, "receptionist.properties").merge(prop)
            new JobReceptionist(propmanForReceptionist).run()
        }

        ///// -install
        if (prop['install']){
            propmanForInstaller = getProperties(propertiesDir, "installer.properties").merge(propmanForReceptionist)
            new JobInstaller(propmanForInstaller).run()
        }

        ///// do task
        if (true){
            PropMan propman = new PropMan(prop)
            new MacGyver(propman).run()
            System.exit(0)
        }

        /////LOG
        logFinished()
    }



    void logStart(){
        println ""
        println "<<< WELCOME INSTALLER >>>"
        println " - [OS]            : ${osName}, ${osVersion}"
        println " - [USER]          : ${userName}"
        println " - [JAVA Version]  : ${javaVersion} (${javaHome})"
        println " - [HOME Path]     : ${homePath}"
        println " - [INSTALLER Path]: ${thisPath}"
        println " - [YOUR Path]     : ${nowPath}"

        println ""
    }

    void logFinished(){
        println ""
        println "<<< FINISHED INSTALLER >>>"
        println ""
    }

    String getThisAppPath(){
        return new File(this.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent()
    }

    PropMan getProperties(String propertiesDirPath, String propertiesFileName){
        PropMan propman = new PropMan()
        //1. Try Get FileSystem's Properties OR Resource Properties
        try{
            propman.readFile("${propertiesDirPath}/${propertiesFileName}")
        }catch(e){
            propman.readResource("${propertiesFileName}")
        }
        //2. Set Default Value
        seDefaultValue(propman)
        return propman
    }


    void seDefaultValue(PropMan propman){
        if (!propman.get('os.name'))
            propman.set('os.name', osName)
        if (!propman.get('os.version'))
            propman.set('os.version', osVersion)
        if (!propman.get('os.name'))
            propman.set('os.name', userName)
        if (!propman.get('java.version'))
            propman.set('java.version', javaVersion)
        if (!propman.get('java.home'))
            propman.set('java.home', javaHome)
        if (!propman.get('installer.home'))
            propman.set('installer.home', thisPath)
        if (!propman.get('user.dir'))
            propman.set('user.dir', nowPath)
        if (!propman.get('user.home'))
            propman.set('user.home', homePath)
    }



}

