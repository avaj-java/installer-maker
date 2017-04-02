package install

import com.jaemisseo.man.FileMan
import com.jaemisseo.man.PropMan
import com.jaemisseo.man.util.PropertiesGenerator
import install.task.TaskUtil

/**
 * Created by sujkim on 2017-03-29.
 */
class InstallerPropertiesGenerator extends PropertiesGenerator{



    //Generate Properties Perspective Map
    Map genPropertiesValueMap(String[] args){
        Map valueListMap = super.genValueListMap(args)
        return genPropertiesValueMap(valueListMap)
    }


    //Generate Properties Perspective Map
    Map genPropertiesValueMap(Map valueListMap){
        Map propValueMap = [:]
        Map<String, List> valueProtocolListMap = getValueProtocolListMap()

        valueListMap.each{ String exPropName, def valueList ->
            List valueOrderList = valueProtocolListMap[exPropName.toUpperCase()]
            if (valueList instanceof List){
                // -KEY VALUE VALUE ..
                if (valueOrderList){
                    valueList.eachWithIndex{ def value, int i ->
                        String propName = valueOrderList[i]
                        propValueMap[propName] = value
                    }
                }
                // -KEY
                if (exPropName){
                    String propName = exPropName
                    propValueMap[propName] = valueList ?: true
                // VALUE VALUE ..
                }else{
                    valueList.each{
                        propValueMap[it] = true
                    }
                }
            }else{
                // -KEY=VALUE
                String propName = exPropName
                def value = valueList
                propValueMap[propName] = value
            }
        }

        return propValueMap
    }


    //Protocol of exProperty value
    Map<String, List> getValueProtocolListMap(){
        Map<String, List> valueOrderListMap = [:]
        valueOrderListMap[TaskUtil.TASK_ZIP]    = ['file.path', 'dest.path']
        valueOrderListMap[TaskUtil.TASK_TAR]    = ['file.path', 'dest.path']
        valueOrderListMap[TaskUtil.TASK_JAR]    = ['file.path', 'dest.path']
        valueOrderListMap[TaskUtil.TASK_UNZIP]  = ['file.path', 'dest.path']
        valueOrderListMap[TaskUtil.TASK_UNJAR]  = ['file.path', 'dest.path']
        valueOrderListMap[TaskUtil.TASK_UNTAR]  = ['file.path', 'dest.path']
        valueOrderListMap[TaskUtil.TASK_COPY]   = ['file.path', 'dest.path']
        valueOrderListMap[TaskUtil.TASK_MKDIR]  = ['structure', 'dest.path']
        return valueOrderListMap
    }



    PropMan genSystemDefaultProperties(){
        return new PropMan([
                'os.name': System.getProperty('os.name'),
                'os.version': System.getProperty('os.version'),
                'user.name': System.getProperty('user.name'),
                'java.version': System.getProperty('java.version'),
                'java.home': System.getProperty('java.home'),
                'user.dir': System.getProperty('user.dir'),
                'user.home': System.getProperty('user.home'),
                // installer.jar path
                'lib.dir': getThisAppFile().getParentFile().getPath(),
                'lib.path': getThisAppFile().getPath(),
                'lib.version': FileMan.getFileFromResource('.version').text,
                'lib.compiler': FileMan.getFileFromResource('.compiler').text,
                'lib.build.date': FileMan.getFileFromResource('.date').text,
        ])
    }

    PropMan genBuilderDefaultProperties(){
        return new PropMan().readResource('defaultProperties/builder.default.properties')
    }

    PropMan genReceptionistDefaultProperties(){
        return new PropMan().readResource('defaultProperties/receptionist.default.properties')
    }

    PropMan genInstallerDefaultProperties(){
        return new PropMan().readResource('defaultProperties/installer.default.properties')
    }



    File getThisAppFile(){
        return new File(this.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
    }

}
