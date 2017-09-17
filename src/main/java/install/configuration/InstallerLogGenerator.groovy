package install.configuration

import jaemisseo.man.PropMan
import jaemisseo.man.util.PropertiesGenerator

/**
 * Created by sujkim on 2017-03-29.
 */
class InstallerLogGenerator extends PropertiesGenerator{



    void logVersion(PropMan propman){
        String thisVersion  = propman.get('lib.version')
        String thisBuildDate = propman.get('lib.build.date')
        println ""
        println "Installer Version ${thisVersion}"
        println "${propman.get('lib.compiler')} compiled on ${thisBuildDate}"
        println "https://github.com/avaj-java/installer-maker"
        println ""
    }

    void logSystem(PropMan propman){
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
        println "Check your system "
        println " - OS            : ${osName}, ${osVersion}"
        println " - USER          : ${userName}"
        println " - JAVA Version  : ${javaVersion} (${javaHome})"
        println " - HOME Path     : ${homePath}"
        println " - INSTALLER Path: ${thisPath}"
        println " - YOUR Path     : ${nowPath}"
        println ""
    }

    void logFinished(){
        logFinished('')
    }

    void logFinished(String message){
        println ""
        println "   <<< Finish >>> ${message?:''}"
        println ""
    }


}
