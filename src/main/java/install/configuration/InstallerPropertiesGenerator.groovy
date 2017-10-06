package install.configuration

import install.configuration.annotation.type.Bean
import install.task.Copy
import install.task.Jar
import install.task.MergeProperties
import install.task.Sql
import install.task.Tar
import install.task.TestJDBC
import install.task.TestPort
import install.task.TestREST
import install.task.TestSocket
import install.task.Unjar
import install.task.Untar
import install.task.Unzip
import install.task.Zip
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.util.PropertiesGenerator

import java.security.CodeSource

/**
 * Created by sujkim on 2017-03-29.
 */
@Bean
class InstallerPropertiesGenerator extends PropertiesGenerator{

    Map<String, PropMan> dataMap = [:]


    PropMan defaultProperties
    PropMan externalProperties



    //Generate Properties Perspective Map
    Map genPropertiesValueMap(String[] args){
        Map valueListMap = super.genValueListMap(args)

        //Check
//        println "- Check Arguments."
//        propertiesValueMap.each{
//            println "${it.key}=${it.value}"
//        }
//        println ""
        return valueListMap
    }


    //Generate Properties Perspective Map
    Map genPropertiesValueMap(Map valueListMap, Map<Class, List> lowerTaskNameAndValueProtocolListMap){
        Map propValueMap = [:]

        valueListMap.each{ String exPropName, def valueList ->
            if (valueList instanceof List){

                // -KEY VALUE VALUE ..
                List valueProtocolList = lowerTaskNameAndValueProtocolListMap[exPropName.toLowerCase()]
                if (valueProtocolList){
                    if (valueProtocolList.size() >= valueList.size()){
                        valueList.eachWithIndex{ def value, int i ->
                            String propName = valueProtocolList[i]
                            propValueMap[propName] = parseValue(value)
                        }
                    }else{
                        throw new Exception("So Many arguments!. Check ${exPropName}'s Arguments")
                    }
                }

                // -KEY
                if (exPropName){
                    String propName = exPropName
                    propValueMap[propName] = parseValue(valueList) ?: true

                // VALUE VALUE ..
                }else{
                    propValueMap[''] = valueList
                }

            }else{
                // -KEY=VALUE
                String propName = exPropName
                def value = parseValue(valueList)
                propValueMap[propName] = value
            }
        }

        return propValueMap
    }

    def parseValue(def value){
        if (['true', 'True', 'TRUE'].contains(value))
            return true
        if (['false', 'False', 'FALSE'].contains(value))
            return false
        return value
    }



    static PropMan genDefaultProperties(){
        return new PropMan([
                'os.name': System.getProperty('os.name'),
                'os.version': System.getProperty('os.version'),
                'user.name': System.getProperty('user.name'),
                'java.version': System.getProperty('java.version'),
                'java.home': System.getProperty('java.home'),
                'user.dir': System.getProperty('user.dir'),
                'user.home': System.getProperty('user.home'),
                // installer.jar path
                'lib.dir': new InstallerPropertiesGenerator().getThisAppFile()?.getParentFile()?.getPath() ?: '',
                'lib.path': new InstallerPropertiesGenerator().getThisAppFile()?.getPath() ?: '' ,
                'lib.version': FileMan.getFileFromResource('.version').text,
                'lib.compiler': FileMan.getFileFromResource('.compiler').text,
                'lib.build.date': FileMan.getFileFromResource('.date').text,
        ])
    }

    static PropMan gen(String filePath){
        return new PropMan().readFile(filePath)
    }

    static PropMan genResource(String resourcePath){
        return new PropMan().readResource(resourcePath)
    }



    boolean containsKey(String key){
        return dataMap.containsKey(key)
    }



    /**
     *
     */
    PropMan get(String key){
        return dataMap[key]
    }

    InstallerPropertiesGenerator add(String key, String filePath){
        dataMap[key] = new PropMan().readFile(filePath)
        return this
    }

    InstallerPropertiesGenerator addResource(String key, String resourcePath){
        dataMap[key] = new PropMan().readResource(resourcePath)
        return this
    }

    PropMan genSingleton(String key, String filePath){
        if (!dataMap.containsKey(key))
            add(key, filePath)
        return dataMap[key]
    }

    PropMan genResourceSingleton(String key, String filePath){
        if (!dataMap.containsKey(key))
            addResource(key, filePath)
        return dataMap[key]
    }



    /**
     *
     */
    InstallerPropertiesGenerator makeExternalProperties(String[] args, Map<Class, List> valueProtocolListMap){
        Map argsMap = genPropertiesValueMap(args)
        Map propertiesValueMap = genPropertiesValueMap(argsMap, valueProtocolListMap)
        externalProperties = new PropMan(propertiesValueMap)
        externalProperties.set('args', args.join(' '))
        externalProperties.set('args.except.command', argsMap.findAll{ it.key }.collect{ "-${it.key}=${it.value}" }.join(' '))
        return this
    }

    InstallerPropertiesGenerator makeDefaultProperties(){
        defaultProperties = genDefaultProperties()
        return this
    }

    PropMan getExternalProperties(){
        return externalProperties
    }

    PropMan getDefaultProperties(){
        return defaultProperties
    }



    File getThisAppFile(){
        File thisAppFile
        CodeSource src = this.getClass().getProtectionDomain().getCodeSource()
        if (src)
            thisAppFile = new File( src.getLocation().toURI().getPath() )
        return thisAppFile
    }

}
