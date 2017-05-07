package install

import jaemisseo.man.PropMan
import jaemisseo.man.util.PropertiesGenerator

/**
 * Created by sujkim on 2017-03-29.
 */
class InstallerLogGenerator extends PropertiesGenerator{



    void logVersion(PropMan propman){
        println "Installer Version ${propman.get('lib.version')}"
        println "${propman.get('lib.compiler')} compiled on ${propman.get('lib.build.date')}"
        println ""
        println "Sinna Jaemisseo"
        println "http://jaemi.me"
        println "http://youtube.com/chooseamenu"
        println "http://github.com/souljungkim"
        println '\n"Have a Good Time" :) '
    }

    void logStart(PropMan propman){
        String thisVersion  = propman.get('lib.version')
        String thisPath     = propman.get('lib.path')
        String osName       = propman.get('os.name')
        String osVersion    = propman.get('os.version')
        String userName     = propman.get('user.name')
        String javaVersion  = propman.get('java.version')
        String javaHome     = propman.get('java.home')
        String homePath     = propman.get('user.home')
        String nowPath      = propman.get('user.dir')
        println ""
        println "<<< WELCOME INSTALLER ${thisVersion} >>>"
        println " - OS            : ${osName}, ${osVersion}"
        println " - USER          : ${userName}"
        println " - JAVA Version  : ${javaVersion} (${javaHome})"
        println " - HOME Path     : ${homePath}"
        println " - INSTALLER Path: ${thisPath}"
        println " - YOUR Path     : ${nowPath}"
        println ""
    }

    void logFinished(){
        println ""
        println "<<< FINISHED INSTALLER >>>"
        println ""
    }


}
