package install.configuration

import install.configuration.annotation.type.Bean
import jaemisseo.man.FileMan
import jaemisseo.man.PropMan
import jaemisseo.man.util.PropertiesGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.security.CodeSource

/**
 * Created by sujkim on 2017-03-29.
 */
@Bean
class InstallerPropertiesGenerator extends PropertiesGenerator{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    Map<String, PropMan> dataMap = [:]


    PropMan defaultProperties
    PropMan externalProperties



    //Generate Properties Perspective Map
    Map genApplicationPropertiesValueMap(String[] args){
        Map valueListMap = super.genPropertyValueMap(args)

        //Check
//        println "- Check Arguments."
//        propertiesValueMap.each{
//            println "${it.key}=${it.value}"
//        }
//        println ""
        return valueListMap
    }


    //Generate Properties Perspective Map
    Map genApplicationPropertiesValueMap(Map terminalPropertiesValueMap, Map<Class, List> lowerTaskNameAndValueProtocolListMap){
        Map applicationPropertyValueMap = [
            ''      : [],
            '--'    : []
        ]

        terminalPropertiesValueMap.each{ String proppertyName, def propertyValue ->
            if (propertyValue instanceof List){

                // -KEY VALUE VALUE ..
                List valueProtocolList = lowerTaskNameAndValueProtocolListMap[proppertyName.toLowerCase()]
                if (valueProtocolList){
                    if (valueProtocolList.size() >= propertyValue.size()){
                        propertyValue.eachWithIndex{ def value, int i ->
                            String propName = valueProtocolList[i]
                            applicationPropertyValueMap[propName] = parseValue(value)
                        }
                    }else{
                        throw new Exception("So Many arguments!. Check ${proppertyName}'s Arguments")
                    }
                }


                if (proppertyName){
                    // --VALUE
                    if (proppertyName == '--'){
                        applicationPropertyValueMap['--'] = propertyValue

                    // -KEY
                    }else{
                        applicationPropertyValueMap[proppertyName] = parseValue(propertyValue) ?: true
                    }

                // VALUE VALUE ..
                }else{
                    applicationPropertyValueMap[''] = propertyValue
                }

            }else{
                // -KEY=VALUE
                applicationPropertyValueMap[proppertyName] = parseValue(propertyValue)
            }
        }

        return applicationPropertyValueMap
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
                'product.name': FileMan.getFileFromResource('.productname').text?.trim(),
                'product.version': FileMan.getFileFromResource('.productversion').text?.trim(),
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
        Map argsMap = genApplicationPropertiesValueMap(args)
        Map propertiesValueMap = genApplicationPropertiesValueMap(argsMap, valueProtocolListMap)
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
